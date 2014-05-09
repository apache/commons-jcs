/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.jcs.jcache.extras.web;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;
import javax.cache.spi.CachingProvider;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.list;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class JCacheFilter implements Filter
{
    private Cache<String, Page> cache;
    private CachingProvider provider;
    private CacheManager manager;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException
    {
        final ClassLoader classLoader = filterConfig.getServletContext().getClassLoader();
        provider = Caching.getCachingProvider(classLoader);

        String uri = filterConfig.getInitParameter("configuration");
        if (uri == null)
        {
            uri = provider.getDefaultURI().toString();
        }
        final Properties properties = new Properties();
        for (final String key : list(filterConfig.getInitParameterNames()))
        {
            final String value = filterConfig.getInitParameter(key);
            if (value != null)
            {
                properties.put(key, value);
            }
        }
        manager = provider.getCacheManager(URI.create(uri), classLoader, properties);

        String cacheName = filterConfig.getInitParameter("cache-name");
        if (cacheName == null)
        {
            cacheName = JCacheFilter.class.getName();
        }
        cache = manager.getCache(cacheName);
        if (cache == null)
        {
            final MutableConfiguration<String, Page> configuration = new MutableConfiguration<String, Page>()
                    .setStoreByValue(false);
            configuration.setReadThrough("true".equals(properties.getProperty("read-through", "false")));
            configuration.setWriteThrough("true".equals(properties.getProperty("write-through", "false")));
            if (configuration.isReadThrough())
            {
                configuration.setCacheLoaderFactory(new FactoryBuilder.ClassFactory<CacheLoader<String, Page>>(properties.getProperty("cache-loader-factory")));
            }
            if (configuration.isWriteThrough())
            {
                configuration.setCacheWriterFactory(new FactoryBuilder.ClassFactory<CacheWriter<? super String, ? super Page>>(properties.getProperty("cache-writer-factory")));
            }
            final String expirtyPolicy = properties.getProperty("expiry-policy-factory");
            if (expirtyPolicy != null)
            {
                configuration.setExpiryPolicyFactory(new FactoryBuilder.ClassFactory<ExpiryPolicy>(expirtyPolicy));
            }
            configuration.setManagementEnabled("true".equals(properties.getProperty("management-enabled", "false")));
            configuration.setStatisticsEnabled("true".equals(properties.getProperty("statistics-enabled", "false")));
            cache = manager.createCache(cacheName, configuration);
        }
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException
    {
        final HttpServletResponse httpServletResponse = HttpServletResponse.class.cast(servletResponse);
        checkResponse(httpServletResponse);

        final String key = key(servletRequest);
        Page page = cache.get(key);
        if (page == null)
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final InMemoryResponse response = new InMemoryResponse(httpServletResponse, baos);
            filterChain.doFilter(servletRequest, response);
            response.flushBuffer();

            page = new Page(
                    response.getStatus(),
                    response.getContentType(),
                    response.getContentLength(),
                    response.getCookies(),
                    response.getHeaders(),
                    response.getOut());
            cache.put(key, page);
        }

        if (page.status == SC_OK) {
            checkResponse(httpServletResponse);

            httpServletResponse.setStatus(page.status);
            if (page.contentType != null)
            {
                httpServletResponse.setContentType(page.contentType);
            }
            if (page.contentLength > 0)
            {
                httpServletResponse.setContentLength(page.contentLength);
            }
            for (final Cookie c : page.cookies)
            {
                httpServletResponse.addCookie(c);
            }
            for (final Map.Entry<String, List<Serializable>> entry : page.headers.entrySet())
            {
                for (final Serializable value : entry.getValue())
                {
                    if (Integer.class.isInstance(value))
                    {
                        httpServletResponse.addIntHeader(entry.getKey(), Integer.class.cast(entry.getValue()));
                    }
                    else if (String.class.isInstance(value))
                    {
                        httpServletResponse.addHeader(entry.getKey(), String.class.cast(entry.getValue()));
                    }
                    else if (Long.class.isInstance(value))
                    {
                        httpServletResponse.addDateHeader(entry.getKey(), Long.class.cast(entry.getValue()));
                    }
                }
            }
            httpServletResponse.setContentLength(page.out.length);
            final BufferedOutputStream bos = new BufferedOutputStream(httpServletResponse.getOutputStream());
            if (page.out.length != 0)
            {
                bos.write(page.out);
            }
            else
            {
                bos.write(new byte[0]);
            }
            bos.flush();
        }
    }

    protected String key(final ServletRequest servletRequest)
    {
        if (HttpServletRequest.class.isInstance(servletRequest))
        {
            final HttpServletRequest request = HttpServletRequest.class.cast(servletRequest);
            return request.getMethod() + '_' + request.getRequestURI() + '_' + request.getQueryString();
        }
        return servletRequest.toString();
    }

    private void checkResponse(final ServletResponse servletResponse)
    {
        if (servletResponse.isCommitted()) {
            throw new IllegalStateException("Response committed");
        }
    }

    @Override
    public void destroy()
    {
        if (!cache.isClosed())
        {
            cache.close();
        }
        if (!manager.isClosed())
        {
            manager.close();
        }
        provider.close();
    }

    protected static class Page implements Serializable {
        private final int status;
        private final String contentType;
        private final int contentLength;
        private final Collection<Cookie> cookies;
        private final Map<String, List<Serializable>> headers;
        private final byte[] out;

        public Page(final int status,
                    final String contentType, final int contentLength,
                    final Collection<Cookie> cookies, final Map<String, List<Serializable>> headers,
                    final byte[] out)
        {
            this.status = status;
            this.contentType = contentType;
            this.contentLength = contentLength;
            this.cookies = cookies;
            this.headers = headers;
            this.out = out;
        }
    }
}

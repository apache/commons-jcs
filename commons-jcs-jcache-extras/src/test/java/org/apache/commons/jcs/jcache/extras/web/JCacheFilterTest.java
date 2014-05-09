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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class JCacheFilterTest
{
    private final ThreadLocal<ByteArrayOutputStream> outputStreamAsBytes = new ThreadLocal<ByteArrayOutputStream>() {
        @Override
        protected ByteArrayOutputStream initialValue()
        {
            return new ByteArrayOutputStream();
        }
    };
    private final ThreadLocal<ServletOutputStream> outputStream = new ThreadLocal<ServletOutputStream>() {
        @Override
        protected ServletOutputStream initialValue()
        {
            return new ServletOutputStream()
            {
                @Override
                public void write(final int b) throws IOException
                {
                    outputStreamAsBytes.get().write(b);
                }
            };
        }

        @Override
        public void remove()
        {
            super.remove();
            outputStreamAsBytes.remove();
        }
    };

    @Before
    @After
    public void cleanup() {
        outputStream.remove();
    }

    @Test
    public void testFilterNoOutput() throws ServletException, IOException
    {
        final Filter filter = initFilter();
        final AtomicInteger counter = new AtomicInteger(0);
        final FilterChain chain = new FilterChain()
        {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException
            {
                counter.incrementAndGet();
                response.getWriter().write("");
            }
        };
        filter.doFilter(new HttpServletRequestWrapper(newProxy(HttpServletRequest.class)), new HttpServletResponseWrapper(newProxy(HttpServletResponse.class)), chain);
        assertEquals(1, counter.get());
        assertEquals("", new String(outputStreamAsBytes.get().toByteArray()));
        outputStream.remove();
        filter.doFilter(new HttpServletRequestWrapper(newProxy(HttpServletRequest.class)), new HttpServletResponseWrapper(newProxy(HttpServletResponse.class)), chain);
        assertEquals(1, counter.get());
        assertEquals("", new String(outputStreamAsBytes.get().toByteArray()));
        filter.destroy();
    }

    @Test
    public void testFilter() throws ServletException, IOException
    {
        final Filter filter = initFilter();
        final AtomicInteger counter = new AtomicInteger(0);
        final FilterChain chain = new FilterChain()
        {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException
            {
                counter.incrementAndGet();
                response.getWriter().write("Hello!");
            }
        };
        filter.doFilter(new HttpServletRequestWrapper(newProxy(HttpServletRequest.class)), new HttpServletResponseWrapper(newProxy(HttpServletResponse.class)), chain);
        assertEquals(1, counter.get());
        assertEquals("Hello!", new String(outputStreamAsBytes.get().toByteArray()));
        outputStream.remove();
        filter.doFilter(new HttpServletRequestWrapper(newProxy(HttpServletRequest.class)), new HttpServletResponseWrapper(newProxy(HttpServletResponse.class)), chain);
        assertEquals(1, counter.get());
        assertEquals("Hello!", new String(outputStreamAsBytes.get().toByteArray()));
        filter.destroy();
    }

    private JCacheFilter initFilter() throws ServletException
    {
        final JCacheFilter filter = new JCacheFilter();
        filter.init(new FilterConfig()
        {
            @Override
            public String getFilterName()
            {
                return null;
            }

            @Override
            public ServletContext getServletContext()
            {
                return newProxy(ServletContext.class);
            }

            @Override
            public String getInitParameter(String name)
            {
                return null;
            }

            @Override
            public Enumeration<String> getInitParameterNames()
            {
                return Collections.emptyEnumeration();
            }
        });
        return filter;
    }

    private <T> T newProxy(final Class<T> clazz)
    {
        return clazz.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{clazz}, new InvocationHandler()
                        {
                            @Override
                            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
                            {
                                if (method.getReturnType().getName().equals("boolean"))
                                {
                                    return false;
                                }
                                if ("getCharacterEncoding".equals(method.getName()))
                                {
                                    return "UTF-8";
                                }
                                if ("getOutputStream".equals(method.getName()))
                                {
                                    return outputStream.get();
                                }
                                return null;
                            }
                        }
                )
        );
    }
}

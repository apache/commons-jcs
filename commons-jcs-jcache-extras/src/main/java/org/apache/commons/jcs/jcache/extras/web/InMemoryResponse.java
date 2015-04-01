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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class InMemoryResponse extends HttpServletResponseWrapper implements Serializable
{
    private final OutputStream buffer;

    private final Collection<Cookie> cookies = new CopyOnWriteArraySet<Cookie>();
    private final Map<String, List<Serializable>> headers = new TreeMap<String, List<Serializable>>(String.CASE_INSENSITIVE_ORDER);
    private int status = SC_OK;
    private String contentType = null;
    private PrintWriter writer;
    private int contentLength;

    public InMemoryResponse(final HttpServletResponse response, final OutputStream baos)
    {
        super(response);
        this.buffer = baos;
    }

    private List<Serializable> ensureHeaderExists(final String s)
    {
        List<Serializable> values = headers.get(s);
        if (values == null) {
            values = new LinkedList<Serializable>();
            headers.put(s, values);
        }
        return values;
    }

    @Override
    public void addCookie(final Cookie cookie)
    {
        super.addCookie(cookie);
        cookies.add(cookie);
    }

    @Override
    public void addDateHeader(final String s, final long l)
    {
        super.addDateHeader(s, l);
        ensureHeaderExists(s).add(l);
    }

    @Override
    public void addHeader(final String s, final String s2)
    {
        super.addHeader(s, s2);
        ensureHeaderExists(s).add(s2);
    }

    @Override
    public void addIntHeader(final String s, final int i)
    {
        super.addIntHeader(s, i);
        ensureHeaderExists(s).add(i);
    }

    @Override
    public boolean containsHeader(final String s)
    {
        return headers.containsKey(s);
    }

    @Override
    public String getHeader(final String s)
    {
        final List<Serializable> serializables = headers.get(s);
        if (serializables.isEmpty())
        {
            return null;
        }
        return serializables.iterator().next().toString();
    }

    @Override
    public Collection<String> getHeaderNames()
    {
        return headers.keySet();
    }

    @Override
    public Collection<String> getHeaders(final String s)
    {
        final List<Serializable> serializables = headers.get(s);
        final Collection<String> strings = new ArrayList<String>(serializables.size());
        for (final Serializable ser : serializables)
        {
            strings.add(ser.toString());
        }
        return strings;
    }

    @Override
    public int getStatus()
    {
        return status;
    }

    @Override
    public void sendError(final int i) throws IOException
    {
        status = i;
        super.sendError(i);
    }

    @Override
    public void sendError(final int i, final String s) throws IOException
    {
        status = i;
        super.sendError(i, s);
    }

    @Override
    public void sendRedirect(final String s) throws IOException
    {
        status = SC_MOVED_TEMPORARILY;
        super.sendRedirect(s);
    }

    @Override
    public void setDateHeader(final String s, final long l)
    {
        super.setDateHeader(s, l);
        final List<Serializable> serializables = ensureHeaderExists(s);
        serializables.clear();
        serializables.add(l);
    }

    @Override
    public void setHeader(final String s, final String s2)
    {
        super.setHeader(s, s2);
        final List<Serializable> serializables = ensureHeaderExists(s);
        serializables.clear();
        serializables.add(s2);
    }

    @Override
    public void setIntHeader(final String s, final int i)
    {
        super.setIntHeader(s, i);
        final List<Serializable> serializables = ensureHeaderExists(s);
        serializables.clear();
        serializables.add(i);
    }

    @Override
    public void setStatus(int i)
    {
        status = i;
        super.setStatus(i);
    }

    @Override
    public void setStatus(final int i, final String s)
    {
        status = i;
        super.setStatus(i, s);
    }

    @Override
    public String getContentType()
    {
        return contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        return new ServletOutputStream()
        {
            @Override
            public void write(final int b) throws IOException
            {
                buffer.write(b);
            }
        };
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(buffer, getCharacterEncoding()), true);
        }
        return writer;
    }

    @Override
    public void reset()
    {
        super.reset();
        status = SC_OK;
        headers.clear();
        cookies.clear();
        contentType = null;
        contentLength = 0;
    }

    @Override
    public void setContentLength(final int i)
    {
        super.setContentLength(i);
        contentLength = i;
    }

    @Override
    public void setContentType(final String s)
    {
        contentType = s;
        super.setContentType(s);
    }

    @Override
    public void flushBuffer() throws IOException
    {
        if (writer != null)
        {
            writer.flush();
        }
        else
        {
            buffer.flush();
        }
    }

    public int getContentLength()
    {
        return contentLength;
    }

    public Collection<Cookie> getCookies()
    {
        return cookies;
    }

    public Map<String, List<Serializable>> getHeaders()
    {
        return headers;
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.jcs3.jcache.extras.web;

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
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class InMemoryResponse extends HttpServletResponseWrapper implements Serializable
{
    private static final long serialVersionUID = 6827502171557661881L;

    private final OutputStream buffer;

    private final Collection<Cookie> cookies = new CopyOnWriteArraySet<>();
    private final Map<String, List<Serializable>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private int status = SC_OK;
    private String contentType;
    private PrintWriter writer;
    private int contentLength;

    public InMemoryResponse(final HttpServletResponse response, final OutputStream baos)
    {
        super(response);
        this.buffer = baos;
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

    private List<Serializable> ensureHeaderExists(final String s)
    {
        return headers.computeIfAbsent(s, k -> new LinkedList<>());
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

    @Override
    public String getContentType()
    {
        return contentType;
    }

    public Collection<Cookie> getCookies()
    {
        return cookies;
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

    public Map<String, List<Serializable>> getHeaders()
    {
        return headers;
    }

    @Override
    public Collection<String> getHeaders(final String s)
    {
        final List<Serializable> serializables = headers.get(s);
        final Collection<String> strings = new ArrayList<>(serializables.size());
        for (final Serializable ser : serializables)
        {
            strings.add(ser.toString());
        }
        return strings;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        return new ServletOutputStream()
        {
            @Override
            public boolean isReady() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void setWriteListener(final WriteListener writeListener) {
                // TODO Auto-generated method stub

            }

            @Override
            public void write(final int b) throws IOException
            {
                buffer.write(b);
            }
        };
    }

    @Override
    public int getStatus()
    {
        return status;
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
    public void setStatus(final int i)
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
}

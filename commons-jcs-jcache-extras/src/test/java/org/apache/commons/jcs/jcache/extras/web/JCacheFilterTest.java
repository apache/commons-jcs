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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class JCacheFilterTest
{
    private static File docBase;

    @BeforeClass
    public static void createEmptyDir() {
        docBase = new File("target/missing/");
        docBase.mkdirs();
        docBase.deleteOnExit();
    }

    @Test
    public void testFilterNoOutput() throws Exception
    {
        Empty.COUNTER.set(0);
        final Tomcat tomcat = new Tomcat();
        tomcat.setHostname("localhost");
        tomcat.setPort(0);
        try {
            tomcat.getEngine();
            tomcat.start();
            final Context ctx = tomcat.addWebapp("/sample", docBase.getAbsolutePath());
            Tomcat.addServlet(ctx, "empty", Empty.class.getName());
            ctx.addServletMapping("/", "empty");
            addJcsFilter(ctx);
            StandardContext.class.cast(ctx).filterStart();

            final URL url = new URL("http://localhost:" + tomcat.getConnector().getLocalPort() + "/sample/");

            assertEquals("", IOUtils.toString(url.openStream()));
            assertEquals(1, Empty.COUNTER.get());

            assertEquals("", IOUtils.toString(url.openStream()));
            assertEquals(1, Empty.COUNTER.get());
        } finally {
            stop(tomcat);
        }
    }

    @Test
    public void testFilter() throws Exception
    {
        Hello.COUNTER.set(0);
        final Tomcat tomcat = new Tomcat();
        tomcat.setPort(0);
        try {
            tomcat.getEngine();
            tomcat.start();
            final Context ctx = tomcat.addContext("/sample", docBase.getAbsolutePath());
            Tomcat.addServlet(ctx, "hello", Hello.class.getName());
            ctx.addServletMapping("/", "hello");
            addJcsFilter(ctx);
            StandardContext.class.cast(ctx).filterStart();

            final URL url = new URL("http://localhost:" + tomcat.getConnector().getLocalPort() + "/sample/");
            assertEquals("hello", IOUtils.toString(url.openStream()));
            assertEquals(1, Hello.COUNTER.get());

            assertEquals("hello", IOUtils.toString(url.openStream()));
            assertEquals(1, Hello.COUNTER.get());
        } finally {
            stop(tomcat);
        }
    }

    private void stop(final Tomcat tomcat) throws LifecycleException {
        if (LifecycleState.STARTED.equals(tomcat.getServer().getState())) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    private void addJcsFilter(final Context ctx) {
        final FilterDef filterDef = new FilterDef();
        filterDef.setFilterName("jcs");
        filterDef.setFilterClass(JCacheFilter.class.getName());
        ctx.addFilterDef(filterDef);

        final FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(filterDef.getFilterName());
        filterMap.addURLPattern("/*");
        ctx.addFilterMap(filterMap);
    }

    public static class Hello extends HttpServlet {
        public static final AtomicInteger COUNTER = new AtomicInteger();

        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().write("hello");
            COUNTER.incrementAndGet();
        }
    }

    public static class Empty extends HttpServlet {
        public static final AtomicInteger COUNTER = new AtomicInteger();

        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().write("");
            COUNTER.incrementAndGet();
        }
    }
}

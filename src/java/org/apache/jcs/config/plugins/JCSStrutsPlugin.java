package org.apache.jcs.config.plugins;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import javax.servlet.ServletException;

import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;

/**
 *   This plugin provides a simple way to integrate with Struts.
 *   It allows you to specify the configuration file for JCS in
 *   the struts-config.xml file.
 *   JCS will initialize and be destroyed at applicatiohn shutdown.
 *
 *   Add these lines to your struts-config.xml
 *
 *   <plug-in className="org.apache.jcs.config.plugins.JCSStrutsPlugin">
 *    <set-property property="config-file-name" value="cache.ccf" />
 *   </plug-in>
 *
 *
 */
public class JCSStrutsPlugin implements PlugIn
{

  private static CompositeCacheManager cacheMgr;

  /**
   * Initialize JCS with config-file-name param.
   * If no file is specified, cache.ccf will be used.
   *
   * @param servlet ActionServlet
   * @param config ModuleConfig
   * @throws ServletException
   */
  public void init( ActionServlet servlet, ModuleConfig config )
    throws ServletException
  {

    String configFileName = servlet.getInitParameter( "config-file-name" );
    if ( configFileName == null )
    {
      configFileName = "cache.ccf";
    }

    if ( cacheMgr == null )
    {
        if ( configFileName == null )
        {
            cacheMgr = CompositeCacheManager.getInstance();
        }
        else
        {
            cacheMgr = CompositeCacheManager.getUnconfiguredInstance();

            cacheMgr.configure( configFileName );
        }
    }

  }

  /**
   * Destroys all the regions.
   */
  public void destroy()
  {
    cacheMgr.release();
  }

}

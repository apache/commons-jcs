package org.apache.jcs.auxiliary.lateral.http.server;

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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class DeleteCacheServlet extends AbstractDeleteCacheServlet
{

    /** Description of the Method */
    public void init( ServletConfig config )
        throws ServletException
    {

        cacheMgr = CompositeCacheManager.getInstance();

        super.init( config );

    }

    /**
     * Gets the servletInfo attribute of the DeleteCacheServlet object
     *
     * @return The servletInfo value
     */
    public String getServletInfo()
    {
        return "DeleteCacheServlet Version 2, extends AbstractDeleteCacheServlet";
    }

}

// end class

//public class DeleteCacheServlet extends HttpServlet implements SingleThreadModel {
//  boolean debug = true;
//  String servlet = "/jcs/cache/DeleteCache"; //"/ramraf/DeleteCacheServlet";
//  Logger log;
//  CacheHub cacheMgr;
//  //Initialize global variables
//  public void init(ServletConfig config) throws ServletException {
//   log = LoggerManager.getLogger( this );
//   cacheMgr = CacheManagerFactory.getInstance();
//   super.init( config );
//  }// end init
//  //SERVICE THE REQUEST
//  public void service(HttpServletRequest req,
//    HttpServletResponse res)
//    throws ServletException, IOException {
//    if ( log.isDebugEnabled() ) {
//      log.logIt( "The DeleteCacheServlet has been called.\n" );
//    }
//    Hashtable params = new Hashtable();
//		res.setContentType("text/html");
//		PrintWriter out = res.getWriter();
//    try {
//			String paramName;
//			String paramValue;
//			// GET PARAMETERS INTO HASHTABLE
//			for ( Enumeration e = req.getParameterNames() ; e.hasMoreElements() ; ) {
//				paramName = (String)e.nextElement();
//				paramValue = req.getParameter(paramName);
//				params.put(paramName, paramValue);
//        log.logIt(paramName + "=" + paramValue);
//			}
//		  String hashtableName = req.getParameter( "hashtableName" );
// 		  String key = req.getParameter( "key" );
//      if ( hashtableName == null ) {
//  		  hashtableName = req.getParameter( "cacheName" );
//      }
//      if ( hashtableName != null ) {
//        if ( log.isDebugEnabled() ) {
//          log.logIt( "hashtableName = " + hashtableName );
//        }
//        out.println( "hashtableName = " + hashtableName );
//        ICache cache = cacheMgr.getCache( hashtableName );
//        if ( key != null ) {
//          if ( key.toUpperCase().equals("ALL") ) {
//            cache.removeAll();
//            if ( log.isDebugEnabled() ) {
//              log.logIt( "Removed all elements from " + hashtableName );
//            }
//            out.println( "key = " + key );
//          } else {
//            if ( log.isDebugEnabled() ) {
//              log.logIt( "key = " + key );
//            }
//            out.println( "key = " + key );
//            StringTokenizer toke = new StringTokenizer( key, "_" );
//            while ( toke.hasMoreElements() ) {
//              String temp = (String)toke.nextElement();
//              // this remove shouldn't spawn another lateral remove call
//              // it is debateable whether is should call a remote remove
//              // probably not since this should only be called
//              // non remote caches.  Running the two together could be a mess.
//              // If a remove call was made on a cahce with both, then the remote
//              // should have been called.  If it wasn't then the remote is down.
//              // we'll assume it is down for all.
//              //cache.remove( key, cache.LATERAL_INVOKATION );
//              //cache.remove( key, true );
//              cache.remove( key );
//              if ( log.isDebugEnabled() ) {
//                log.logIt( "Removed " + temp + " from " + hashtableName );
//              }
//            }
//          }
//        } else {
//          out.println( "key is null" );
//        }
//      } else {
//        out.println( "hashTableName is null" );
//      }
//      out.println( "<br>" );
//			int antiCacheRandom = (int)(10000.0 * Math.random() );
//      out.println( "<a href=" + servlet + "?antiCacheRandom=" + antiCacheRandom + ">List all caches</a><br>" );
//      out.println( "<br>" );
//      String[] list = cacheMgr.getCacheNames();
//      for (int i=0; i < list.length; i++) {
//        String name = list[i];
//        out.println( "<a href=" + servlet + "?hashtableName=" + name + "&key=ALL&antiCacheRandom=" + antiCacheRandom + ">" + name + "</a><br>" );
//      }
//    }//CATCH EXCEPTIONS
//    catch (Exception e) {
//      log.logEx( e );
//      //log.logIt( "hashtableName = " + hashtableName );
//      //log.logIt( "key = " + key );
//    }// end try{
//    finally {
//			String isRedirect = (String)params.get( "isRedirect" );
//      if ( isRedirect == null ) {
//        isRedirect = "N";
//      }
//			if ( log.isDebugEnabled() ) {
//				log.logIt( "isRedirect = " + isRedirect );
//			}
//      String url;
//			if ( isRedirect.equals("Y")) {
//				url = (String)params.get( "url" );
//				if ( log.isDebugEnabled() ) {
//					log.logIt( "url = " + url );
//				}
//				res.sendRedirect( url );	// will not work if there's a previously sent header
//				out.println( "<br>\n" );
// 				out.println( " <script>" );
//				out.println( " location.href='" + url + "'; ");
//				out.println( " </script> " );
//				out.flush();
//			} else {
//        url="";
//      }
//    }
//  } //end service()
//  /////////////////////////////////////////////////////////////////////////
//  public void destroy() {
//    cacheMgr.release();
//  }
//  //Get Servlet information
//  public String getServletInfo() {
//    return "DeleteCacheServlet Information";
//  }
//}//end servlet


package org.apache.jcs.servlet;

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

import javax.servlet.http.HttpServlet;

/**
 * Description of the Class
 *
 */
public class ListTestCacheServlet
    extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    //    /** Description of the Field */
    //    protected static CacheHub cacheMgr;
    //    /** Description of the Field */
    //    protected static ICache zipListCache;
    //    /** Description of the Field */
    //    protected static ICache zipToCityCache;
    //    // you can also access via the access classes
    //    // these provide some useful methods, They are necessary for groups.
    //    /** Description of the Field */
    //    protected static IGroupCacheAccess cityGroupCache;
    //
    //    private static int numToShow = 1000;
    //
    //
    //    /** Description of the Method */
    //    public void init( ServletConfig config )
    //        throws ServletException
    //    {
    //        cacheMgr = GroupCacheHub.getInstance();
    //        // use deafult cattr
    //        zipListCache = cacheMgr.getCache( "zipListCache" );
    //
    //        // I want to modify the defualts so I will get the
    //        // cattr and modify it. Otherwise I could call
    //        // zipToCityCache = cacheMgr.getCache( "zipToCityCache" );
    //        ICompositeCacheAttributes cattr = cacheMgr.getDefaultCacheAttributes();
    //        cattr.setMaxObjects( 10000 );
    //        zipToCityCache = cacheMgr.getCache( "zipToCityCache", cattr );
    //        // get another copy of the cattr
    //        cattr = cacheMgr.getDefaultCacheAttributes();
    //        cattr.setMaxObjects( 10000 );
    //        try
    //        {
    //            cityGroupCache = GroupCacheAccess.getGroupAccess( "cityGroupCache", cattr
    // );
    //        }
    //        catch ( Exception e )
    //        {
    //            log.error( e );
    //        }
    //    }
    //    // end init
    //
    //    /** Description of the Method */
    //    public void service( HttpServletRequest req, HttpServletResponse res )
    //        throws ServletException,
    //        IOException
    //    {
    //
    //        Hashtable params = new Hashtable();
    //        res.setContentType( "text/html" );
    //        PrintWriter out = res.getWriter();
    //
    //        try
    //        {
    //
    //            out.println( "<html><body bgcolor=#FFFFFF>" );
    //            out.println( "<a href=?task=zipList>List of Zip Codes</a><br>" );
    //
    //            out.println( "<br>" );
    //            out.println( "<form method=get action=\"\">" );
    //            out.println( "<input type=hidden name=task value=cityForZip>" );
    //            out.println( "<input type=text name=zip value=>" );
    //            out.println( "<input type=submit value='Find City'>" );
    //            out.println( "</form>" );
    //            out.println( "<br>" );
    //            out.println( "<br>" );
    //
    //            String paramName;
    //            String paramValue;
    //            // GET PARAMETERS INTO HASHTABLE
    //            for ( Enumeration e = req.getParameterNames(); e.hasMoreElements(); )
    //            {
    //                paramName = ( String ) e.nextElement();
    //                paramValue = req.getParameter( paramName );
    //                params.put( paramName, paramValue );
    //                if ( log.isDebugEnabled() )
    //                {
    //                    log.debug( paramName + "=" + paramValue );
    //                }
    //            }
    //
    //            String task = ( String ) params.get( "task" );
    //            if ( task == null )
    //            {
    //                task = "zipList";
    //            }
    //
    //            if ( task.equals( "cityForZip" ) )
    //            {
    //                getCity( params, out );
    //            }
    //            else if ( task.equals( "zipList" ) )
    //            {
    //                out.println( "Showing first " + numToShow + " <br>" );
    //                getList( params, out );
    //            }
    //            else if ( task.equals( "zipForCity" ) )
    //            {
    //                getZipForCity( params, out );
    //            }
    //        }
    //        //CATCH EXCEPTIONS
    //        catch ( Exception e )
    //        {
    //            log.error( e );
    //        }
    //        // end try{
    //        finally
    //        {
    //            String isRedirect = ( String ) params.get( "isRedirect" );
    //            if ( isRedirect == null )
    //            {
    //                isRedirect = "N";
    //            }
    //            if ( log.isDebugEnabled() )
    //            {
    //                log.debug( "isRedirect = " + isRedirect );
    //            }
    //            String url;
    //            if ( isRedirect.equals( "Y" ) )
    //            {
    //                url = ( String ) params.get( "url" );
    //                if ( log.isDebugEnabled() )
    //                {
    //                    log.debug( "url = " + url );
    //                }
    //                res.sendRedirect( url );
    //                // will not work if there's a previously sent header
    //                out.println( "<br>\n" );
    //                out.println( " <script>" );
    //                out.println( " location.href='" + url + "'; " );
    //                out.println( " </script> " );
    //                out.flush();
    //            }
    //            else
    //            {
    //                url = "";
    //            }
    //            out.println( "</body></html>" );
    //        }
    //
    //    }
    //    //end service()
    //
    //    /** Gets the city attribute of the ListTestCacheServlet object */
    //    private void getCity( Hashtable params, PrintWriter out )
    //    {
    //
    //        try
    //        {
    //            String zip = ( String ) params.get( "zip" );
    //
    //            String city = ( String ) zipToCityCache.get( zip );
    //            if ( city == null )
    //            {
    //                out.println( "<br>The city is NOT in the cache.<br>" );
    //                out.println( "<br>looking in file.<br>" );
    //                city = findCity( zip );
    //                if ( city == null )
    //                {
    //                    out.println( "<br>Couldn't find city.<br>" );
    //                }
    //                else
    //                {
    //                    out.println( "Zip code " + zip + " is in <b>" );
    //                    out.println( "<a href=\"?task=zipForCity&city=" + city + "\">" + city +
    // "</a>" );
    //                    out.println( "</b> city. <br>" );
    //                }
    //            }
    //            else
    //            {
    //                out.println( "Zip code " + zip + " is in <b>" );
    //                out.println( "<a href=\"?task=zipForCity&city=" + city + "\">" + city +
    // "</a>" );
    //                out.println( "</b> city. <br>" );
    //            }
    //        }
    //        catch ( Exception e )
    //        {
    //            log.error( e );
    //        }
    //
    //    }
    //    // end getCity
    //
    //    /** Gets the list attribute of the ListTestCacheServlet object */
    //    private void getList( Hashtable params, PrintWriter out )
    //    {
    //
    //        try
    //        {
    //            ArrayList zipList = ( ArrayList ) zipListCache.get( "zipList" );
    //
    //            // counter
    //            long counter = 0;
    //
    //            if ( zipList == null )
    //            {
    //                out.println( "<br>The list was not in the cache.<br>" );
    //
    //                zipList = new ArrayList();
    //
    //                // use some od the data from jisp examples
    //
    //                String s_nameInput = "zipcodes.txt";
    //                // open text file
    //                BufferedReader reader = null;
    //
    //                try
    //                {
    //                    reader = new BufferedReader( new FileReader( s_nameInput ) );
    //                }
    //                catch ( FileNotFoundException not_found )
    //                {
    //                    System.err.println( "ERROR: file " + s_nameInput + " was not found" );
    //                    return;
    //                }
    //
    //                // flag to break out of loops if an exception is thrown
    //                boolean broken = false;
    //
    //                while ( reader.ready() )
    //                {
    //
    //                    // read a line
    //                    String line = reader.readLine();
    //
    //                    if ( line.length() < 1 )
    //                    {
    //                        break;
    //                    }
    //
    //                    // count it
    //                    ++counter;
    //
    //                    //if ((counter % 100) == 0)
    //                    // System.out.println(" finished line #" + counter);
    //
    //                    int i;
    //
    //                    // extract ZIP
    //                    String textZIP = "";
    //
    //                    for ( i = 0; ( line.charAt( i ) != ',' ); ++i )
    //                    {
    //                        textZIP += line.charAt( i );
    //                    }
    //
    //                    String zip = textZIP;
    //
    //                    // extract city name
    //                    ++i;
    //
    //                    String city = line.substring( i );
    //
    //                    zipToCityCache.put( zip, city );
    //
    //                    // key, group, value
    //                    // could create a group if we need to retrieve the list of elements
    //                    cityGroupCache.putInGroup( city + ":" + zip, city, zip );
    //                    // or you could just use the : for partial removal if all you need
    //                    // to do is remove, can't do partial lookup like this
    //                    // if you wanted to store info instead of the zip this would
    //                    // be usefule
    //                    //cityGroupCache.put( city + ":" + zip, zip);
    //
    //                    zipList.add( zip );
    //                    if ( counter < numToShow )
    //                    {
    //                        out.println( "<a href=?task=cityForZip&zip=" + zip + ">" + zip + "<br>"
    // );
    //                    }
    //                }
    //                out.println( "<br>Finished creating list of " + counter + ".<br>" );
    //
    //                zipListCache.put( "zipList", zipList );
    //
    //            }
    //            else
    //            {
    //                Iterator it = zipList.iterator();
    //                while ( it.hasNext() && counter < numToShow )
    //                {
    //                    counter++;
    //                    String zip = ( String ) it.next();
    //                    out.println( "<a href=?task=cityForZip&zip=" + zip + ">" + zip +
    // "</a><br>" );
    //                }
    //
    //            }
    //
    //        }
    //        catch ( Exception e )
    //        {
    //            log.error( e );
    //        }
    //
    //    }
    //    // end zipList
    //
    //    /** Description of the Method */
    //    private String findCity( String zip2Find )
    //    {
    //
    //        try
    //        {
    //
    //            // use some od the data from jisp examples
    //
    //            String s_nameInput = "zipcodes.txt";
    //            // open text file
    //            BufferedReader reader = null;
    //
    //            try
    //            {
    //                reader = new BufferedReader( new FileReader( s_nameInput ) );
    //            }
    //            catch ( FileNotFoundException not_found )
    //            {
    //                System.err.println( "ERROR: file " + s_nameInput + " was not found" );
    //                return null;
    //            }
    //
    //            // flag to break out of loops if an exception is thrown
    //            boolean broken = false;
    //
    //            // counter
    //            long counter = 0;
    //            while ( reader.ready() )
    //            {
    //
    //                // read a line
    //                String line = reader.readLine();
    //
    //                if ( line.length() < 1 )
    //                {
    //                    break;
    //                }
    //
    //                // count it
    //                ++counter;
    //
    //                //if ((counter % 100) == 0)
    //                // System.out.println(" finished line #" + counter);
    //
    //                int i;
    //
    //                // extract ZIP
    //                String textZIP = "";
    //
    //                for ( i = 0; ( line.charAt( i ) != ',' ); ++i )
    //                {
    //                    textZIP += line.charAt( i );
    //                }
    //
    //                String zip = textZIP;
    //
    //                // extract city name
    //                ++i;
    //
    //                String city = line.substring( i );
    //
    //                if ( zip2Find.equals( zip ) )
    //                {
    //                    zipToCityCache.put( zip, city );
    //                    return city;
    //                }
    //            }
    //
    //        }
    //        catch ( Exception e )
    //        {
    //            log.error( e );
    //        }
    //
    //        return null;
    //    }
    //    // end findCity
    //
    //    /**
    //     * Gets the zipForCity attribute of the ListTestCacheServlet object
    //     */
    //    private void getZipForCity( Hashtable params, PrintWriter out )
    //    {
    //
    //        try
    //        {
    //            String city = ( String ) params.get( "city" );
    //
    //            // providing this feature adds a significant burden to the cache,
    //            // but it makes it able to completely implement the seesion api
    //            Enumeration en = this.cityGroupCache.getAttributeNames( city );
    //            if ( en == null )
    //            {
    //                out.println( "<br>There is no info for this city. <br>" );
    //            }
    //            else
    //            {
    //                while ( en.hasMoreElements() )
    //                {
    //                    out.println( "Zip code " + ( String ) en.nextElement() + " is in <b>" );
    //                    out.println( "<a href=\"?task=zipForCity&city=" + city + "\">" + city +
    // "</a>" );
    //                    out.println( "</b> city. <br>" );
    //                }
    //            }
    //
    //        }
    //        catch ( Exception e )
    //        {
    //            log.error( e );
    //        }
    //
    //    }
    //    // end getZipForCity

}
// end class

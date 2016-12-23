package org.apache.commons.jcs.access;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;
import org.apache.commons.jcs.engine.control.event.ElementEventHandlerMockImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Allows the user to run common cache commands from the command line for a test cache. This also
 * provide basic methods for use in unit tests.
 */
public class TestCacheAccess
{
    /** log instance */
    private static final Log log = LogFactory.getLog( TestCacheAccess.class );

    /** cache instance to use in testing */
    private CacheAccess<String, String> cache_control = null;

    /** cache instance to use in testing */
    private GroupCacheAccess<String, String> group_cache_control = null;

    /** do we use system.out.println to print out debug data? */
    private static boolean isSysOut = false;

    /** Construct and initialize the cachecontrol based on the config file. */
    public TestCacheAccess()
    {
        this( "testCache1" );
    }

    /**
     * @param regionName the name of the region.
     */
    public TestCacheAccess( String regionName )
    {
        try
        {
            cache_control = JCS.getInstance( regionName );
            group_cache_control = JCS.getGroupCacheInstance( regionName );
        }
        catch ( Exception e )
        {
            log.error( "Problem getting cache instance", e );
            p( e.toString() );
        }
    }

    /**
     * This is the main loop called by the main method.
     */
    public void runLoop()
    {
        try
        {
            // process user input till done
            boolean notDone = true;
            String message = null;
            // wait to dispose
            BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );

            help();

            while ( notDone )
            {
                p( "enter command:" );

                message = br.readLine();

                if ( message == null || message.startsWith( "help" ) )
                {
                    help();
                }
                else if ( message.startsWith( "gc" ) )
                {
                    System.gc();
                }
                else if ( message.startsWith( "getAttributeNames" ) )
                {
                    long n_start = System.currentTimeMillis();
                    String groupName = null;
                    StringTokenizer toke = new StringTokenizer( message );
                    int tcnt = 0;
                    while ( toke.hasMoreElements() )
                    {
                        tcnt++;
                        String t = (String) toke.nextElement();
                        if ( tcnt == 2 )
                        {
                            groupName = t.trim();
                        }
                    }
                    getAttributeNames( groupName );
                    long n_end = System.currentTimeMillis();
                    p( "---got attrNames for " + groupName + " in " + String.valueOf( n_end - n_start ) + " millis ---" );
                }
                else if ( message.startsWith( "shutDown" ) )
                {
                    CompositeCacheManager.getInstance().shutDown();
                    //cache_control.dispose();
                    notDone = false;
                    //System.exit( -1 );
                    return;
                }
                /////////////////////////////////////////////////////////////////////
                // get multiple from a region
                else if ( message.startsWith( "getm" ) )
                {
                    processGetMultiple( message );
                }
                else if ( message.startsWith( "getg" ) )
                {
                    processGetGroup( message );
                }
                else if ( message.startsWith( "getag" ) )
                {
                    processGetAutoGroup( message );
                }
                else if ( message.startsWith( "getMatching" ) )
                {
                    processGetMatching( message );
                }
                else if ( message.startsWith( "get" ) )
                {
                    processGet( message );
                }
                else if ( message.startsWith( "putg" ) )
                {
                    processPutGroup( message );
                }
                // put automatically
                else if ( message.startsWith( "putag" ) )
                {
                    processPutAutoGroup( message );
                }
                else if ( message.startsWith( "putm" ) )
                {
                    String numS = message.substring( message.indexOf( " " ) + 1, message.length() );
                    if ( numS == null )
                    {
                        p( "usage: putm numbertoput" );
                    }
                    else
                    {
                        int num = Integer.parseInt( numS.trim() );
                        putMultiple( num );
                    }
                }
                else if ( message.startsWith( "pute" ) )
                {
                    String numS = message.substring( message.indexOf( " " ) + 1, message.length() );
                    if ( numS == null )
                    {
                        p( "usage: putme numbertoput" );
                    }
                    else
                    {
                        int num = Integer.parseInt( numS.trim() );
                        long n_start = System.currentTimeMillis();
                        for ( int n = 0; n < num; n++ )
                        {
                            IElementAttributes attrp = cache_control.getDefaultElementAttributes();
                            ElementEventHandlerMockImpl hand = new ElementEventHandlerMockImpl();
                            attrp.addElementEventHandler( hand );
                            cache_control.put( "key" + n, "data" + n + " put from ta = junk", attrp );
                        }
                        long n_end = System.currentTimeMillis();
                        p( "---put " + num + " in " + String.valueOf( n_end - n_start ) + " millis ---" );
                    }
                }
                else if ( message.startsWith( "put" ) )
                {
                    processPut( message );
                }
                else if ( message.startsWith( "removem" ) )
                {
                    String numS = message.substring( message.indexOf( " " ) + 1, message.length() );
                    if ( numS == null )
                    {
                        p( "usage: removem numbertoremove" );
                    }
                    else
                    {
                        int num = Integer.parseInt( numS.trim() );
                        removeMultiple( num );
                    }
                }
                else if ( message.startsWith( "removeall" ) )
                {
                    cache_control.clear();
                    p( "removed all" );
                }
                else if ( message.startsWith( "remove" ) )
                {
                    String key = message.substring( message.indexOf( " " ) + 1, message.length() );
                    cache_control.remove( key );
                    p( "removed " + key );
                }
                else if ( message.startsWith( "deattr" ) )
                {
                    IElementAttributes ae = cache_control.getDefaultElementAttributes();
                    p( "Default IElementAttributes " + ae );
                }
                else if ( message.startsWith( "cloneattr" ) )
                {
                    String numS = message.substring( message.indexOf( " " ) + 1, message.length() );
                    if ( numS == null )
                    {
                        p( "usage: put numbertoput" );
                    }
                    else
                    {
                        int num = Integer.parseInt( numS.trim() );
                        IElementAttributes attrp = new ElementAttributes();
                        long n_start = System.currentTimeMillis();
                        for ( int n = 0; n < num; n++ )
                        {
                            attrp.clone();
                        }
                        long n_end = System.currentTimeMillis();
                        p( "---cloned attr " + num + " in " + String.valueOf( n_end - n_start ) + " millis ---" );
                    }
                }
                else if ( message.startsWith( "switch" ) )
                {
                    String name = message.substring( message.indexOf( " " ) + 1, message.length() );

                    setRegion( name );
                    p( "switched to cache = " + name );
                    p( cache_control.toString() );
                }
                else if ( message.startsWith( "stats" ) )
                {
                    p( cache_control.getStats() );
                }
                else if ( message.startsWith( "gc" ) )
                {
                    System.gc();
                    p( "Called system.gc()" );
                }
                else if ( message.startsWith( "random" ) )
                {
                    processRandom( message );
                }
            }
        }
        catch ( CacheException e )
        {
            p( e.toString() );
            e.printStackTrace( System.out );
        }
        catch (IOException e)
        {
            p( e.toString() );
            e.printStackTrace( System.out );
        }
    }

    /**
     * @param message
     */
    private void processGetMultiple( String message )
    {
        int num = 0;
        boolean show = true;

        StringTokenizer toke = new StringTokenizer( message );
        int tcnt = 0;
        while ( toke.hasMoreElements() )
        {
            tcnt++;
            String t = (String) toke.nextElement();
            if ( tcnt == 2 )
            {
                try
                {
                    num = Integer.parseInt( t.trim() );
                }
                catch ( NumberFormatException nfe )
                {
                    p( t + "not a number" );
                }
            }
            else if ( tcnt == 3 )
            {
                show = Boolean.valueOf( t ).booleanValue();
            }
        }

        if ( tcnt < 2 )
        {
            p( "usage: get numbertoget show values[true|false]" );
        }
        else
        {
            getMultiple( num, show );
        }
    }

    /**
     * @param message
     */
    private void processGetGroup( String message )
    {
        String key = null;
        String group = null;
        boolean show = true;

        StringTokenizer toke = new StringTokenizer( message );
        int tcnt = 0;
        while ( toke.hasMoreElements() )
        {
            tcnt++;
            String t = (String) toke.nextElement();
            if ( tcnt == 2 )
            {
                key = t.trim();
            }
            else if ( tcnt == 3 )
            {
                group = t.trim();
            }
            else if ( tcnt == 4 )
            {
                show = Boolean.valueOf( t ).booleanValue();
            }
        }

        if ( tcnt < 2 )
        {
            p( "usage: get key show values[true|false]" );
        }
        else
        {
            long n_start = System.currentTimeMillis();
            try
            {
                Object obj = group_cache_control.getFromGroup( key, group );
                if ( show && obj != null )
                {
                    p( obj.toString() );
                }
            }
            catch ( Exception e )
            {
                log.error( e );
            }
            long n_end = System.currentTimeMillis();
            p( "---got " + key + " from group " + group + " in " + String.valueOf( n_end - n_start ) + " millis ---" );
        }
    }

    /**
     * @param message
     */
    private void processGetAutoGroup( String message )
    {
        // get auto from group

        int num = 0;
        String group = null;
        boolean show = true;

        StringTokenizer toke = new StringTokenizer( message );
        int tcnt = 0;
        while ( toke.hasMoreElements() )
        {
            tcnt++;
            String t = (String) toke.nextElement();
            if ( tcnt == 2 )
            {
                num = Integer.parseInt( t.trim() );
            }
            else if ( tcnt == 3 )
            {
                group = t.trim();
            }
            else if ( tcnt == 4 )
            {
                show = Boolean.valueOf( t ).booleanValue();
            }
        }

        if ( tcnt < 2 )
        {
            p( "usage: get key show values[true|false]" );
        }
        else
        {
            long n_start = System.currentTimeMillis();
            try
            {
                for ( int a = 0; a < num; a++ )
                {
                    Object obj = group_cache_control.getFromGroup( "keygr" + a, group );
                    if ( show && obj != null )
                    {
                        p( obj.toString() );
                    }
                }
            }
            catch ( Exception e )
            {
                log.error( e );
            }
            long n_end = System.currentTimeMillis();
            p( "---got " + num + " from group " + group + " in " + String.valueOf( n_end - n_start ) + " millis ---" );
        }
    }

    /**
     * @param message
     * @throws CacheException
     */
    private void processPutGroup( String message )
        throws CacheException
    {
        String group = null;
        String key = null;
        StringTokenizer toke = new StringTokenizer( message );
        int tcnt = 0;
        while ( toke.hasMoreElements() )
        {
            tcnt++;
            String t = (String) toke.nextElement();
            if ( tcnt == 2 )
            {
                key = t.trim();
            }
            else if ( tcnt == 3 )
            {
                group = t.trim();
            }
        }

        if ( tcnt < 3 )
        {
            p( "usage: putg key group" );
        }
        else
        {
            long n_start = System.currentTimeMillis();
            group_cache_control.putInGroup( key, group, "data from putg ----asdfasfas-asfasfas-asfas in group " + group );
            long n_end = System.currentTimeMillis();
            p( "---put " + key + " in group " + group + " in " + String.valueOf( n_end - n_start ) + " millis ---" );
        }
    }

    /**
     * @param message
     * @throws CacheException
     */
    private void processPutAutoGroup( String message )
        throws CacheException
    {
        String group = null;
        int num = 0;
        StringTokenizer toke = new StringTokenizer( message );
        int tcnt = 0;
        while ( toke.hasMoreElements() )
        {
            tcnt++;
            String t = (String) toke.nextElement();
            if ( tcnt == 2 )
            {
                num = Integer.parseInt( t.trim() );
            }
            else if ( tcnt == 3 )
            {
                group = t.trim();
            }
        }

        if ( tcnt < 3 )
        {
            p( "usage: putag num group" );
        }
        else
        {
            long n_start = System.currentTimeMillis();
            for ( int a = 0; a < num; a++ )
            {
                group_cache_control.putInGroup( "keygr" + a, group, "data " + a
                    + " from putag ----asdfasfas-asfasfas-asfas in group " + group );
            }
            long n_end = System.currentTimeMillis();
            p( "---put " + num + " in group " + group + " in " + String.valueOf( n_end - n_start ) + " millis ---" );
        }
    }

    /**
     * @param message
     * @throws CacheException
     */
    private void processPut( String message )
        throws CacheException
    {
        String key = null;
        String val = null;
        StringTokenizer toke = new StringTokenizer( message );
        int tcnt = 0;
        while ( toke.hasMoreElements() )
        {
            tcnt++;
            String t = (String) toke.nextElement();
            if ( tcnt == 2 )
            {
                key = t.trim();
            }
            else if ( tcnt == 3 )
            {
                val = t.trim();
            }
        }

        if ( tcnt < 3 )
        {
            p( "usage: put key val" );
        }
        else
        {

            long n_start = System.currentTimeMillis();
            cache_control.put( key, val );
            long n_end = System.currentTimeMillis();
            p( "---put " + key + " | " + val + " in " + String.valueOf( n_end - n_start ) + " millis ---" );
        }
    }

    /**
     * @param message
     */
    private void processRandom( String message )
    {
        String rangeS = "";
        String numOpsS = "";
        boolean show = true;

        StringTokenizer toke = new StringTokenizer( message );
        int tcnt = 0;
        while ( toke.hasMoreElements() )
        {
            tcnt++;
            String t = (String) toke.nextElement();
            if ( tcnt == 2 )
            {
                rangeS = t.trim();
            }
            else if ( tcnt == 3 )
            {
                numOpsS = t.trim();
            }
            else if ( tcnt == 4 )
            {
                show = Boolean.valueOf( t ).booleanValue();
            }
        }

        String numS = message.substring( message.indexOf( " " ) + 1, message.length() );

        int range = 0;
        int numOps = 0;
        try
        {
            range = Integer.parseInt( rangeS.trim() );
            numOps = Integer.parseInt( numOpsS.trim() );
        }
        catch ( Exception e )
        {
            p( "usage: random range numOps show" );
            p( "ex.  random 100 1000 false" );
        }
        if ( numS == null )
        {
            p( "usage: random range numOps show" );
            p( "ex.  random 100 1000 false" );
        }
        else
        {
            random( range, numOps, show );
        }
    }

    /**
     * @param message
     */
    private void processGet( String message )
    {
        // plain old get

        String key = null;
        boolean show = true;

        StringTokenizer toke = new StringTokenizer( message );
        int tcnt = 0;
        while ( toke.hasMoreElements() )
        {
            tcnt++;
            String t = (String) toke.nextElement();
            if ( tcnt == 2 )
            {
                key = t.trim();
            }
            else if ( tcnt == 3 )
            {
                show = Boolean.valueOf( t ).booleanValue();
            }
        }

        if ( tcnt < 2 )
        {
            p( "usage: get key show values[true|false]" );
        }
        else
        {
            long n_start = System.currentTimeMillis();
            try
            {
                Object obj = cache_control.get( key );
                if ( show && obj != null )
                {
                    p( obj.toString() );
                }
            }
            catch ( Exception e )
            {
                log.error( e );
            }
            long n_end = System.currentTimeMillis();
            p( "---got " + key + " in " + String.valueOf( n_end - n_start ) + " millis ---" );
        }
    }

    /**
     * @param message
     */
    private void processGetMatching( String message )
    {
        // plain old get

        String pattern = null;
        boolean show = true;

        StringTokenizer toke = new StringTokenizer( message );
        int tcnt = 0;
        while ( toke.hasMoreElements() )
        {
            tcnt++;
            String t = (String) toke.nextElement();
            if ( tcnt == 2 )
            {
                pattern = t.trim();
            }
            else if ( tcnt == 3 )
            {
                show = Boolean.valueOf( t ).booleanValue();
            }
        }

        if ( tcnt < 2 )
        {
            p( "usage: getMatching key show values[true|false]" );
        }
        else
        {
            long n_start = System.currentTimeMillis();
            try
            {
                Map<String, String> results = cache_control.getMatching( pattern );
                if ( show && results != null )
                {
                    p( results.toString() );
                }
            }
            catch ( Exception e )
            {
                log.error( e );
            }
            long n_end = System.currentTimeMillis();
            p( "---gotMatching [" + pattern + "] in " + String.valueOf( n_end - n_start ) + " millis ---" );
        }
    }

    /**
     * Test harness.
     * @param args The command line arguments
     */
    public static void main( String[] args )
    {
        isSysOut = true;
        String ccfFileName = args[0];
        if ( ccfFileName != null )
        {
            JCS.setConfigFilename( ccfFileName );
        }
        TestCacheAccess tca = new TestCacheAccess( "testCache1" );
        tca.runLoop();
    }

    // end main
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Gets multiple items from the cache with keys of the form key1, key2, key3 up to key[num].
     * @param num int
     */
    public void getMultiple( int num )
    {
        getMultiple( num, false );
    }

    /**
     * @param num
     * @param show
     */
    public void getMultiple( int num, boolean show )
    {
        long n_start = System.currentTimeMillis();
        for ( int n = 0; n < num; n++ )
        {
            try
            {
                Object obj = cache_control.get( "key" + n );
                if ( show && obj != null )
                {
                    p( obj.toString() );
                }
            }
            catch ( Exception e )
            {
                log.error( e );
            }
        }
        long n_end = System.currentTimeMillis();
        p( "---got " + num + " in " + String.valueOf( n_end - n_start ) + " millis ---" );
    }

    /**
     * Puts multiple items into the cache.
     * @param num int
     */
    public void putMultiple( int num )
    {
        try
        {
            long n_start = System.currentTimeMillis();
            for ( int n = 0; n < num; n++ )
            {
                cache_control.put( "key" + n, "data" + n + " put from ta = junk" );
            }
            long n_end = System.currentTimeMillis();
            p( "---put " + num + " in " + String.valueOf( n_end - n_start ) + " millis ---" );
        }
        catch ( Exception e )
        {
            log.error( e );
        }
    }

    /**
     * Removes multiple items from the cache.
     * @param num int
     */
    public void removeMultiple( int num )
    {
        try
        {
            long n_start = System.currentTimeMillis();
            for ( int n = 0; n < num; n++ )
            {
                cache_control.remove( "key" + n );
            }
            long n_end = System.currentTimeMillis();
            p( "---removed " + num + " in " + String.valueOf( n_end - n_start ) + " millis ---" );
        }
        catch ( Exception e )
        {
            log.error( e );
        }
    }

    /**
     * The random method performs numOps number of operations. The operations will be a mix of puts,
     * gets, and removes. The key range will be from 0 to range.
     * @param range int The end of the key range.
     * @param numOps int The number of operations to perform
     */
    public void random( int range, int numOps )
    {
        random( range, numOps, false );
    }

    /**
     * @param range
     * @param numOps
     * @param show
     */
    public void random( int range, int numOps, boolean show )
    {
        try
        {
            for ( int i = 1; i < numOps; i++ )
            {
                Random ran = new Random( i );
                int n = ran.nextInt( 4 );
                int kn = ran.nextInt( range );
                String key = "key" + kn;
                if ( n == 1 )
                {
                    cache_control.put( key, "data" + i + " junk asdfffffffadfasdfasf " + kn + ":" + n );
                    if ( show )
                    {
                        p( "put " + key );
                    }
                }
                else if ( n == 2 )
                {
                    cache_control.remove( key );
                    if ( show )
                    {
                        p( "removed " + key );
                    }
                }
                else
                {
                    // slightly greater chance of get
                    Object obj = cache_control.get( key );
                    if ( show && obj != null )
                    {
                        p( obj.toString() );
                    }
                }

                if ( i % 10000 == 0 )
                {
                    p( cache_control.getStats() );
                }

            }
            p( "Finished random cycle of " + numOps );
        }
        catch ( Exception e )
        {
            p( e.toString() );
            e.printStackTrace( System.out );
        }
    }

    /**
     * Sets the region to be used by test methods.
     * @param name String -- Name of region
     */
    public void setRegion( String name )
    {
        try
        {
            cache_control = JCS.getInstance( name );
        }
        catch ( Exception e )
        {
            p( e.toString() );
            e.printStackTrace( System.out );
        }

    }

    /////////////////////////////////////////////////////////////////////////////
    /**
     * The tester will print to the console if isSysOut is true, else it will log. It is false by
     * default. When run via the main method, isSysOut will be set to true
     * @param s String to print or log
     */
    public static void p( String s )
    {
        if ( isSysOut )
        {
            System.out.println( s );
        }
        else
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( s );
            }
        }
    }

    /**
     * Displays usage information for command line testing.
     */
    public static void help()
    {
        p( "\n\n\n\n" );
        p( "type 'shutDown' to shutdown the cache" );
        p( "type 'getm num show[false|true]' to get num automatically from a region" );
        p( "type 'putm num' to put num automatically to a region" );
        p( "type 'removeall' to remove all items in a region" );
        p( "type 'remove key' to remove" );
        p( "type 'removem num' to remove a number automatically" );
        p( "type 'getMatching pattern show' to getMatching" );
        p( "type 'get key show' to get" );
        p( "type 'getg key group show' to get" );
        p( "type 'getag num group show' to get automatically from a group" );
        p( "type 'getAttributeNames group' to get a list og the group elements" );
        p( "type 'putg key group val' to put" );
        p( "type 'putag num group' to put automatically from a group" );
        p( "type 'put key val' to put" );
        p( "type 'stats' to get stats" );
        p( "type 'deattr' to get the default element attributes" );
        p( "type 'cloneattr num' to clone attr" );
        p( "type 'random range numOps' to put, get, and remove randomly" );
        p( "type 'switch name' to switch to this region name" );
        p( "type 'gc' to call System.gc()" );
        p( "type 'help' for commands" );

    }

    /**
     * Gets the attributeNames attribute of the TestCacheAccess class
     * @param groupName
     */
    public void getAttributeNames( String groupName )
    {
        Iterator<String> iter = group_cache_control.getGroupKeys( groupName ).iterator();

        while ( iter.hasNext() )
        {
            p( "=" + iter.next() );
        }
    }
}

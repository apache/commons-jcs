package org.apache.jcs.access;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.StringTokenizer;
import java.util.Iterator;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.ElementAttributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.engine.control.event.TestElementEventHandler;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * Allows the user to run common cache commands fromt he command line for a test
 * cache.
 *
 */
public class TestCacheAccess
{
  private final static Log log =
      LogFactory.getLog( TestCacheAccess.class );

  static GroupCacheAccess cache_control = null;

  /**
   * Test harness.
   *
   * @param args The command line arguments
   */
  public static void main( String[] args )
  {

    try
    {
      try
      {

        //CacheManager   cacheMgr = CacheManagerFactory.getInstance();
        //CacheAttributes cattr = new CacheAttributes();
        //cattr.setMaxObjects( 10 );
        //cattr.setUseDisk( true );
        //CacheAccess cache_control= CacheAccess.getAccess( "testCache" );
        //cache_control= GroupCacheAccess.getGroupAccess( "testGroupCache" );

        // start the local cache witht he appropriate props file
        CompositeCacheManager.getInstance( args[0] );

        cache_control = GroupCacheAccess.getGroupAccess( "testCache1" );

        // not necessary if you don't set default element attributes
//                try
//                {
//                    cache_control.defineGroup( "gr" );
//                }
//                catch ( CacheException ce )
//                {
//                    p( ce.toString() + " /n" + ce.getMessage() );
//                }
//                try
//                {
//                    cache_control.defineGroup( "gr2" );
//                }
//                catch ( CacheException ce )
//                {
//                    p( ce.toString() + " /n" + ce.getMessage() );
//                }

        GroupCacheAccess cache_control2 = GroupCacheAccess.getGroupAccess(
            "testCache2" );
        p( "cache_control = " + cache_control );

        // process user input till done
        boolean notDone = true;
        String message = null;
        // wait to dispose
        BufferedReader br = new BufferedReader( new InputStreamReader( System.
            in ) );

        help();

        while ( notDone )
        {
          p( "enter command:" );

          message = br.readLine();

          if ( message.startsWith( "help" ) )
          {
            help();
          }
//                    else
//                        if ( message.startsWith( "removeLateralDirect" ) )
//                    {
//                        removeLateralDirect( message );
//                    }
          else
          if ( message.startsWith( "getAttributeNames" ) )
          {
            long n_start = System.currentTimeMillis();
            String groupName = null;
            StringTokenizer toke = new StringTokenizer( message );
            int tcnt = 0;
            while ( toke.hasMoreElements() )
            {
              tcnt++;
              String t = ( String ) toke.nextElement();
              if ( tcnt == 2 )
              {
                groupName = t.trim();
              }
            }
            getAttributeNames( groupName );
            long n_end = System.currentTimeMillis();
            p( "---got attrNames for " + groupName + " in " +
               String.valueOf( n_end - n_start ) + " millis ---" );
          }
          else
          if ( message.startsWith( "dispose" ) )
          {
            cache_control.dispose();
            notDone = false;
            System.exit( -1 );
          }
          else

          // get multiple from a region
          if ( message.startsWith( "getm" ) )
          {

            int num = 0;
            boolean show = true;

            StringTokenizer toke = new StringTokenizer( message );
            int tcnt = 0;
            while ( toke.hasMoreElements() )
            {
              tcnt++;
              String t = ( String ) toke.nextElement();
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
              else
              if ( tcnt == 3 )
              {
                show = new Boolean( t ).booleanValue();
              }
            }

            if ( tcnt < 2 )
            {
              p( "usage: get numbertoget show values[true|false]" );
            }
            else
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
              p( "---got " + num + " in " + String.valueOf( n_end - n_start ) +
                 " millis ---" );
            }
          }
          else
          if ( message.startsWith( "getg" ) )
          {

            String key = null;
            String group = null;
            boolean show = true;
            boolean auto = true;

            StringTokenizer toke = new StringTokenizer( message );
            int tcnt = 0;
            while ( toke.hasMoreElements() )
            {
              tcnt++;
              String t = ( String ) toke.nextElement();
              if ( tcnt == 2 )
              {
                key = t.trim();
              }
              else
              if ( tcnt == 3 )
              {
                group = t.trim();
              }
              else
              if ( tcnt == 4 )
              {
                show = new Boolean( t ).booleanValue();
              }
              if ( tcnt == 5 )
              {
                auto = new Boolean( t ).booleanValue();
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
                Object obj = cache_control.getFromGroup( key,
                    group );
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
              p( "---got " + key + " from group " + group + " in " +
                 String.valueOf( n_end - n_start ) + " millis ---" );
            }
          }
          else
          if ( message.startsWith( "getag" ) )
          {
            // get auto from group

            int num = 0;
            String group = null;
            boolean show = true;
            boolean auto = true;

            StringTokenizer toke = new StringTokenizer( message );
            int tcnt = 0;
            while ( toke.hasMoreElements() )
            {
              tcnt++;
              String t = ( String ) toke.nextElement();
              if ( tcnt == 2 )
              {
                num = Integer.parseInt( t.trim() );
              }
              else
              if ( tcnt == 3 )
              {
                group = t.trim();
              }
              else
              if ( tcnt == 4 )
              {
                show = new Boolean( t ).booleanValue();
              }
              if ( tcnt == 5 )
              {
                auto = new Boolean( t ).booleanValue();
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
                  Object obj = cache_control.getFromGroup( "keygr" + a, group );
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
              p( "---got " + num + " from group " + group + " in " +
                 String.valueOf( n_end - n_start ) + " millis ---" );
            }
          }
          else
          if ( message.startsWith( "get" ) )
          {
            // plain old get

            String key = null;
            boolean show = true;

            StringTokenizer toke = new StringTokenizer( message );
            int tcnt = 0;
            while ( toke.hasMoreElements() )
            {
              tcnt++;
              String t = ( String ) toke.nextElement();
              if ( tcnt == 2 )
              {
                key = t.trim();
              }
              else
              if ( tcnt == 3 )
              {
                show = new Boolean( t ).booleanValue();
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
              p( "---got " + key + " in " + String.valueOf( n_end - n_start ) +
                 " millis ---" );
            }
          }
          else if ( message.startsWith( "putg" ) )
          {

            String group = null;
            String key = null;
            StringTokenizer toke = new StringTokenizer( message );
            int tcnt = 0;
            while ( toke.hasMoreElements() )
            {
              tcnt++;
              String t = ( String ) toke.nextElement();
              if ( tcnt == 2 )
              {
                key = t.trim();
              }
              else
              if ( tcnt == 3 )
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
//                            IElementAttributes attrp = new ElementAttributes();
//                            attrp.setIsLateral(true);
//                            attrp.setIsRemote(true);
              long n_start = System.currentTimeMillis();
              cache_control.putInGroup( key,
                                        group,
                                        "data from putg ----asdfasfas-asfasfas-asfas in group " +
                                        group );
              long n_end = System.currentTimeMillis();
              p( "---put " + key + " in group " + group + " in " +
                 String.valueOf( n_end - n_start ) + " millis ---" );
            }
          }
          else

          // put automatically
          if ( message.startsWith( "putag" ) )
          {

            String group = null;
            int num = 0;
            StringTokenizer toke = new StringTokenizer( message );
            int tcnt = 0;
            while ( toke.hasMoreElements() )
            {
              tcnt++;
              String t = ( String ) toke.nextElement();
              if ( tcnt == 2 )
              {
                num = Integer.parseInt( t.trim() );
              }
              else
              if ( tcnt == 3 )
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
//                            IElementAttributes attrp = new ElementAttributes();
//                            attrp.setIsLateral(true);
//                            attrp.setIsRemote(true);
              long n_start = System.currentTimeMillis();
              for ( int a = 0; a < num; a++ )
              {
                cache_control.putInGroup( "keygr" + a,
                                          group,
                                          "data " + a +
                    " from putag ----asdfasfas-asfasfas-asfas in group " +
                                          group );
              }
              long n_end = System.currentTimeMillis();
              p( "---put " + num + " in group " + group + " in " +
                 String.valueOf( n_end - n_start ) + " millis ---" );
            }
          }
          else
          if ( message.startsWith( "putm" ) )
          {
            String numS = message.substring( message.indexOf( " " ) + 1,
                                             message.length() );
            int num = Integer.parseInt( numS.trim() );
            if ( numS == null )
            {
              p( "usage: putm numbertoput" );
            }
            else
            {
//                            IElementAttributes attrp = new ElementAttributes();
              //attrp.setIsEternal(false);
              //attrp.setMaxLifeSeconds(30);

//                            attrp.setIsLateral(true);
//                            attrp.setIsRemote(true);
              long n_start = System.currentTimeMillis();
              for ( int n = 0; n < num; n++ )
              {
                cache_control.put( "key" + n,
                                   "data" + n + " put from ta = junk" );
              }
              long n_end = System.currentTimeMillis();
              p( "---put " + num + " in " + String.valueOf( n_end - n_start ) +
                 " millis ---" );
            }
          }
          else
          if ( message.startsWith( "pute" ) )
          {
            String numS = message.substring( message.indexOf( " " ) + 1,
                                             message.length() );
            int num = Integer.parseInt( numS.trim() );
            if ( numS == null )
            {
              p( "usage: putme numbertoput" );
            }
            else
            {
//                            IElementAttributes attrp = new ElementAttributes();
              //attrp.setIsEternal(false);
              //attrp.setMaxLifeSeconds(30);

//                            attrp.setIsLateral(true);
//                            attrp.setIsRemote(true);

              long n_start = System.currentTimeMillis();
              for ( int n = 0; n < num; n++ )
              {
                IElementAttributes attrp = cache_control.getElementAttributes();
                TestElementEventHandler hand = new TestElementEventHandler();
                attrp.addElementEventHandler( hand );
                cache_control.put( "key" + n,
                                   "data" + n + " put from ta = junk", attrp );
              }
              long n_end = System.currentTimeMillis();
              p( "---put " + num + " in " + String.valueOf( n_end - n_start ) +
                 " millis ---" );
            }
          }
          else
          if ( message.startsWith( "put" ) )
          {

            String key = null;
            String val = null;
            StringTokenizer toke = new StringTokenizer( message );
            int tcnt = 0;
            while ( toke.hasMoreElements() )
            {
              tcnt++;
              String t = ( String ) toke.nextElement();
              if ( tcnt == 2 )
              {
                key = t.trim();
              }
              else
              if ( tcnt == 3 )
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
//                            IElementAttributes attrp = new ElementAttributes();
//                            attrp.setIsLateral(true);
//                            attrp.setIsRemote(true);
              long n_start = System.currentTimeMillis();
//                            cache_control.put( key, val, attrp.copy() );
              cache_control.put( key, val );
              long n_end = System.currentTimeMillis();
              p( "---put " + key + " | " + val + " in " +
                 String.valueOf( n_end - n_start ) + " millis ---" );
            }
          }
          else
          if ( message.startsWith( "removeall" ) )
          {
            String key = message.substring( message.indexOf( " " ) + 1,
                                            message.length() );
            cache_control.remove();
            p( "removed all" );
          }
          else
          if ( message.startsWith( "remove" ) )
          {
            String key = message.substring( message.indexOf( " " ) + 1,
                                            message.length() );
            cache_control.remove( key );
            p( "removed " + key );
          }
          else
          if ( message.startsWith( "deattr" ) )
          {
            IElementAttributes ae = cache_control.getElementAttributes();
            p( "Default IElementAttributes " + ae );
          }
          else
          if ( message.startsWith( "cloneattr" ) )
          {
            String numS = message.substring( message.indexOf( " " ) + 1,
                                             message.length() );
            int num = Integer.parseInt( numS.trim() );
            if ( numS == null )
            {
              p( "usage: put numbertoput" );
            }
            else
            {
              IElementAttributes attrp = new ElementAttributes();
              long n_start = System.currentTimeMillis();
              for ( int n = 0; n < num; n++ )
              {
                attrp.copy();
              }
              long n_end = System.currentTimeMillis();
              p( "---cloned attr " + num + " in " +
                 String.valueOf( n_end - n_start ) + " millis ---" );
            }
          }
          else
          if ( message.startsWith( "switch" ) )
          {
            String numS = message.substring( message.indexOf( " " ) + 1,
                                             message.length() );
            try
            {
              int num = Integer.parseInt( numS.trim() );
            }
            catch ( Exception e )
            {
              p( "usage: switch number" );
              p( "  1 == testCache1" );
            }
            if ( numS == null )
            {
              p( "usage: switch number" );
              p( "  1 == testCache1" );
            }
            else
            {
              cache_control = GroupCacheAccess.getGroupAccess( "testCache" +
                  numS );
              p( "switched to cache = " + "testCache" + numS );
              p( cache_control.toString() );
            }
          }
        }

      }
      catch ( Exception e )
      {
        p( e.toString() );
        e.printStackTrace( System.out );
      }

    }
    catch ( Exception e )
    {
      p( e.toString() );
      e.printStackTrace( System.out );
    }

  }

  // end main


  /** Description of the Method */
  public static void p( String s )
  {
    System.out.println( s );
  }

  /** Description of the Method */
  public static void help()
  {

    p( "\n\n\n\n" );
    p( "type 'dispose' to dispose of the cache" );
    p(
        "type 'getm num show[false|true]' to get num automatically from a region" );
    p( "type 'putm num' to put num automatically to a region" );
    p( "type 'removeall' to remove all items in a region" );
    p( "type 'remove key' to remove" );
    p( "type 'get key show' to get" );
    p( "type 'getg key group show' to get" );
    p( "type 'getag num group show' to get automatically from a group" );
    p( "type 'getAttributeNames group' to get a list og the group elements" );
    p( "type 'putg key group val' to put" );
    p( "type 'putag num group' to put automatically from a group" );
    p( "type 'put key val' to put" );
    p( "type 'stats' to get stats" );
    p( "type 'deattr' to get teh default element attributes" );
    p( "type 'cloneattr num' to clone attr" );
//        p( "type 'removeLateralDirect key' to remove lateral" );
    p( "type 'switch number' to switch to testCache[number], 1 == testCache1" );
    p( "type 'help' for commands" );

  }

//    ////////////////////////////////////////
//    /**
//     *  Description of the Method
//     *
//     */
//    public static void removeLateralDirect( String message )
//    {
//        String key = null;
//        StringTokenizer toke = new StringTokenizer( message );
//        int tcnt = 0;
//        while ( toke.hasMoreElements() )
//        {
//            tcnt++;
//            String t = ( String ) toke.nextElement();
//            if ( tcnt == 2 )
//            {
//                key = t.trim();
//            }
//        }
//        if ( tcnt < 2 )
//        {
//            key = "ALL";
//        }
//        cache_control.removeLateralDirect( key );
//        p( "called delete multicast for key " + key );
//    }

  // end help

  /** Gets the attributeNames attribute of the TestCacheAccess class */
  static void getAttributeNames( String groupName )
  {
    Iterator iter = cache_control.getGroupKeys( groupName ).iterator();

    while ( iter.hasNext() )
    {
      p( "=" + ( String ) iter.next() );
    }
  }
}

// end test

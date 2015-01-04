package org.apache.commons.jcs.auxiliary.javagroups;

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

import junit.framework.TestCase;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;

import java.io.IOException;
import java.util.Properties;

public class JavaGroupsCacheTest extends TestCase
{
    public JavaGroupsCacheTest( String testName )
    {
        super( testName );
    }

    public void testStuff() throws Exception
    {
        // Create and configure two managers for the same channel

        CompositeCacheManager manager1 = new CompositeCacheManager();

        manager1.configure( getProperties() );

        CompositeCacheManager manager2 = new CompositeCacheManager();

        manager2.configure( getProperties() );

        // Get two the same region in each of the managers

        CacheAccess one = new CacheAccess( manager1.getCache( "testCache" ) );
        CacheAccess two = new CacheAccess( manager2.getCache( "testCache" ) );

        // Put some items to one of the mangers

        one.put( "1", "one" );
        one.put( "2", "two" );
        one.put( "3", "three" );
        one.put( "4", "four" );
        one.put( "5", "five" );

        // Wait for it to propogate -- FIXME: This is time sensitive and thus
        //                                    a bad idea for a unit test.

        Thread.sleep( 500 );

        // Assert that the values were correctly propogated

        assertEquals( "one",   two.get( "1" ) );
        assertEquals( "two",   two.get( "2" ) );
        assertEquals( "three", two.get( "3" ) );
        assertEquals( "four",  two.get( "4" ) );
        assertEquals( "five",  two.get( "5" ) );

        // Remove a value

        one.remove( "2" );

        Thread.sleep( 500 );

        assertEquals( null, two.get( "2") );

        // Remove all values

        one.remove();

        Thread.sleep( 500 );

        assertEquals( null, two.get( "1" ) );
        assertEquals( null, two.get( "2" ) );
        assertEquals( null, two.get( "3" ) );
        assertEquals( null, two.get( "4" ) );
        assertEquals( null, two.get( "5" ) );

        // Free caches

        manager1.freeCache( "testCache" );
        manager2.freeCache( "testCache" );
    }

    private Properties getProperties() throws IOException
    {
        Properties props = new Properties();

        props.load( getClass().getResourceAsStream(
            "/org/apache/jcs/auxiliary/javagroups/JavaGroupsCacheTest.ccf" ) );

        return props;
    }
}

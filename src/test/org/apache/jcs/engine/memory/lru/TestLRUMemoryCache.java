package org.apache.jcs.engine.memory.lru;

/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache Turbine" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache Turbine", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCache;

/**
 * Test which exercises the indexed disk cache. This one uses three different
 * regions for thre threads.
 *
 * @author <a href="mailto:james@jamestaylor.org">James Taylor</a>
 * @version $Id$
 */
public class TestLRUMemoryCache extends TestCase
{
    /**
     * Number of items to cache, twice the configured maxObjects for the
     * memory cache regions.
     */
    private static int items = 200;

    /**
     * Constructor for the TestDiskCache object.
     */
    public TestLRUMemoryCache( String testName )
    {
        super( testName );
    }

    /**
     * Main method passes this test to the text test runner.
     */
    public static void main( String args[] )
    {
        String[] testCaseName = {TestLRUMemoryCache.class.getName()};
        junit.textui.TestRunner.main( testCaseName );
    }

    /**
     * A unit test suite for JUnit
     *
     * @return    The test suite
     */
    public static Test suite()
    {
        ActiveTestSuite suite = new ActiveTestSuite();

        suite.addTest( new TestLRUMemoryCache( "testLRUMemoryCache" )
        {
            public void runTest() throws Exception
            {
                this.runTestForRegion( "indexedRegion1" );
            }
        } );

        /*
        suite.addTest( new TestDiskCache( "testIndexedDiskCache2" )
        {
            public void runTest() throws Exception
            {
                this.runTestForRegion( "indexedRegion2" );
            }
        } );

        suite.addTest( new TestDiskCache( "testIndexedDiskCache3" )
        {
            public void runTest() throws Exception
            {
                this.runTestForRegion( "indexedRegion3" );
            }
        } );
        */
        return suite;
    }

    /**
     * Test setup
     */
    public void setUp()
    {
        //JCS.setConfigFilename( "/TestDiskCache.ccf" );
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more
     * than the size of the memory cache, so items should be dumped.
     *
     * @param region Name of the region to access
     *
     * @exception Exception If an error occurs
     */
    public void runTestForRegion( String region )
        throws Exception
    {
        CompositeCacheManager cacheMgr = CompositeCacheManager
            .getUnconfiguredInstance();
        cacheMgr.configure( "/TestDiskCache.ccf" );
        CompositeCache cache = cacheMgr.getCache( region );

        LRUMemoryCache lru = new LRUMemoryCache();
        lru.initialize(cache);

        // Add items to cache

        for ( int i = 0; i < items; i++ )
        {
            ICacheElement ice = new CacheElement(
                cache.getCacheName(), i + ":key", region + " data " + i);
            ice.setElementAttributes(cache.getElementAttributes().copy());
            lru.update(ice);
        }

        // Test that initial items have been purged

        for ( int i = 0; i < 102; i++ )
        {
            this.assertNull(lru.get( i + ":key" ));
        }

        // Test that last items are in cache

        for ( int i = 102; i < items; i++ )
        {
            String value = (String) 
                ((ICacheElement)lru.get( i + ":key" )).getVal();
            this.assertEquals( region + " data " + i, value );
        }

        // Remove all the items

        for ( int i = 0; i < items; i++ )
        {
            lru.remove( i + ":key" );
        }

        // Verify removal

        for ( int i = 0; i < items; i++ )
        {
            assertNull( "Removed key should be null: " + i + ":key",
                        lru.get( i + ":key" ) );
        }
    }
}

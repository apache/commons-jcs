package org.apache.jcs;

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

/**
 * Test which exercises the indexed disk cache. Runs three threads against the
 * same region.
 *
 * @author <a href="mailto:james@jamestaylor.org">James Taylor</a>
 * @version $Id$
 */
public class TestDiskCacheConcurrent extends TestCase
{
    /**
     * Constructor for the TestDiskCache object.
     */
    public TestDiskCacheConcurrent( String testName )
    {
        super( testName );
    }

    /**
     * Main method passes this test to the text test runner.
     */
    public static void main( String args[] )
    {
        String[] testCaseName = {TestDiskCacheConcurrent.class.getName()};
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

        suite.addTest( new TestDiskCacheConcurrent( "testIndexedDiskCache1" )
        {
            public void runTest() throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 0, 200 );
            }
        } );

        suite.addTest( new TestDiskCacheConcurrent( "testIndexedDiskCache2" )
        {
            public void runTest() throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 1000, 1200 );
            }
        } );

        suite.addTest( new TestDiskCacheConcurrent( "testIndexedDiskCache3" )
        {
            public void runTest() throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 2000, 2200 );
            }
        });

        return suite;
    }

    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestDiskCache.ccf" );
    }

//    /**
//     * Tests the region which uses the indexed disk cache
//     */
//    public void testIndexedDiskCache()
//        throws Exception
//    {
//        runTestForRegion( "indexedRegion" );
//    }
//
//    /**
//     * Tests the region which uses the indexed disk cache
//     */
//    public void testIndexedDiskCache2()
//        throws Exception
//    {
//        runTestForRegion( "indexedRegion2" );
//    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more
     * than the size of the memory cache, so items should spool to disk.
     *
     * @param region Name of the region to access
     *
     * @exception Exception If an error occurs
     */
    public void runTestForRegion( String region, int start, int end )
        throws Exception
    {
        JCS jcs = JCS.getInstance( region );

        // Add items to cache

        for ( int i = start; i <= end; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        // Test that all items are in cache

        for ( int i = start; i <= end; i++ )
        {
            String value = ( String ) jcs.get( i + ":key" );

            this.assertEquals( region + " data " + i, value );
        }

        // Remove all the items

        for ( int i = start; i <= end; i++ )
        {
            jcs.destroy( i + ":key" );
        }

        // Verify removal

        for ( int i = start; i <= end; i++ )
        {
            assertNull( "Removed key should be null: " + i + ":key",
                        jcs.get( i + ":key" ) );
        }
    }
}

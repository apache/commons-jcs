package org.apache.jcs;

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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.jcs.JCS;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Random;

/**
 * Simple test for the JCS class.
 *
 * @version $Id$
 */
public class JCSUniTest
    extends TestCase
{
    Random random = new Random();

    /**
     * @param testName
     */
    public JCSUniTest( String testName )
    {
        super( testName );
    }

    /**
     * @return Test
     */
    public static Test suite()
    {
        return new TestSuite( JCSUniTest.class );
    }

    /**
     * @param args
     */
    public static void main( String args[] )
    {
        String[] testCaseName = { JCSUniTest.class.getName() };
        junit.textui.TestRunner.main( testCaseName );
    }

    /**
     * @throws Exception
     */
    public void testJCS()
        throws Exception
    {
        JCS jcs = JCS.getInstance( "testCache1" );

        LinkedList list = buildList();

        jcs.put( "some:key", list );

        assertEquals( list, jcs.get( "some:key" ) );
    }

    private LinkedList buildList()
    {
        LinkedList list = new LinkedList();

        for ( int i = 0; i < 100; i++ )
        {
            list.add( buildMap() );
        }

        return list;
    }

    private HashMap buildMap()
    {
        HashMap map = new HashMap();

        byte[] keyBytes = new byte[32];
        byte[] valBytes = new byte[128];

        for ( int i = 0; i < 10; i++ )
        {
            random.nextBytes( keyBytes );
            random.nextBytes( valBytes );

            map.put( new String( keyBytes ), new String( valBytes ) );
        }

        return map;
    }

}

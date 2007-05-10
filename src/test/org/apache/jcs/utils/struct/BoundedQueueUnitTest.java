package org.apache.jcs.utils.struct;

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

/**
 * Unit tests for the bounded queue.
 * <p>
 * @author Aaron Smuts
 */
public class BoundedQueueUnitTest
    extends TestCase
{
    /**
     * Verify null returned for empty.
     */
    public void testTakeLastEmpty()
    {
        // SETUP
        int maxSize = 10;
        BoundedQueue queue = new BoundedQueue( maxSize );

        // DO WORK
        Object result = queue.take();

        // VERIFY
        assertNull( "Result should be null", result );
    }

    /**
     * Verify that the queue returns the number of elements and the it does not exceed the max.
     */
    public void testSize()
    {
        // SETUP
        int maxSize = 10;
        BoundedQueue queue = new BoundedQueue( maxSize );

        // DO WORK
        for ( int i = 0; i < maxSize * 2; i++ )
        {
            queue.add( "adfadsf sad " + i );
        }

        int result = queue.size();

        // VERIFY
        assertEquals( "Result size not as expected", maxSize, result );
    }

    /**
     * Verify that the items come back in the order put in.
     */
    public void testFIFOOrderedTake()
    {
        // SETUP
        int maxSize = 10;
        BoundedQueue queue = new BoundedQueue( maxSize );

        // DO WORK
        for ( int i = 0; i < maxSize; i++ )
        {
            queue.add( String.valueOf( i ) );
        }


        // VERIFY

        for ( int i = 0; i < maxSize; i++ )
        {
            String result = (String)queue.take();
            assertEquals( "Result not as expected",  String.valueOf( i ) ,  result  );
        }
    }
}

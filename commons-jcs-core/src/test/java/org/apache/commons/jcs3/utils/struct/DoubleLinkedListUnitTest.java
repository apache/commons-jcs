package org.apache.commons.jcs3.utils.struct;



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

/** Unit tests for the double linked list. */
public class DoubleLinkedListUnitTest
    extends TestCase
{
    /** verify that the last is added when the list is empty. */
    public void testAddLast_Empty()
    {
        // SETUP
        DoubleLinkedList<DoubleLinkedListNode<String>> list = new DoubleLinkedList<>();

        String payload1 = "payload1";
        DoubleLinkedListNode<String> node1 = new DoubleLinkedListNode<>( payload1 );

        // WO WORK
        list.addLast( node1 );

        // VERIFY
        assertEquals( "Wrong last", node1, list.getLast() );
    }

    /** verify that the last is added when the list is empty. */
    public void testAddLast_NotEmpty()
    {
        // SETUP
        DoubleLinkedList<DoubleLinkedListNode<String>> list = new DoubleLinkedList<>();

        String payload1 = "payload1";
        DoubleLinkedListNode<String> node1 = new DoubleLinkedListNode<>( payload1 );

        String payload2 = "payload2";
        DoubleLinkedListNode<String> node2 = new DoubleLinkedListNode<>( payload2 );

        // WO WORK
        list.addLast( node1 );
        list.addLast( node2 );

        // VERIFY
        assertEquals( "Wrong last", node2, list.getLast() );
    }

    /** verify that it's added last. */
    public void testMakeLast_wasFirst()
    {
        // SETUP
        DoubleLinkedList<DoubleLinkedListNode<String>> list = new DoubleLinkedList<>();

        String payload1 = "payload1";
        DoubleLinkedListNode<String> node1 = new DoubleLinkedListNode<>( payload1 );

        String payload2 = "payload2";
        DoubleLinkedListNode<String> node2 = new DoubleLinkedListNode<>( payload2 );

        list.addFirst( node2 );
        list.addFirst(  node1 );

        // DO WORK
        list.makeLast( node1 );

        // VERIFY
        assertEquals( "Wrong size", 2, list.size() );
        assertEquals( "Wrong last", node1, list.getLast() );
        assertEquals( "Wrong first", node2, list.getFirst() );
    }

    /** verify that it's added last. */
    public void testMakeLast_wasLast()
    {
        // SETUP
        DoubleLinkedList<DoubleLinkedListNode<String>> list = new DoubleLinkedList<>();

        String payload1 = "payload1";
        DoubleLinkedListNode<String> node1 = new DoubleLinkedListNode<>( payload1 );

        String payload2 = "payload2";
        DoubleLinkedListNode<String> node2 = new DoubleLinkedListNode<>( payload2 );

        list.addFirst( node1 );
        list.addFirst(  node2 );

        // DO WORK
        list.makeLast( node1 );

        // VERIFY
        assertEquals( "Wrong size", 2, list.size() );
        assertEquals( "Wrong last", node1, list.getLast() );
        assertEquals( "Wrong first", node2, list.getFirst() );
    }

    /** verify that it's added last. */
    public void testMakeLast_wasAlone()
    {
        // SETUP
        DoubleLinkedList<DoubleLinkedListNode<String>> list = new DoubleLinkedList<>();

        String payload1 = "payload1";
        DoubleLinkedListNode<String> node1 = new DoubleLinkedListNode<>( payload1 );

        list.addFirst( node1 );

        // DO WORK
        list.makeLast( node1 );

        // VERIFY
        assertEquals( "Wrong size", 1, list.size() );
        assertEquals( "Wrong last", node1, list.getLast() );
        assertEquals( "Wrong first", node1, list.getFirst() );
    }

    /** verify that it's added last. */
    public void testMakeLast_wasInMiddle()
    {
        // SETUP
        DoubleLinkedList<DoubleLinkedListNode<String>> list = new DoubleLinkedList<>();

        String payload1 = "payload1";
        DoubleLinkedListNode<String> node1 = new DoubleLinkedListNode<>( payload1 );

        String payload2 = "payload2";
        DoubleLinkedListNode<String> node2 = new DoubleLinkedListNode<>( payload2 );

        String payload3 = "payload3";
        DoubleLinkedListNode<String> node3 = new DoubleLinkedListNode<>( payload3 );

        list.addFirst( node2 );
        list.addFirst(  node1 );
        list.addFirst(  node3 );

        // DO WORK
        list.makeLast( node1 );

        // VERIFY
        assertEquals( "Wrong size", 3, list.size() );
        assertEquals( "Wrong last", node1, list.getLast() );
        assertEquals( "Wrong first", node3, list.getFirst() );
    }
}

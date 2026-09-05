package org.apache.commons.jcs4.utils.struct;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Tests for the double linked list. */
class DoubleLinkedListUnitTest
{
    /** Verify that the last is added when the list is empty. */
    @Test
    void testAddLast_Empty()
    {
        // SETUP
        final DoubleLinkedList<DoubleLinkedListNode> list = new DoubleLinkedList<>();

        final DoubleLinkedListNode node1 = new DoubleLinkedListNode();

        // WO WORK
        list.addLast( node1 );

        // VERIFY
        assertEquals( node1, list.getLast(), "Wrong last" );
    }

    /** Verify that the last is added when the list is empty. */
    @Test
    void testAddLast_NotEmpty()
    {
        // SETUP
        final DoubleLinkedList<DoubleLinkedListNode> list = new DoubleLinkedList<>();

        final DoubleLinkedListNode node1 = new DoubleLinkedListNode();
        final DoubleLinkedListNode node2 = new DoubleLinkedListNode();

        // WO WORK
        list.addLast( node1 );
        list.addLast( node2 );

        // VERIFY
        assertEquals( node2, list.getLast(), "Wrong last" );
    }

    /** Verify that it's added last. */
    @Test
    void testMakeLast_wasAlone()
    {
        // SETUP
        final DoubleLinkedList<DoubleLinkedListNode> list = new DoubleLinkedList<>();

        final DoubleLinkedListNode node1 = new DoubleLinkedListNode();

        list.addFirst( node1 );

        // DO WORK
        list.makeLast( node1 );

        // VERIFY
        assertEquals( 1, list.size(), "Wrong size" );
        assertEquals( node1, list.getLast(), "Wrong last" );
        assertEquals( node1, list.getFirst(), "Wrong first" );
    }

    /** Verify that it's added last. */
    @Test
    void testMakeLast_wasFirst()
    {
        // SETUP
        final DoubleLinkedList<DoubleLinkedListNode> list = new DoubleLinkedList<>();

        final DoubleLinkedListNode node1 = new DoubleLinkedListNode();
        final DoubleLinkedListNode node2 = new DoubleLinkedListNode();

        list.addFirst( node2 );
        list.addFirst(  node1 );

        // DO WORK
        list.makeLast( node1 );

        // VERIFY
        assertEquals( 2, list.size(), "Wrong size" );
        assertEquals( node1, list.getLast(), "Wrong last" );
        assertEquals( node2, list.getFirst(), "Wrong first" );
    }

    /** Verify that it's added last. */
    @Test
    void testMakeLast_wasInMiddle()
    {
        // SETUP
        final DoubleLinkedList<DoubleLinkedListNode> list = new DoubleLinkedList<>();

        final DoubleLinkedListNode node1 = new DoubleLinkedListNode();
        final DoubleLinkedListNode node2 = new DoubleLinkedListNode();
        final DoubleLinkedListNode node3 = new DoubleLinkedListNode();

        list.addFirst( node2 );
        list.addFirst(  node1 );
        list.addFirst(  node3 );

        // DO WORK
        list.makeLast( node1 );

        // VERIFY
        assertEquals( 3, list.size(), "Wrong size" );
        assertEquals( node1, list.getLast(), "Wrong last" );
        assertEquals( node3, list.getFirst(), "Wrong first" );
    }

    /** Verify that it's added last. */
    @Test
    void testMakeLast_wasLast()
    {
        // SETUP
        final DoubleLinkedList<DoubleLinkedListNode> list = new DoubleLinkedList<>();

        final DoubleLinkedListNode node1 = new DoubleLinkedListNode();
        final DoubleLinkedListNode node2 = new DoubleLinkedListNode();

        list.addFirst( node1 );
        list.addFirst(  node2 );

        // DO WORK
        list.makeLast( node1 );

        // VERIFY
        assertEquals( 2, list.size(), "Wrong size" );
        assertEquals( node1, list.getLast(), "Wrong last" );
        assertEquals( node2, list.getFirst(), "Wrong first" );
    }

    /** Verify that remove and removeAll work. */
    @Test
    void testRemove()
    {
        // SETUP
        final DoubleLinkedList<DoubleLinkedListNode> list = new DoubleLinkedList<>();

        final DoubleLinkedListNode node1 = new DoubleLinkedListNode();
        final DoubleLinkedListNode node2 = new DoubleLinkedListNode();

        list.addFirst( node1 );
        list.addFirst( node2 );
        assertEquals( 2, list.size(), "Wrong size" );

        // DO WORK
        list.remove( node1 );

        // VERIFY
        assertEquals( 1, list.size(), "Wrong size" );
        assertEquals( node2, list.getLast(), "Wrong last" );
        assertEquals( node2, list.getFirst(), "Wrong first" );

        list.addFirst( node1 );
        assertEquals( 2, list.size(), "Wrong size" );
        assertEquals( node1, list.getFirst(), "Wrong first" );
        assertEquals( node2, list.getLast(), "Wrong last" );

        // DO WORK
        list.removeAll();

        // VERIFY
        assertEquals( 0, list.size(), "Wrong size" );
        assertEquals( list.getFirst().prev, list.getLast(), "Wrong last" );
        assertEquals( list.getLast().next, list.getFirst(), "Wrong first" );
        assertNull(node1.next, "node1.next should be null");
        assertNull(node1.prev, "node1.prev should be null");
        assertNull(node2.next, "node2.next should be null");
        assertNull(node2.prev, "node2.prev should be null");
    }
}

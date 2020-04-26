package org.apache.commons.jcs3.utils.struct;

import java.io.StringWriter;

import org.apache.commons.jcs3.TestLogConfigurationUtil;


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
public class DoubleLinkedListDumpUnitTest
    extends TestCase
{
    /** verify that the entries are dumped. */
    public void testDumpEntries_DebugTrue()
    {
        // SETUP
        StringWriter stringWriter = new StringWriter();
        TestLogConfigurationUtil.configureLogger( stringWriter, DoubleLinkedList.class.getName() );

        DoubleLinkedList<DoubleLinkedListNode<String>> list = new DoubleLinkedList<>();

        String payload1 = "payload1";
        DoubleLinkedListNode<String> node1 = new DoubleLinkedListNode<>( payload1 );

        String payload2 = "payload2";
        DoubleLinkedListNode<String> node2 = new DoubleLinkedListNode<>( payload2 );

        list.addLast( node1 );
        list.addLast( node2 );
        list.debugDumpEntries();

        // WO WORK
        String result = stringWriter.toString();

        // VERIFY
        assertTrue( "Missing node in log dump", result.indexOf( payload1 ) != -1 );
        assertTrue( "Missing node in log dump", result.indexOf( payload2 ) != -1 );
    }
}

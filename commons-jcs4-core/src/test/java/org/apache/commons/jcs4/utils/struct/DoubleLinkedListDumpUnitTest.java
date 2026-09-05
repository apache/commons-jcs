package org.apache.commons.jcs4.utils.struct;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;

import org.apache.commons.jcs4.TestLogConfigurationUtil;
import org.junit.jupiter.api.Test;

/** Tests for the double linked list. */
class DoubleLinkedListDumpUnitTest
{
    /** Verify that the entries are dumped. */
    @Test
    void testDumpEntries_DebugTrue()
    {
        // SETUP
        final StringWriter stringWriter = new StringWriter();
        TestLogConfigurationUtil.configureLogger( stringWriter, DoubleLinkedList.class.getName() );

        final DoubleLinkedList<DoubleLinkedListNode> list = new DoubleLinkedList<>();

        final DoubleLinkedListNode node1 = new DoubleLinkedListNode();
        final DoubleLinkedListNode node2 = new DoubleLinkedListNode();

        list.addLast( node1 );
        list.addLast( node2 );
        list.debugDumpEntries();

        // WO WORK
        final String result = stringWriter.toString();

        // VERIFY
        assertTrue(result.indexOf(node1.toString()) != -1, "Missing node in log dump");
        assertTrue(result.indexOf(node2.toString()) != -1, "Missing node in log dump");
    }
}

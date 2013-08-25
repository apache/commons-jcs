package org.apache.commons.jcs.auxiliary.disk;

import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.commons.jcs.TestLogConfigurationUtil;
import org.apache.commons.jcs.auxiliary.disk.LRUMapJCS;

/** Unit tests for the LRUMapJCS implementation. */
public class LRUMapJCSUnitTest
    extends TestCase
{
    /** Verify that we default to unlimited */
    public void testDefault()
    {
        // SETUP

        // DO WORK
        LRUMapJCS<String, String> map = new LRUMapJCS<String, String>();

        // VERIFY
        assertEquals( "Should be unlimted", -1, map.getMaxObjects() );
    }

    /** Verify that we default to unlimited */
    public void testLimited()
    {
        // SETUP
        int expected = 100;

        // DO WORK
        LRUMapJCS<String, String> map = new LRUMapJCS<String, String>( expected );

        // VERIFY
        assertEquals( "Should be expected", expected, map.getMaxObjects() );
    }

    /** Verify that the log message. */
    public void testProcessRemovedLRU()
    {
        // SETUP
        StringWriter stringWriter = new StringWriter();
        TestLogConfigurationUtil.configureLogger( stringWriter, LRUMapJCS.class.getName() );

        LRUMapJCS<String, String> map = new LRUMapJCS<String, String>();

        String key = "myKey";
        String value = "myValue";

        // DO WORK
        map.processRemovedLRU( key, value );
        String result = stringWriter.toString();

        // VERIFY
        assertTrue( "Debug log should contain the key,", result.indexOf( key ) != -1 );
        assertTrue( "Debug log should contain the value,", result.indexOf( value ) != -1 );
    }
}

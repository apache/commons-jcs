package org.apache.jcs.auxiliary.javagroups;

import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.access.CacheAccess;
import org.javagroups.log.Tracer;
import org.javagroups.log.Trace;

import java.util.Properties;
import java.io.IOException;

import junit.framework.TestCase;

public class JavaGroupsCacheWithGetTest extends TestCase
{
    public JavaGroupsCacheWithGetTest( String testName )
    {
        super( testName );
    }

    public void testWithGet() throws Exception
    {
        // Create and configure first manager and region

        CompositeCacheManager manager1 = new CompositeCacheManager();

        manager1.configure( getProperties() );

        CacheAccess one = new CacheAccess( manager1.getCache( "testCache" ) );

        one.put( "1", "one" );
        one.put( "2", "two" );
        one.put( "3", "three" );
        one.put( "4", "four" );
        one.put( "5", "five" );

        // Now get second manager and region, it will join the group

        CompositeCacheManager manager2 = new CompositeCacheManager();

        manager2.configure( getProperties() );

        CacheAccess two = new CacheAccess( manager2.getCache( "testCache" ) );

        assertEquals( "one",   two.get( "1" ) );
        assertEquals( "two",   two.get( "2" ) );
        assertEquals( "three", two.get( "3" ) );
        assertEquals( "four",  two.get( "4" ) );
        assertEquals( "five",  two.get( "5" ) );

        // Free caches

        manager1.freeCache( "testCache" );
        manager2.freeCache( "testCache" );
    }

    private Properties getProperties() throws IOException
    {
        Properties props = new Properties();

        props.load( getClass().getResourceAsStream(
            "/org/apache/jcs/auxiliary/javagroups/JavaGroupsCacheWithGetTest.ccf" ) );

        return props;
    }
}

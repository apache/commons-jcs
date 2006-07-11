package org.apache.jcs.auxiliary.disk.jdbc.mysql;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.disk.jdbc.TableState;

/**
 * Simple tests for the MySQLDisk Cache.
 * <p>
 * We will probably need to setup an hsql behind this, to test some of the pass through methods.
 * <p>
 * @author Aaron Smuts
 */
public class MySQLDiskCacheUnitTest
    extends TestCase
{
    /**
     * Verify that we simply return null on get if an optimization is in
     * progress and the cache is configured to balk on optimization.
     * <p>
     * This is a bit tricky since we don't want to have to have a mysql instance
     * running. Right now this doesn't really test much
     */
    public void testBalkOnGet()
    {
        MySQLDiskCacheAttributes attributes = new MySQLDiskCacheAttributes();
        String tableName = "JCS_TEST";
        attributes.setDriverClassName( "org.gjt.mm.mysql.Driver" );        
        attributes.setTableName( tableName );
        attributes.setBalkDuringOptimization( true );

        TableState tableState = new TableState( tableName );
        tableState.setState( TableState.OPTIMIZATION_RUNNING );

        MySQLDiskCache cache = new MySQLDiskCache( attributes, tableState );

        Object result = cache.doGet( "myKey" );
        assertNull( "The result should be null", result );
    }
}

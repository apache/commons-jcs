package org.apache.jcs.auxiliary.disk.jdbc.mysql;

import org.apache.jcs.auxiliary.disk.jdbc.TableState;

import junit.framework.TestCase;

/**
 * Hand run tests for the MySQL table optimizer.
 * <p>
 * @author Aaron Smuts
 */
public class MySQLTableOptimizerManualTester
    extends TestCase
{

    /**
     * Run the optimization against live a table.
     */
    public void testBasicOptimization()
    {
        MySQLDiskCacheAttributes attributes = new MySQLDiskCacheAttributes();
        attributes.setUserName( "java" );
        attributes.setPassword( "letmein" );
        attributes.setUrl( "jdbc:mysql://10.19.98.43:3306/flight_option_cache" );
        attributes.setDriverClassName( "org.gjt.mm.mysql.Driver" );
        String tableName = "JCS_STORE_FLIGHT_OPTION_ITINERARY";
        attributes.setTableName( tableName );
        TableState tableState = new TableState( tableName);
        
        MySQLTableOptimizer optimizer = new MySQLTableOptimizer( attributes, tableState );

        optimizer.optimizeTable();
    }
    
    /**
     * Run the optimization against live a table.
     */
    public void testBasicOptimizationUnknownTable()
    {
        MySQLDiskCacheAttributes attributes = new MySQLDiskCacheAttributes();
        attributes.setUserName( "java" );
        attributes.setPassword( "letmein" );
        attributes.setUrl( "jdbc:mysql://10.19.98.43:3306/flight_option_cache" );
        attributes.setDriverClassName( "org.gjt.mm.mysql.Driver" );
        String tableName = "DOESNTEXIST";
        attributes.setTableName( tableName );
        TableState tableState = new TableState( tableName);
        
        MySQLTableOptimizer optimizer = new MySQLTableOptimizer( attributes, tableState );

        optimizer.optimizeTable();
    }

}

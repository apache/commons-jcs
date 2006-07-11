package org.apache.jcs.auxiliary.disk.jdbc.mysql;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.disk.jdbc.JDBCDiskCache;
import org.apache.jcs.auxiliary.disk.jdbc.TableState;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * The MySQLDiskCache extends the core JDBCDiskCache.
 * <p>
 * Although the generic JDBC Disk Cache can be used for MySQL, the MySQL JDBC
 * Disk Cache has additional features, such as table optimization that are
 * particular to MySQL.
 * <p>
 * @author Aaron Smuts
 */
public class MySQLDiskCache
    extends JDBCDiskCache
{
    private static final long serialVersionUID = -7169488308515823491L;

    private final static Log log = LogFactory.getLog( MySQLDiskCache.class );

    MySQLDiskCacheAttributes mySQLDiskCacheAttributes;

    /**
     * Delegates to the super and makes use of the MySQL specific parameters
     * used for scheduled optimization.
     * <p>
     * @param attributes
     * @param tableState
     */
    public MySQLDiskCache( MySQLDiskCacheAttributes attributes, TableState tableState )
    {
        super( attributes, tableState );

        mySQLDiskCacheAttributes = attributes;

        if ( log.isDebugEnabled() )
        {
            log.debug( "MySQLDiskCacheAttributes = " + attributes );
        }
    }

    /**
     * This delegates to the generic JDBC disk cache. If we are currently
     * optimizing, then this method will balk and return null.
     * <p>
     * @param key
     *            Key to locate value for.
     * @return An object matching key, or null.
     */
    public ICacheElement doGet( Serializable key )
    {
        if ( this.getTableState().getState() == TableState.OPTIMIZATION_RUNNING )
        {
            if ( this.mySQLDiskCacheAttributes.isBalkDuringOptimization() )
            {
                return null;
            }
        }
        return super.doGet( key );
    }

    /**
     * This delegates to the generic JDBC disk cache. If we are currently
     * optimizing, then this method will balk and do nothing. A
     * <p>
     * @param element
     */
    public void doUpdate( ICacheElement element )
    {
        if ( this.getTableState().getState() == TableState.OPTIMIZATION_RUNNING )
        {
            if ( this.mySQLDiskCacheAttributes.isBalkDuringOptimization() )
            {
                return;
            }
        }
        super.doUpdate( element );
    }

}

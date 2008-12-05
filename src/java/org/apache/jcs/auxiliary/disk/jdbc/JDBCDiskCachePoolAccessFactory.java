package org.apache.jcs.auxiliary.disk.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** This is just a helper util. */
public class JDBCDiskCachePoolAccessFactory
{
    /** The local logger. */
    private final static Log log = LogFactory.getLog( JDBCDiskCachePoolAccessFactory.class );

    /**
     * Creates a JDBCDiskCachePoolAccess object from the JDBCDiskCachePoolAccessAttributes. This is
     * used by the connection pool manager.
     * <p>
     * @param poolAttributes
     * @return JDBCDiskCachePoolAccess
     * @throws Exception
     */
    public static JDBCDiskCachePoolAccess createPoolAccess( JDBCDiskCachePoolAccessAttributes poolAttributes )
        throws Exception
    {
        return createPoolAccess( poolAttributes.getDriverClassName(), poolAttributes.getPoolName(), poolAttributes
            .getUrl()
            + poolAttributes.getDatabase(), poolAttributes.getUserName(), poolAttributes.getPassword(), poolAttributes
            .getMaxActive() );
    }

    /**
     * Creates a JDBCDiskCachePoolAccess object from the JDBCDiskCacheAttributes. Use this when not
     * using the connection pool manager.
     * <p>
     * @param cattr
     * @return JDBCDiskCachePoolAccess
     * @throws Exception
     */
    public static JDBCDiskCachePoolAccess createPoolAccess( JDBCDiskCacheAttributes cattr )
        throws Exception
    {
        return createPoolAccess( cattr.getDriverClassName(), cattr.getName(), cattr.getUrl() + cattr.getDatabase(),
                                 cattr.getUserName(), cattr.getPassword(), cattr.getMaxActive() );
    }

    /**
     * Creates a pool access object and registers the driver.
     * <p>
     * @param driverClassName
     * @param poolName
     * @param fullURL = (url + database)
     * @param userName
     * @param password
     * @param maxActive
     * @return JDBCDiskCachePoolAccess
     * @throws Exception
     */
    public static JDBCDiskCachePoolAccess createPoolAccess( String driverClassName, String poolName, String fullURL,
                                                            String userName, String password, int maxActive )
        throws Exception
    {
        JDBCDiskCachePoolAccess poolAccess = null;

        try
        {
            // org.gjt.mm.mysql.Driver
            Class.forName( driverClassName );
        }
        catch ( ClassNotFoundException e )
        {
            log.error( "Couldn't find class for driver [" + driverClassName + "]", e );
        }

        poolAccess = new JDBCDiskCachePoolAccess( poolName );

        poolAccess.setupDriver( fullURL, userName, password, maxActive );

        poolAccess.logDriverStats();

        if ( log.isInfoEnabled() )
        {
            log.info( "Created: " + poolAccess );
        }
        
        return poolAccess;
    }
}

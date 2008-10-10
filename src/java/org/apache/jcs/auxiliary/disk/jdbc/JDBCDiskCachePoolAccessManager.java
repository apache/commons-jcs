package org.apache.jcs.auxiliary.disk.jdbc;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.utils.config.PropertySetter;

/**
 * Manages JDBCDiskCachePoolAccess instances. If a connectionPoolName value is supplied, the JDBC
 * disk cache will try to use this manager to create a pool. Assuming the name is "MyPool":
 * 
 * <pre>
 * jcs.connectionpool.MyPool.attributes.userName=MyUserName
 * jcs.connectionpool.MyPool.attributes.password=MyPassword
 * jcs.connectionpool.MyPool.attributes.url=MyUrl
 * jcs.connectionpool.MyPool.attributes.maxActive=MyMaxActive
 * jcs.connectionpool.MyPool.attributes.driverClassName=MyDriverClassName
 * </pre>
 */
public class JDBCDiskCachePoolAccessManager
{
    /** Singleton instance */
    private static JDBCDiskCachePoolAccessManager instance;

    /** Pool name to JDBCDiskCachePoolAccess */
    private Map pools = new HashMap();

    /** props prefix */
    public static final String POOL_CONFIGURATION_PREFIX = "jcs.connectionpool.";

    /** .attributes */
    public final static String ATTRIBUTE_PREFIX = ".attributes";

    /** The logger. */
    private static final Log log = LogFactory.getLog( JDBCDiskCachePoolAccessManager.class );

    /**
     * You can specify the properties to be used to configure the thread pool. Setting this post
     * initialization will have no effect.
     */
    private Properties props = null;

    /**
     * Singleton, private
     * <p>
     * @param props
     */
    private JDBCDiskCachePoolAccessManager( Properties props )
    {
        this.setProps( props );
    }

    /**
     * returns a singleton instance
     * <p>
     * @param props
     * @return JDBCDiskCachePoolAccessManager
     */
    public static synchronized JDBCDiskCachePoolAccessManager getInstance( Properties props )
    {
        if ( instance == null )
        {
            instance = new JDBCDiskCachePoolAccessManager( props );
        }
        return instance;
    }

    /**
     * Returns a pool for the name if one has been created. Otherwise it creates a pool.
     * <p>
     * @param poolName
     * @return JDBCDiskCachePoolAccess
     */
    public synchronized JDBCDiskCachePoolAccess getJDBCDiskCachePoolAccess( String poolName )
    {
        JDBCDiskCachePoolAccess poolAccess = (JDBCDiskCachePoolAccess) pools.get( poolName );

        if ( poolAccess == null )
        {
            JDBCDiskCachePoolAccessAttributes poolAttributes = configurePoolAccessAttributes( poolName );
            try
            {
                try
                {
                    // org.gjt.mm.mysql.Driver
                    Class.forName( poolAttributes.getDriverClassName() );
                }
                catch ( ClassNotFoundException e )
                {
                    log.error( "Couldn't find class for driver [" + poolAttributes.getDriverClassName() + "]", e );
                }

                poolAccess = new JDBCDiskCachePoolAccess( poolAttributes.getPoolName() );

                poolAccess.setupDriver( poolAttributes.getUrl() + poolAttributes.getDatabase(), poolAttributes
                    .getUserName(), poolAttributes.getPassword(), poolAttributes.getMaxActive() );

                poolAccess.logDriverStats();
                
                pools.put( poolName, poolAccess );
            }
            catch ( Exception e )
            {
                log.error( "Problem creating connection pool.", e );
            }
        }

        return poolAccess;
    }

    /**
     * Configures the attributes using the properties.
     * <p>
     * @param poolName
     * @return JDBCDiskCachePoolAccessAttributes
     */
    protected JDBCDiskCachePoolAccessAttributes configurePoolAccessAttributes( String poolName )
    {
        JDBCDiskCachePoolAccessAttributes poolAttributes = new JDBCDiskCachePoolAccessAttributes();

        String poolAccessAttributePrefix = POOL_CONFIGURATION_PREFIX + poolName + ATTRIBUTE_PREFIX;
        PropertySetter.setProperties( poolAttributes, getProps(), poolAccessAttributePrefix + "." );

        poolAttributes.setPoolName( poolName );

        if ( log.isInfoEnabled() )
        {
            log.info( "Configured attributes " + poolAttributes );
        }
        return poolAttributes;
    }

    /**
     * @param props the props to set
     */
    protected void setProps( Properties props )
    {
        this.props = props;
    }

    /**
     * @return the props
     */
    protected Properties getProps()
    {
        return props;
    }
}

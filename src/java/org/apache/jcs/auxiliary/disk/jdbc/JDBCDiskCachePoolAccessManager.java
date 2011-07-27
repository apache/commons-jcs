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
 * jcs.jdbcconnectionpool.MyPool.attributes.userName=MyUserName
 * jcs.jdbcconnectionpool.MyPool.attributes.password=MyPassword
 * jcs.jdbcconnectionpool.MyPool.attributes.url=MyUrl
 * jcs.jdbcconnectionpool.MyPool.attributes.maxActive=MyMaxActive
 * jcs.jdbcconnectionpool.MyPool.attributes.driverClassName=MyDriverClassName
 * </pre>
 */
public class JDBCDiskCachePoolAccessManager
{
    /** Singleton instance */
    private static JDBCDiskCachePoolAccessManager instance;

    /** Pool name to JDBCDiskCachePoolAccess */
    private final Map<String, JDBCDiskCachePoolAccess> pools = new HashMap<String, JDBCDiskCachePoolAccess>();

    /** props prefix */
    public static final String POOL_CONFIGURATION_PREFIX = "jcs.jdbcconnectionpool.";

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
        JDBCDiskCachePoolAccess poolAccess = pools.get( poolName );

        if ( poolAccess == null )
        {
            JDBCDiskCachePoolAccessAttributes poolAttributes = configurePoolAccessAttributes( poolName );
            try
            {
                poolAccess = JDBCDiskCachePoolAccessFactory.createPoolAccess( poolAttributes );

                if ( log.isInfoEnabled() )
                {
                    log.info( "Created shared pooled access for pool name [" + poolName + "]." );
                }
                pools.put( poolName, poolAccess );
            }
            catch ( Exception e )
            {
                log.error( "Problem creating connection poolfor pool name [" + poolName + "].", e );
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

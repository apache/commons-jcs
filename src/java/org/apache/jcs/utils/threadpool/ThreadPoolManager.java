package org.apache.jcs.utils.threadpool;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * This manages threadpools for an application using Doug Lea's Util Concurrent
 * package.
 * http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html
 * 
 * It is a singleton since threads need to be managed vm wide.
 * 
 * This managers force you to use a bounded queue. By default it uses the
 * current thread for execuion when the buffer is full and no free threads can
 * be created.
 * 
 * You can specify the props file to use or pass in a properties object prior to
 * configuration. By default it looks for configuration information in
 * thread_pool.properties.
 * 
 * If set the Properties object will take precedence.
 * 
 * If a value is not set for a particular pool, the hard coded defaults will be
 * used.
 * 
 * int boundarySize_DEFAULT = 75; int maximumPoolSize_DEFAULT = 150; int
 * minimumPoolSize_DEFAULT = 4; int keepAliveTime_DEFAULT = 1000 * 60 * 5;
 * boolean abortWhenBlocked = false; int startUpSize_DEFAULT = 4;
 * 
 * 
 * You can configure default settings by specifying a default pool in the
 * properties, ie "cache.ccf"
 * 
 * @author Aaron Smuts
 *  
 */
public class ThreadPoolManager
{

  private static final Log         log                      = LogFactory
                                                                .getLog( ThreadPoolManager.class );

  // DEFAULT SETTINGS, these are not final since they can be set
  // via the Propeties file or object
  private static int               boundarySize_DEFAULT     = 75;

  private static int               maximumPoolSize_DEFAULT  = 150;

  private static int               minimumPoolSize_DEFAULT  = 4;

  private static int               keepAliveTime_DEFAULT    = 1000 * 60 * 5;

  private static boolean           abortWhenBlocked_DEFAULT = false;

  private static int               startUpSize_DEFAULT      = 4;

  private static PoolConfiguration defaultConfig;

  // This is the default value. Setting this after
  // inialization will have no effect
  private static String            propsFileName            = "cache.ccf";

  // the root property name
  private static String            PROP_NAME_ROOT           = "thread_pool";

  private static String            DEFAULT_PROP_NAME_ROOT   = "thread_pool.default";

  // You can specify the properties to be used to configure
  // the thread pool. Setting this post initialization will have
  // no effect.
  private static Properties        props                    = null;

  private static HashMap           pools                    = new HashMap();

  // singleton instance
  private static ThreadPoolManager INSTANCE                 = null;

  /**
   * No instances please. This is a singleton.
   *  
   */
  private ThreadPoolManager()
  {
    configure();
  }

  /**
   * Creates a pool based on the configuration info.
   * 
   * @param config
   * @return
   */
  private PooledExecutor createPool( PoolConfiguration config )
  {

    PooledExecutor pool = new PooledExecutor( new BoundedBuffer( config
        .getBoundarySize() ), config.getMaximumPoolSize() );
    pool.setMinimumPoolSize( config.getMinimumPoolSize() );
    pool.setKeepAliveTime( config.getKeepAliveTime() );
    if (config.isAbortWhenBlocked())
    {
      pool.abortWhenBlocked();
    }
    else
    {
      pool.runWhenBlocked();
    }
    pool.createThreads( config.getStartUpSize() );

    return pool;
  }

  /**
   * Returns a configured instance of the ThreadPoolManger To specify a
   * configuation file or Properties object to use call the appropriate setter
   * prior to calling getInstance.
   * 
   * @return
   */
  public static synchronized ThreadPoolManager getInstance()
  {
    if (INSTANCE == null)
    {
      INSTANCE = new ThreadPoolManager();
    }
    return INSTANCE;
  }

  /**
   * Returns a pool by name. If a pool by this name does not exist in the
   * configuration file or properties, one will be created using the default
   * values.
   * 
   * Pools are lazily created.
   * 
   * 
   * @param name
   * @return
   */
  public PooledExecutor getPool( String name )
  {
    PooledExecutor pool = null;

    synchronized (pools)
    {
      pool = (PooledExecutor) pools.get( name );
      if (pool == null)
      {
        if (log.isDebugEnabled())
        {
          log.debug( "Creating pool for name [" + name + "]" );
        }
        PoolConfiguration config = this
            .loadConfig( PROP_NAME_ROOT + "." + name );
        pool = createPool( config );

        if (pool != null)
        {
          pools.put( name, pool );
        }

        if (log.isDebugEnabled())
        {
          log.debug( "PoolName = " + getPoolNames() );
        }
      }
    }

    return pool;
  }

  /**
   * returns the names of all configured pools.
   * 
   * @return ArrayList of string names
   */
  public ArrayList getPoolNames()
  {
    ArrayList poolNames = new ArrayList();
    synchronized (pools)
    {
      Set names = pools.keySet();
      Iterator it = names.iterator();
      while (it.hasNext())
      {
        poolNames.add( (String) it.next() );
      }
    }
    return poolNames;
  }

  /**
   * Setting this post initialization will have no effect.
   * 
   * @param propsFileName
   *          The propsFileName to set.
   */
  public static void setPropsFileName( String propsFileName )
  {
    ThreadPoolManager.propsFileName = propsFileName;
  }

  /**
   * 
   * @return Returns the propsFileName.
   */
  public static String getPropsFileName()
  {
    return propsFileName;
  }

  /**
   * This will be used if it is not null on initialzation. Setting this post
   * initialization will have no effect.
   * 
   * @param props
   *          The props to set.
   */
  public static void setProps( Properties props )
  {
    ThreadPoolManager.props = props;
  }

  /**
   * @return Returns the props.
   */
  public static Properties getProps()
  {
    return props;
  }

  //-------------------------- Private Methods ----------

  /**
   * Intialize the ThreadPoolManager and create all the pools defined in the
   * configuration.
   *  
   */
  private void configure()
  {
    if (log.isDebugEnabled())
    {
      log.debug( "Initializing ThreadPoolManager" );
    }

    if (props == null)
    {

      InputStream is = getClass().getResourceAsStream( "/" + propsFileName );

      try
      {
        props.load( is );

        if (log.isDebugEnabled())
        {
          log.debug( "File contained " + props.size() + " properties" );
        }
      }
      catch (IOException ex)
      {
        log.error( "Failed to load properties", ex );
        throw new IllegalStateException( ex.getMessage() );
      }
      finally
      {
        try
        {
          is.close();
        }
        catch (Exception ignore)
        {
          // Ignored
        }
      }

    }
    if (props == null)
    {
      log
          .warn( "No configuration settings found.  Using hardcoded default values for all pools." );
      props = new Properties();
    }

    // set intial default and then override if new
    // settings are available
    defaultConfig = new PoolConfiguration( boundarySize_DEFAULT,
        maximumPoolSize_DEFAULT, minimumPoolSize_DEFAULT,
        keepAliveTime_DEFAULT, abortWhenBlocked_DEFAULT, startUpSize_DEFAULT );

    defaultConfig = loadConfig( DEFAULT_PROP_NAME_ROOT );

  }

  /**
   * Configures the default PoolConfiguration settings
   *  
   */
  private PoolConfiguration loadConfig( String root )
  {

    PoolConfiguration config = (PoolConfiguration) defaultConfig.clone();

    // load default if they exist
    if (props.containsKey( root + ".boundarySize" ))
    {
      try
      {
        config.setBoundarySize( Integer.parseInt( (String) props.get( root
            + ".boundarySize" ) ) );
      }
      catch (NumberFormatException nfe)
      {
        log.error( "boundarySize not a number.", nfe );
      }
    }

    if (props.containsKey( root + ".maximumPoolSize" ))
    {
      try
      {
        config.setMaximumPoolSize( Integer.parseInt( (String) props.get( root
            + ".maximumPoolSize" ) ) );
      }
      catch (NumberFormatException nfe)
      {
        log.error( "maximumPoolSize not a number.", nfe );
      }
    }

    if (props.containsKey( root + ".minimumPoolSize" ))
    {
      try
      {
        config.setMinimumPoolSize( Integer.parseInt( (String) props.get( root
            + ".minimumPoolSize" ) ) );
      }
      catch (NumberFormatException nfe)
      {
        log.error( "minimumPoolSize not a number.", nfe );
      }
    }

    if (props.containsKey( root + ".keepAliveTime" ))
    {
      try
      {
        config.setKeepAliveTime( Integer.parseInt( (String) props.get( root
            + ".keepAliveTime" ) ) );
      }
      catch (NumberFormatException nfe)
      {
        log.error( "keepAliveTime not a number.", nfe );
      }
    }

    if (props.containsKey( root + ".startUpSize" ))
    {
      try
      {
        config.setAbortWhenBlocked( Boolean.getBoolean( (String) props
            .get( root + ".abortWhenBlocked" ) ) );
      }
      catch (NumberFormatException nfe)
      {
        log.error( "abortWhenBlocked not a boolean.", nfe );
      }
    }

    if (props.containsKey( root + ".startUpSize" ))
    {
      try
      {
        config.setStartUpSize( Integer.parseInt( (String) props.get( root
            + ".startUpSize" ) ) );
      }
      catch (NumberFormatException nfe)
      {
        log.error( "startUpSize not a number.", nfe );
      }
    }

    if (log.isDebugEnabled())
    {
      log.debug( root + " PoolConfiguration = " + config );
    }

    return config;
  }

}
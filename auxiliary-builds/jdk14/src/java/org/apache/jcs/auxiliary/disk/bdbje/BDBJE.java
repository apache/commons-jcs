package org.apache.jcs.auxiliary.disk.bdbje;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.auxiliary.disk.bdbje.behavior.IBDBJECacheAttributes;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseStats;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentStats;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.StatsConfig;
import com.sleepycat.je.Transaction;

/**
 *  All regions share an environment.
 *  In this environment each region has a database.
 *
 *  As this is implemented JE is extremely slow at getting things
 *  to disk, but extremely fast at retrievals.  i will experiment
 *  with a non-transactional enviroment.
 *
 *  To use the je.properties file to configure the Berkeley DB,
 *  put it in the environment home directory.  It will take
 *  precedent over all cache.ccf configurations.
 */
public class BDBJE
{

  private final static Log log = LogFactory.getLog( BDBJE.class );

  private File envDir;

  private static Environment coreEnv;
  private static Hashtable databases = new Hashtable();
  private static Database catalogDb;
  private static StoredClassCatalog catalog;

  private Database coreDb;

  private IBDBJECacheAttributes attributes;

  /**
   *  Create the cache with the IBDBJECacheAttributes
   */
  public BDBJE( IBDBJECacheAttributes attr )
  {
    attributes = attr;
    // create the directory if it doesn't exist.  JE won't do it.
    envDir = new File( attributes.getDiskPath() );
    if ( !envDir.exists() )
    {
      envDir.mkdir();
    }
    init();
  }

  /**
   *   This method makes sure that there is a core env defined.
   *   A single Environment is shared by all databases.
   */
  private synchronized void verifyCoreEnv()
  {

    if ( coreEnv == null )
    {
      try
      {
        /* Create a new, transactional database environment */
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional( true );
        // create the env if it doesn't exist, else do nothing
        envConfig.setAllowCreate( true );

        if ( this.attributes.getCacheSize() != -1 )
        {
          envConfig.setCacheSize( this.attributes.getCacheSize() );
          if ( log.isDebugEnabled() )
          {
            log.debug( "Set JE CacheSize to '" + this.attributes.getCacheSize() +
                       "'" );
          }
        }
        if ( this.attributes.getCachePercent() != -1 )
        {
          envConfig.setCachePercent( this.attributes.getCachePercent() );
          if ( log.isDebugEnabled() )
          {
            log.debug( "Set JE CachePercent to '" +
                       this.attributes.getCachePercent() + "'" );
          }
        }

        coreEnv = new Environment( envDir, envConfig );

        /*
         * A class catalog database is needed for storing class descriptions
         * for the serial binding used below.  This avoids storing class
         * descriptions redundantly in each record.
         */
        Transaction txn = coreEnv.beginTransaction( null, null );
        DatabaseConfig catalogConfig = new DatabaseConfig();
        catalogConfig.setTransactional( true );
        catalogConfig.setAllowCreate( true );
        catalogDb = coreEnv.openDatabase( txn, "catalogDb", catalogConfig );
        catalog = new StoredClassCatalog( catalogDb );
        txn.commit();
      }
      catch ( Exception e )
      {
        log.error( "Problem creating coreEnv", e );
      }
    } // end if
  }

  /**
   * Initialize the database for this region.
   */
  private synchronized void init()
  {
    coreDb = ( Database ) databases.get( attributes.getCacheName() );
    if ( coreDb == null )
    {
      verifyCoreEnv();

      try
      {
        /* Make a database within the core environment */
        Transaction txn = coreEnv.beginTransaction( null, null );
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional( true );
        dbConfig.setAllowCreate( true );
        dbConfig.setSortedDuplicates( false );
        // create a database for this region.  Aovids the overhead of
        // a secondary database for grouping.
        coreDb =
            coreEnv.openDatabase( txn, attributes.getCacheName(), dbConfig );
        if ( log.isInfoEnabled() )
        {
          log.info(
              "created db for region = '"
              + attributes.getCacheName()
              + "'" );
        }
        txn.commit();

        databases.put( attributes.getCacheName(), coreDb );
      }
      catch ( Exception e )
      {
        log.error( "Problem init", e );
      }
      if ( log.isDebugEnabled() )
      {
        log.debug( "Intitialized BDBJE" );
      }
    }
  } // end init

  /** Getts an item from the cache. */
  public ICacheElement get( Serializable key ) throws IOException
  {

    if ( log.isDebugEnabled() )
    {
      log.debug( "get key= '" + key + "'" );
    }

    ICacheElement ice = null;

    try
    {
      /* DatabaseEntry represents the key and data of each record */
      DatabaseEntry searchKey = new DatabaseEntry();
      EntryBinding keyBinding =
          new SerialBinding( catalog, key.getClass() );

      /*
       * Create a serial binding for MyData data objects.  Serial bindings
       * can be used to store any Serializable object.
       */
      EntryBinding dataBinding =
          new SerialBinding( catalog, ICacheElement.class );
      keyBinding.objectToEntry( key, searchKey );

      // foundKey and foundData are populated from the primary entry that
      DatabaseEntry foundKey = new DatabaseEntry();
      DatabaseEntry foundData = new DatabaseEntry();

      OperationStatus retVal =
          coreDb.get( null, searchKey, foundData, LockMode.DEFAULT );

      if ( retVal == OperationStatus.SUCCESS )
      {
        ice = ( ICacheElement ) dataBinding.entryToObject( foundData );
        if ( log.isDebugEnabled() )
        {
          log.debug( "key=" + key + " ice=" + ice );
        }
      }
    }
    catch ( Exception e )
    {
      log.error( "Problem updating", e );
    }
    return ice;
  }

  /** Puts a cache item to the cache. */
  public void update( ICacheElement item ) throws IOException
  {
    try
    {
      Transaction txn = coreEnv.beginTransaction( null, null );
      /*
       * Create a serial binding for MyData data objects.  Serial bindings
       * can be used to store any Serializable object.
       */
      EntryBinding dataBinding =
          new SerialBinding( catalog, ICacheElement.class );
      EntryBinding keyBinding =
          new SerialBinding( catalog, item.getKey().getClass() );

      /* DatabaseEntry represents the key and data of each record */
      DatabaseEntry dataEntry = new DatabaseEntry();
      DatabaseEntry keyEntry = new DatabaseEntry();

      dataBinding.objectToEntry( item, dataEntry );
      keyBinding.objectToEntry( item.getKey(), keyEntry );

      OperationStatus status = coreDb.put( txn, keyEntry, dataEntry );

      if ( log.isDebugEnabled() )
      {
        log.debug(
            "Put key '"
            + item.getKey()
            + "' on disk \n status = '"
            + status
            + "'" );
      }

      /*
       * Note that put will throw a DatabaseException when
       * error conditions are found such as deadlock.
       * However, the status return conveys a variety of
       * information. For example, the put might succeed,
       * or it might not succeed if the record exists
       * and duplicates were not
       */
      if ( status != OperationStatus.SUCCESS )
      {
        throw new DatabaseException(
            "Data insertion got status " + status );
      }
      txn.commit();
    }
    catch ( Exception e )
    {
      log.error( e );
    }
  }

  /** Removes the given key from the specified cache. */
  public void remove( Serializable key ) throws IOException
  {
    try
    {
      DatabaseEntry searchKey = new DatabaseEntry();

      EntryBinding keyBinding =
          new SerialBinding( catalog, key.getClass() );
      keyBinding.objectToEntry( key, searchKey );

      coreDb.delete( null, searchKey );
      if ( log.isDebugEnabled() )
      {
        log.debug( "removed, key = '" + key + "'" );
      }
    }
    catch ( Exception e )
    {
      log.error( "Problem removing key = '" + key + "'", e );
    }
  }

  /** Remove all keys from the sepcified cache. */
  public void removeAll() throws IOException
  {
    Transaction txn = null;
    try
    {
      txn = coreEnv.beginTransaction( null, null );
      coreDb.truncate( txn, false );
    }
    catch ( Exception e )
    {
      log.error( e );
    }
    finally
    {
      try
      {
        txn.commit();
      }
      catch ( Exception e )
      {
        log.error( e );
      }
    }

  }

  /*
   * Closes the database and the environment.  Client should do some
   * client checks.
   */
  protected synchronized void dispose()
  {
    if ( log.isInfoEnabled() )
    {
      log.info( "Disposig of region [" + attributes.getCacheName() +
                "], and environment if this is the only region." );
    }
    if ( coreEnv != null & coreDb != null )
    {

      // close databases
      try
      {
        if ( log.isInfoEnabled() )
        {
          log.info( this.getDBStats() );
        }
        coreDb.close();
        databases.remove( attributes.getCacheName() );
      }
      catch ( DatabaseException dbe )
      {
        log.error( "Error closing database: " + dbe.toString() );
      }

      if ( databases.size() == 0 )
      {
        // close catalog database
        try
        {
          catalogDb.close();
        }
        catch ( DatabaseException dbe )
        {
          log.error( "Error closing catalogDB: " + dbe.toString() );
        }

        /*        // synch env
                try
                {
                  coreEnv.sync();
                  if (log.isInfoEnabled()) {
                    log.info("Synchronizing coreEnv");
                  }
                }
                catch (DatabaseException dbe) {
                  log.error("Error synching coreEnv: " + dbe.toString());
                }
         */
        // close environment
        try
        {
          // Finally, close the environment.
          if ( log.isInfoEnabled() )
          {
            log.info( this.toString() );
          }
          coreEnv.close();
        }
        catch ( DatabaseException dbe )
        {
          log.error( "Error closing coreEnv: " + dbe.toString() );
        }
      }
    }
  }

  /*
   * Returns info about the JE
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    try
    {
      buf.append( "\n Environment Data:" );
      EnvironmentStats stats = coreEnv.getStats( new StatsConfig() );
      buf.append( "\n NCacheMiss: " + stats.getNCacheMiss() );
      buf.append( "\n CacheTotalBytes: " + stats.getCacheTotalBytes() );
      buf.append( "\n NCleanerRuns: " + stats.getNCleanerRuns() );
      buf.append( "\n -------------------------------------" );
      buf.append( "\n Other Databases in this Environment:" );
      List myDbNames = coreEnv.getDatabaseNames();
      for ( int i = 0; i < myDbNames.size(); i++ )
      {
        buf.append( "\n Database Name: " + ( String ) myDbNames.get( i ) );
      }
    }
    catch ( DatabaseException dbe )
    {
      log.error( "Error getting toString()" + dbe.toString() );
    }
    return buf.toString();
  }

  /**
   * Gets the stats for this db.
   * @return
   */
  public String getDBStats()
  {
    StringBuffer buf = new StringBuffer();
    try
    {
      DatabaseStats stats = coreDb.getStats( new StatsConfig() );
      buf.append( "\n This database name: " + coreDb.getDatabaseName() );
      buf.append( "\n This database stats: " );
      buf.append( "\n BinCount: " + stats.getBinCount() );
      buf.append( "\n DeletedLNCount: " + stats.getDeletedLNCount() );
      buf.append( "\n DupCountLNCount: " + stats.getDupCountLNCount() );
      buf.append( "\n InCount: " + stats.getInCount() );
      buf.append( "\n LnCount: " + stats.getLnCount() );
      buf.append( "\n MaxDepth: " + stats.getMaxDepth() );
    }
    catch ( DatabaseException dbe )
    {
      log.error( "Error getting stats" + dbe.toString() );
    }
    return buf.toString();
  }
}

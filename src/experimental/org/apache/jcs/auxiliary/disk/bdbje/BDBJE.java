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
import java.io.Serializable;

import java.io.IOException;

import org.apache.jcs.engine.behavior.ICacheElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

/**
 *  For now this is shared by all regions.
 */
public class BDBJE
{

  private final static Log log = LogFactory.getLog( BDBJE.class );

  private File envDir;

  private Environment coreEnv;
  private Database coreDb;
  private Database catalogDb;
  private StoredClassCatalog catalog;

  private BDBJECacheAttributes attributes;

  /**
   *
   */
  public BDBJE( BDBJECacheAttributes attr )
  {
    attributes = attr;
    envDir = new File( attributes.getDiskPath() );
    init();
  }

  private void init()
  {
    try
    {
      /* Create a new, transactional database environment */
      EnvironmentConfig envConfig = new EnvironmentConfig();
      envConfig.setTransactional( true );
      envConfig.setAllowCreate( true );
      coreEnv = new Environment( envDir, envConfig );

      /* Make a database within that environment */
      Transaction txn = coreEnv.beginTransaction( null, null );
      DatabaseConfig dbConfig = new DatabaseConfig();
      dbConfig.setTransactional( true );
      dbConfig.setAllowCreate( true );
      coreDb = coreEnv.openDatabase( txn, "bindingsDb", dbConfig );

      /*
       * A class catalog database is needed for storing class descriptions
       * for the serial binding used below.  This avoids storing class
       * descriptions redundantly in each record.
       */
      DatabaseConfig catalogConfig = new DatabaseConfig();
      catalogConfig.setTransactional( true );
      catalogConfig.setAllowCreate( true );
      catalogDb = coreEnv.openDatabase( txn, "catalogDb", catalogConfig );
      catalog = new StoredClassCatalog( catalogDb );
      txn.commit();

    }
    catch ( Exception e )
    {
      log.error( "Problem init", e );
    }
    if ( log.isDebugEnabled() )
    {
      log.debug( "Intitialized BDBJE" );
    }
  } // end intit

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

      // store for data
      //	DatabaseEntry dataEntry = new DatabaseEntry();

      /*
       * Create a serial binding for MyData data objects.  Serial bindings
       * can be used to store any Serializable object.
       */
      EntryBinding dataBinding =
          new SerialBinding( catalog, ICacheElement.class );
      keyBinding.objectToEntry( key, searchKey );

      // retrieve the data
      Cursor cursor = coreDb.openCursor( null, null );
      //if (cursor.getNext(keyEntry, dataEntry, LockMode.DEFAULT)
      //	== OperationStatus.SUCCESS) {
      //

      // searchKey is the key that we want to find in the secondary db.
      //DatabaseEntry searchKey = new DatabaseEntry(key..getBytes());

      // foundKey and foundData are populated from the primary entry that
      // is associated with the secondary db key.
      DatabaseEntry foundKey = new DatabaseEntry();
      DatabaseEntry foundData = new DatabaseEntry();

      OperationStatus retVal =
          cursor.getSearchKey( searchKey,
                               foundData, LockMode.DEFAULT );

      if ( retVal == OperationStatus.SUCCESS )
      {
        //Object keyN = bind.entryToObject(keyEntry);
        ice =
            ( ICacheElement ) dataBinding.entryToObject( foundData );
        if ( log.isDebugEnabled() )
        {
          log.debug( "key=" + key + " ice=" + ice );
        }
      }
      cursor.close();
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

      if ( log.isInfoEnabled() )
      {
        log.info( "Put key '" + item.getKey() + "' on disk \n status = '" +
                  status + "'" );
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
  public void remove( String cacheName, Serializable key ) throws IOException
  {

  }

  /** Remove all keys from the sepcified cache. */
  public void removeAll( String cacheName, long requesterId ) throws
      IOException
  {

  }

}

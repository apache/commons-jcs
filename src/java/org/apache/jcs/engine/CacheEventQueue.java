package org.apache.jcs.engine;

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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheListener;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * An event queue is used to propagate ordered cache events to one and only one
 * target listener.
 *
 * This is a modified version of the experimental version.
 * It should lazy initilaize the processor thread, and kill the thread if
 * the queue goes emtpy for a specified period, now set to 1 minute.  If
 * something comes in after that a new processor thread should be created.
 *
 * I didn't get all of Hanson's changes in yet, but I did add the syncronization.
 */
public class CacheEventQueue
    implements ICacheEventQueue
{
  private static final Log log = LogFactory.getLog( CacheEventQueue.class );

  // private LinkedQueue queue = new LinkedQueue();

  private static final int queueType = SINGLE_QUEUE_TYPE;
  
  // time to wait for an event before snuffing the background thread
  // if the queue is empty.
  // make configurable later
  private int waitToDieMillis = 10000;

  private ICacheListener listener;
  private long listenerId;
  private String cacheName;

  private int failureCount;
  private int maxFailure;

  // in milliseconds
  private int waitBeforeRetry;

  private boolean destroyed = true;
  private boolean working = true;
  private Thread processorThread;

  // Internal queue implementation

  private Object queueLock = new Object();

  // Dummy node

  private Node head = new Node();
  private Node tail = head;

  /**
   * Constructs with the specified listener and the cache name.
   *
   * @param listener
   * @param listenerId
   * @param cacheName
   */
  public CacheEventQueue( ICacheListener listener, long listenerId,
                          String cacheName )
  {
    this( listener, listenerId, cacheName, 10, 500 );
  }

  /**
   * Constructor for the CacheEventQueue object
   *
   * @param listener
   * @param listenerId
   * @param cacheName
   * @param maxFailure
   * @param waitBeforeRetry
   */
  public CacheEventQueue(
      ICacheListener listener,
      long listenerId,
      String cacheName,
      int maxFailure,
      int waitBeforeRetry )
  {
    if ( listener == null )
    {
      throw new IllegalArgumentException( "listener must not be null" );
    }

    this.listener = listener;
    this.listenerId = listenerId;
    this.cacheName = cacheName;
    this.maxFailure = maxFailure <= 0 ? 3 : maxFailure;
    this.waitBeforeRetry = waitBeforeRetry <= 0 ? 500 : waitBeforeRetry;

    if ( log.isDebugEnabled() )
    {
      log.debug( "Constructed: " + this );
    }
  }

  /*
   *  (non-Javadoc)
   * @see org.apache.jcs.engine.behavior.ICacheEventQueue#getQueueType()
   */
  public int getQueueType()
  {
    return queueType;
  }
  
  /**
   * Event Q is emtpy.
   */
  public synchronized void stopProcessing()
  {

    destroyed = true;
    processorThread = null;

  }

  /**
   * Returns the time to wait for events before killing the background thread.
   */
  public int getWaitToDieMillis()
  {
    return waitToDieMillis;
  }

  /**
   * Sets the time to wait for events before killing the background thread.
   */
  public void setWaitToDieMillis( int wtdm)
  {
    waitToDieMillis = wtdm;
  }

  /**
   * @return
   */
  public String toString()
  {
    return "CacheEventQueue [listenerId=" + listenerId + ", cacheName=" +
        cacheName + "]";
  }

  /**
   * @return The {3} value
   */
  public boolean isAlive()
  {
    return ( !destroyed );
  }

  public void setAlive( boolean aState )
  {
    destroyed = !aState;
  }

  /**
   * @return The {3} value
   */
  public long getListenerId()
  {
    return listenerId;
  }

  /**
   * Event Q is emtpy.
   */
  public synchronized void destroy()
  {
    if ( !destroyed )
    {
      destroyed = true;

      if ( log.isInfoEnabled() )
      {
          log.info( "Destroying queue, stats =  " + getStatistics() );
      }
      
      // sychronize on queue so the thread will not wait forever,
      // and then interrupt the QueueProcessor

      if ( processorThread != null )
      {
        synchronized ( queueLock )
        {
          processorThread.interrupt();
        }
      }
      processorThread = null;

      if ( log.isInfoEnabled() )
      {
          log.info( "Cache event queue destroyed: " + this );
      }
    }
    else
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "Destroy was called after queue was destroyed.  Doing nothing.  Stats =  " + getStatistics() );
        }
    }
  }

  /**
   * @param ce
   *          The feature to be added to the PutEvent attribute
   * @exception IOException
   */
  public synchronized void addPutEvent( ICacheElement ce ) throws IOException
  {
    if ( isWorking() )
    {
      put( new PutEvent( ce ) );
    }
    else
    {
      if ( log.isWarnEnabled() )
      {
        log.warn( "Not enqueuing Put Event for [" +
                     this +"] because it's non-functional." );
      }
    }
  }

  /**
   * @param key
   *          The feature to be added to the RemoveEvent attribute
   * @exception IOException
   */
  public synchronized void addRemoveEvent( Serializable key ) throws IOException
  {
    if ( isWorking() )
    {
      put( new RemoveEvent( key ) );
    }
    else
    {
      if ( log.isWarnEnabled() )
      {
        log.warn( "Not enqueuing Remove Event for [" +
                     this +"] because it's non-functional." );
      }
    }
  }

  /**
   * @exception IOException
   */
  public synchronized void addRemoveAllEvent() throws IOException
  {
    if ( isWorking() )
    {
      put( new RemoveAllEvent() );
    }
    else
    {
      if ( log.isWarnEnabled() )
      {
        log.warn( "Not enqueuing RemoveAll Event for [" +
                     this +"] because it's non-functional." );
      }
    }
  }

  /**
   * @exception IOException
   */
  public synchronized void addDisposeEvent() throws IOException
  {
    if ( isWorking() )
    {
      put( new DisposeEvent() );
    }
    else
    {
      if ( log.isWarnEnabled() )
      {
        log.warn( "Not enqueuing Dispose Event for [" +
                     this +"] because it's non-functional." );
      }
    }
  }

  /**
   * Adds an event to the queue.
   *
   * @param event
   */
  private void put( AbstractCacheEvent event )
  {
    Node newNode = new Node();
    if ( log.isDebugEnabled() )
    {
      log.debug( "Event entering Queue for " + cacheName + ": " + event );
    }

    newNode.event = event;

    synchronized ( queueLock )
    {
      tail.next = newNode;
      tail = newNode;
      if ( isWorking() )
      {
        if ( !isAlive() )
        {
          destroyed = false;
          processorThread = new QProcessor( this );
          processorThread.start();
          log.info( "Cache event queue created: " + this );
        }
        else
        {
          queueLock.notify();
        }
      }
    }
  }

  /**
   * Returns the next cache event from the queue or null if there are no events
   * in the queue.
   *
   */
  private AbstractCacheEvent take()
  {
    synchronized ( queueLock )
    {
      // wait until there is something to read
      if ( head == tail )
      {
        return null;
      }

      Node node = head.next;

      AbstractCacheEvent value = node.event;

      if ( log.isDebugEnabled() )
      {
        log.debug( "head.event = " + head.event );
        log.debug( "node.event = " + node.event );
      }

      // Node becomes the new head (head is always empty)

      node.event = null;
      head = node;

      return value;
    }
  }

  /*
   *  (non-Javadoc)
   * @see org.apache.jcs.engine.behavior.ICacheEventQueue#getStatistics()
   */
  public IStats getStatistics()
  {
  	IStats stats = new Stats();
  	stats.setTypeName( "Cache Event Queue" );
  	
  	ArrayList elems = new ArrayList();
  	
  	IStatElement se = null;
  	
  	se = new StatElement();
  	se.setName( "Working" );
  	se.setData("" + this.working);
  	elems.add(se);
  	
  	se.setName( "Alive" );
  	se = new StatElement();
  	se.setData("" + this.isAlive());
  	elems.add(se);

  	se = new StatElement();
  	se.setName( "Empty" );
  	se.setData("" + this.isEmpty());
  	elems.add(se);

    int size = 0;
    synchronized (queueLock)
    {
      // wait until there is something to read
      if (head == tail)
      {
        size = 0;
      }
      else
      {
          Node n = head;
          while ( n != null )
          {
            n = n.next;
            size++;
          }
      }

    	se = new StatElement();
    	se.setName( "Size" );
      	se.setData("" + size);
      	elems.add(se);
    }

  	// get an array and put them in the Stats object
  	IStatElement[] ses = (IStatElement[])elems.toArray( new StatElement[0] );
  	stats.setStatElements( ses );

  	return stats;
  }     
  
  
  ///////////////////////////// Inner classes /////////////////////////////

  private static class Node
  {
    Node next = null;
    AbstractCacheEvent event = null;
  }

  /**
   * @author asmuts @created January 15, 2002
   */
  private class QProcessor
      extends Thread
  {
    CacheEventQueue queue;
    /**
     * Constructor for the QProcessor object
     */
    QProcessor( CacheEventQueue aQueue )
    {

      super( "CacheEventQueue.QProcessor-" + aQueue.cacheName );

      setDaemon( true );
      queue = aQueue;
    }

    /**
     * Main processing method for the QProcessor object.
     *
     * Waits for a specified time (waitToDieMillis) for something to come in
     * and if no new events come in during that period the run method can exit
     * and the thread is dereferenced.
     */
    public void run()
    {
      AbstractCacheEvent r = null;

      while ( queue.isAlive() )
      {
        r = queue.take();

        if ( log.isDebugEnabled() )
        {
          log.debug( "Event from queue = " + r );
        }

        if ( r == null )
        {
          synchronized ( queueLock )
          {
            try
            {
              queueLock.wait( queue.getWaitToDieMillis() );
            }
            catch ( InterruptedException e )
            {
              log.warn(
                  "Interrupted while waiting for another event to come in before we die." );
              return;
            }
            r = queue.take();
            if ( log.isDebugEnabled() )
            {
              log.debug( "Event from queue after sleep = " + r );
            }
          }
          if ( r == null )
          {
            queue.stopProcessing();
          }
        }

        if ( queue.isWorking() && queue.isAlive() && r != null )
        {
          r.run();
        }
      }
      if ( log.isInfoEnabled() )
      {
        log.info( "QProcessor exiting for " + queue );
      }
    }
  }

  /**
   * Retries before declaring failure.
   *
   * @author asmuts @created January 15, 2002
   */
  private abstract class AbstractCacheEvent
      implements Runnable
  {
    int failures = 0;
    boolean done = false;

    /**
     * Main processing method for the AbstractCacheEvent object
     */
    public void run()
    {
      try
      {
        doRun();
      }
      catch ( IOException e )
      {
        if ( log.isWarnEnabled() )
        {
          log.warn( e );
        }
        if ( ++failures >= maxFailure )
        {
          if ( log.isWarnEnabled() )
          {
            log.warn(
                "Error while running event from Queue: "
                + this
                +". Dropping Event and marking Event Queue as non-functional." );
          }
          setWorking( false );
          setAlive( false );
          return;
        }
	    if ( log.isInfoEnabled() )
	    {
	      log.info( "Error while running event from Queue: " +
	                   this +". Retrying..." );
	    }
	    try
	    {
	      Thread.sleep( waitBeforeRetry );
	      run();
	    }
	    catch ( InterruptedException ie )
	    {
	      if ( log.isErrorEnabled() )
	      {
	        log.warn( "Interrupted while sleeping for retry on event " + this +
	                "." );
	      }
	      setWorking( false );
	      setAlive( false );
	    }
      }
    }

    /**
     * @exception IOException
     */
    protected abstract void doRun() throws IOException;
  }

  /**
   * @author asmuts @created January 15, 2002
   */
  private class PutEvent
      extends AbstractCacheEvent
  {

    private ICacheElement ice;

    /**
     * Constructor for the PutEvent object
     *
     * @param ice
     * @exception IOException
     */
    PutEvent( ICacheElement ice ) throws IOException
    {
      this.ice = ice;
      /*
       * this.key = key; this.obj = CacheUtils.dup(obj); this.attr = attr; this.groupName = groupName;
       */
    }

    /**
     * Description of the Method
     *
     * @exception IOException
     */
    protected void doRun() throws IOException
    {
      /*
       * CacheElement ce = new CacheElement(cacheName, key, obj); ce.setElementAttributes( attr ); ce.setGroupName(
       * groupName );
       */
      listener.handlePut( ice );
    }

    public String toString()
    {
      return new StringBuffer( "PutEvent for key: " )
          .append( ice.getKey() )
          .append( " value: " )
          .append( ice.getVal() )
          .toString();
    }

  }

  /**
   * Description of the Class
   *
   * @author asmuts @created January 15, 2002
   */
  private class RemoveEvent
      extends AbstractCacheEvent
  {
    private Serializable key;

    /**
     * Constructor for the RemoveEvent object
     *
     * @param key
     * @exception IOException
     */
    RemoveEvent( Serializable key ) throws IOException
    {
      this.key = key;
    }

    /**
     * Description of the Method
     *
     * @exception IOException
     */
    protected void doRun() throws IOException
    {
      listener.handleRemove( cacheName, key );
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return new StringBuffer( "RemoveEvent for " ).append( key ).toString();
    }

  }

  /**
   * Description of the Class
   *
   * @author asmuts @created January 15, 2002
   */
  private class RemoveAllEvent
      extends AbstractCacheEvent
  {

    /**
     * Description of the Method
     *
     * @exception IOException
     */
    protected void doRun() throws IOException
    {
      listener.handleRemoveAll( cacheName );
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return "RemoveAllEvent";
    }

  }

  /**
   * Description of the Class
   *
   * @author asmuts @created January 15, 2002
   */
  private class DisposeEvent
      extends AbstractCacheEvent
  {

    /**
     * Called when gets to the end of the queue
     *
     * @exception IOException
     */
    protected void doRun() throws IOException
    {
      listener.handleDispose( cacheName );
    }

    public String toString()
    {
      return "DisposeEvent";
    }
  }

  /**
   * @return
   */
  public boolean isWorking()
  {
    return working;
  }

  /**
   * @param b
   */
  public void setWorking( boolean b )
  {
    working = b;
  }

  public boolean isEmpty()
  {
    return tail == head;
  }

}

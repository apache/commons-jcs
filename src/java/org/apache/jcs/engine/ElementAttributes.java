package org.apache.jcs.engine;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;

/**
 * This it the element attribute descriptor class. Each element in the cache has
 * an ElementAttribute object associated with it. An ElementAttributes object
 * can be associated with an element in 3 ways:
 * <ol>
 * <li>When the item is put into the cache, you can associate an element
 * attributes object.</li>
 * <li>If not attributes object is include when the element is put into the
 * cache, then the default attributes for the region will be used.</li>
 * <li>The element attributes can be reset. This effectively results in a
 * retrieval followed by a put. Hence, this is the same as 1.</li>
 * </ol>
 * @version $Id: ILateralCacheTCPListener.java,v 1.2 2002/01/18 22:08:26
 */
public class ElementAttributes
    implements IElementAttributes, Serializable, Cloneable
{
    private static final long serialVersionUID = 7814990748035017441L;

    /**
     * Can this item be flushed to disk
     */
    public boolean IS_SPOOL = true;

    /**
     * Is this item laterally distributable
     */
    public boolean IS_LATERAL = true;

    /**
     * Can this item be sent to the remote cache
     */
    public boolean IS_REMOTE = true;

    /**
     * You can turn off expiration by setting this to true. This causes the
     * cache to bypass both max life and idle time expiration.
     */
    public boolean IS_ETERNAL = true;

    /**
     * The object version. This is currently not used.
     */
    public long version = 0;

    /**
     * Max life seconds
     */
    public long maxLifeSeconds = -1;

    /**
     * The maximum time an entry can be idle. Setting this to -1 causes the idle
     * time check to be ignored.
     */
    public long maxIdleTimeSeconds = -1;

    /**
     * The byte size of the field. Must be manually set.
     */
    public int size = 0;

    /**
     * The creation time. This is used to enforce the max life.
     */
    public long createTime = 0;

    /**
     * The last access time. This is used to enforce the max idel time.
     */
    public long lastAccessTime = 0;

    /**
     * The list of Event handlers to use. This is transient, since the event
     * handlers cannot usually be serialized. This means that you cannot attach
     * a post serialization event to an item.
     * <p>
     * TODO we need to check that when an item is passed to a non-local cache
     * that if the local cache had a copy with event handlers, that those
     * handlers are used.
     */
    public transient ArrayList eventHandlers;

    /**
     * Constructor for the IElementAttributes object
     */
    public ElementAttributes()
    {
        this.createTime = System.currentTimeMillis();
        this.lastAccessTime = this.createTime;
    }

    /**
     * Constructor for the IElementAttributes object
     * <p>
     * @param attr
     */
    protected ElementAttributes( ElementAttributes attr )
    {
        IS_ETERNAL = attr.IS_ETERNAL;

        // waterfal onto disk, for pure disk set memory to 0
        IS_SPOOL = attr.IS_SPOOL;

        // lateral
        IS_LATERAL = attr.IS_LATERAL;

        // central rmi store
        IS_REMOTE = attr.IS_REMOTE;

        maxLifeSeconds = attr.maxLifeSeconds;
        // timetolive
        maxIdleTimeSeconds = attr.maxIdleTimeSeconds;
        size = attr.size;
    }

    /**
     * Copies the attributes, including references to event handlers.
     * <p>
     * @return a copy of the Attributes
     */
    public IElementAttributes copy()
    {
        try
        {
            // need to make this more efficient. Just want to insure
            // a proper copy
            ElementAttributes attr = new ElementAttributes();
            attr.setIdleTime( this.getIdleTime() );
            attr.setIsEternal( this.getIsEternal() );
            attr.setIsLateral( this.getIsLateral() );
            attr.setIsRemote( this.getIsRemote() );
            attr.setIsSpool( this.getIsSpool() );
            attr.setMaxLifeSeconds( this.getMaxLifeSeconds() );
            attr.addElementEventHandlers( this.eventHandlers );
            return attr;
        }
        catch ( Exception e )
        {
            return new ElementAttributes();
        }
    }

    /**
     * Deep clone the attributes.
     * <p>
     * @return a clone of these attributes
     */
    public Object clone2()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream( 100 );
            ObjectOutputStream oos = new ObjectOutputStream( baos );
            oos.writeObject( this );
            byte buf[] = baos.toByteArray();
            oos.close();

            // deserialize byte array into ArrayList

            ByteArrayInputStream bais = new ByteArrayInputStream( buf );
            ObjectInputStream ois = new ObjectInputStream( bais );
            ElementAttributes attr = (ElementAttributes) ois.readObject();
            ois.close();

            attr.createTime = System.currentTimeMillis();
            return attr;
        }
        catch ( Exception e )
        {
            // swallow
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#setVersion(long)
     */
    public void setVersion( long version )
    {
        this.version = version;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#setMaxLifeSeconds(long)
     */
    public void setMaxLifeSeconds( long mls )
    {
        this.maxLifeSeconds = mls;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#getMaxLifeSeconds()
     */
    public long getMaxLifeSeconds()
    {
        return this.maxLifeSeconds;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#setIdleTime(long)
     */
    public void setIdleTime( long idle )
    {
        this.maxIdleTimeSeconds = idle;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#setSize(int)
     */
    public void setSize( int size )
    {
        this.size = size;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#getSize()
     */
    public int getSize()
    {
        return size;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#getCreateTime()
     */
    public long getCreateTime()
    {
        return createTime;
    }

    /**
     * Sets the createTime attribute of the IElementAttributes object
     */
    public void setCreateTime()
    {
        createTime = System.currentTimeMillis();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#getVersion()
     */
    public long getVersion()
    {
        return version;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#getIdleTime()
     */
    public long getIdleTime()
    {
        return this.maxIdleTimeSeconds;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#getTimeToLiveSeconds()
     */
    public long getTimeToLiveSeconds()
    {
        long now = System.currentTimeMillis();
        return ( ( this.getCreateTime() + ( this.getMaxLifeSeconds() * 1000 ) ) - now ) / 1000;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#getLastAccessTime()
     */
    public long getLastAccessTime()
    {
        return this.lastAccessTime;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#setLastAccessTimeNow()
     */
    public void setLastAccessTimeNow()
    {
        this.lastAccessTime = System.currentTimeMillis();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#getIsSpool()
     */
    public boolean getIsSpool()
    {
        return this.IS_SPOOL;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#setIsSpool(boolean)
     */
    public void setIsSpool( boolean val )
    {
        this.IS_SPOOL = val;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#getIsLateral()
     */
    public boolean getIsLateral()
    {
        return this.IS_LATERAL;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IElementAttributes#setIsLateral(boolean)
     */
    public void setIsLateral( boolean val )
    {
        this.IS_LATERAL = val;
    }

    /**
     * Can this item be sent to the remote cache
     * @return true if the item can be sent to a remote auxiliary
     */
    public boolean getIsRemote()
    {
        return this.IS_REMOTE;
    }

    /**
     * Sets the isRemote attribute of the ElementAttributes object
     * @param val
     *            The new isRemote value
     */
    public void setIsRemote( boolean val )
    {
        this.IS_REMOTE = val;
    }

    /**
     * You can turn off expiration by setting this to true. The max life value
     * will be ignored.
     * <p>
     * @return true if the item cannot expire.
     */
    public boolean getIsEternal()
    {
        return this.IS_ETERNAL;
    }

    /**
     * Sets the isEternal attribute of the ElementAttributes object. True means
     * that the item should never expire. If can still be removed if it is the
     * least recently used, and you are using the LRUMemory cache. it just will
     * not be filtered for expiration by the cache hub.
     * <p>
     * @param val
     *            The new isEternal value
     */
    public void setIsEternal( boolean val )
    {
        this.IS_ETERNAL = val;
    }

    /**
     * Adds a ElementEventHandler. Handler's can be registered for multiple
     * events. A registered handler will be called at every recognized event.
     * <p>
     * The alternative would be to register handlers for each event. Or maybe
     * The handler interface should have a method to return whether it cares
     * about certain events.
     * <p>
     * @param eventHandler
     *            The ElementEventHandler to be added to the list.
     */
    public void addElementEventHandler( IElementEventHandler eventHandler )
    {
        // lazy here, no concurrency problems expected
        if ( this.eventHandlers == null )
        {
            this.eventHandlers = new ArrayList();
        }
        this.eventHandlers.add( eventHandler );
    }

    /**
     * Sets the eventHandlers of the IElementAttributes object.
     * <p>
     * This add the references to the local list. Subsequent changes in the
     * caller's list will not be reflected.
     * <p>
     * @param eventHandlers
     *            List of IElementEventHandler objects
     */
    public void addElementEventHandlers( ArrayList eventHandlers )
    {
        if ( eventHandlers == null )
        {
            return;
        }

        for ( Iterator iter = eventHandlers.iterator(); iter.hasNext(); )
        {
            addElementEventHandler( (IElementEventHandler) iter.next() );
        }
    }

    /**
     * Gets the elementEventHandlers. Returns null if none exist. Makes checking
     * easy.
     * <p>
     * @return The elementEventHandlers List of IElementEventHandler objects
     */
    public ArrayList getElementEventHandlers()
    {
        return this.eventHandlers;
    }

    /**
     * For logging and debugging the element IElementAttributes.
     * <p>
     * @return String info about the values.
     */
    public String toString()
    {
        StringBuffer dump = new StringBuffer();

        dump.append( "[ IS_LATERAL = " ).append( IS_LATERAL );
        dump.append( ", IS_SPOOL = " ).append( IS_SPOOL );
        dump.append( ", IS_REMOTE = " ).append( IS_REMOTE );
        dump.append( ", IS_ETERNAL = " ).append( IS_ETERNAL );
        dump.append( ", MaxLifeSeconds = " ).append( this.getMaxLifeSeconds() );
        dump.append( ", IdleTime = " ).append( this.getIdleTime() );
        dump.append( ", CreateTime = " ).append( this.getCreateTime() );
        dump.append( ", LastAccessTime = " ).append( this.getLastAccessTime() );
        dump.append( ", getTimeToLiveSeconds() = " ).append( String.valueOf( getTimeToLiveSeconds() ) );
        dump.append( ", createTime = " ).append( String.valueOf( createTime ) ).append( " ]" );

        return dump.toString();
    }
}

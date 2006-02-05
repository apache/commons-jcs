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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.jcs.engine.behavior.IAttributes;

/**
 * Element attribute descriptor class.
 * 
 * @version $Id: ILateralCacheTCPListener.java,v 1.2 2002/01/18 22:08:26
 */
public class Attributes
    implements IAttributes, Serializable, Cloneable
{

    private static final long serialVersionUID = 2245148271982787250L;

    // too slow to be private
    // need direct access for performance.
    
    /** Can this be send over a lateral or remote service. */
    public boolean IS_DISTRIBUTE = false;

    /** Can the element be set laterally */
    public boolean IS_LATERAL = false;

    /** Can the element be spooled to disk. */
    public boolean IS_SPOOL = false;

    /** Description of the Field */
    public boolean IS_REMOTE = false;

    /** Is the attribute above the max life law. */
    public boolean IS_ETERNAL = true;

    /** Description of the Field */
    public long version = 0;

    /** Description of the Field */
    public long ttl = 0;

    /** Description of the Field */
    public long default_ttl = 0;

    /** Description of the Field */
    public long idle = 0;

    /** Description of the Field */
    public long lastAccess = 0;

    /** Description of the Field */
    public int size = 0;

    /** Description of the Field */
    public long createTime = 0;

    /** 
     * Set the create time.
     */
    public Attributes()
    {
        this.createTime = System.currentTimeMillis();
    }

    /**
     * Constructor for the Attributes object
     * 
     * @param attr
     */
    protected Attributes( Attributes attr )
    {
        IS_ETERNAL = attr.IS_ETERNAL;

        IS_SPOOL = attr.IS_SPOOL;
        // waterfal onto disk, for pure disk set memory to 0
        IS_DISTRIBUTE = attr.IS_DISTRIBUTE;
        // lateral
        IS_REMOTE = attr.IS_REMOTE;
        // central rmi store

        version = attr.version;
        ttl = attr.ttl;
        // timetolive
        default_ttl = attr.default_ttl;
        idle = attr.idle;

        size = attr.size;

    }

    /**
     * clone
     * @return Attributes
     */
    public Attributes copy()
    {
        try
        {
            Attributes attr = (Attributes) this.clone();
            attr.createTime = System.currentTimeMillis();
            return attr;
        }
        catch ( Exception e )
        {
            return new Attributes();
        }
    }

    /**
     * Deep clone
     * 
     * @return
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
            Attributes attr = (Attributes) ois.readObject();
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

    /**
     * Sets the version attribute of the Attributes object
     * 
     * @param version
     *            The new version value
     */
    public void setVersion( long version )
    {
        this.version = version;
    }

    /**
     * Sets the timeToLive attribute of the Attributes object
     * 
     * @param ttl
     *            The new timeToLive value
     */
    public void setTimeToLive( long ttl )
    {
        this.ttl = ttl;
    }

    /**
     * Sets the idleTime attribute of the Attributes object
     * 
     * @param idle
     *            The new idleTime value
     */
    public void setIdleTime( long idle )
    {
        this.idle = idle;
    }

    /**
     * Size in bytes.
     * 
     * @param size
     *            The new size value
     */
    public void setSize( int size )
    {
        this.size = size;
    }

    /**
     * Gets the size attribute of the Attributes object
     * 
     * @return The size value
     */
    public int getSize()
    {
        return size;
    }

    /**
     * Gets the createTime attribute of the Attributes object
     * 
     * @return The createTime value
     */
    public long getCreateTime()
    {
        return createTime;
    }

    /** Sets the createTime attribute of the Attributes object */
    public void setCreateTime()
    {
        createTime = System.currentTimeMillis();
    }

    //public CacheLoader getLoader( ) {
    //  return new CacheLoader
    //}

    /**
     * Gets the version attribute of the Attributes object
     * 
     * @return The version value
     */
    public long getVersion()
    {
        return version;
    }

    /**
     * Gets the idleTime attribute of the Attributes object
     * 
     * @return The idleTime value
     */
    public long getIdleTime()
    {
        return this.idle;
    }

    /**
     * Gets the timeToLive attribute of the Attributes object. This should be
     * called maxLifeSeconds since it is the number of seconds teh item will be
     * available after creation, not the time from now. Need another method.
     * This one can calculate the time left.
     * 
     * @return The timeToLive value
     */
    public long getTimeToLive()
    {
        return this.ttl;
    }

    /*
     * 
     */
    public String toString()
    {
        StringBuffer dump = new StringBuffer();

        dump.append( "[ IS_LATERAL = " ).append( IS_LATERAL ).append( ", IS_SPOOL = " ).append( IS_SPOOL )
            .append( ", IS_REMOTE = " ).append( IS_REMOTE ).append( ", IS_ETERNAL = " ).append( IS_ETERNAL )
            .append( ", ttl = " ).append( String.valueOf( ttl ) ).append( ", createTime = " )
            .append( String.valueOf( createTime ) ).append( " ]" );

        return dump.toString();
    }
}

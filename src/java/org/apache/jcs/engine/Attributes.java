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
public class Attributes implements IAttributes, Serializable, Cloneable
{

    // too slow to be private
    // need direct access
    // remove
    /** Description of the Field */
    public boolean IS_DISTRIBUTE = false;
    // lateral

    /** Description of the Field */
    public boolean IS_LATERAL = false;
    // lateral
    /** Description of the Field */
    public boolean IS_NOFLUSH = false;
    /** Description of the Field */
    public boolean IS_REPLY = false;
    /** Description of the Field */
    public boolean IS_SYNCHRONIZE = false;
    /** Description of the Field */
    public boolean IS_SPOOL = false;
    /** Description of the Field */
    public boolean IS_GROUP_TTL_DESTROY = false;
    /** Description of the Field */
    public boolean IS_ORIGINAL = false;
    /** Description of the Field */
    public boolean IS_REMOTE = false;
    // central rmi store
    /** Description of the Field */
    public boolean IS_ETERNAL = true;
    //false; // can turn off expiration

    /** Description of the Field */
    public long version = 0;
    /** Description of the Field */
    public long ttl = 0;
    // timetolive
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


    /** Constructor for the Attributes object */
    public Attributes()
    {
        this.createTime = System.currentTimeMillis();
    }


    /**
     * Constructor for the Attributes object
     *
     * @param attr
     */
    private Attributes( Attributes attr )
    {

        IS_NOFLUSH = attr.IS_NOFLUSH;
        IS_REPLY = attr.IS_REPLY;
        IS_SYNCHRONIZE = attr.IS_SYNCHRONIZE;
        IS_GROUP_TTL_DESTROY = attr.IS_GROUP_TTL_DESTROY;
        IS_ETERNAL = attr.IS_ETERNAL;
        // central rmi store
        IS_ORIGINAL = attr.IS_ORIGINAL;

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


    //public Object clone () {
    /** Description of the Method */
    public Attributes copy()
    {
        try
        {
            Attributes attr = ( Attributes ) this.clone();
            attr.createTime = System.currentTimeMillis();
            return attr;
        }
        catch ( Exception e )
        {
            return new Attributes();
        }
    }


    /** Description of the Method */
    public Object clone2()
    {

        try
        {
            ByteArrayOutputStream baos =
                new ByteArrayOutputStream( 100 );
            ObjectOutputStream oos = new
                ObjectOutputStream( baos );
            oos.writeObject( this );
            byte buf[] = baos.toByteArray();
            oos.close();

            // deserialize byte array into ArrayList

            ByteArrayInputStream bais =
                new ByteArrayInputStream( buf );
            ObjectInputStream ois = new
                ObjectInputStream( bais );
            Attributes attr =
                ( Attributes ) ois.readObject();
            ois.close();

            attr.createTime = System.currentTimeMillis();
            return attr;
        }
        catch ( Exception e )
        {
        }
        return null;
        /*
         * System.out.println( "cloning" );
         * Attributes attr = new Attributes( this );
         * return attr;
         */
    }

    /**
     * Sets the version attribute of the Attributes object
     *
     * @param version The new version value
     */
    public void setVersion( long version )
    {
        this.version = version;
    }


    /**
     * Sets the timeToLive attribute of the Attributes object
     *
     * @param ttl The new timeToLive value
     */
    public void setTimeToLive( long ttl )
    {
        this.ttl = ttl;
    }


//    /**
//     * Sets the defaultTimeToLive attribute of the Attributes object
//     *
//     * @param ttl The new defaultTimeToLive value
//     */
//    public void setDefaultTimeToLive( long ttl )
//    {
//        this.default_ttl = ttl;
//    }


    /**
     * Sets the idleTime attribute of the Attributes object
     *
     * @param idle The new idleTime value
     */
    public void setIdleTime( long idle )
    {
        this.idle = idle;
    }


    //public void setListener( int event, CacheEventListener listerner) {}

    /**
     * Size in bytes.
     *
     * @param size The new size value
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
     * Gets the timeToLive attribute of the Attributes object.
     * This should be called maxLifeSeconds since it is the number of
     * seconds teh item will be available after creation, not the time
     * from now.  Need another method.  This one can calculate the time left.
     *
     * @return The timeToLive value
     */
    public long getTimeToLive()
    {
        return this.ttl;
    }


// NOT NECESSARY will be using max life not time left pattern
//    /** Description of the Method */
//    public long timeToSeconds( int days, int hours, int minutes, int seconds )
//        throws InvalidArgumentException
//    {
//        return 5;
//    }


    /** Description of the Method */
    public String toString()
    {
        StringBuffer dump = new StringBuffer();

        dump.append( "[ IS_LATERAL = " ).append( IS_LATERAL )
            .append( ", IS_SPOOL = " ).append( IS_SPOOL )
            .append( ", IS_REMOTE = " ).append( IS_REMOTE )
            .append( ", IS_ETERNAL = " ).append( IS_ETERNAL )
            .append( ", ttl = " ).append( String.valueOf( ttl ) )
            .append( ", createTime = " ).append( String.valueOf( createTime ) )
            .append( " ]" );

        return dump.toString();
        //dump.append( " IS_NOFLUSH = " + IS_NOFLUSH + "\n");
        //dump.append( " IS_REPLY = " + IS_REPLY + "\n");
        //dump.append( " IS_SYNCHRONIZE = " + IS_SYNCHRONIZE + "\n");
        //dump.append( " IS_GROUP_TTL_DESTROY = " + IS_GROUP_TTL_DESTROY + "\n");
        //dump.append( " IS_ORIGINAL = " + IS_ORIGINAL + "\n");
    }
}

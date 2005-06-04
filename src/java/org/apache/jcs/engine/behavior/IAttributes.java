package org.apache.jcs.engine.behavior;

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

/**
 * Description of the Interface
 *  
 */
public interface IAttributes
{

    //    // remove
    //    //static int DISTRIBUTE = 1; // lateral
    //    static int LATERAL = 1;
    //    // lateral
    //    //static int NOFLUSH = 2;
    //    //static int REPLY = 3;
    //    //static int SYNCHRONIZE = 4;
    //    static int SPOOL = 5;
    //    //static int GROUP_TTL_DESTROY = 6;
    //    //static int ORIGINAL = 7;
    //    static int REMOTE = 8;
    //    // central rmi store
    //    static int ETERNAL = 8;

    /**
     * Sets the version attribute of the IAttributes object
     * 
     * @param version
     *            The new version value
     */
    public void setVersion( long version );

    /**
     * Sets the timeToLive attribute of the IAttributes object
     * 
     * @param ttl
     *            The new timeToLive value
     */
    public void setTimeToLive( long ttl );

    //    /**
    //     * Sets the defaultTimeToLive attribute of the IAttributes object
    //     *
    //     * @param ttl The new defaultTimeToLive value
    //     */
    //    public void setDefaultTimeToLive( long ttl );

    /**
     * Sets the idleTime attribute of the IAttributes object
     * 
     * @param idle
     *            The new idleTime value
     */
    public void setIdleTime( long idle );

    //public void setListener( int event, CacheEventListener listerner) {}

    /**
     * Size in bytes.
     * 
     * @param size
     *            The new size value
     */
    public void setSize( int size );

    /**
     * Gets the size attribute of the IAttributes object
     * 
     * @return The size value
     */
    public int getSize();

    /**
     * Gets the createTime attribute of the IAttributes object
     * 
     * @return The createTime value
     */
    public long getCreateTime();

    //public CacheLoader getLoader( );

    /**
     * Gets the version attribute of the IAttributes object
     * 
     * @return The version value
     */
    public long getVersion();

    /**
     * Gets the idleTime attribute of the IAttributes object
     * 
     * @return The idleTime value
     */
    public long getIdleTime();

    /**
     * Gets the timeToLive attribute of the IAttributes object
     * 
     * @return The timeToLive value
     */
    public long getTimeToLive();

    //    /** Description of the Method */
    //    public long timeToSeconds( int days, int hours, int minutes, int seconds
    // )
    //        throws InvalidArgumentException;

}

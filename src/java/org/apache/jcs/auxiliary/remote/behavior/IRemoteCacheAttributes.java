package org.apache.jcs.auxiliary.remote.behavior;


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


import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;

//import org.apache.jcs.auxiliary.*;

/**
 * Description of the Interface
 *
 */
public interface IRemoteCacheAttributes extends AuxiliaryCacheAttributes
{

    /*
     * A remote cache is either a local cache or a cluster cache.
     */
    /** Description of the Field */
    public static int LOCAL = 0;
    /** Description of the Field */
    public static int CLUSTER = 1;


    /**
     * Gets the remoteTypeName attribute of the IRemoteCacheAttributes object
     *
     * @return The remoteTypeName value
     */
    public String getRemoteTypeName();


    /**
     * Sets the remoteTypeName attribute of the IRemoteCacheAttributes object
     *
     * @param s The new remoteTypeName value
     */
    public void setRemoteTypeName( String s );


    /**
     * Gets the remoteType attribute of the IRemoteCacheAttributes object
     *
     * @return The remoteType value
     */
    public int getRemoteType();


    /**
     * Sets the remoteType attribute of the IRemoteCacheAttributes object
     *
     * @param p The new remoteType value
     */
    public void setRemoteType( int p );


    // specifies which server in the list we are listening to
    // if the number is greater than 0 we will try to move to 0 position
    // the primary is added as position 1 if it is present
    /**
     * Gets the failoverIndex attribute of the IRemoteCacheAttributes object
     *
     * @return The failoverIndex value
     */
    public int getFailoverIndex();


    /**
     * Sets the failoverIndex attribute of the IRemoteCacheAttributes object
     *
     * @param p The new failoverIndex value
     */
    public void setFailoverIndex( int p );


    /**
     * Gets the failovers attribute of the IRemoteCacheAttributes object
     *
     * @return The failovers value
     */
    public String[] getFailovers();


    /**
     * Sets the failovers attribute of the IRemoteCacheAttributes object
     *
     * @param f The new failovers value
     */
    public void setFailovers( String[] f );


    /**
     * Gets the remoteServiceName attribute of the IRemoteCacheAttributes object
     *
     * @return The remoteServiceName value
     */
    public String getRemoteServiceName();


    /**
     * Sets the remoteServiceName attribute of the IRemoteCacheAttributes object
     *
     * @param s The new remoteServiceName value
     */
    public void setRemoteServiceName( String s );


    /**
     * Gets the remoteHost attribute of the IRemoteCacheAttributes object
     *
     * @return The remoteHost value
     */
    public String getRemoteHost();


    /**
     * Sets the remoteHost attribute of the IRemoteCacheAttributes object
     *
     * @param s The new remoteHost value
     */
    public void setRemoteHost( String s );


    /**
     * Gets the remotePort attribute of the IRemoteCacheAttributes object
     *
     * @return The remotePort value
     */
    public int getRemotePort();


    /**
     * Sets the remotePort attribute of the IRemoteCacheAttributes object
     *
     * @param p The new remotePort value
     */
    public void setRemotePort( int p );


    /**
     * Gets the localPort attribute of the IRemoteCacheAttributes object
     *
     * @return The localPort value
     */
    public int getLocalPort();


    /**
     * Sets the localPort attribute of the IRemoteCacheAttributes object
     *
     * @param p The new localPort value
     */
    public void setLocalPort( int p );


    /**
     * Gets the clusterServers attribute of the IRemoteCacheAttributes object
     *
     * @return The clusterServers value
     */
    public String getClusterServers();


    /**
     * Sets the clusterServers attribute of the IRemoteCacheAttributes object
     *
     * @param s The new clusterServers value
     */
    public void setClusterServers( String s );


    /**
     * Gets the failoverServers attribute of the IRemoteCacheAttributes object
     *
     * @return The failoverServers value
     */
    public String getFailoverServers();


    /**
     * Sets the failoverServers attribute of the IRemoteCacheAttributes object
     *
     * @param s The new failoverServers value
     */
    public void setFailoverServers( String s );


    /**
     * Gets the removeUponRemotePut attribute of the IRemoteCacheAttributes
     * object
     *
     * @return The removeUponRemotePut value
     */
    public boolean getRemoveUponRemotePut();


    /**
     * Sets the removeUponRemotePut attribute of the IRemoteCacheAttributes
     * object
     *
     * @param r The new removeUponRemotePut value
     */
    public void setRemoveUponRemotePut( boolean r );


    /**
     * Gets the getOnly attribute of the IRemoteCacheAttributes object
     *
     * @return The getOnly value
     */
    public boolean getGetOnly();


    /**
     * Sets the getOnly attribute of the IRemoteCacheAttributes object
     *
     * @param r The new getOnly value
     */
    public void setGetOnly( boolean r );

    /**
     * Should cluster updates be propogated to the locals
     *
     * @return The localClusterConsistency value
     */
    public boolean getLocalClusterConsistency();

    /**
     * Should cluster updates be propogated to the locals
     *
     * @param r The new localClusterConsistency value
     */
    public void setLocalClusterConsistency( boolean r );
    
    /**
     * The thread pool the remote cache should use.  
     * At first this will only be for gets.
     * <p>
     * The default name is "remote_cache_client"
     * 
     * @return
     */
    public abstract String getThreadPoolName();
    
    /**
     * Set the anme of the pool to use.  Pools should be defined in the 
     * cache.ccf.
     * 
     * @param name
     */
    public abstract void setThreadPoolName( String name );

    /**
     * -1 and 0 mean no timeout, this is the default
     * if the timeout is -1 or 0, no threadpool will be used.
     * 
     * @return
     */
    public abstract int getGetTimeoutMillis();

    /**
     * -1 means no timeout, this is the default
     * if the timeout is -1 or 0, no threadpool will be used.
     * If the timeout is greater than 0 a threadpool will be used
     * for get requests. 
     * 
     * @param millis
     */
    public abstract void setGetTimeoutMillis( int millis );
}

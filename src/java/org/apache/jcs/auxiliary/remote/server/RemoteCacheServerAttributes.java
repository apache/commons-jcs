package org.apache.jcs.auxiliary.remote.server;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;

import org.apache.jcs.auxiliary.remote.server.behavior.IRemoteCacheServerAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheServerAttributes implements IRemoteCacheServerAttributes
{

    private String cacheName;
    private String name;

    private String remoteServiceName = IRemoteCacheConstants.REMOTE_CACHE_SERVICE_VAL;
    private String remoteHost;
    private int remotePort;

    /*
     * failover servers will be used by local caches one at a time.
     * Listeners will be registered with all cluster servers.
     * If we add a get from cluster attribute we will have the ability
     * to chain clusters and have them get from each other.
     */
    private String clusterServers = "";
    private boolean getFromCluster = true;

    private int servicePort = 0;

    private int remoteType = LOCAL;

    private boolean removeUponRemotePut = true;
    private boolean getOnly = false;

    private boolean localClusterConsistency = false;

    private boolean allowClusterGet = false;
    private String configFileName = "";

    /** Constructor for the RemoteCacheAttributes object */
    public RemoteCacheServerAttributes() { }


    /**
     * Gets the remoteTypeName attribute of the RemoteCacheAttributes object
     *
     * @return The remoteTypeName value
     */
    public String getRemoteTypeName()
    {
        if ( remoteType == LOCAL )
        {
            return "LOCAL";
        }
        else if ( remoteType == CLUSTER )
        {
            return "CLUSTER";
        }
        return "LOCAL";
    }


    /**
     * Sets the remoteTypeName attribute of the RemoteCacheAttributes object
     *
     * @param s The new remoteTypeName value
     */
    public void setRemoteTypeName( String s )
    {
        if ( s.equals( "LOCAL" ) )
        {
            remoteType = LOCAL;
        }
        else if ( s.equals( "CLUSTER" ) )
        {
            remoteType = CLUSTER;
        }
    }


    /**
     * Gets the remoteType attribute of the RemoteCacheAttributes object
     *
     * @return The remoteType value
     */
    public int getRemoteType()
    {
        return remoteType;
    }


    /**
     * Sets the remoteType attribute of the RemoteCacheAttributes object
     *
     * @param p The new remoteType value
     */
    public void setRemoteType( int p )
    {
        this.remoteType = p;
    }


    /**
     * Sets the cacheName attribute of the RemoteCacheAttributes object
     *
     * @param s The new cacheName value
     */
    public void setCacheName( String s )
    {
        this.cacheName = s;
    }


    /**
     * Gets the cacheName attribute of the RemoteCacheAttributes object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return this.cacheName;
    }


    /**
     * Gets the name attribute of the RemoteCacheAttributes object
     *
     * @return The name value
     */
    public String getName()
    {
        return this.name;
    }


    /**
     * Sets the name attribute of the RemoteCacheAttributes object
     *
     * @param name The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }


    /** Description of the Method */
    public AuxiliaryCacheAttributes copy()
    {
        try
        {
            return ( AuxiliaryCacheAttributes ) this.clone();
        }
        catch ( Exception e )
        {
        }
        return ( AuxiliaryCacheAttributes ) this;
    }


    /**
     * Gets the remoteServiceName attribute of the RemoteCacheAttributes object
     *
     * @return The remoteServiceName value
     */
    public String getRemoteServiceName()
    {
        return this.remoteServiceName;
    }


    /**
     * Sets the remoteServiceName attribute of the RemoteCacheAttributes object
     *
     * @param s The new remoteServiceName value
     */
    public void setRemoteServiceName( String s )
    {
        this.remoteServiceName = s;
    }


    /**
     * Gets the remoteHost attribute of the RemoteCacheAttributes object
     *
     * @return The remoteHost value
     */
    public String getRemoteHost()
    {
        return this.remoteHost;
    }


    /**
     * Sets the remoteHost attribute of the RemoteCacheAttributes object
     *
     * @param s The new remoteHost value
     */
    public void setRemoteHost( String s )
    {
        this.remoteHost = s;
    }


    /**
     * Gets the remotePort attribute of the RemoteCacheAttributes object
     *
     * @return The remotePort value
     */
    public int getRemotePort()
    {
        return this.remotePort;
    }


    /**
     * Sets the remotePort attribute of the RemoteCacheAttributes object
     *
     * @param p The new remotePort value
     */
    public void setRemotePort( int p )
    {
        this.remotePort = p;
    }


    /**
     * Gets the clusterServers attribute of the RemoteCacheAttributes object
     *
     * @return The clusterServers value
     */
    public String getClusterServers()
    {
        return this.clusterServers;
    }


    /**
     * Sets the clusterServers attribute of the RemoteCacheAttributes object
     *
     * @param s The new clusterServers value
     */
    public void setClusterServers( String s )
    {
        this.clusterServers = s;
    }


    /**
     * Gets the localPort attribute of the RemoteCacheAttributes object
     *
     * @return The localPort value
     */
    public int getServicePort()
    {
        return this.servicePort;
    }


    /**
     * Sets the localPort attribute of the RemoteCacheAttributes object
     *
     * @param p The new localPort value
     */
    public void setServicePort( int p )
    {
        this.servicePort = p;
    }


    /**
     * Gets the removeUponRemotePut attribute of the RemoteCacheAttributes
     * object
     *
     * @return The removeUponRemotePut value
     */
    public boolean getRemoveUponRemotePut()
    {
        return this.removeUponRemotePut;
    }


    /**
     * Sets the removeUponRemotePut attribute of the RemoteCacheAttributes
     * object
     *
     * @param r The new removeUponRemotePut value
     */
    public void setRemoveUponRemotePut( boolean r )
    {
        this.removeUponRemotePut = r;
    }


    /**
     * Gets the getOnly attribute of the RemoteCacheAttributes object
     *
     * @return The getOnly value
     */
    public boolean getGetOnly()
    {
        return this.getOnly;
    }


    /**
     * Sets the getOnly attribute of the RemoteCacheAttributes object
     *
     * @param r The new getOnly value
     */
    public void setGetOnly( boolean r )
    {
        this.getOnly = r;
    }

    /**
     * Should cluster updates be propogated to the locals
     *
     * @return The localClusterConsistency value
     */
    public boolean getLocalClusterConsistency()
    {
        return localClusterConsistency;
    }

    /**
     * Should cluster updates be propogated to the locals
     *
     * @param r The new localClusterConsistency value
     */
    public void setLocalClusterConsistency( boolean r )
    {
        this.localClusterConsistency = r;
    }


    /**
     * Should cluster updates be propogated to the locals
     *
     * @return The localClusterConsistency value
     */
    public boolean getAllowClusterGet()
    {
        return allowClusterGet;
    }

    /**
     * Should cluster updates be propogated to the locals
     *
     * @param r The new localClusterConsistency value
     */
    public void setAllowClusterGet( boolean r )
    {
        allowClusterGet = r;
    }

    /**
     * Gets the ConfigFileName attribute of the IRemoteCacheAttributes object
     *
     * @return The clusterServers value
     */
    public String getConfigFileName()
    {
        return configFileName;
    }


    /**
     * Sets the ConfigFileName attribute of the IRemoteCacheAttributes object
     *
     * @param s The new clusterServers value
     */
    public void setConfigFileName( String s )
    {
        configFileName = s;
    }


    /** Description of the Method */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\nremotePort = " + this.remoteHost );
        buf.append( "\nremotePort = " + this.remotePort );
        buf.append( "\ncacheName = " + this.cacheName );
        buf.append( "\nremoveUponRemotePut = " + this.removeUponRemotePut );
        buf.append( "\ngetOnly = " + getOnly );
        return buf.toString();
    }

}

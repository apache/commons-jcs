package org.apache.jcs.auxiliary.remote.server.behavior;

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

//import org.apache.jcs.auxiliary.*;

/**
 * Description of the Interface
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IRemoteCacheServerAttributes extends AuxiliaryCacheAttributes
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
    public int getServicePort();


    /**
     * Sets the localPort attribute of the IRemoteCacheAttributes object
     *
     * @param p The new localPort value
     */
    public void setServicePort( int p );


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
     * Should cluster updates be propogated to the locals
     *
     * @return The localClusterConsistency value
     */
    public boolean getAllowClusterGet();

    /**
     * Should cluster updates be propogated to the locals
     *
     * @param r The new localClusterConsistency value
     */
    public void setAllowClusterGet( boolean r );

    /**
     * Gets the ConfigFileName attribute of the IRemoteCacheAttributes object
     *
     * @return The clusterServers value
     */
    public String getConfigFileName();


    /**
     * Sets the ConfigFileName attribute of the IRemoteCacheAttributes object
     *
     * @param s The new clusterServers value
     */
    public void setConfigFileName( String s );

}

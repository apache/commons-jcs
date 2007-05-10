package org.apache.jcs.auxiliary.lateral.socket.tcp;

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

import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;

/**
 * This interface defines functions that are particular to the TCP Lateral Cache
 * plugin. It extends the generic LateralCacheAttributes interface which in turn
 * extends the AuxiliaryCache interface.
 *
 * @author Aaron Smuts
 *
 */
public class TCPLateralCacheAttributes
    extends LateralCacheAttributes
    implements ITCPLateralCacheAttributes
{

    private static final long serialVersionUID = 1077889204513905220L;

    private static final String DEFAULT_UDP_DISCOVERY_ADDRESS = "228.5.6.7";

    private static final int DEFAULT_UDP_DISCOVERY_PORT = 6789;

    private static final boolean DEFAULT_UDP_DISCOVERY_ENABLED = true;

    private static final boolean DEFAULT_ALLOW_GET = true;

    private static final boolean DEFAULT_ALLOW_PUT = true;

    private static final boolean DEFAULT_ISSUE_REMOVE_FOR_PUT = false;

    private static final boolean DEFAULT_FILTER_REMOVE_BY_HASH_CODE = true;

    // TCP --------------------------------------------
    private String tcpServers = "";

    // used to identify the service that this manager will be
    // operating on
    private String tcpServer = "";

    private int tcpListenerPort = 0;

    // udp discovery for tcp server
    private String udpDiscoveryAddr = DEFAULT_UDP_DISCOVERY_ADDRESS;

    private int udpDiscoveryPort = DEFAULT_UDP_DISCOVERY_PORT;

    private boolean udpDiscoveryEnabled = DEFAULT_UDP_DISCOVERY_ENABLED;

    private boolean allowPut = DEFAULT_ALLOW_GET;

    private boolean allowGet = DEFAULT_ALLOW_PUT;

    private boolean issueRemoveOnPut = DEFAULT_ISSUE_REMOVE_FOR_PUT;

    private boolean filterRemoveByHashCode = DEFAULT_FILTER_REMOVE_BY_HASH_CODE;

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes#setTcpServer(java.lang.String)
     */
    public void setTcpServer( String val )
    {
        this.tcpServer = val;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes#getTcpServer()
     */
    public String getTcpServer()
    {
        return this.tcpServer;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes#setTcpServers(java.lang.String)
     */
    public void setTcpServers( String val )
    {
        this.tcpServers = val;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes#getTcpServers()
     */
    public String getTcpServers()
    {
        return this.tcpServers;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes#setTcpListenerPort(int)
     */
    public void setTcpListenerPort( int val )
    {
        this.tcpListenerPort = val;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes#getTcpListenerPort()
     */
    public int getTcpListenerPort()
    {
        return this.tcpListenerPort;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes#setUdpDiscoveryEnabled(boolean)
     */
    public void setUdpDiscoveryEnabled( boolean udpDiscoveryEnabled )
    {
        this.udpDiscoveryEnabled = udpDiscoveryEnabled;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes#isUdpDiscoveryEnabled()
     */
    public boolean isUdpDiscoveryEnabled()
    {
        return this.udpDiscoveryEnabled;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes#getUdpDiscoveryPort()
     */
    public int getUdpDiscoveryPort()
    {
        return this.udpDiscoveryPort;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes#setUdpDiscoveryPort(int)
     */
    public void setUdpDiscoveryPort( int udpDiscoveryPort )
    {
        this.udpDiscoveryPort = udpDiscoveryPort;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes#getUdpDiscoveryAddr()
     */
    public String getUdpDiscoveryAddr()
    {
        return this.udpDiscoveryAddr;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes#setUdpDiscoveryAddr(java.lang.String)
     */
    public void setUdpDiscoveryAddr( String udpDiscoveryAddr )
    {
        this.udpDiscoveryAddr = udpDiscoveryAddr;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes#setAllowGet(boolean)
     */
    public void setAllowGet( boolean allowGet )
    {
        this.allowGet = allowGet;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes#isAllowGet()
     */
    public boolean isAllowGet()
    {
        return this.allowGet;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes#setAllowPut(boolean)
     */
    public void setAllowPut( boolean allowPut )
    {
        this.allowPut = allowPut;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes#isAllowPut()
     */
    public boolean isAllowPut()
    {
        return this.allowPut;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes#setIssueRemoveOnPut(boolean)
     */
    public void setIssueRemoveOnPut( boolean issueRemoveOnPut )
    {
        this.issueRemoveOnPut = issueRemoveOnPut;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes#isIssueRemoveOnPut()
     */
    public boolean isIssueRemoveOnPut()
    {
        return this.issueRemoveOnPut;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#copy()
     */
    public AuxiliaryCacheAttributes copy()
    {
        try
        {
            return (AuxiliaryCacheAttributes) this.clone();
        }
        catch ( Exception e )
        {
            //noop
        }
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes#isFilterRemoveByHashCode()
     */
    public boolean isFilterRemoveByHashCode()
    {
        return this.filterRemoveByHashCode;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes#setFilterRemoveByHashCode(boolean)
     */
    public void setFilterRemoveByHashCode( boolean filter )
    {
        this.filterRemoveByHashCode = filter;
    }

    /**
     * Used to key the instance TODO create another method for this and use
     * toString for debugging only.
     *
     * @return String
     */
    public String toString()
    {
        return this.getTcpServer() + ":" + this.getTcpListenerPort();
    }

}

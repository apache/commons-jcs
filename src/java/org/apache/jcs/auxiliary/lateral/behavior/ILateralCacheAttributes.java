package org.apache.jcs.auxiliary.lateral.behavior;


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


import java.io.Serializable;

import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;

/**
 * Description of the Interface
 *
 */
public interface ILateralCacheAttributes extends Serializable, AuxiliaryCacheAttributes
{

    final static int HTTP = 1;
    final static int UDP = 2;
    final static int TCP = 3;
    final static int XMLRPC = 4;
    final static int JAVAGROUPS = 5;


    /**
     * Sets the httpServer attribute of the ILateralCacheAttributes object
     *
     * @param val The new httpServer value
     */
    public void setHttpServer( String val );


    /**
     * Gets the httpServer attribute of the ILateralCacheAttributes object
     *
     * @return The httpServer value
     */
    public String getHttpServer();


    /**
     * Sets the tcpServer attribute of the ILateralCacheAttributes object
     *
     * @param val The new tcpServer value
     */
    public void setTcpServer( String val );


    /**
     * Gets the tcpServer attribute of the ILateralCacheAttributes object
     *
     * @return The tcpServer value
     */
    public String getTcpServer();


    /**
     * Sets the tcpServers attribute of the ILateralCacheAttributes object
     *
     * @param val The new tcpServers value
     */
    public void setTcpServers( String val );


    /**
     * Gets the tcpServers attribute of the ILateralCacheAttributes object
     *
     * @return The tcpServers value
     */
    public String getTcpServers();


    /**
     * Sets the tcpListenerPort attribute of the ILateralCacheAttributes object
     *
     * @param val The new tcpListenerPort value
     */
    public void setTcpListenerPort( int val );


    /**
     * Gets the tcpListenerPort attribute of the ILateralCacheAttributes object
     *
     * @return The tcpListenerPort value
     */
    public int getTcpListenerPort();

    /**
     * Sets the httpListenerPort attribute of the ILateralCacheAttributes object
     *
     * @param val The new tcpListenerPort value
     */
    public void setHttpListenerPort( int val );


    /**
     * Gets the httpListenerPort attribute of the ILateralCacheAttributes object
     *
     * @return The httpListenerPort value
     */
    public int getHttpListenerPort();

    /**
     * Sets the httpServers attribute of the LateralCacheAttributes object
     *
     * @param val The new httpServers value
     */
    public void setHttpServers( String val );


    /**
     * Gets the httpSrvers attribute of the LateralCacheAttributes object
     *
     * @return The httpServers value
     */
    public String getHttpServers();



    // configure udp multicast parameters
    /**
     * Sets the udpMulticastAddr attribute of the ILateralCacheAttributes object
     *
     * @param val The new udpMulticastAddr value
     */
    public void setUdpMulticastAddr( String val );


    /**
     * Gets the udpMulticastAddr attribute of the ILateralCacheAttributes object
     *
     * @return The udpMulticastAddr value
     */
    public String getUdpMulticastAddr();


    /**
     * Sets the udpMulticastPort attribute of the ILateralCacheAttributes object
     *
     * @param val The new udpMulticastPort value
     */
    public void setUdpMulticastPort( int val );


    /**
     * Gets the udpMulticastPort attribute of the ILateralCacheAttributes object
     *
     * @return The udpMulticastPort value
     */
    public int getUdpMulticastPort();


    /**
     * Sets the transmissionType attribute of the ILateralCacheAttributes object
     *
     * @param val The new transmissionType value
     */
    public void setTransmissionType( int val );


    /**
     * Gets the transmissionType attribute of the ILateralCacheAttributes object
     *
     * @return The transmissionType value
     */
    public int getTransmissionType();


    /**
     * Sets the transmissionTypeName attribute of the ILateralCacheAttributes
     * object
     *
     * @param val The new transmissionTypeName value
     */
    public void setTransmissionTypeName( String val );


    /**
     * Gets the transmissionTypeName attribute of the ILateralCacheAttributes
     * object
     *
     * @return The transmissionTypeName value
     */
    public String getTransmissionTypeName();


    /**
     * Sets the putOnlyMode attribute of the ILateralCacheAttributes. When this
     * is true the lateral cache will only issue put and remove order and will
     * not try to retrieve elements from other lateral caches.
     *
     * @param val The new transmissionTypeName value
     */
    public void setPutOnlyMode( boolean val );


    /**
     * @return The outgoingOnlyMode value. Stops gets from going remote.
     */
    public boolean getPutOnlyMode();

    /**
     *
     * @return String The Javagroups channel propeties.
     */
    public String getJGChannelProperties();

    /**
     *
     *   Sets the Javagroups channel propeties.
     *
     * @param channelProperties String
     */
    public void setJGChannelProperties( String channelProperties );

}

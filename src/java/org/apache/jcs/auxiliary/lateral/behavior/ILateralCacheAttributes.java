package org.apache.jcs.auxiliary.lateral.behavior;

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

import java.io.Serializable;

import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;

/**
 * Description of the Interface
 *
 * @author asmuts
 * @created January 15, 2002
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

}

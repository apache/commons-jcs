package org.apache.jcs.auxiliary.lateral;


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
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;

/**
 * Description of the Class
 *
 */
public class LateralCacheAttributes implements Serializable, ILateralCacheAttributes
{
    String transmissionTypeName = "UDP";
    int transmissionType = UDP;

    String httpServers;
    // used to identify the service that this manager will be
    // operating on
    String httpServer = "";
    String httpReceiveServlet = "";
    String httpDeleteServlet = "";

    String udpMulticastAddr = "228.5.6.7";
    int udpMulticastPort = 6789;

    //ArrayList tcpServers;
    String tcpServers;
    // used to identify the service that this manager will be
    // operating on
    String tcpServer = "";
    int tcpListenerPort = 1111;
    int httpListenerPort = 8080;

    // javagroups
    private String jgChannelProperties = null;

    // general
    private String cacheName;
    private String name;

    // disables gets from laterals
    boolean putOnlyMode = true;

    /**
     * Sets the httpServer attribute of the LateralCacheAttributes object
     *
     * @param val The new httpServer value
     */
    public void setHttpServer( String val )
    {
        httpServer = val;
    }


    /**
     * Gets the httpServer attribute of the LateralCacheAttributes object
     *
     * @return The httpServer value
     */
    public String getHttpServer()
    {
        return httpServer;
    }


    /*
     *
     * public void setTcpServers( ArrayList val ) {
     * tcpServers = val;
     * }
     * public ArrayList getTcpServers( ) {
     * return tcpServers;
     * }
     */
    /**
     * Sets the tcpServers attribute of the LateralCacheAttributes object
     *
     * @param val The new tcpServers value
     */
    public void setTcpServers( String val )
    {
        tcpServers = val;
    }


    /**
     * Gets the tcpServers attribute of the LateralCacheAttributes object
     *
     * @return The tcpServers value
     */
    public String getTcpServers()
    {
        return tcpServers;
    }

    /**
     * Sets the httpServers attribute of the LateralCacheAttributes object
     *
     * @param val The new httpServers value
     */
    public void setHttpServers( String val )
    {
        httpServers = val;
    }


    /**
     * Gets the httpSrvers attribute of the LateralCacheAttributes object
     *
     * @return The httpServers value
     */
    public String getHttpServers()
    {
        return httpServers;
    }

    /**
     * Sets the tcpServer attribute of the LateralCacheAttributes object
     *
     * @param val The new tcpServer value
     */
    public void setTcpServer( String val )
    {
        tcpServer = val;
    }


    /**
     * Gets the tcpServer attribute of the LateralCacheAttributes object
     *
     * @return The tcpServer value
     */
    public String getTcpServer()
    {
        return tcpServer;
    }


    /**
     * Sets the tcpListenerPort attribute of the LateralCacheAttributes object
     *
     * @param val The new tcpListenerPort value
     */
    public void setTcpListenerPort( int val )
    {
        this.tcpListenerPort = val;
    }


    /**
     * Gets the tcpListenerPort attribute of the LateralCacheAttributes object
     *
     * @return The tcpListenerPort value
     */
    public int getTcpListenerPort()
    {
        return this.tcpListenerPort;
    }


    /**
     * Sets the httpListenerPort attribute of the ILateralCacheAttributes object
     *
     * @param val The new tcpListenerPort value
     */
    public void setHttpListenerPort( int val )
    {
        this.httpListenerPort = val;
    }


    /**
     * Gets the httpListenerPort attribute of the ILateralCacheAttributes object
     *
     * @return The httpListenerPort value
     */
    public int getHttpListenerPort()
    {
        return this.httpListenerPort;
    }


    /**
     * Sets the udpMulticastAddr attribute of the LateralCacheAttributes object
     *
     * @param val The new udpMulticastAddr value
     */
    public void setUdpMulticastAddr( String val )
    {
        udpMulticastAddr = val;
    }


    /**
     * Gets the udpMulticastAddr attribute of the LateralCacheAttributes object
     *
     * @return The udpMulticastAddr value
     */
    public String getUdpMulticastAddr()
    {
        return udpMulticastAddr;
    }


    /**
     * Sets the udpMulticastPort attribute of the LateralCacheAttributes object
     *
     * @param val The new udpMulticastPort value
     */
    public void setUdpMulticastPort( int val )
    {
        udpMulticastPort = val;
    }


    /**
     * Gets the udpMulticastPort attribute of the LateralCacheAttributes object
     *
     * @return The udpMulticastPort value
     */
    public int getUdpMulticastPort()
    {
        return udpMulticastPort;
    }


    /**
     * Sets the transmissionType attribute of the LateralCacheAttributes object
     *
     * @param val The new transmissionType value
     */
    public void setTransmissionType( int val )
    {
        this.transmissionType = val;
        if ( val == UDP )
        {
            transmissionTypeName = "UDP";
        }
        else
            if ( val == HTTP )
        {
            transmissionTypeName = "HTTP";
        }
        else
            if ( val == TCP )
        {
            transmissionTypeName = "TCP";
        }
        else
            if ( val == XMLRPC )
        {
            transmissionTypeName = "XMLRPC";
        }
        else
            if ( val == JAVAGROUPS )
        {
            transmissionTypeName = "JAVAGROUPS";
        }
    }


    /**
     * Gets the transmissionType attribute of the LateralCacheAttributes object
     *
     * @return The transmissionType value
     */
    public int getTransmissionType()
    {
        return this.transmissionType;
    }


    /**
     * Sets the transmissionTypeName attribute of the LateralCacheAttributes
     * object
     *
     * @param val The new transmissionTypeName value
     */
    public void setTransmissionTypeName( String val )
    {
        this.transmissionTypeName = val;
        if ( val.equals( "UDP" ) )
        {
            transmissionType = UDP;
        }
        else
            if ( val.equals( "HTTP" ) )
        {
            transmissionType = HTTP;
        }
        else
            if ( val.equals( "TCP" ) )
        {
            transmissionType = TCP;
        }
        else
            if ( val.equals( "XMLRPC" ) )
        {
            transmissionType = XMLRPC;
        }
        else
            if ( val.equals( "JAVAGROUPS" ) )
        {
            transmissionType = JAVAGROUPS;
        }

    }


    /**
     * Gets the transmissionTypeName attribute of the LateralCacheAttributes
     * object
     *
     * @return The transmissionTypeName value
     */
    public String getTransmissionTypeName()
    {
        return this.transmissionTypeName;
    }


    /**
     * Sets the cacheName attribute of the LateralCacheAttributes object
     *
     * @param s The new cacheName value
     */
    public void setCacheName( String s )
    {
        this.cacheName = s;
    }


    /**
     * Gets the cacheName attribute of the LateralCacheAttributes object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return this.cacheName;
    }


    /**
     * Gets the name attribute of the LateralCacheAttributes object
     *
     * @return The name value
     */
    public String getName()
    {
        return this.name;
    }


    /**
     * Sets the name attribute of the LateralCacheAttributes object
     *
     * @param name The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * Sets the outgoingOnlyMode attribute of the ILateralCacheAttributes. When
     * this is true the lateral cache will only issue put and remove order and
     * will not try to retrieve elements from other lateral caches.
     *
     * @param val The new transmissionTypeName value
     */
    public void setPutOnlyMode( boolean val )
    {
        this.putOnlyMode = val;
    }


    /**
     * @return The outgoingOnlyMode value. Stops gets from going remote.
     */
    public boolean getPutOnlyMode()
    {
        return putOnlyMode;
    }


    public String getJGChannelProperties()
    {
      return jgChannelProperties;
    }

    public void setJGChannelProperties( String channelProperties )
    {
      this.jgChannelProperties = channelProperties;
    }


    /**
     * Returns a clone of the attributes.
     *
     * @return Self
     */
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
     * Description of the Method
     *
     * @return
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "cacheName=" + cacheName + "\n" );
        buf.append( "putOnlyMode=" + putOnlyMode + "\n" );
        buf.append( "transmissionTypeName=" + transmissionTypeName + "\n" );
        buf.append( "transmissionType=" + transmissionType + "\n" );
        buf.append( "tcpServer=" + tcpServer + "\n" );
        buf.append( httpServer + udpMulticastAddr + String.valueOf( udpMulticastPort ) + tcpServer );
        return buf.toString();
    }

}

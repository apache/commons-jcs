package org.apache.jcs.utils.discovery;

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

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This contains info about a discovered service. These objects are stored in a set in the
 * UDPDiscoveryService.
 * <p>
 * @author Aaron Smuts
 */
public class DiscoveredService
    implements Serializable
{
    /** For serialization. Don't change. */
    private static final long serialVersionUID = -7810164772089509751L;

    /** region names */
    private ArrayList cacheNames;

    /** service address */
    private String serviceAddress;

    /** service port */
    private int servicePort;

    /** last time we heard from this service? */
    private long lastHearFromTime = 0;

    /**
     * @param cacheNames the cacheNames to set
     */
    public void setCacheNames( ArrayList cacheNames )
    {
        this.cacheNames = cacheNames;
    }

    /**
     * @return the cacheNames
     */
    public ArrayList getCacheNames()
    {
        return cacheNames;
    }

    /**
     * @param serviceAddress The serviceAddress to set.
     */
    public void setServiceAddress( String serviceAddress )
    {
        this.serviceAddress = serviceAddress;
    }

    /**
     * @return Returns the serviceAddress.
     */
    public String getServiceAddress()
    {
        return serviceAddress;
    }

    /**
     * @param servicePort The servicePort to set.
     */
    public void setServicePort( int servicePort )
    {
        this.servicePort = servicePort;
    }

    /**
     * @return Returns the servicePort.
     */
    public int getServicePort()
    {
        return servicePort;
    }

    /**
     * @param lastHearFromTime The lastHearFromTime to set.
     */
    public void setLastHearFromTime( long lastHearFromTime )
    {
        this.lastHearFromTime = lastHearFromTime;
    }

    /**
     * @return Returns the lastHearFromTime.
     */
    public long getLastHearFromTime()
    {
        return lastHearFromTime;
    }

    /** @return hashcode based on address/port */
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append( this.getServiceAddress() );
        builder.append( this.getServicePort() );
        //builder.append( this.getCacheNames().toString() );
        return builder.toHashCode();
    }

    /**
     * NOTE - this object is often put into sets, so equals needs to be overridden.
     * <p>
     * We can't use cache names as part of the equals unless we manually only use the address and
     * port in a contains check. So that we can use normal set functionality, I've kept the cache
     * names out.
     * <p>
     * @param otherArg other
     * @return equality based on the address/port
     */
    public boolean equals( Object otherArg )
    {
        if ( otherArg instanceof DiscoveredService )
        {
            DiscoveredService other = (DiscoveredService) otherArg;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append( this.getServiceAddress(), other.getServiceAddress() );
            builder.append( this.getServicePort(), other.getServicePort() );
            //builder.append( this.getCacheNames().toString(), other.getCacheNames().toString() );
            return builder.isEquals();
        }
        return false;
    }

    /**
     * @return string for debugging purposes.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\n DiscoveredService" );
        buf.append( "\n CacheNames = [" + getCacheNames() + "]" );
        buf.append( "\n ServiceAddress = [" + getServiceAddress() + "]" );
        buf.append( "\n ServicePort = [" + getServicePort() + "]" );
        buf.append( "\n LastHearFromTime = [" + getLastHearFromTime() + "]" );
        return buf.toString();
    }
}

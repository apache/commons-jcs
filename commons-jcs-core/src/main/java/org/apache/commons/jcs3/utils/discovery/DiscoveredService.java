package org.apache.commons.jcs3.utils.discovery;

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
import java.util.Objects;

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
    private ArrayList<String> cacheNames;

    /** service address */
    private String serviceAddress;

    /** service port */
    private int servicePort;

    /** last time we heard from this service? */
    private long lastHearFromTime;

    /**
     * Default constructor
     */
    public DiscoveredService()
    {
        // empty
    }

    /**
     * Constructor
     *
     * @param message incoming message
     * @since 3.1
     */
    public DiscoveredService(UDPDiscoveryMessage message)
    {
        setServiceAddress( message.getHost() );
        setCacheNames( message.getCacheNames() );
        setServicePort( message.getPort() );
        setLastHearFromTime( System.currentTimeMillis() );
    }

    /**
     * @param cacheNames the cacheNames to set
     */
    public void setCacheNames( final ArrayList<String> cacheNames )
    {
        this.cacheNames = cacheNames;
    }

    /**
     * @return the cacheNames
     */
    public ArrayList<String> getCacheNames()
    {
        return cacheNames;
    }

    /**
     * @param serviceAddress The serviceAddress to set.
     */
    public void setServiceAddress( final String serviceAddress )
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
    public void setServicePort( final int servicePort )
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
    public void setLastHearFromTime( final long lastHearFromTime )
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
	@Override
	public int hashCode()
	{
		return Objects.hash(serviceAddress, servicePort);
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
	@Override
	public boolean equals(final Object otherArg)
	{
		if (this == otherArg)
		{
			return true;
		}
		if (otherArg == null)
		{
			return false;
		}
		if (!(otherArg instanceof DiscoveredService))
		{
			return false;
		}
		final DiscoveredService other = (DiscoveredService) otherArg;
		if (!Objects.equals(serviceAddress, other.serviceAddress))
		{
			return false;
		}
        return servicePort == other.servicePort;
    }

    /**
     * @return string for debugging purposes.
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\n DiscoveredService" );
        buf.append( "\n CacheNames = [" + getCacheNames() + "]" );
        buf.append( "\n ServiceAddress = [" + getServiceAddress() + "]" );
        buf.append( "\n ServicePort = [" + getServicePort() + "]" );
        buf.append( "\n LastHearFromTime = [" + getLastHearFromTime() + "]" );
        return buf.toString();
    }
}

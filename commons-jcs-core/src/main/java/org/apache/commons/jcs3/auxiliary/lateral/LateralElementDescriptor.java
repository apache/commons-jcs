package org.apache.commons.jcs3.auxiliary.lateral;

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

import org.apache.commons.jcs3.engine.behavior.ICacheElement;

/**
 * This class wraps command to other laterals. It is essentially a
 * JCS-TCP-Lateral packet. The headers specify the action the receiver should
 * take.
 */
public class LateralElementDescriptor<K, V>
    implements Serializable
{
    /** Don't change */
    private static final long serialVersionUID = 5268222498076063575L;

    /** The Cache Element that we are distributing. */
    public ICacheElement<K, V> ce;

    /**
     * The id of the the source of the request. This is used to prevent infinite
     * loops.
     */
    public long requesterId;

    /** The operation has been requested by the client. */
    public LateralCommand command = LateralCommand.UPDATE;

    /**
     * The hashcode value for this element.
     */
    public int valHashCode = -1;

    /** Constructor for the LateralElementDescriptor object */
    @Deprecated // Not used
    public LateralElementDescriptor()
    {
    }

    /**
     * Constructor for the LateralElementDescriptor object
     * <p>
     * @param ce ICacheElement&lt;K, V&gt; payload
     */
    public LateralElementDescriptor( final ICacheElement<K, V> ce )
    {
        this.ce = ce;
    }

    /**
     * Constructor for the LateralElementDescriptor object
     * <p>
     * @param ce ICacheElement&lt;K, V&gt; payload
     * @param command operation requested by the client
     * @since 3.1
     */
    public LateralElementDescriptor( final ICacheElement<K, V> ce, LateralCommand command)
    {
        this(ce);
        this.command = command;
    }

    /**
     * Constructor for the LateralElementDescriptor object
     * <p>
     * @param ce ICacheElement&lt;K, V&gt; payload
     * @param command operation requested by the client
     * @param requesterId id of the the source of the request
     * @since 3.1
     */
    public LateralElementDescriptor( final ICacheElement<K, V> ce, LateralCommand command, long requesterId)
    {
        this(ce, command);
        this.requesterId = requesterId;
    }

    /**
     * Return payload
     *
     * @return the ce
     * @since 3.1
     */
    public ICacheElement<K, V> getPayload()
    {
        return ce;
    }

    /**
     * Return id of the the source of the request
     *
     * @return the requesterId
     * @since 3.1
     */
    public long getRequesterId()
    {
        return requesterId;
    }

    /**
     * Return operation requested by the client
     *
     * @return the command
     * @since 3.1
     */
    public LateralCommand getCommand()
    {
        return command;
    }

    /**
     * @return the valHashCode
     * @since 3.1
     */
    public int getValHashCode()
    {
        return valHashCode;
    }

    /**
     * @return String, all the important values that can be configured
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\n LateralElementDescriptor " );
        buf.append( "\n command = [" + this.command + "]" );
        buf.append( "\n valHashCode = [" + this.valHashCode + "]" );
        buf.append( "\n ICacheElement = [" + this.ce + "]" );
        return buf.toString();
    }
}

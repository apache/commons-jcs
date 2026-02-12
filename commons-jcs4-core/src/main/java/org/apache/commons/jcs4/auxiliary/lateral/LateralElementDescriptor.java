package org.apache.commons.jcs4.auxiliary.lateral;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.Serializable;

import org.apache.commons.jcs4.engine.behavior.ICacheElement;

/**
 * This class wraps command to other laterals. It is essentially a
 * JCS-TCP-Lateral packet. The headers specify the action the receiver should
 * take.
 */
public record LateralElementDescriptor<K, V>(
        /** The Cache Element that we are distributing. */
        ICacheElement<K, V> payload,

        /** The operation has been requested by the client. */
        LateralCommand command,

        /**
         * The id of the source of the request. This is used to prevent infinite
         * loops.
         */
        long requesterId,

        /**
         * The hash code value for this element.
         */
        int valHashCode
) implements Serializable
{
    /** Don't change */
    private static final long serialVersionUID = 5268222498076063575L;

    /**
     * Constructor for the LateralElementDescriptor object
     *
     * @param ce ICacheElement&lt;K, V&gt; payload
     * @param command operation requested by the client
     * @since 3.1
     */
    public LateralElementDescriptor( final ICacheElement<K, V> ce, final LateralCommand command)
    {
        this(ce, command, 0, -1);
    }

    /**
     * Constructor for the LateralElementDescriptor object
     *
     * @param ce ICacheElement&lt;K, V&gt; payload
     * @param command operation requested by the client
     * @param requesterId id of the source of the request
     * @since 3.1
     */
    public LateralElementDescriptor( final ICacheElement<K, V> ce, final LateralCommand command, final long requesterId)
    {
        this(ce, command, requesterId, -1);
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
        buf.append( "\n ICacheElement = [" + this.payload + "]" );
        return buf.toString();
    }
}

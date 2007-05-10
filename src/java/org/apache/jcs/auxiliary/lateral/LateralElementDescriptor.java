package org.apache.jcs.auxiliary.lateral;

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

import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * This class wraps command to other laterals. It is essentially a
 * JCS-TCP-Lateral packet. The headers specify the action the receiver should
 * take.
 */
public class LateralElementDescriptor
    implements Serializable
{
    private static final long serialVersionUID = 5268222498076063575L;

    /** The int for updates */
    public final static int UPDATE = 1;

    /** The int for removes */
    public final static int REMOVE = 2;

    /** The int instructing us to remove all */
    public final static int REMOVEALL = 3;

    /** The int for disposing the cache. */
    public final static int DISPOSE = 4;

    /** Command to return an object. */
    public final static int GET = 5;

    /** The Cache Element that we are distributing. */
    public ICacheElement ce;

    /**
     * The id of the the source of the request. This is used to prevent infinite
     * loops.
     */
    public long requesterId;

    /** The operation has been requested by the client. */
    public int command = UPDATE;

    /**
     * The hashcode value for this element.
     */
    public int valHashCode = -1;

    /** Constructor for the LateralElementDescriptor object */
    public LateralElementDescriptor()
    {
        super();
    }

    /**
     * Constructor for the LateralElementDescriptor object
     * <p>
     * @param ce ICacheElement payload
     */
    public LateralElementDescriptor( ICacheElement ce )
    {
        this.ce = ce;
    }

    /**
     * @return String, all the important values that can be configured
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\n LateralElementDescriptor " );
        buf.append( "\n command = [" + this.command + "]" );
        buf.append( "\n valHashCode = [" + this.valHashCode + "]" );
        buf.append( "\n ICacheElement = [" + this.ce + "]" );
        return buf.toString();
    }
}

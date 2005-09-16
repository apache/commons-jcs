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

import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * This class wraps command to other laterals.
 *  
 */
public class LateralElementDescriptor
    implements Serializable
{

    // command types
    /** Description of the Field */
    public final static int UPDATE = 1;

    /** Description of the Field */
    public final static int REMOVE = 2;

    /** Description of the Field */
    public final static int REMOVEALL = 3;

    /** Description of the Field */
    public final static int DISPOSE = 4;

    /** Command to return an object. */
    public final static int GET = 5;

    /** Description of the Field */
    public ICacheElement ce;

    /** Description of the Field */
    public long requesterId;

    /** Description of the Field */
    public int command = UPDATE;

    public int valHashCode = -1;
    
    // for update command
    /** Constructor for the LateralElementDescriptor object */
    public LateralElementDescriptor()
    {
    }

    /**
     * Constructor for the LateralElementDescriptor object
     * 
     * @param ce
     */
    public LateralElementDescriptor( ICacheElement ce )
    {
        this.ce = ce;
    }

}

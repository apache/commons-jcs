package org.apache.jcs.config;

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

/**
 * This class is based on the log4j class org.apache.log4j.config.PropertySetter
 * that was made by Anders Kristensen
 *
 */

/**
 * Thrown when an error is encountered whilst attempting to set a property using
 * the {@link PropertySetter}utility class.
 *
 * @since 1.1
 */
public class PropertySetterException
    extends Exception
{
    private static final long serialVersionUID = -210271658004609028L;

    /** Description of the Field */
    protected Throwable rootCause;

    /**
     * Constructor for the PropertySetterException object
     *
     * @param msg
     */
    public PropertySetterException( String msg )
    {
        super( msg );
    }

    /**
     * Constructor for the PropertySetterException object
     *
     * @param rootCause
     */
    public PropertySetterException( Throwable rootCause )
    {
        super();
        this.rootCause = rootCause;
    }

    /**
     * Returns descriptive text on the cause of this exception.
     *
     * @return The message value
     */

    public String getMessage()
    {
        String msg = super.getMessage();
        if ( msg == null && rootCause != null )
        {
            msg = rootCause.getMessage();
        }
        return msg;
    }
}

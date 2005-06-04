package org.apache.jcs.access.exception;

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

import org.apache.commons.lang.exception.NestableException;

/**
 * Description of the Class
 *  
 */
public class CacheException
    extends NestableException
{
    /** Constructor for the CacheException object */
    public CacheException()
    {
        super();
    }

    /**
     * Constructor for the CacheException object
     * 
     * @param nested
     */
    public CacheException( Throwable nested )
    {
        super( nested );
    }

    /**
     * Constructor for the CacheException object
     * 
     * @param message
     */
    public CacheException( String message )
    {
        super( message );
    }

    /**
     * Constructs a new <code>CacheException</code> with specified detail
     * message and nested <code>Throwable</code>.
     * 
     * @param msg
     *            the error message.
     * @param nested
     *            the exception or error that caused this exception to be
     *            thrown.
     */
    public CacheException( String msg, Throwable nested )
    {
        super( msg, nested );
    }
}

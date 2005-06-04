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

/**
 * I'm removing this exception from use. The overhead of throwing exceptions and
 * the cumbersomeness of coding around exceptions warrants removal. Exceptions
 * like this don't make sense to throw in the course of normal operations to
 * signify a normal and expected condition. Returning null if an object isn't
 * found is sufficient.
 *  
 */
public class ObjectExistsException
    extends CacheException
{

    /** Constructor for the ObjectExistsException object */
    public ObjectExistsException()
    {
        super();
    }

    /**
     * Constructor for the ObjectExistsException object
     * 
     * @param message
     */
    public ObjectExistsException( String message )
    {
        super( message );
    }

}

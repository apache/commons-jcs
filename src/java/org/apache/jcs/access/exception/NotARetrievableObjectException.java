package org.apache.jcs.access.exception;

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
 * This would be thrown if the object could not be retrieved from a method that throws exceptions
 * rather than null.
 * <p>
 * TODO check to see if we can get rid of this.
 */
public class NotARetrievableObjectException
    extends CacheException
{
    /** Don't change. */
    private static final long serialVersionUID = 4501711027054012410L;

    /** Constructor for the NotARetrievableObjectException object */
    public NotARetrievableObjectException()
    {
        super();
    }

    /**
     * Constructor for the NotARetrievableObjectException object
     * <p>
     * @param message
     */
    public NotARetrievableObjectException( String message )
    {
        super( message );
    }

}

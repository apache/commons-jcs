package org.apache.commons.jcs.engine.logging;

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

import org.apache.commons.jcs.engine.logging.behavior.ICacheEvent;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;

/**
 * For testing the configurator.
 */
public class MockCacheEventLogger
    implements ICacheEventLogger
{
    /** test property */
    private String testProperty;

    /**
     * @param source
     * @param eventName
     * @param optionalDetails
     */
    @Override
    public void logApplicationEvent( String source, String eventName, String optionalDetails )
    {
        // TODO Auto-generated method stub
    }

    /**
     * @param source
     * @param eventName
     * @param errorMessage
     */
    @Override
    public void logError( String source, String eventName, String errorMessage )
    {
        // TODO Auto-generated method stub
    }

    /**
     * @param source
     * @param region
     * @param eventName
     * @param optionalDetails
     * @param key
     * @return ICacheEvent
     */
    @Override
    public <T> ICacheEvent<T> createICacheEvent( String source, String region, String eventName, String optionalDetails,
                                          T key )
    {
        return new CacheEvent<T>();
    }

    /**
     * @param event
     */
    @Override
    public <T> void logICacheEvent( ICacheEvent<T> event )
    {
        // TODO Auto-generated method stub
    }

    /**
     * @param testProperty
     */
    public void setTestProperty( String testProperty )
    {
        this.testProperty = testProperty;
    }

    /**
     * @return testProperty
     */
    public String getTestProperty()
    {
        return testProperty;
    }
}

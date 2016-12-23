package org.apache.commons.jcs.auxiliary;

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

import org.apache.commons.jcs.engine.logging.CacheEvent;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEvent;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * For testing auxiliary event logging. Improve later so we can test the details. This is very
 * crude.
 */
public class MockCacheEventLogger
    implements ICacheEventLogger
{
    /** times called */
    public int applicationEventCalls = 0;

    /** times called */
    public int startICacheEventCalls = 0;

    /** times called */
    public int endICacheEventCalls = 0;

    /** times called */
    public int errorEventCalls = 0;

    /** list of messages */
    public List<String> errorMessages = new ArrayList<String>();

    /**
     * @param source
     * @param eventName
     * @param optionalDetails
     */
    @Override
    public void logApplicationEvent( String source, String eventName, String optionalDetails )
    {
        applicationEventCalls++;
    }

    /**
     * @param event
     */
    @Override
    public <T> void logICacheEvent( ICacheEvent<T> event )
    {
        endICacheEventCalls++;
    }

    /**
     * @param source
     * @param eventName
     * @param errorMessage
     */
    @Override
    public void logError( String source, String eventName, String errorMessage )
    {
        errorEventCalls++;
        errorMessages.add( errorMessage );
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
    public <T> ICacheEvent<T> createICacheEvent( String source, String region,
            String eventName, String optionalDetails, T key )
    {
        startICacheEventCalls++;
        return new CacheEvent<T>();
    }
}

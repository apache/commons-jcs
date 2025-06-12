package org.apache.commons.jcs3.auxiliary;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jcs3.engine.logging.CacheEvent;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEvent;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;

/**
 * For testing auxiliary event logging. Improve later so we can test the details. This is very
 * crude.
 */
public class MockCacheEventLogger
    implements ICacheEventLogger
{
    /** Times called */
    public int applicationEventCalls;

    /** Times called */
    public int startICacheEventCalls;

    /** Times called */
    public int endICacheEventCalls;

    /** Times called */
    public int errorEventCalls;

    /** List of messages */
    public List<String> errorMessages = new ArrayList<>();

    /**
     * @param source
     * @param region
     * @param eventName
     * @param optionalDetails
     * @param key
     * @return ICacheEvent
     */
    @Override
    public <T> ICacheEvent<T> createICacheEvent( final String source, final String region,
            final String eventName, final String optionalDetails, final T key )
    {
        startICacheEventCalls++;
        return new CacheEvent<>();
    }

    /**
     * @param source
     * @param eventName
     * @param optionalDetails
     */
    @Override
    public void logApplicationEvent( final String source, final String eventName, final String optionalDetails )
    {
        applicationEventCalls++;
    }

    /**
     * @param source
     * @param eventName
     * @param errorMessage
     */
    @Override
    public void logError( final String source, final String eventName, final String errorMessage )
    {
        errorEventCalls++;
        errorMessages.add( errorMessage );
    }

    /**
     * @param event
     */
    @Override
    public <T> void logICacheEvent( final ICacheEvent<T> event )
    {
        endICacheEventCalls++;
    }
}

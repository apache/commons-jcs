package org.apache.commons.jcs4.utils.threadpool;

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

/**
 * This object holds configuration data for a thread pool.
 */
public record PoolConfiguration(
        /** Should we bound the queue */
        boolean useBoundary,

        /** If the queue is bounded, how big can it get */
        int boundarySize,

        /** Only has meaning if a boundary is used */
        int maximumPoolSize,

        /**
         * the exact number that will be used in a boundless queue. If the queue has a boundary, more
         * will be created if the queue fills.
         */
        int minimumPoolSize,

        /** How long idle threads above the minimum should be kept alive. */
        int keepAliveTime,

        /** Should be ABORT, BLOCK, RUN, WAIT, DISCARDOLDEST, */
        WhenBlockedPolicy whenBlockedPolicy,

        /** The number of threads to create on startup */
        int startUpSize
) implements Cloneable
{
    public enum WhenBlockedPolicy
    {
        /** Abort when queue is full and max threads is reached. */
        ABORT,

        /** Run in current thread when queue is full and max threads is reached. */
        RUN,

        /** Discard oldest when queue is full and max threads is reached. */
        DISCARDOLDEST,

        /** Silently discard submitted job when queue is full and max threads is reached. */
        DISCARD
    }

    /**
     * DEFAULT SETTINGS
     */
    private static final boolean DEFAULT_USE_BOUNDARY = true;

    /** Default queue size limit */
    private static final int DEFAULT_BOUNDARY_SIZE = 2000;

    /** Default max size */
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 150;

    /** Default min */
    private static final int DEFAULT_MINIMUM_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    /** Default keep alive */
    private static final int DEFAULT_KEEPALIVE_TIME = 1000 * 60 * 5;

    /** Default when blocked */
    private static final WhenBlockedPolicy DEFAULT_WHEN_BLOCKED_POLICY = WhenBlockedPolicy.RUN;

    /** Default startup size */
    private static final int DEFAULT_STARTUP_SIZE = DEFAULT_MINIMUM_POOL_SIZE;

    /**
     * Default
     */
    private static PoolConfiguration DEFAULT = new PoolConfiguration(DEFAULT_USE_BOUNDARY,
            DEFAULT_BOUNDARY_SIZE, DEFAULT_MAXIMUM_POOL_SIZE, DEFAULT_MINIMUM_POOL_SIZE,
            DEFAULT_KEEPALIVE_TIME, DEFAULT_WHEN_BLOCKED_POLICY, DEFAULT_STARTUP_SIZE);

    /**
     * @return an object containing the default settings
     */
    public static PoolConfiguration defaults()
    {
        return DEFAULT;
    }


    /**
     * To string for debugging purposes.
     * @return String
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "useBoundary = [" + useBoundary() + "] " );
        buf.append( "boundarySize = [" + boundarySize() + "] " );
        buf.append( "maximumPoolSize = [" + maximumPoolSize() + "] " );
        buf.append( "minimumPoolSize = [" + minimumPoolSize() + "] " );
        buf.append( "keepAliveTime = [" + keepAliveTime() + "] " );
        buf.append( "whenBlockedPolicy = [" + whenBlockedPolicy() + "] " );
        buf.append( "startUpSize = [" + startUpSize() + "]" );
        return buf.toString();
    }
}

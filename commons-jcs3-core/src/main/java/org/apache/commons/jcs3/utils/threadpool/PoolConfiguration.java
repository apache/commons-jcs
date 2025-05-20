package org.apache.commons.jcs3.utils.threadpool;

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
public final class PoolConfiguration
    implements Cloneable
{
    public enum WhenBlockedPolicy {
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

    /** Should we bound the queue */
    private boolean useBoundary = DEFAULT_USE_BOUNDARY;

    /** If the queue is bounded, how big can it get */
    private int boundarySize = DEFAULT_BOUNDARY_SIZE;

    /** Only has meaning if a boundary is used */
    private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;

    /**
     * the exact number that will be used in a boundless queue. If the queue has a boundary, more
     * will be created if the queue fills.
     */
    private int minimumPoolSize = DEFAULT_MINIMUM_POOL_SIZE;

    /** How long idle threads above the minimum should be kept alive. */
    private int keepAliveTime = DEFAULT_KEEPALIVE_TIME;

    /** Should be ABORT, BLOCK, RUN, WAIT, DISCARDOLDEST, */
    private WhenBlockedPolicy whenBlockedPolicy = DEFAULT_WHEN_BLOCKED_POLICY;

    /** The number of threads to create on startup */
    private int startUpSize = DEFAULT_MINIMUM_POOL_SIZE;

    /**
     * Default
     */
    public PoolConfiguration()
    {
        this( DEFAULT_USE_BOUNDARY, DEFAULT_BOUNDARY_SIZE, DEFAULT_MAXIMUM_POOL_SIZE,
              DEFAULT_MINIMUM_POOL_SIZE, DEFAULT_KEEPALIVE_TIME,
              DEFAULT_WHEN_BLOCKED_POLICY, DEFAULT_STARTUP_SIZE );
    }

    /**
     * Constructs a completely configured instance.
     *
     * @param useBoundary
     * @param boundarySize
     * @param maximumPoolSize
     * @param minimumPoolSize
     * @param keepAliveTime
     * @param whenBlockedPolicy
     * @param startUpSize
     */
    public PoolConfiguration( final boolean useBoundary, final int boundarySize, final int maximumPoolSize, final int minimumPoolSize,
                              final int keepAliveTime, final WhenBlockedPolicy whenBlockedPolicy, final int startUpSize )
    {
        setUseBoundary( useBoundary );
        setBoundarySize( boundarySize );
        setMaximumPoolSize( maximumPoolSize );
        setMinimumPoolSize( minimumPoolSize );
        setKeepAliveTime( keepAliveTime );
        setWhenBlockedPolicy( whenBlockedPolicy );
        setStartUpSize( startUpSize );
    }

    /**
     * Copies the instance variables to another instance.
     *
     * @return PoolConfiguration
     */
    @Override
    public PoolConfiguration clone()
    {
        return new PoolConfiguration( isUseBoundary(), boundarySize, maximumPoolSize, minimumPoolSize, keepAliveTime,
                                      getWhenBlockedPolicy(), startUpSize );
    }

    /**
     * @return the boundarySize.
     */
    public int getBoundarySize()
    {
        return boundarySize;
    }

    /**
     * @return the keepAliveTime.
     */
    public int getKeepAliveTime()
    {
        return keepAliveTime;
    }

    /**
     * @return the maximumPoolSize.
     */
    public int getMaximumPoolSize()
    {
        return maximumPoolSize;
    }

    /**
     * @return the minimumPoolSize.
     */
    public int getMinimumPoolSize()
    {
        return minimumPoolSize;
    }

    /**
     * @return the startUpSize.
     */
    public int getStartUpSize()
    {
        return startUpSize;
    }

    /**
     * @return the whenBlockedPolicy.
     */
    public WhenBlockedPolicy getWhenBlockedPolicy()
    {
        return whenBlockedPolicy;
    }

    /**
     * @return the useBoundary.
     */
    public boolean isUseBoundary()
    {
        return useBoundary;
    }

    /**
     * @param boundarySize The boundarySize to set.
     */
    public void setBoundarySize( final int boundarySize )
    {
        this.boundarySize = boundarySize;
    }

    /**
     * @param keepAliveTime The keepAliveTime to set.
     */
    public void setKeepAliveTime( final int keepAliveTime )
    {
        this.keepAliveTime = keepAliveTime;
    }

    /**
     * @param maximumPoolSize The maximumPoolSize to set.
     */
    public void setMaximumPoolSize( final int maximumPoolSize )
    {
        this.maximumPoolSize = maximumPoolSize;
    }

    /**
     * @param minimumPoolSize The minimumPoolSize to set.
     */
    public void setMinimumPoolSize( final int minimumPoolSize )
    {
        this.minimumPoolSize = minimumPoolSize;
    }

    /**
     * @param startUpSize The startUpSize to set.
     */
    public void setStartUpSize( final int startUpSize )
    {
        this.startUpSize = startUpSize;
    }

    /**
     * @param useBoundary The useBoundary to set.
     */
    public void setUseBoundary( final boolean useBoundary )
    {
        this.useBoundary = useBoundary;
    }

    /**
     * @param whenBlockedPolicy The whenBlockedPolicy to set.
     */
    public void setWhenBlockedPolicy( final String whenBlockedPolicy )
    {
        if ( whenBlockedPolicy != null )
        {
            final WhenBlockedPolicy policy = WhenBlockedPolicy.valueOf(whenBlockedPolicy.trim().toUpperCase());
            setWhenBlockedPolicy(policy);
        }
        else
        {
            // the value is null, default to RUN
            this.whenBlockedPolicy = WhenBlockedPolicy.RUN;
        }
    }

    /**
     * @param whenBlockedPolicy The whenBlockedPolicy to set.
     */
    public void setWhenBlockedPolicy( final WhenBlockedPolicy whenBlockedPolicy )
    {
        if ( whenBlockedPolicy != null )
        {
            this.whenBlockedPolicy = whenBlockedPolicy;
        }
        else
        {
            // the value is null, default to RUN
            this.whenBlockedPolicy = WhenBlockedPolicy.RUN;
        }
    }

    /**
     * To string for debugging purposes.
     * @return String
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "useBoundary = [" + isUseBoundary() + "] " );
        buf.append( "boundarySize = [" + boundarySize + "] " );
        buf.append( "maximumPoolSize = [" + maximumPoolSize + "] " );
        buf.append( "minimumPoolSize = [" + minimumPoolSize + "] " );
        buf.append( "keepAliveTime = [" + keepAliveTime + "] " );
        buf.append( "whenBlockedPolicy = [" + getWhenBlockedPolicy() + "] " );
        buf.append( "startUpSize = [" + startUpSize + "]" );
        return buf.toString();
    }
}

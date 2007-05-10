package org.apache.jcs.utils.threadpool;

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

import org.apache.jcs.utils.threadpool.behavior.IPoolConfiguration;


/**
 * This object holds configuration data for a thread pool.
 * <p>
 * @author Aaron Smuts
 */
public class PoolConfiguration
    implements Cloneable, IPoolConfiguration
{
    private boolean useBoundary = true;

    private int boundarySize = 2000;

    // only has meaning if a bounday is used
    private int maximumPoolSize = 150;

    // the exact number that will be used in a boundless queue. If the queue has
    // a boundary, more will be created if the queue fills.
    private int minimumPoolSize = 4;

    private int keepAliveTime = 1000 * 60 * 5;

    // should be ABORT, BLOCK, RUN, WAIT, DISCARDOLDEST,
    private String whenBlockedPolicy = POLICY_RUN;

    private int startUpSize = 4;

    /**
     * @param useBoundary
     *            The useBoundary to set.
     */
    public void setUseBoundary( boolean useBoundary )
    {
        this.useBoundary = useBoundary;
    }

    /**
     * @return Returns the useBoundary.
     */
    public boolean isUseBoundary()
    {
        return useBoundary;
    }

    /**
     * Default
     */
    public PoolConfiguration()
    {
        // nop
    }

    /**
     * Construct a completely configured instance.
     * <p>
     * @param useBoundary
     * @param boundarySize
     * @param maximumPoolSize
     * @param minimumPoolSize
     * @param keepAliveTime
     * @param whenBlockedPolicy
     * @param startUpSize
     */
    public PoolConfiguration( boolean useBoundary, int boundarySize, int maximumPoolSize, int minimumPoolSize,
                             int keepAliveTime, String whenBlockedPolicy, int startUpSize )
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
     * @param boundarySize
     *            The boundarySize to set.
     */
    public void setBoundarySize( int boundarySize )
    {
        this.boundarySize = boundarySize;
    }

    /**
     * @return Returns the boundarySize.
     */
    public int getBoundarySize()
    {
        return boundarySize;
    }

    /**
     * @param maximumPoolSize
     *            The maximumPoolSize to set.
     */
    public void setMaximumPoolSize( int maximumPoolSize )
    {
        this.maximumPoolSize = maximumPoolSize;
    }

    /**
     * @return Returns the maximumPoolSize.
     */
    public int getMaximumPoolSize()
    {
        return maximumPoolSize;
    }

    /**
     * @param minimumPoolSize
     *            The minimumPoolSize to set.
     */
    public void setMinimumPoolSize( int minimumPoolSize )
    {
        this.minimumPoolSize = minimumPoolSize;
    }

    /**
     * @return Returns the minimumPoolSize.
     */
    public int getMinimumPoolSize()
    {
        return minimumPoolSize;
    }

    /**
     * @param keepAliveTime
     *            The keepAliveTime to set.
     */
    public void setKeepAliveTime( int keepAliveTime )
    {
        this.keepAliveTime = keepAliveTime;
    }

    /**
     * @return Returns the keepAliveTime.
     */
    public int getKeepAliveTime()
    {
        return keepAliveTime;
    }

    /**
     * @param whenBlockedPolicy
     *            The whenBlockedPolicy to set.
     */
    public void setWhenBlockedPolicy( String whenBlockedPolicy )
    {
        if ( whenBlockedPolicy != null )
        {
            whenBlockedPolicy = whenBlockedPolicy.trim();

            if ( whenBlockedPolicy.equalsIgnoreCase( POLICY_ABORT ) )
            {
                this.whenBlockedPolicy = POLICY_ABORT;
            }
            else if ( whenBlockedPolicy.equalsIgnoreCase( POLICY_RUN ) )
            {
                this.whenBlockedPolicy = POLICY_RUN;
            }
            else if ( whenBlockedPolicy.equalsIgnoreCase( POLICY_BLOCK ) )
            {
                this.whenBlockedPolicy = POLICY_BLOCK;
            }
            else if ( whenBlockedPolicy.equalsIgnoreCase( POLICY_DISCARDOLDEST ) )
            {
                this.whenBlockedPolicy = POLICY_DISCARDOLDEST;
            }
            else if ( whenBlockedPolicy.equalsIgnoreCase( POLICY_WAIT ) )
            {
                this.whenBlockedPolicy = POLICY_WAIT;
            }
            else
            {
                // the value is invalid, dfault to RUN
                this.whenBlockedPolicy = POLICY_RUN;
            }
        }
        else
        {
            // the value is null, dfault to RUN
            this.whenBlockedPolicy = POLICY_RUN;
        }
    }

    /**
     * @return Returns the whenBlockedPolicy.
     */
    public String getWhenBlockedPolicy()
    {
        return whenBlockedPolicy;
    }

    /**
     * @param startUpSize
     *            The startUpSize to set.
     */
    public void setStartUpSize( int startUpSize )
    {
        this.startUpSize = startUpSize;
    }

    /**
     * @return Returns the startUpSize.
     */
    public int getStartUpSize()
    {
        return startUpSize;
    }

    /**
     * To string for debugging purposes.
     * @return String
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "useBoundary = [" + isUseBoundary() + "] " );
        buf.append( "boundarySize = [" + boundarySize + "] " );
        buf.append( "maximumPoolSize = [" + maximumPoolSize + "] " );
        buf.append( "minimumPoolSize = [" + minimumPoolSize + "] " );
        buf.append( "keepAliveTime = [" + keepAliveTime + "] " );
        buf.append( "whenBlockedPolicy = [" + getWhenBlockedPolicy() + "] " );
        buf.append( "startUpSize = [" + startUpSize + "]" );
        return buf.toString();
    }

    /**
     * Copies the instance variables to another instance.
     * <p>
     * @return PoolConfiguration
     */
    public Object clone()
    {
        return new PoolConfiguration( isUseBoundary(), boundarySize, maximumPoolSize, minimumPoolSize, keepAliveTime,
                                      getWhenBlockedPolicy(), startUpSize );
    }
}

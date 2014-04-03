package org.apache.commons.jcs.engine.stats;

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

import org.apache.commons.jcs.engine.stats.behavior.ICacheStats;
import org.apache.commons.jcs.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs.engine.stats.behavior.IStats;

/**
 * This class stores cache historical and statistics data for a region.
 * <p>
 * Only the composite cache knows what the hit count across all auxiliaries is.
 */
public class CacheStats
    extends Stats
    implements ICacheStats
{
    /** Don't change. */
    private static final long serialVersionUID = 529914708798168590L;

    /** The region */
    private String regionName = null;

    /** What that auxiliaries are reporting. */
    private IStats[] auxStats = null;

    /** stats */
    private IStatElement[] stats = null;

    /**
     * Stats are for a region, though auxiliary data may be for more.
     * <p>
     * @return The region name
     */
    @Override
    public String getRegionName()
    {
        return regionName;
    }

    /**
     * Stats are for a region, though auxiliary data may be for more.
     * <p>
     * @param name - The region name
     */
    @Override
    public void setRegionName( String name )
    {
        regionName = name;
    }

    /**
     * @return IStats[]
     */
    @Override
    public IStats[] getAuxiliaryCacheStats()
    {
        return auxStats;
    }

    /**
     * @param stats
     */
    @Override
    public void setAuxiliaryCacheStats( IStats[] stats )
    {
        auxStats = stats;
    }

    /**
     * This returns data about the auxiliaries, such as hit count. Only the composite cache knows
     * what the hit count across all auxiliaries is.
     * <p>
     * @return IStatElement[]
     */
    @Override
    public IStatElement[] getStatElements()
    {
        return stats;
    }

    /**
     * @param stats
     */
    @Override
    public void setStatElements( IStatElement[] stats )
    {
        this.stats = stats;
    }

    /**
     * @return readable string that can be logged.
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();

        buf.append( "Region Name = " + regionName );

        if ( stats != null )
        {
            for ( int i = 0; i < stats.length; i++ )
            {
                buf.append( "\n" );
                buf.append( stats[i] );
            }
        }

        if ( auxStats != null )
        {
            for ( int i = 0; i < auxStats.length; i++ )
            {
                buf.append( "\n" );
                buf.append( "---------------------------" );
                buf.append( auxStats[i] );
            }
        }

        return buf.toString();
    }
}

package org.apache.commons.jcs3.engine.stats;

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

import java.util.List;

import org.apache.commons.jcs3.engine.stats.behavior.ICacheStats;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;

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
    private String regionName;

    /** What that auxiliaries are reporting. */
    private List<IStats> auxStats;

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
    public void setRegionName( final String name )
    {
        regionName = name;
    }

    /**
     * @return IStats[]
     */
    @Override
    public List<IStats> getAuxiliaryCacheStats()
    {
        return auxStats;
    }

    /**
     * @param stats
     */
    @Override
    public void setAuxiliaryCacheStats( final List<IStats> stats )
    {
        auxStats = stats;
    }

    /**
     * @return readable string that can be logged.
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();

        buf.append( "Region Name = " + regionName );

        if ( getStatElements() != null )
        {
            for ( final Object stat : getStatElements() )
            {
                buf.append( "\n" );
                buf.append( stat );
            }
        }

        if ( auxStats != null )
        {
            for ( final Object auxStat : auxStats )
            {
                buf.append( "\n" );
                buf.append( "---------------------------" );
                buf.append( auxStat );
            }
        }

        return buf.toString();
    }
}

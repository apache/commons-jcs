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

import org.apache.commons.jcs3.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;

/**
 * @author aaronsm
 */
public class Stats
    implements IStats
{
    /** Don't change */
    private static final long serialVersionUID = 227327902875154010L;

    /** The stats */
    private List<IStatElement<?>> stats;

    /** The type of stat */
    private String typeName;

    /**
     * @return IStatElement[]
     */
    @Override
    public List<IStatElement<?>> getStatElements()
    {
        return stats;
    }

    /**
     * @param stats
     */
    @Override
    public void setStatElements( final List<IStatElement<?>> stats )
    {
        this.stats = stats;
    }

    /**
     * @return typeName
     */
    @Override
    public String getTypeName()
    {
        return typeName;
    }

    /**
     * @param name
     */
    @Override
    public void setTypeName( final String name )
    {
        typeName = name;
    }

    /**
     * @return the stats in a readable string
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();

        buf.append( typeName );

        if ( stats != null )
        {
            for (final Object stat : stats)
            {
                buf.append( "\n" );
                buf.append( stat );
            }
        }

        return buf.toString();
    }
}

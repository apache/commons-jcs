package org.apache.commons.jcs4.engine.stats;

import java.util.ArrayList;
import java.util.Collections;

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

import java.util.List;

import org.apache.commons.jcs4.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs4.engine.stats.behavior.IStats;

/**
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
     * Default constructor
     */
    public Stats()
    {
        this.stats = new ArrayList<>();
    }

    /**
     * Constructor
     * @param typeName
     */
    public Stats(String typeName)
    {
        this();
        this.typeName = typeName;
    }

    /**
     * @return IStatElement[]
     */
    @Override
    public List<IStatElement<?>> getStatElements()
    {
        return Collections.unmodifiableList(stats);
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
     * @param stats
     */
    @Override
    public void addStatElements( final List<IStatElement<?>> stats )
    {
        this.stats.addAll(stats);
    }

    /**
     * @param stats
     */
    @Override
    public void addStatElement( final IStatElement<?> stats )
    {
        this.stats.add(stats);
    }

    /**
     * Adds generic statistical or historical data.
     *
     * @param name name of the StatElement
     * @param data value of the StatElement
     */
    @Override
    public <V> void addStatElement(String name, V data)
    {
        addStatElement(new StatElement<V>(name, data));
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

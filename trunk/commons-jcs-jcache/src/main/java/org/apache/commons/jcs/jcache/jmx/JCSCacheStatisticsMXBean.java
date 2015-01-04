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
package org.apache.commons.jcs.jcache.jmx;

import org.apache.commons.jcs.jcache.Statistics;

import javax.cache.management.CacheStatisticsMXBean;

public class JCSCacheStatisticsMXBean implements CacheStatisticsMXBean
{
    private final Statistics statistics;

    public JCSCacheStatisticsMXBean(final Statistics stats)
    {
        this.statistics = stats;
    }

    @Override
    public void clear()
    {
        statistics.reset();
    }

    @Override
    public long getCacheHits()
    {
        return statistics.getHits();
    }

    @Override
    public float getCacheHitPercentage()
    {
        final long hits = getCacheHits();
        if (hits == 0)
        {
            return 0;
        }
        return (float) hits / getCacheGets() * 100.0f;
    }

    @Override
    public long getCacheMisses()
    {
        return statistics.getMisses();
    }

    @Override
    public float getCacheMissPercentage()
    {
        final long misses = getCacheMisses();
        if (misses == 0)
        {
            return 0;
        }
        return (float) misses / getCacheGets() * 100.0f;
    }

    @Override
    public long getCacheGets()
    {
        return getCacheHits() + getCacheMisses();
    }

    @Override
    public long getCachePuts()
    {
        return statistics.getPuts();
    }

    @Override
    public long getCacheRemovals()
    {
        return statistics.getRemovals();
    }

    @Override
    public long getCacheEvictions()
    {
        return statistics.getEvictions();
    }

    @Override
    public float getAverageGetTime()
    {
        return averageTime(statistics.getTimeTakenForGets());
    }

    @Override
    public float getAveragePutTime()
    {
        return averageTime(statistics.getTimeTakenForPuts());
    }

    @Override
    public float getAverageRemoveTime()
    {
        return averageTime(statistics.getTimeTakenForRemovals());
    }

    private float averageTime(final long timeTaken)
    {
        final long gets = getCacheGets();
        if (timeTaken == 0 || gets == 0)
        {
            return 0;
        }
        return timeTaken / gets;
    }
}

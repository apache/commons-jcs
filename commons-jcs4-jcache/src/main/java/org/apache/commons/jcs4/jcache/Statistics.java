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
package org.apache.commons.jcs4.jcache;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Statistics
{
    private static final Duration MAX_DURATION = ChronoUnit.FOREVER.getDuration();

    private volatile boolean active = true;

    private final AtomicLong removals = new AtomicLong();
    private final AtomicLong expiries = new AtomicLong();
    private final AtomicLong puts = new AtomicLong();
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong evictions = new AtomicLong();
    private final AtomicReference<Duration> putTimeTaken = new AtomicReference<Duration>(Duration.ZERO);
    private final AtomicReference<Duration> getTimeTaken = new AtomicReference<Duration>(Duration.ZERO);
    private final AtomicReference<Duration> removeTimeTaken = new AtomicReference<Duration>(Duration.ZERO);

    public void addGetTime(final Duration duration)
    {
        increment(duration, getTimeTaken);
    }

    public void addPutTime(final Duration duration)
    {
        increment(duration, putTimeTaken);
    }

    public void addRemoveTime(final Duration duration)
    {
        increment(duration, removeTimeTaken);
    }

    public long getEvictions()
    {
        return evictions.get();
    }

    public long getHits()
    {
        return hits.get();
    }

    public long getMisses()
    {
        return misses.get();
    }

    public long getPuts()
    {
        return puts.get();
    }

    public long getRemovals()
    {
        return removals.get();
    }

    public Duration getTimeTakenForGets()
    {
        return getTimeTaken.get();
    }

    public Duration getTimeTakenForPuts()
    {
        return putTimeTaken.get();
    }

    public Duration getTimeTakenForRemovals()
    {
        return removeTimeTaken.get();
    }

    public void incrementEvictions()
    {
        increment(evictions, 1L);
    }

    public void incrementHits()
    {
        increment(hits, 1L);
    }

    public void decrementHits()
    {
        increment(hits, -1L);
    }

    public void incrementMisses()
    {
        increment(misses, 1L);
    }

    public void decrementMisses()
    {
        increment(misses, -1L);
    }

    public void incrementPuts()
    {
        increment(puts, 1L);
    }

    public void decrementPuts()
    {
        increment(puts, -1L);
    }

    public void incrementRemovals()
    {
        increment(removals, 1L);
    }

    public void decrementRemovals()
    {
        increment(removals, -1L);
    }

    private void increment(final AtomicLong counter, final long number)
    {
        if (!active)
        {
            return;
        }
        counter.addAndGet(number);
    }

    private void increment(final Duration duration, final AtomicReference<Duration> counter)
    {
        if (!active)
        {
            return;
        }

        counter.accumulateAndGet(duration, (u, v) -> {
            if (u.compareTo(MAX_DURATION.minus(v)) < 0)
            {
                return u.plus(v);
            }
            else
            {
                reset();
                return v;
            }
        });
    }

    public void reset()
    {
        puts.set(0);
        misses.set(0);
        removals.set(0);
        expiries.set(0);
        hits.set(0);
        evictions.set(0);
        getTimeTaken.set(Duration.ZERO);
        putTimeTaken.set(Duration.ZERO);
        removeTimeTaken.set(Duration.ZERO);
    }

    public void setActive(final boolean active)
    {
        this.active = active;
    }
}

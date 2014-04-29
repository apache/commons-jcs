package org.apache.commons.jcs.jcache;

import java.util.concurrent.atomic.AtomicLong;

public class Statistics
{
    private volatile boolean active = true;

    private final AtomicLong removals = new AtomicLong();
    private final AtomicLong expiries = new AtomicLong();
    private final AtomicLong puts = new AtomicLong();
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong evictions = new AtomicLong();
    private final AtomicLong putTimeTaken = new AtomicLong();
    private final AtomicLong getTimeTaken = new AtomicLong();
    private final AtomicLong removeTimeTaken = new AtomicLong();

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

    public long getEvictions()
    {
        return evictions.get();
    }

    public long getTimeTakenForGets()
    {
        return getTimeTaken.get();
    }

    public long getTimeTakenForPuts()
    {
        return putTimeTaken.get();
    }

    public long getTimeTakenForRemovals()
    {
        return removeTimeTaken.get();
    }

    public void increaseRemovals(final long number)
    {
        increment(removals, number);
    }

    public void increaseExpiries(final long number)
    {
        increment(expiries, number);
    }

    public void increasePuts(final long number)
    {
        increment(puts, number);
    }

    public void increaseHits(final long number)
    {
        increment(hits, number);
    }

    public void increaseMisses(final long number)
    {
        increment(misses, number);
    }

    public void increaseEvictions(final long number)
    {
        increment(evictions, number);
    }

    public void addGetTime(final long duration)
    {
        increment(duration, getTimeTaken);
    }

    public void addPutTime(final long duration)
    {
        increment(duration, putTimeTaken);
    }

    public void addRemoveTime(final long duration)
    {
        increment(duration, removeTimeTaken);
    }

    private void increment(final AtomicLong counter, final long number)
    {
        if (!active)
        {
            return;
        }
        counter.addAndGet(number);
    }

    private void increment(final long duration, final AtomicLong counter)
    {
        if (!active)
        {
            return;
        }

        if (counter.get() < Long.MAX_VALUE - duration)
        {
            counter.addAndGet(duration);
        }
        else
        {
            reset();
            counter.set(duration);
        }
    }

    public void reset()
    {
        puts.set(0);
        misses.set(0);
        removals.set(0);
        expiries.set(0);
        hits.set(0);
        evictions.set(0);
        getTimeTaken.set(0);
        putTimeTaken.set(0);
        removeTimeTaken.set(0);
    }

    public void setActive(final boolean active)
    {
        this.active = active;
    }
}

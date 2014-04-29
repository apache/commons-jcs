package org.apache.commons.jcs.jcache;

import java.io.Serializable;

import javax.cache.expiry.Duration;

public class JCSElement<V> implements Serializable
{
    /** Serial version */
    private static final long serialVersionUID = 6399046812336629074L;

    private final V element;
    private volatile long end;

    public JCSElement(final V element, final Duration duration)
    {
        this.element = element;
        update(duration);
    }

    public boolean isExpired()
    {
        return end != -1 && (end == 0 || Times.now() > end);
    }

    public V getElement()
    {
        return element;
    }

    public void update(final Duration duration)
    {
        if (duration == null || duration.isEternal())
        {
            end = -1;
        }
        else if (duration.isZero())
        {
            end = 0;
        }
        else
        {
            end = duration.getAdjustedTime(Times.now());
        }
    }
}

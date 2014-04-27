package org.apache.commons.jcs.jcache;

import javax.cache.expiry.Duration;
import java.io.Serializable;

public class JCSElement<V> implements Serializable {
    private final V element;
    private volatile long end;

    public JCSElement(final V element, final Duration duration) {
        this.element = element;
        update(duration);
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    public boolean isExpired() {
        return end != -1 && (end == 0 || now() > end);
    }

    public V getElement() {
        return element;
    }

    public void update(final Duration duration) {
        if (duration == null || duration.isEternal()) {
            end = -1;
        } else if (duration.isZero()) {
            end = 0;
        } else {
            end = duration.getAdjustedTime(now());
        }
    }
}

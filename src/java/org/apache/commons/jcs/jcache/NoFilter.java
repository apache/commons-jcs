package org.apache.commons.jcs.jcache;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListenerException;

public class NoFilter implements CacheEntryEventFilter<Object, Object> {
    public static final CacheEntryEventFilter INSTANCE = new NoFilter();

    private NoFilter() {
        // no-op
    }

    @Override
    public boolean evaluate(final CacheEntryEvent<?, ?> event) throws CacheEntryListenerException {
        return true;
    }
}

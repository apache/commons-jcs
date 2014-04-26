package org.apache.commons.jcs.jcache;

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Factory;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JCSListener<K extends Serializable, V extends Serializable> {
    private final boolean oldValue;
    private final boolean synchronous;
    private final CacheEntryEventFilter<? super K, ? super V> filter;
    private final CacheEntryListener<? super K, ? super V> delegate;
    private final boolean remove;
    private final boolean expire;
    private final boolean update;
    private final boolean create;

    public JCSListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        oldValue = cacheEntryListenerConfiguration.isOldValueRequired();
        synchronous = cacheEntryListenerConfiguration.isSynchronous();

        final Factory<CacheEntryEventFilter<? super K, ? super V>> filterFactory = cacheEntryListenerConfiguration.getCacheEntryEventFilterFactory();
        if (filterFactory == null) {
            filter = NoFilter.INSTANCE;
        } else {
            filter = filterFactory.create();
        }

        delegate = cacheEntryListenerConfiguration.getCacheEntryListenerFactory().create();
        remove = CacheEntryRemovedListener.class.isInstance(delegate);
        expire = CacheEntryExpiredListener.class.isInstance(delegate);
        update = CacheEntryUpdatedListener.class.isInstance(delegate);
        create = CacheEntryCreatedListener.class.isInstance(delegate);
    }

    public void onRemoved(final List<CacheEntryEvent<? extends K, ? extends V>> events) throws CacheEntryListenerException {
        if (remove) {
            CacheEntryRemovedListener.class.cast(delegate).onRemoved(filter(events));
        }
    }


    public void onExpired(final List<CacheEntryEvent<? extends K, ? extends V>> events) throws CacheEntryListenerException {
        if (expire) {
            CacheEntryExpiredListener.class.cast(delegate).onExpired(filter(events));
        }
    }

    public void onUpdated(final List<CacheEntryEvent<? extends K, ? extends V>> events) throws CacheEntryListenerException {
        if (update) {
            CacheEntryUpdatedListener.class.cast(delegate).onUpdated(filter(events));
        }
    }

    public void onCreated(final List<CacheEntryEvent<? extends K, ? extends V>> events) throws CacheEntryListenerException {
        if (create) {
            CacheEntryCreatedListener.class.cast(delegate).onCreated(filter(events));
        }
    }

    private Iterable<CacheEntryEvent<? extends K, ? extends V>> filter(final List<CacheEntryEvent<? extends K, ? extends V>> events) {
        if (filter == NoFilter.INSTANCE) {
            return events;
        }

        final List<CacheEntryEvent<? extends K, ? extends V>> filtered = new ArrayList<CacheEntryEvent<? extends K, ? extends V>>(events.size());
        for (final CacheEntryEvent<? extends K, ? extends V> event : events) {
            if (filter.evaluate(event)) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    public void close() {

    }
}

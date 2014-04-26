package org.apache.commons.jcs.jcache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ImmutableIterable<T> implements Iterable<T> {
    private final Collection<T> delegate;

    public ImmutableIterable(final Collection<T> delegate) {
        this.delegate = new ArrayList<T>(delegate);
    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableIterator<T>(delegate.iterator());
    }
}

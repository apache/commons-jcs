package org.apache.commons.jcs.jcache;

import java.util.Iterator;

public class ImmutableIterator<T> implements Iterator<T> {
    private final Iterator<T> delegate;

    public ImmutableIterator(final Iterator<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public T next() {
        return delegate.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("this iterator is immutable");
    }
}

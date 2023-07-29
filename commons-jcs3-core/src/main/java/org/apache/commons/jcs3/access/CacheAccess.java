package org.apache.commons.jcs3.access;

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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.jcs3.access.behavior.ICacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.access.exception.InvalidArgumentException;
import org.apache.commons.jcs3.access.exception.InvalidHandleException;
import org.apache.commons.jcs3.access.exception.ObjectExistsException;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.apache.commons.jcs3.engine.control.CompositeCache;

/**
 * This class provides an interface for all types of access to the cache.
 * <p>
 * An instance of this class is tied to a specific cache region. Static methods are provided to get
 * such instances.
 * <p>
 * Using this class you can retrieve an item, the item's wrapper, and the element's configuration.  You can also put an
 * item in the cache, remove an item, and clear a region.
 * <p>
 * The JCS class is the preferred way to access these methods.
 */
public class CacheAccess<K, V>
    extends AbstractCacheAccess<K, V>
    implements ICacheAccess<K, V>
{
    /**
     * Constructor for the CacheAccess object.
     * <p>
     * @param cacheControl The cache which the created instance accesses
     */
    public CacheAccess( final CompositeCache<K, V> cacheControl )
    {
        super(cacheControl);
    }

    /**
     * Retrieve an object from the cache region this instance provides access to.
     * <p>
     * @param name Key the object is stored as
     * @return The object if found or null
     */
    @Override
    public V get( final K name )
    {
        final ICacheElement<K, V> element = this.getCacheControl().get( name );

        return ( element != null ) ? element.getVal() : null;
    }

    /**
     * Retrieve an object from the cache region this instance provides access to.
     * If the object cannot be found in the cache, it will be retrieved by
     * calling the supplier and subsequently storing it in the cache.
     * <p>
     * @param name
     * @param supplier supplier to be called if the value is not found
     * @return Object.
     */
    @Override
    public V get(final K name, final Supplier<V> supplier)
    {
        V value = get(name);

        if (value == null)
        {
            value = supplier.get();
            put(name, value);
        }

        return value;
    }

    /**
     * Retrieve matching objects from the cache region this instance provides access to.
     * <p>
     * @param pattern - a key pattern for the objects stored
     * @return A map of key to values.  These are stripped from the wrapper.
     */
    @Override
    public Map<K, V> getMatching( final String pattern )
    {
        final Map<K, V> unwrappedResults;

        final Map<K, ICacheElement<K, V>> wrappedResults = this.getCacheControl().getMatching( pattern );

        if ( wrappedResults == null )
        {
            unwrappedResults = new HashMap<>();
        }
        else
        {
            unwrappedResults = wrappedResults.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != null)
                    .collect(Collectors.toMap(
                            Entry::getKey,
                            entry -> entry.getValue().getVal()));
        }

        return unwrappedResults;
    }

    /**
     * This method returns the ICacheElement&lt;K, V&gt; wrapper which provides access to element info and other
     * attributes.
     * <p>
     * This returns a reference to the wrapper. Any modifications will be reflected in the cache. No
     * defensive copy is made.
     * <p>
     * This method is most useful if you want to determine things such as the how long the element
     * has been in the cache.
     * <p>
     * The last access time in the ElementAttributes should be current.
     * <p>
     * @param name Key the Serializable is stored as
     * @return The ICacheElement&lt;K, V&gt; if the object is found or null
     */
    @Override
    public ICacheElement<K, V> getCacheElement( final K name )
    {
        return this.getCacheControl().get( name );
    }

    /**
     * Get multiple elements from the cache based on a set of cache keys.
     * <p>
     * This method returns the ICacheElement&lt;K, V&gt; wrapper which provides access to element info and other
     * attributes.
     * <p>
     * This returns a reference to the wrapper. Any modifications will be reflected in the cache. No
     * defensive copy is made.
     * <p>
     * This method is most useful if you want to determine things such as the how long the element
     * has been in the cache.
     * <p>
     * The last access time in the ElementAttributes should be current.
     * <p>
     * @param names set of Serializable cache keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or empty map if none of the keys are present
     */
    @Override
    public Map<K, ICacheElement<K, V>> getCacheElements( final Set<K> names )
    {
        return this.getCacheControl().getMultiple( names );
    }

    /**
     * Get multiple elements from the cache based on a set of cache keys.
     * <p>
     * This method returns the ICacheElement&lt;K, V&gt; wrapper which provides access to element info and other
     * attributes.
     * <p>
     * This returns a reference to the wrapper. Any modifications will be reflected in the cache. No
     * defensive copy is made.
     * <p>
     * This method is most useful if you want to determine things such as the how long the element
     * has been in the cache.
     * <p>
     * The last access time in the ElementAttributes should be current.
     * <p>
     * @param pattern key search pattern
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or empty map if no keys match the pattern
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatchingCacheElements( final String pattern )
    {
        return this.getCacheControl().getMatching( pattern );
    }

    /**
     * Place a new object in the cache, associated with key name. If there is currently an object
     * associated with name in the region an ObjectExistsException is thrown. Names are scoped to a
     * region so they must be unique within the region they are placed.
     * <p>
     * @param key Key object will be stored with
     * @param value Object to store
     * @throws CacheException and ObjectExistsException is thrown if the item is already in the
     *                cache.
     */
    @Override
    public void putSafe( final K key, final V value )
    {
        if ( this.getCacheControl().get( key ) != null )
        {
            throw new ObjectExistsException( "putSafe failed.  Object exists in the cache for key [" + key
                + "].  Remove first or use a non-safe put to override the value." );
        }
        put( key, value );
    }

    /**
     * Place a new object in the cache, associated with key name. If there is currently an object
     * associated with name in the region it is replaced. Names are scoped to a region so they must
     * be unique within the region they are placed.
     * @param name Key object will be stored with
     * @param obj Object to store
     */
    @Override
    public void put( final K name, final V obj )
    {
        // Call put with a copy of the contained caches default attributes.
        // the attributes are copied by the cacheControl
        put( name, obj, this.getCacheControl().getElementAttributes() );
    }

    /**
     * Constructs a cache element with these attributes, and puts it into the cache.
     * <p>
     * If the key or the value is null, and InvalidArgumentException is thrown.
     * <p>
     * @see org.apache.commons.jcs3.access.behavior.ICacheAccess#put(Object, Object, IElementAttributes)
     */
    @Override
    public void put( final K key, final V val, final IElementAttributes attr )
    {
        if ( key == null )
        {
            throw new InvalidArgumentException( "Key must not be null" );
        }

        if ( val == null )
        {
            throw new InvalidArgumentException( "Value must not be null" );
        }

        // Create the element and update. This may throw an IOException which
        // should be wrapped by cache access.
        try
        {
            final CacheElement<K, V> ce = new CacheElement<>( this.getCacheControl().getCacheName(), key,
                                                val );

            ce.setElementAttributes( attr );

            this.getCacheControl().update( ce );
        }
        catch ( final IOException e )
        {
            throw new CacheException( e );
        }
    }

    /**
     * Removes a single item by name.
     * <p>
     * @param name the name of the item to remove.
     */
    @Override
    public void remove( final K name )
    {
        this.getCacheControl().remove( name );
    }

    /**
     * Reset attributes for a particular element in the cache. NOTE: this method is currently not
     * implemented.
     * <p>
     * @param name Key of object to reset attributes for
     * @param attr New attributes for the object
     * @throws InvalidHandleException if the item does not exist.
     */
    @Override
    public void resetElementAttributes( final K name, final IElementAttributes attr )
    {
        final ICacheElement<K, V> element = this.getCacheControl().get( name );

        if ( element == null )
        {
            throw new InvalidHandleException( "Object for name [" + name + "] is not in the cache" );
        }

        // Although it will work currently, don't assume pass by reference here,
        // i.e. don't do this:
        // element.setElementAttributes( attr );
        // Another reason to call put is to force the changes to be distributed.

        put( element.getKey(), element.getVal(), attr );
    }

    /**
     * GetElementAttributes will return an attribute object describing the current attributes
     * associated with the object name. The name object must override the Object.equals and
     * Object.hashCode methods.
     * <p>
     * @param name Key of object to get attributes for
     * @return Attributes for the object, null if object not in cache
     */
    @Override
    public IElementAttributes getElementAttributes( final K name ) throws CacheException
    {
        IElementAttributes attr = null;

        try
        {
            attr = this.getCacheControl().getElementAttributes( name );
        }
        catch ( final IOException ioe )
        {
            throw new CacheException("Failure getting element attributes", ioe);
        }

        return attr;
    }
}

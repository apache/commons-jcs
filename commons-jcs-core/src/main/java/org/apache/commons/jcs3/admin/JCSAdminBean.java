package org.apache.commons.jcs3.admin;

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
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.auxiliary.remote.server.RemoteCacheServer;
import org.apache.commons.jcs3.auxiliary.remote.server.RemoteCacheServerFactory;
import org.apache.commons.jcs3.engine.CacheElementSerialized;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.apache.commons.jcs3.engine.memory.behavior.IMemoryCache;

/**
 * A servlet which provides HTTP access to JCS. Allows a summary of regions to be viewed, and
 * removeAll to be run on individual regions or all regions. Also provides the ability to remove
 * items (any number of key arguments can be provided with action 'remove'). Should be initialized
 * with a properties file that provides at least a classpath resource loader.
 */
public class JCSAdminBean implements JCSJMXBean
{
    /** The cache manager. */
    private final CompositeCacheManager cacheHub;

    /**
     * Default constructor
     */
    public JCSAdminBean()
    {
        try
        {
            this.cacheHub = CompositeCacheManager.getInstance();
        }
        catch (final CacheException e)
        {
            throw new RuntimeException("Could not retrieve cache manager instance", e);
        }
    }

    /**
     * Parameterized constructor
     *
	 * @param cacheHub the cache manager instance
	 */
	public JCSAdminBean(final CompositeCacheManager cacheHub)
	{
		this.cacheHub = cacheHub;
	}

	/**
     * Builds up info about each element in a region.
     * <p>
     * @param cacheName
     * @return List of CacheElementInfo objects
     * @throws IOException
     */
    @Override
    public List<CacheElementInfo> buildElementInfo( final String cacheName )
        throws IOException
    {
        final CompositeCache<Object, Object> cache = cacheHub.getCache( cacheName );

        // Convert all keys to string, store in a sorted map
        final TreeMap<String, ?> keys = new TreeMap<>(cache.getMemoryCache().getKeySet()
                .stream()
                .collect(Collectors.toMap(Object::toString, k -> k)));

        final LinkedList<CacheElementInfo> records = new LinkedList<>();

        final DateFormat format = DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT );

        final long now = System.currentTimeMillis();

        for (final Map.Entry<String, ?> key : keys.entrySet())
        {
            final ICacheElement<?, ?> element = cache.getMemoryCache().getQuiet( key.getValue() );

            final IElementAttributes attributes = element.getElementAttributes();

            final CacheElementInfo elementInfo = new CacheElementInfo(
            		key.getKey(),
            		attributes.getIsEternal(),
            		format.format(new Date(attributes.getCreateTime())),
            		attributes.getMaxLife(),
            		(now - attributes.getCreateTime() - attributes.getMaxLife() * 1000 ) / -1000);

            records.add( elementInfo );
        }

        return records;
    }

    /**
     * Builds up data on every region.
     * <p>
     * TODO we need a most light weight method that does not count bytes. The byte counting can
     *       really swamp a server.
     * @return List of CacheRegionInfo objects
     */
    @Override
    public List<CacheRegionInfo> buildCacheInfo()
    {
        final TreeSet<String> cacheNames = new TreeSet<>(cacheHub.getCacheNames());

        final LinkedList<CacheRegionInfo> cacheInfo = new LinkedList<>();

        for (final String cacheName : cacheNames)
        {
            final CompositeCache<?, ?> cache = cacheHub.getCache( cacheName );

            final CacheRegionInfo regionInfo = new CacheRegionInfo(
                    cache.getCacheName(),
                    cache.getSize(),
                    cache.getStatus().toString(),
                    cache.getStats(),
                    cache.getHitCountRam(),
                    cache.getHitCountAux(),
                    cache.getMissCountNotFound(),
                    cache.getMissCountExpired(),
                    getByteCount( cache ));

            cacheInfo.add( regionInfo );
        }

        return cacheInfo;
    }


	/**
     * Tries to estimate how much data is in a region. This is expensive. If there are any non serializable objects in
     * the region or an error occurs, suppresses exceptions and returns 0.
     * <p>
     *
     * @return int The size of the region in bytes.
     */
	@Override
    public long getByteCount(final String cacheName)
	{
		return getByteCount(cacheHub.getCache(cacheName));
	}

	/**
     * Tries to estimate how much data is in a region. This is expensive. If there are any non serializable objects in
     * the region or an error occurs, suppresses exceptions and returns 0.
     * <p>
     *
     * @return int The size of the region in bytes.
     */
    public <K, V> long getByteCount(final CompositeCache<K, V> cache)
    {
        if (cache == null)
        {
            throw new IllegalArgumentException("The cache object specified was null.");
        }

        long size = 0;
        final IMemoryCache<K, V> memCache = cache.getMemoryCache();

        for (final K key : memCache.getKeySet())
        {
            ICacheElement<K, V> ice = null;
			try
			{
				ice = memCache.get(key);
			}
			catch (final IOException e)
			{
                throw new RuntimeException("IOException while trying to get a cached element", e);
			}

			if (ice == null)
			{
				continue;
			}

			if (ice instanceof CacheElementSerialized)
            {
                size += ((CacheElementSerialized<K, V>) ice).getSerializedValue().length;
            }
            else
            {
                final Object element = ice.getVal();

                //CountingOnlyOutputStream: Keeps track of the number of bytes written to it, but doesn't write them anywhere.
                final CountingOnlyOutputStream counter = new CountingOnlyOutputStream();
                try (ObjectOutputStream out = new ObjectOutputStream(counter))
                {
                    out.writeObject(element);
                }
                catch (final IOException e)
                {
                    throw new RuntimeException("IOException while trying to measure the size of the cached element", e);
                }
                finally
                {
                	try
                	{
						counter.close();
					}
                	catch (final IOException e)
                	{
                		// ignore
					}
                }

                // 4 bytes lost for the serialization header
                size += counter.getCount() - 4;
            }
        }

        return size;
    }

    /**
     * Clears all regions in the cache.
     * <p>
     * If this class is running within a remote cache server, clears all regions via the <code>RemoteCacheServer</code>
     * API, so that removes will be broadcast to client machines. Otherwise clears all regions in the cache directly via
     * the usual cache API.
     */
    @Override
    public void clearAllRegions() throws IOException
    {
        final RemoteCacheServer<?, ?> remoteCacheServer = RemoteCacheServerFactory.getRemoteCacheServer();

        if (remoteCacheServer == null)
        {
            // Not running in a remote cache server.
            // Remove objects from the cache directly, as no need to broadcast removes to client machines...
            for (final String name : cacheHub.getCacheNames())
            {
                cacheHub.getCache(name).removeAll();
            }
        }
        else
        {
            // Running in a remote cache server.
            // Remove objects via the RemoteCacheServer API, so that removes will be broadcast to client machines...
            // Call remoteCacheServer.removeAll(String) for each cacheName...
            for (final String name : cacheHub.getCacheNames())
            {
                remoteCacheServer.removeAll(name);
            }
        }
    }

    /**
     * Clears a particular cache region.
     * <p>
     * If this class is running within a remote cache server, clears the region via the <code>RemoteCacheServer</code>
     * API, so that removes will be broadcast to client machines. Otherwise clears the region directly via the usual
     * cache API.
     */
    @Override
    public void clearRegion(final String cacheName) throws IOException
    {
        if (cacheName == null)
        {
            throw new IllegalArgumentException("The cache name specified was null.");
        }
        if (RemoteCacheServerFactory.getRemoteCacheServer() == null)
        {
            // Not running in a remote cache server.
            // Remove objects from the cache directly, as no need to broadcast removes to client machines...
            cacheHub.getCache(cacheName).removeAll();
        }
        else
        {
            // Running in a remote cache server.
            // Remove objects via the RemoteCacheServer API, so that removes will be broadcast to client machines...
            try
            {
                // Call remoteCacheServer.removeAll(String)...
                final RemoteCacheServer<?, ?> remoteCacheServer = RemoteCacheServerFactory.getRemoteCacheServer();
                remoteCacheServer.removeAll(cacheName);
            }
            catch (final IOException e)
            {
                throw new IllegalStateException("Failed to remove all elements from cache region [" + cacheName + "]: " + e, e);
            }
        }
    }

    /**
     * Removes a particular item from a particular region.
     * <p>
     * If this class is running within a remote cache server, removes the item via the <code>RemoteCacheServer</code>
     * API, so that removes will be broadcast to client machines. Otherwise clears the region directly via the usual
     * cache API.
     *
     * @param cacheName
     * @param key
     *
     * @throws IOException
     */
    @Override
    public void removeItem(final String cacheName, final String key) throws IOException
    {
        if (cacheName == null)
        {
            throw new IllegalArgumentException("The cache name specified was null.");
        }
        if (key == null)
        {
            throw new IllegalArgumentException("The key specified was null.");
        }
        if (RemoteCacheServerFactory.getRemoteCacheServer() == null)
        {
            // Not running in a remote cache server.
            // Remove objects from the cache directly, as no need to broadcast removes to client machines...
            cacheHub.getCache(cacheName).remove(key);
        }
        else
        {
            // Running in a remote cache server.
            // Remove objects via the RemoteCacheServer API, so that removes will be broadcast to client machines...
            try
            {
                Object keyToRemove = null;
                final CompositeCache<?, ?> cache = CompositeCacheManager.getInstance().getCache(cacheName);

                // A String key was supplied, but to remove elements via the RemoteCacheServer API, we need the
                // actual key object as stored in the cache (i.e. a Serializable object). To find the key in this form,
                // we iterate through all keys stored in the memory cache until we find one whose toString matches
                // the string supplied...
                final Set<?> allKeysInCache = cache.getMemoryCache().getKeySet();
                for (final Object keyInCache : allKeysInCache)
                {
                    if (keyInCache.toString().equals(key))
                    {
                        if (keyToRemove != null) {
                            // A key matching the one specified was already found...
                            throw new IllegalStateException("Unexpectedly found duplicate keys in the cache region matching the key specified.");
                        }
                        keyToRemove = keyInCache;
                    }
                }
                if (keyToRemove == null)
                {
                    throw new IllegalStateException("No match for this key could be found in the set of keys retrieved from the memory cache.");
                }
                // At this point, we have retrieved the matching K key.

                // Call remoteCacheServer.remove(String, Serializable)...
                final RemoteCacheServer<Serializable, Serializable> remoteCacheServer = RemoteCacheServerFactory.getRemoteCacheServer();
                remoteCacheServer.remove(cacheName, key);
            }
            catch (final Exception e)
            {
                throw new IllegalStateException("Failed to remove element with key [" + key + ", " + key.getClass() + "] from cache region [" + cacheName + "]: " + e, e);
            }
        }
    }
}

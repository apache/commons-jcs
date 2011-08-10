package org.apache.jcs.access;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.access.behavior.ICacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.access.exception.ConfigurationException;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.utils.props.AbstractPropertyContainer;

/**
 * TODO:  Add new methods that will allow you to provide a partition indicator for all major calls.  Add an interface as well.
 * <p>
 * This handles dividing puts and gets.
 * <p>
 * There are two required properties.
 * <p>
 * <ol>
 * <li>.numberOfPartitions</li>
 * <li>.partitionRegionNamePrefix</li>
 * </ol>
 * System properties will override values in the properties file.
 * <p>
 * We use a JCS region name for each partition that looks like this: partitionRegionNamePrefix + "_"
 * + patitionNuber. The number is ) indexed based.
 * <p>
 * @author Aaron Smuts
 */
public class PartitionedCacheAccess<K, V>
    extends AbstractPropertyContainer
    implements ICacheAccess<K, V>
{
    /** the logger. */
    private static final Log log = LogFactory.getLog( PartitionedCacheAccess.class );

    /** The number of partitions. */
    private int numberOfPartitions = 1;

    /**
     * We use a JCS region name for each partition that looks like this: partitionRegionNamePrefix +
     * "_" + partitionNumber
     */
    private String partitionRegionNamePrefix;

    /** An array of partitions built during initialization. */
    private ICacheAccess<K, V>[] partitions;

    /** Is the class initialized. */
    private boolean initialized = false;

    /** Sets default properties heading and group. */
    public PartitionedCacheAccess()
    {
        setPropertiesHeading( "PartitionedCacheAccess" );
        setPropertiesGroup( "cache" );
    }

    /**
     * Puts the value into the appropriate cache partition.
     * <p>
     * @param key key
     * @param object object
     * @throws CacheException on configuration problem
     */
    public void put( K key, V object )
        throws CacheException
    {
        if ( key == null || object == null )
        {
            log.warn( "Bad input key [" + key + "].  Cannot put null into the cache." );
            return;
        }
        ensureInit();

        int partition = getPartitionNumberForKey( key );
        try
        {
            partitions[partition].put( key, object );
        }
        catch ( CacheException e )
        {
            log.error( "Problem putting value for key [" + key + "] in cache [" + partitions[partition] + "]" );
            throw ( e );
        }
    }

    /**
     * Puts in cache if an item does not exist with the name in that region.
     * <p>
     * @param key
     * @param object
     * @throws CacheException
     */
    public void putSafe( K key, V object )
        throws CacheException
    {
        if ( key == null || object == null )
        {
            log.warn( "Bad input key [" + key + "].  Cannot putSafe null into the cache." );
        }
        ensureInit();

        int partition = getPartitionNumberForKey( key );
        partitions[partition].putSafe( key, object );
    }

    /**
     * Puts the value into the appropriate cache partition.
     * <p>
     * @param key key
     * @param object object
     * @param attr
     * @throws CacheException on configuration problem
     */
    public void put( K key, V object, IElementAttributes attr )
        throws CacheException
    {
        if ( key == null || object == null )
        {
            log.warn( "Bad input key [" + key + "].  Cannot put null into the cache." );
            return;
        }
        ensureInit();

        int partition = getPartitionNumberForKey( key );
        try
        {
            partitions[partition].put( key, object, attr );
        }
        catch ( CacheException e )
        {
            log.error( "Problem putting value for key [" + key + "] in cache [" + partitions[partition] + "]" );
            throw ( e );
        }
    }

    /**
     * Gets the object for the key from the desired partition.
     * <p>
     * @param key key
     * @return result, null if not found.
     */
    public V get( K key )
    {
        if ( key == null )
        {
            log.warn( "Input key is null." );
            return null;
        }
        try
        {
            ensureInit();
        }
        catch ( ConfigurationException e )
        {
            // TODO add exception to interface method.
            log.error( "Couldn't configure partioned access.", e );
            return null;
        }

        int partition = getPartitionNumberForKey( key );

        return partitions[partition].get( key );
    }

    /**
     * Gets the ICacheElement (the wrapped object) for the key from the desired partition.
     * <p>
     * @param key key
     * @return result, null if not found.
     */
    public ICacheElement getCacheElement( K key )
    {
        if ( key == null )
        {
            log.warn( "Input key is null." );
            return null;
        }
        try
        {
            ensureInit();
        }
        catch ( ConfigurationException e )
        {
            // TODO add exception to interface method.
            log.error( "Couldn't configure partioned access.", e );
            return null;
        }

        int partition = getPartitionNumberForKey( key );

        return partitions[partition].getCacheElement( key );
    }

    /**
     * This is a getMultiple. We try to group the keys so that we make as few calls as needed.
     * <p>
     * @param names
     * @return Map of keys to ICacheElement
     */
    public Map<K, ICacheElement> getCacheElements( Set<K> names )
    {
        if ( names == null )
        {
            log.warn( "Bad input names cannot be null." );
            return Collections.emptyMap();
        }

        Set<K>[] dividedNames = new Set[this.getNumberOfPartitions()];

        for (K key : names)
        {
            int partition = getPartitionNumberForKey( key );
            if ( dividedNames[partition] == null )
            {
                dividedNames[partition] = new HashSet<K>();
            }
            dividedNames[partition].add( key );
        }

        Map<K, ICacheElement> result = new HashMap<K, ICacheElement>();
        for ( int i = 0; i < partitions.length; i++ )
        {
            if ( dividedNames[i] != null && !dividedNames[i].isEmpty() )
            {
                result.putAll( partitions[i].getCacheElements( dividedNames[i] ) );
            }
        }
        return result;
    }

    /**
     * This is tricky. Do we need to get from all the partitions?
     * <p>
     * If this interface took an object, we could use the hashcode to determine the partition. Then
     * we could use the toString for the pattern.
     * <p>
     * @param pattern
     * @return HashMap key to value
     */
    public Map<K, V> getMatching( String pattern )
    {
        if ( pattern == null )
        {
            log.warn( "Input pattern is null." );
            return null;
        }
        try
        {
            ensureInit();
        }
        catch ( ConfigurationException e )
        {
            // TODO add exception to interface method.
            log.error( "Couldn't configure partioned access.", e );
            return null;
        }

        Map<K, V> result = new HashMap<K,V>();
        for ( int i = 0; i < partitions.length; i++ )
        {
            result.putAll( partitions[i].getMatching( pattern ) );
        }
        return result;
    }

    /**
     * This is tricky. Do we need to get from all the partitions?
     * <p>
     * @param pattern
     * @return HashMap key to ICacheElement
     */
    public Map<K, ICacheElement> getMatchingCacheElements( String pattern )
    {
        if ( pattern == null )
        {
            log.warn( "Input pattern is null." );
            return null;
        }
        try
        {
            ensureInit();
        }
        catch ( ConfigurationException e )
        {
            // TODO add exception to interface method.
            log.error( "Couldn't configure partioned access.", e );
            return null;
        }

        Map<K, ICacheElement> result = new HashMap<K, ICacheElement>();
        for ( int i = 0; i < partitions.length; i++ )
        {
            result.putAll( partitions[i].getMatchingCacheElements( pattern ) );
        }
        return result;
    }

    /**
     * Calls remove on all partitions. This gets translated into a removeAll call.
     * <p>
     * @throws CacheException
     */
    public void remove()
        throws CacheException
    {
        ensureInit();

        for ( int i = 0; i < partitions.length; i++ )
        {
            partitions[i].remove();
        }
    }

    /**
     * Removes the item from the appropriate partition.
     * <p>
     * @param key
     * @throws CacheException
     */
    public void remove( K key )
        throws CacheException
    {
        if ( key == null )
        {
            log.warn( "Input key is null. Cannot remove null from the cache." );
            return;
        }
        ensureInit();

        int partition = getPartitionNumberForKey( key );
        try
        {
            partitions[partition].remove( key );
        }
        catch ( CacheException e )
        {
            log.error( "Problem removing value for key [" + key + "] in cache [" + partitions[partition] + "]" );
            throw ( e );
        }
    }

    /**
     * Calls free on each partition.
     * <p>
     * @param numberToFree
     * @return number removed
     * @throws CacheException
     */
    public int freeMemoryElements( int numberToFree )
        throws CacheException
    {
        ensureInit();

        int count = 0;
        for ( int i = 0; i < partitions.length; i++ )
        {
            count += partitions[i].freeMemoryElements( numberToFree );
        }
        return count;
    }

    /**
     * @return ICompositeCacheAttributes from the first partition.
     */
    public ICompositeCacheAttributes getCacheAttributes()
    {
        try
        {
            ensureInit();
        }
        catch ( ConfigurationException e )
        {
            // TODO add exception to interface method.
            log.error( "Couldn't configure partioned access.", e );
            return null;
        }

        if ( partitions.length == 0 )
        {
            return null;
        }

        return partitions[0].getCacheAttributes();
    }

    /**
     * @return IElementAttributes from the first partition.
     * @throws CacheException
     */
    public IElementAttributes getElementAttributes()
        throws CacheException
    {
        ensureInit();

        if ( partitions.length == 0 )
        {
            return null;
        }

        return partitions[0].getElementAttributes();
    }

    /**
     * This is no more efficient than simply getting the cache element.
     * <p>
     * @param key
     * @return IElementAttributes
     * @throws CacheException
     */
    public IElementAttributes getElementAttributes( K key )
        throws CacheException
    {
        if ( key == null )
        {
            log.warn( "Input key is null. Cannot getElementAttributes for null from the cache." );
            return null;
        }
        ensureInit();

        int partition = getPartitionNumberForKey( key );

        return partitions[partition].getElementAttributes( key );
    }

    /**
     * Resets the default element attributes on all partitions. This does not change items that are
     * already in the cache.
     * <p>
     * @param attributes
     * @throws CacheException
     */
    public void resetElementAttributes( IElementAttributes attributes )
        throws CacheException
    {
        ensureInit();

        for ( int i = 0; i < partitions.length; i++ )
        {
            partitions[i].resetElementAttributes( attributes );
        }
    }

    /**
     * Resets the attributes for this item. This has the same effect as an update, in most cases.
     * None of the auxiliaries are optimized to do this more efficiently than a simply update.
     * <p>
     * @param key
     * @param attributes
     * @throws CacheException
     */
    public void resetElementAttributes( K key, IElementAttributes attributes )
        throws CacheException
    {
        if ( key == null )
        {
            log.warn( "Input key is null. Cannot resetElementAttributes for null." );
            return;
        }
        ensureInit();

        int partition = getPartitionNumberForKey( key );

        partitions[partition].resetElementAttributes( key, attributes );
    }

    /**
     * Sets the attributes on all the partitions.
     * <p>
     * @param cattr
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr )
    {
        try
        {
            ensureInit();
        }
        catch ( ConfigurationException e )
        {
            // TODO add exception to interface method.
            log.error( "Couldn't configure partioned access.", e );
            return;
        }

        for ( int i = 0; i < partitions.length; i++ )
        {
            partitions[i].setCacheAttributes( cattr );
        }
    }

    /**
     * This expects a numeric key. If the key cannot be converted into a number, we will return 0.
     * TODO we could md5 it or get the hashcode.
     * <p>
     * We determine the partition by taking the mod of the number of partitions.
     * <p>
     * @param key key
     * @return the partition number.
     */
    protected int getPartitionNumberForKey( K key )
    {
        if ( key == null )
        {
            return 0;
        }

        long keyNum = getNumericValueForKey( key );

        int partition = (int) ( keyNum % getNumberOfPartitions() );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Using partition [" + partition + "] for key [" + key + "]" );
        }

        return partition;
    }

    /**
     * This can be overridden for special purposes.
     * <p>
     * @param key key
     * @return long
     */
    public long getNumericValueForKey( K key )
    {
        String keyString = key.toString();
        long keyNum = -1;
        try
        {
            keyNum = Long.parseLong( keyString );
        }
        catch ( NumberFormatException e )
        {
            // THIS IS UGLY, but I can't think of a better failsafe right now.
            keyNum = key.hashCode();
            log.warn( "Couldn't convert [" + key + "] into a number.  Will use hashcode [" + keyNum + "]" );
        }
        return keyNum;
    }

    /**
     * Initialize if we haven't already.
     * <p>
     * @throws ConfigurationException on configuration problem
     */
    protected synchronized void ensureInit()
        throws ConfigurationException
    {
        if ( !initialized )
        {
            initialize();
        }
    }

    /**
     * Use the partition prefix and the number of partitions to get JCS regions.
     * <p>
     * @throws ConfigurationException on configuration problem
     */
    protected synchronized void initialize()
        throws ConfigurationException
    {
        ensureProperties();

        ICacheAccess<K, V>[] tempPartitions = new ICacheAccess[this.getNumberOfPartitions()];
        for ( int i = 0; i < this.getNumberOfPartitions(); i++ )
        {
            String regionName = this.getPartitionRegionNamePrefix() + "_" + i;
            try
            {
                tempPartitions[i] = (ICacheAccess<K, V>) CacheAccess.getAccess( regionName );
            }
            catch ( CacheException e )
            {
                log.error( "Problem getting cache for region [" + regionName + "]" );
            }
        }
        partitions = tempPartitions;
        initialized = true;
    }

    /**
     * Loads in the needed configuration settings. System properties are checked first. A system
     * property will override local property value.
     * <p>
     * Loads the following JCS Cache specific properties:
     * <ul>
     * <li>heading.numberOfPartitions</li>
     * <li>heading.partitionRegionNamePrefix</li>
     * </ul>
     * @throws ConfigurationException on configuration problem
     */
    @Override
    protected void handleProperties()
        throws ConfigurationException
    {
        // Number of Partitions.
        String numberOfPartitionsPropertyName = this.getPropertiesHeading() + ".numberOfPartitions";
        String numberOfPartitionsPropertyValue = getPropertyForName( numberOfPartitionsPropertyName, true );
        try
        {
            this.setNumberOfPartitions( Integer.parseInt( numberOfPartitionsPropertyValue ) );
        }
        catch ( NumberFormatException e )
        {
            String message = "Could not convert [" + numberOfPartitionsPropertyValue + "] into a number for ["
                + numberOfPartitionsPropertyName + "]";
            log.error( message );
            throw new ConfigurationException( message );
        }

        // Partition Name Prefix.
        String prefixPropertyName = this.getPropertiesHeading() + ".partitionRegionNamePrefix";
        String prefix = getPropertyForName( prefixPropertyName, true );
        this.setPartitionRegionNamePrefix( prefix );
    }

    /**
     * Checks the system properties before the properties.
     * <p>
     * @param propertyName name
     * @param required is it required?
     * @return the property value if one is found
     * @throws ConfigurationException thrown if it is required and not found.
     */
    protected String getPropertyForName( String propertyName, boolean required )
        throws ConfigurationException
    {
        String propertyValue = null;
        propertyValue = System.getProperty( propertyName );
        if ( propertyValue != null )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Found system property override: Name [" + propertyName + "] Value [" + propertyValue + "]" );
            }
        }
        else
        {
            propertyValue = this.getProperties().getProperty( propertyName );
            if ( required && propertyValue == null )
            {
                String message = "Could not find required property [" + propertyName + "] in propertiesGroup ["
                    + this.getPropertiesGroup() + "]";
                log.error( message );
                throw new ConfigurationException( message );
            }
            else
            {
                if ( log.isInfoEnabled() )
                {
                    log.info( "Name [" + propertyName + "] Value [" + propertyValue + "]" );
                }
            }
        }
        return propertyValue;
    }

    /**
     * @param numberOfPartitions The numberOfPartitions to set.
     */
    protected void setNumberOfPartitions( int numberOfPartitions )
    {
        this.numberOfPartitions = numberOfPartitions;
    }

    /**
     * @return Returns the numberOfPartitions.
     */
    protected int getNumberOfPartitions()
    {
        return numberOfPartitions;
    }

    /**
     * @param partitionRegionNamePrefix The partitionRegionNamePrefix to set.
     */
    protected void setPartitionRegionNamePrefix( String partitionRegionNamePrefix )
    {
        this.partitionRegionNamePrefix = partitionRegionNamePrefix;
    }

    /**
     * @return Returns the partitionRegionNamePrefix.
     */
    protected String getPartitionRegionNamePrefix()
    {
        return partitionRegionNamePrefix;
    }

    /**
     * @param partitions The partitions to set.
     */
    protected void setPartitions( ICacheAccess<K, V>[] partitions )
    {
        this.partitions = partitions;
    }

    /**
     * @return Returns the partitions.
     */
    protected ICacheAccess<K, V>[] getPartitions()
    {
        return partitions;
    }
}

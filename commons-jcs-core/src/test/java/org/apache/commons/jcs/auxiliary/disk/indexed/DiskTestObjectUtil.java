package org.apache.commons.jcs.auxiliary.disk.indexed;

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
import java.util.Random;

import org.apache.commons.jcs.auxiliary.disk.DiskTestObject;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.utils.serialization.StandardSerializer;

/**
 * Utility for dealing with test objects.
 * <p>
 * @author Aaron Smuts
 */
public class DiskTestObjectUtil
{
    /**
     * Total from the start to the endPostion.
     * <p>
     * @param testObjects
     * @param endPosition
     * @return size
     * @throws IOException
     */
    public static long totalSize( DiskTestObject[] testObjects, int endPosition )
        throws IOException
    {
        StandardSerializer serializer = new StandardSerializer();
        long total = 0;
        for ( int i = 0; i < endPosition; i++ )
        {
            int tileSize = serializer.serialize( testObjects[i] ).length + IndexedDisk.HEADER_SIZE_BYTES;
            total += tileSize;
        }
        return total;
    }

    /**
     * Total from the start to the endPostion.
     * <p>
     * @param elements
     * @param endPosition
     * @return size
     * @throws IOException
     */
    public static <K, V> long totalSize( ICacheElement<K, V>[] elements, int endPosition )
        throws IOException
    {
        return totalSize( elements, 0, endPosition );
    }

    /**
     * Total from the start to the endPostion.
     * <p>
     * @param elements
     * @param startPosition
     * @param endPosition
     * @return size
     * @throws IOException
     */
    public static <K, V> long totalSize( ICacheElement<K, V>[] elements, int startPosition, int endPosition )
        throws IOException
    {
        StandardSerializer serializer = new StandardSerializer();
        long total = 0;
        for ( int i = startPosition; i < endPosition; i++ )
        {
            int tileSize = serializer.serialize( elements[i] ).length + IndexedDisk.HEADER_SIZE_BYTES;
            total += tileSize;
        }
        return total;
    }

    /**
     * Creates an array of ICacheElements with DiskTestObjects with payloads the byte size.
     * <p>
     * @param numToCreate
     * @param bytes
     * @param cacheName
     * @return ICacheElement[]
     */
    public static ICacheElement<Integer, DiskTestObject>[] createCacheElementsWithTestObjects( int numToCreate, int bytes, String cacheName )
    {
        @SuppressWarnings("unchecked")
        ICacheElement<Integer, DiskTestObject>[] elements = new ICacheElement[numToCreate];
        for ( int i = 0; i < numToCreate; i++ )
        {
            // 24 KB
            int size = bytes * 1024;
            DiskTestObject tile = new DiskTestObject( Integer.valueOf( i ), new byte[size] );

            ICacheElement<Integer, DiskTestObject> element = new CacheElement<Integer, DiskTestObject>( cacheName, tile.id, tile );
            elements[i] = element;
        }
        return elements;
    }

    /**
     * Creates an array of ICacheElements with DiskTestObjects with payloads the byte size.
     * <p>
     * @param numToCreate
     * @param cacheName
     * @return ICacheElement[]
     */
    public static ICacheElement<Integer, DiskTestObject>[] createCacheElementsWithTestObjectsOfVariableSizes( int numToCreate, String cacheName )
    {
        @SuppressWarnings("unchecked")
        ICacheElement<Integer, DiskTestObject>[] elements = new ICacheElement[numToCreate];
        Random random = new Random( 89 );
        for ( int i = 0; i < numToCreate; i++ )
        {
            int bytes = random.nextInt( 20 );
            // 4-24 KB
            int size = ( bytes + 4 ) * 1024;
            DiskTestObject tile = new DiskTestObject( Integer.valueOf( i ), new byte[size] );

            ICacheElement<Integer, DiskTestObject> element = new CacheElement<Integer, DiskTestObject>( cacheName, tile.id, tile );
            elements[i] = element;
        }
        return elements;
    }

}

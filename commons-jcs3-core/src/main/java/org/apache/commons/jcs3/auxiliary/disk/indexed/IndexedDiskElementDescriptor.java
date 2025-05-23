package org.apache.commons.jcs3.auxiliary.disk.indexed;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.Serializable;

/**
 * Disk objects are located by descriptor entries. These are saved on shutdown and loaded into
 * memory on startup.
 */
public class IndexedDiskElementDescriptor
    implements Serializable, Comparable<IndexedDiskElementDescriptor>
{
    /** Don't change */
    private static final long serialVersionUID = -3029163572847659450L;

    /** Position of the cache data entry on disk. */
    long pos;

    /** Number of bytes the serialized form of the cache data takes. */
    int len;

    /**
     * Constructs a usable disk element descriptor.
     *
     * @param pos
     * @param len
     */
    public IndexedDiskElementDescriptor( final long pos, final int len )
    {
        this.pos = pos;
        this.len = len;
    }

    /**
     * Compares based on length, then on pos descending.
     *
     * @param o Object
     * @return int
     */
    @Override
    public int compareTo( final IndexedDiskElementDescriptor o )
    {
        if ( o == null )
        {
            return 1;
        }

        final int lenCompare = Integer.compare(len, o.len);
        if (lenCompare == 0)
        {
            return Long.compare(o.pos, pos);
        }

        return lenCompare;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(final Object o)
    {
    	if (o == null)
    	{
    		return false;
    	}
        if (o instanceof IndexedDiskElementDescriptor)
        {
    		final IndexedDiskElementDescriptor ided = (IndexedDiskElementDescriptor)o;
            return pos == ided.pos && len == ided.len;
        }

        return false;
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Long.valueOf(this.pos).hashCode() ^ Integer.valueOf(len).hashCode();
    }

    /**
     * @return debug string
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "[DED: " );
        buf.append( " pos = " + pos );
        buf.append( " len = " + len );
        buf.append( "]" );
        return buf.toString();
    }
}

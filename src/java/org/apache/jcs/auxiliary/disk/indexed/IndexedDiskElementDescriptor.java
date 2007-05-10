package org.apache.jcs.auxiliary.disk.indexed;

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

import java.io.Serializable;

/**
 * Disk objects are located by descriptor entries. These are saved on shutdown and loaded into
 * memory on startup.
 */
public class IndexedDiskElementDescriptor
    implements Serializable, Comparable
{
    private static final long serialVersionUID = -3029163572847659450L;

    /** Position of the cache data entry on disk. */
    long pos;

    /** Number of bytes the serialized form of the cache data takes. */
    public int len;

    /**
     * Set the offset (i.e. position, and the size of the element)
     * <p>
     * @param pos
     * @param data
     */
    public void init( long pos, byte[] data )
    {
        this.pos = pos;
        this.len = data.length;
    }

    /** Constructor for the DiskElementDescriptor object */
    public IndexedDiskElementDescriptor()
    {
        super();
    }

    /**
     * Constructs a usable disk element descriptor.
     * <p>
     * @param pos
     * @param len
     */
    public IndexedDiskElementDescriptor( long pos, int len )
    {
        this.pos = pos;
        this.len = len;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "[DED: " );
        buf.append( " pos = " + pos );
        buf.append( " len = " + len );
        buf.append( "]" );
        return buf.toString();
    }

    /**
     * Compares based on length.
     * <p>
     * @param o Object
     * @return int
     */
    public int compareTo( Object o )
    {
        if ( o == null )
        {
            return 1;
        }

        int oLen = ( (IndexedDiskElementDescriptor) o ).len;
        if ( oLen == len )
        {
            return 0;
        }
        else if ( oLen > len )
        {
            return -1;
        }
        else if ( oLen < len )
        {
            return 1;
        }
        return 0;
    }
}

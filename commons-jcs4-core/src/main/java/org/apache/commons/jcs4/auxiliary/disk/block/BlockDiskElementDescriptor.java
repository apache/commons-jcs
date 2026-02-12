package org.apache.commons.jcs4.auxiliary.disk.block;

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
import java.util.Arrays;

/**
 * This represents an element on disk. This is used when we persist the keys. We only store the
 * block addresses in memory. We don't need the length here, since all the blocks are the same size
 * recycle bin.
 */
public record BlockDiskElementDescriptor<K>(
        /** The key */
        K key,

        /** The array of block numbers */
        int[] blocks
) implements Serializable
{
    /** Don't change */
    private static final long serialVersionUID = -1400659301208101411L;

    /**
     * For debugging.
     *
     * @return Info on the descriptor.
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append("\nBlockDiskElementDescriptor" );
        buf.append("\n key [").append(this.key()).append("]");
        buf.append("\n blocks [" );
        if ( this.blocks() != null )
        {
            Arrays.stream(this.blocks()).forEach(buf::append);
        }
        buf.append("]");
        return buf.toString();
    }
}

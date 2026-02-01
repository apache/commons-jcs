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

import org.apache.commons.jcs4.auxiliary.disk.AbstractDiskCacheAttributes;

/**
 * This holds attributes for Block Disk Cache configuration.
 */
public class BlockDiskCacheAttributes
    extends AbstractDiskCacheAttributes
{
    /** Don't change */
    private static final long serialVersionUID = 6568840097657265989L;

    /** Maximum number of keys to be kept in memory */
    private static final int DEFAULT_MAX_KEY_SIZE = 5000;

    /** How often should we persist the keys. */
    private static final long DEFAULT_KEY_PERSISTENCE_INTERVAL_SECONDS = 5 * 60;

    /** The size per block in bytes. */
    private int blockSizeBytes;

    /** -1 means no limit. */
    private int maxKeySize = DEFAULT_MAX_KEY_SIZE;

    /** The keys will be persisted at this interval.  -1 mean never. */
    private long keyPersistenceIntervalSeconds = DEFAULT_KEY_PERSISTENCE_INTERVAL_SECONDS;

    /**
     * @return the blockSizeBytes.
     */
    public int getBlockSizeBytes()
    {
        return blockSizeBytes;
    }

    /**
     * @return the keyPersistenceIntervalSeconds.
     */
    public long getKeyPersistenceIntervalSeconds()
    {
        return keyPersistenceIntervalSeconds;
    }

    /**
     * @return the maxKeySize.
     */
    public int getMaxKeySize()
    {
        return maxKeySize;
    }

    /**
     * The size of the blocks. All blocks are the same size.
     *
     * @param blockSizeBytes The blockSizeBytes to set.
     */
    public void setBlockSizeBytes( final int blockSizeBytes )
    {
        this.blockSizeBytes = blockSizeBytes;
    }

    /**
     * @param keyPersistenceIntervalSeconds The keyPersistenceIntervalSeconds to set.
     */
    public void setKeyPersistenceIntervalSeconds( final long keyPersistenceIntervalSeconds )
    {
        this.keyPersistenceIntervalSeconds = keyPersistenceIntervalSeconds;
    }

    /**
     * @param maxKeySize The maxKeySize to set.
     */
    public void setMaxKeySize( final int maxKeySize )
    {
        this.maxKeySize = maxKeySize;
    }

    /**
     * Converts this instance to a String for debugging purposes.
     *
     * @return This instance to a String for debugging purposes.
     */
    @Override
    public String toString()
    {
        final StringBuilder str = new StringBuilder();
        str.append( "\nBlockDiskAttributes " );
        str.append( "\n DiskPath [" + getDiskPath() + "]" );
        str.append( "\n MaxKeySize [" + getMaxKeySize() + "]" );
        str.append( "\n MaxPurgatorySize [" + getMaxPurgatorySize() + "]" );
        str.append( "\n BlockSizeBytes [" + getBlockSizeBytes() + "]" );
        str.append( "\n KeyPersistenceIntervalSeconds [" + getKeyPersistenceIntervalSeconds() + "]" );
        str.append( "\n DiskLimitType [" + getDiskLimitType() + "]" );
        return str.toString();
    }
}

package org.apache.jcs.auxiliary.disk.block;

import org.apache.jcs.auxiliary.disk.AbstractDiskCacheAttributes;

/**
 * This holds attributes for Block Disk Cache configuration.
 * <p>
 * @author Aaron Smuts
 */
public class BlockDiskCacheAttributes
    extends AbstractDiskCacheAttributes
{
    private static final long serialVersionUID = 6568840097657265989L;

    private int blockSizeBytes;

    private static final int DEFAULT_MAX_KEY_SIZE = 5000;

    /** -1 means no limit. */
    private int maxKeySize = DEFAULT_MAX_KEY_SIZE;
    
    private static final long DEFAULT_KEY_PERSISTENCE_INTERVAL_SECONDS = 5 * 60;
    
    /** The keys will be persisted at this interval.  -1 mean never. */
    private long keyPersistenceIntervalSeconds = DEFAULT_KEY_PERSISTENCE_INTERVAL_SECONDS;
    
    
    /**
     * The size of the blocks. All blocks are the same size.
     * <p>
     * @param blockSizeBytes The blockSizeBytes to set.
     */
    public void setBlockSizeBytes( int blockSizeBytes )
    {
        this.blockSizeBytes = blockSizeBytes;
    }

    /**
     * @return Returns the blockSizeBytes.
     */
    public int getBlockSizeBytes()
    {
        return blockSizeBytes;
    }

    /**
     * @param maxKeySize The maxKeySize to set.
     */
    public void setMaxKeySize( int maxKeySize )
    {
        this.maxKeySize = maxKeySize;
    }

    /**
     * @return Returns the maxKeySize.
     */
    public int getMaxKeySize()
    {
        return maxKeySize;
    }

    /**
     * @param keyPersistenceIntervalSeconds The keyPersistenceIntervalSeconds to set.
     */
    public void setKeyPersistenceIntervalSeconds( long keyPersistenceIntervalSeconds )
    {
        this.keyPersistenceIntervalSeconds = keyPersistenceIntervalSeconds;
    }

    /**
     * @return Returns the keyPersistenceIntervalSeconds.
     */
    public long getKeyPersistenceIntervalSeconds()
    {
        return keyPersistenceIntervalSeconds;
    }

    /**
     * Write out the values for debugging purposes.
     * <p>
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append( "\nBlockDiskAttributes " );
        str.append( "\n DiskPath [" + this.getDiskPath() + "]" );
        str.append( "\n MaxKeySize [" + this.getMaxKeySize() + "]" );
        str.append( "\n MaxPurgatorySize [" + this.getMaxPurgatorySize() + "]" );
        str.append( "\n BlockSizeBytes [" + this.getBlockSizeBytes() + "]" );
        str.append( "\n KeyPersistenceIntervalSeconds [" + this.getKeyPersistenceIntervalSeconds() + "]" );
        return str.toString();
    }
}

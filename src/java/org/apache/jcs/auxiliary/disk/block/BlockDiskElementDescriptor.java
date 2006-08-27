package org.apache.jcs.auxiliary.disk.block;

/*
 * Copyright 2001-2004 The Apache Software Foundation. Licensed under the Apache
 * License, Version 2.0 (the "License") you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import java.io.Serializable;

/**
 * This represents an element on disk. It must be kept small since these are stored in memory. We
 * store the key and then offset in memory. That's all. We don't need the length, since we don't
 * have a receyle bin.
 * <p>
 * @author Aaron Smuts
 */
public class BlockDiskElementDescriptor
    implements Serializable
{
    private static final long serialVersionUID = -1400659301208101411L;

    private Serializable key;

    private int[] blocks;

    /**
     * @param key The key to set.
     */
    public void setKey( Serializable key )
    {
        this.key = key;
    }

    /**
     * @return Returns the key.
     */
    public Serializable getKey()
    {
        return key;
    }

    /**
     * @param blocks The blocks to set.
     */
    public void setBlocks( int[] blocks )
    {
        this.blocks = blocks;
    }

    /**
     * This holds the block numbers. An item my be dispersed between multiple blocks.
     * <p>
     * @return Returns the blocks.
     */
    public int[] getBlocks()
    {
        return blocks;
    }

    /**
     * For debugging.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\nBlockDiskElementDescriptor" );
        buf.append( "\n key [" + this.getKey() + "]" );
        buf.append( "\n blocks [" );
        if ( this.getBlocks() != null )
        {
            for ( int i = 0; i < blocks.length; i++ )
            {
                buf.append( this.getBlocks()[i] );
            }
        }
        buf.append( "]" );
        return buf.toString();
    }
}

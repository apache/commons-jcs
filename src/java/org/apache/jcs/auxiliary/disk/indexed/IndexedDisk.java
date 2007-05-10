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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.utils.serialization.StandardSerializer;

/**
 * Provides thread safe access to the underlying random access file.
 */
class IndexedDisk
{
    /**
     * The size of the header in bytes. The header describes the length of the entry.
     */
    public static final int RECORD_HEADER = 4;

    private static final StandardSerializer SERIALIZER = new StandardSerializer();

    private static final Log log = LogFactory.getLog( IndexedDisk.class );

    private final String filepath;

    private RandomAccessFile raf;

    private final byte[] buffer = new byte[16384]; // 16K

    /**
     * Constructor for the Disk object
     * <p>
     * @param file
     * @exception FileNotFoundException
     */
    public IndexedDisk( File file )
        throws FileNotFoundException
    {
        this.filepath = file.getAbsolutePath();
        raf = new RandomAccessFile( filepath, "rw" );
    }

    /**
     * This reads an object from the given starting position on the file.
     * <p>
     * The first four bytes of the record should tell us how long it is. The data is read into a byte
     * array and then an object is constructed from the byte array.
     * <p>
     * @return Serializable
     * @param ded
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected Serializable readObject( IndexedDiskElementDescriptor ded )
        throws IOException, ClassNotFoundException
    {
        byte[] data = null;
        synchronized ( this )
        {
            String message = null;
            boolean corrupted = false;
            long fileLength = raf.length();
            if ( ded.pos > fileLength )
            {
                corrupted = true;
                message = "Record " + ded + " starts past EOF.";
            }
            else
            {
                raf.seek( ded.pos );
                int datalen = raf.readInt();
                if ( ded.len != datalen )
                {
                    corrupted = true;
                    message = "Record " + ded + " does not match data length on disk (" + datalen + ")";
                }
                else if ( ded.pos + ded.len > fileLength )
                {
                    corrupted = true;
                    message = "Record " + ded + " exceeds file length.";
                }
            }

            if ( corrupted )
            {
                log.warn( "\n The file is corrupt: " + "\n " + message );
                throw new IOException( "The File Is Corrupt, need to reset" );
            }

            raf.readFully( data = new byte[ded.len] );
        }

        return (Serializable) SERIALIZER.deSerialize( data );
    }

    /**
     * Moves the data stored from one position to another. The descriptor's position is updated.
     * <p>
     * @param ded
     * @param newPosition
     * @throws IOException
     */
    protected void move( final IndexedDiskElementDescriptor ded, final long newPosition )
        throws IOException
    {
        synchronized ( this )
        {
            raf.seek( ded.pos );
            int length = raf.readInt();

            if ( length != ded.len )
            {
                throw new IOException( "Mismatched memory and disk length (" + length + ") for " + ded );
            }

            // TODO: more checks?

            long readPos = ded.pos;
            long writePos = newPosition;

            // header len + data len
            int remaining = RECORD_HEADER + length;

            while ( remaining > 0 )
            {
                // chunk it
                int chunkSize = Math.min( remaining, buffer.length );
                raf.seek( readPos );
                raf.readFully( buffer, 0, chunkSize );

                raf.seek( writePos );
                raf.write( buffer, 0, chunkSize );

                writePos += chunkSize;
                readPos += chunkSize;
                remaining -= chunkSize;
            }

            ded.pos = newPosition;
        }
    }

    /**
     * Writes the given byte array to the Disk at the specified position.
     * <p>
     * @param data
     * @param ded
     * @return true if we wrote successfully
     * @throws IOException
     */
    protected boolean write( IndexedDiskElementDescriptor ded, byte[] data )
        throws IOException
    {
        long pos = ded.pos;
        if ( log.isTraceEnabled() )
        {
            log.trace( "write> pos=" + pos );
            log.trace( raf + " -- data.length = " + data.length );
        }

        if ( data.length != ded.len )
        {
            throw new IOException( "Mismatched descriptor and data lengths" );
        }

        synchronized ( this )
        {
            raf.seek( pos );
            raf.writeInt( data.length );
            raf.write( data, 0, ded.len );
        }
        return true;
    }

    /**
     * Serializes the object and write it out to the given position.
     * <p>
     * TODO: make this take a ded as well.
     * @return
     * @param obj
     * @param pos
     * @throws IOException
     */
    protected boolean writeObject( Serializable obj, long pos )
        throws IOException
    {
        byte[] data = SERIALIZER.serialize( obj );
        write( new IndexedDiskElementDescriptor( pos, data.length ), data );
        return true;
    }

    /**
     * Returns the raf length.
     * <p>
     * @return
     * @exception IOException
     */
    protected long length()
        throws IOException
    {
        synchronized ( this )
        {
            return raf.length();
        }
    }

    /**
     * Closes the raf.
     * <p>
     * @exception IOException
     */
    protected synchronized void close()
        throws IOException
    {
        raf.close();
    }

    /**
     * Sets the raf to empty.
     * <p>
     * @exception IOException
     */
    protected synchronized void reset()
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Resetting Indexed File [" + filepath + "]" );
        }
        raf.close();
        File f = new File( filepath );
        int i = 0;
        for ( ; i < 10 && !f.delete(); i++ )
        {
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ex )
            {
                // swallow
            }
            log.warn( "Failed to delete " + f.getName() + " " + i );
        }
        if ( i == 10 )
        {
            IllegalStateException ex = new IllegalStateException( "Failed to delete " + f.getName() );
            log.error( ex );
            throw ex;
        }
        raf = new RandomAccessFile( filepath, "rw" );
    }

    /**
     * Returns the serialized form of the given object in a byte array.
     * <p>
     * Use the Serilizer abstraction layer.
     * <p>
     * @return a byte array of the serialized object.
     * @param obj
     * @exception IOException
     */
    protected static byte[] serialize( Serializable obj )
        throws IOException
    {
        return SERIALIZER.serialize( obj );
    }

    /**
     * Truncates the file to a given length.
     * <p>
     * @param length the new length of the file
     * @throws IOException
     */
    protected void truncate( long length )
        throws IOException
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "Trucating file [" + filepath + "] to " + length );
        }
        raf.setLength( length );
    }
}

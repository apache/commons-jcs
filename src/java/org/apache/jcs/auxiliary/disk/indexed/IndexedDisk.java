package org.apache.jcs.auxiliary.disk.indexed;

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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.CacheElement;

/**
 * Provides thread safe access to the underlying random access file.
 */
class IndexedDisk
{
    private static final Log log = LogFactory.getLog( IndexedDisk.class );

    private final String filepath;

    private RandomAccessFile raf;

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
     * The firt four bytes of the record should tell us how long it is. The data
     * is read into a byte array and then an object is constructed from the byte
     * array.
     * <p>
     * @return Serializable
     * @param pos
     * @throws IOException
     */
    protected Serializable readObject( long pos )
        throws IOException
    {
        String message = null;
        byte[] data = null;
        boolean corrupted = false;
        try
        {
            synchronized ( this )
            {
                if ( pos > raf.length() )
                {
                    corrupted = true;
                    message = "Postion is greater than raf length";
                }
                else
                {
                    raf.seek( pos );
                    int datalen = raf.readInt();
                    if ( datalen > raf.length() )
                    {
                        corrupted = true;
                        message = "Postion(" + pos + ") + datalen (" + datalen + ") is greater than raf length";
                    }
                    else
                    {
                        raf.readFully( data = new byte[datalen] );
                    }
                }
            }
            if ( corrupted )
            {
                log.warn( "\n The dataFile is corrupted!" + "\n " + message + "\n raf.length() = " + raf.length()
                    + "\n pos = " + pos );
                // reset();
                throw new IOException( "The Data File Is Corrupt, need to reset" );
                // return null;
            }
            ByteArrayInputStream bais = new ByteArrayInputStream( data );
            BufferedInputStream bis = new BufferedInputStream( bais );
            ObjectInputStream ois = new ObjectInputStream( bis );
            try
            {
                return (Serializable) ois.readObject();
            }
            finally
            {
                ois.close();
            }
        }
        catch ( Exception e )
        {
            log.error( raf, e );
            if ( e instanceof IOException )
            {
                throw (IOException) e;
            }
        }
        return null;
    }

    /**
     * Appends byte array to the Disk.
     * <p>
     * @return
     * @param data
     */
    protected boolean append( byte[] data )
    {
        try
        {
            synchronized ( this )
            {
                return write( data, raf.length() );
            }
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Writes the given byte array to the Disk at the specified position.
     * <p>
     * @param data
     * @param pos
     * @return true if we wrote successfully
     */
    protected boolean write( byte[] data, long pos )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "write> pos=" + pos );
            log.debug( raf + " -- data.length = " + data.length );
        }
        try
        {
            synchronized ( this )
            {
                raf.seek( pos );
                raf.writeInt( data.length );
                raf.write( data );
            }
            return true;
        }
        catch ( IOException ex )
        {
            log.error( "Problem writing object to disk.", ex );
        }
        return false;
    }

    /**
     * Serializes the object and write it out to the given position.
     * <p>
     * @return
     * @param obj
     * @param pos
     */
    protected boolean writeObject( Serializable obj, long pos )
    {
        try
        {
            return write( serialize( obj ), pos );
        }
        catch ( IOException ex )
        {
            log.error( "Problem writing object to disk.", ex );
        }
        return false;
    }

    /**
     * Writes an object to the end of the file.
     * <p>
     * @return
     * @param obj
     */
    protected IndexedDiskElementDescriptor appendObject( CacheElement obj )
    {
        long pos = -1;
        boolean success = false;
        try
        {
            IndexedDiskElementDescriptor ded = new IndexedDiskElementDescriptor();
            byte[] data = serialize( obj );

            synchronized ( this )
            {
                pos = raf.length();
                ded.init( pos, data );
                success = write( data, pos );
            }
            // return success ? new DiskElement(pos, data) : null;
            return success ? ded : null;
        }
        catch ( IOException ex )
        {
            log.error( "Problem writing object to disk.", ex );
        }
        return null;
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
        log.warn( "Resetting data file" );
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        try
        {
            oos.writeObject( obj );
        }
        finally
        {
            oos.close();
        }
        return baos.toByteArray();
    }
}

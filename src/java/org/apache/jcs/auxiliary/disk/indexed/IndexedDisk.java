package org.apache.jcs.auxiliary.disk.indexed;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself,
 * if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 * nor may "Apache" appear in their names without prior written
 * permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

import org.apache.jcs.engine.CacheElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides thread safe access to the underlying random access file.
 *
 * @author asmuts
 * @created January 15, 2002
 */
class IndexedDisk
{
    private final static Log log =
        LogFactory.getLog( IndexedDisk.class );

    private final String filepath;

    private RandomAccessFile raf;


    /**
     * Constructor for the Disk object
     *
     * @param file
     * @exception FileNotFoundException
     */
    IndexedDisk( File file )
        throws FileNotFoundException
    {
        this.filepath = file.getAbsolutePath();
        raf = new RandomAccessFile( filepath, "rw" );
    }


    /**
     * Description of the Method
     *
     * @return
     * @param pos
     */
    Serializable readObject( long pos )
    {
        byte[] data = null;
        boolean corrupted = false;
        try
        {
            synchronized ( this )
            {
                raf.seek( pos );
                int datalen = raf.readInt();
                if ( datalen > raf.length() )
                {
                    corrupted = true;
                }
                else
                {
                    raf.readFully( data = new byte[datalen] );
                }
            }
            if ( corrupted )
            {
                log.debug( "The datFile is corrupted" );
                //reset();
                return null;
            }
            ByteArrayInputStream bais = new ByteArrayInputStream( data );
            BufferedInputStream bis = new BufferedInputStream( bais );
            ObjectInputStream ois = new ObjectInputStream( bis );
            try
            {
                return ( Serializable ) ois.readObject();
            }
            finally
            {
                ois.close();
            }
        }
        catch ( Exception e )
        {
            log.error( raf, e );
        }
        return null;
    }


    /**
     * Appends byte array to the Disk.
     *
     * @return
     * @param data
     */
    boolean append( byte[] data )
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
     *
     * @return
     * @param data
     * @param pos
     */
    boolean write( byte[] data, long pos )
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
            ex.printStackTrace();
        }
        return false;
    }


    /**
     * Description of the Method
     *
     * @return
     * @param obj
     * @param pos
     */
    boolean writeObject( Serializable obj, long pos )
    {
        try
        {
            return write( serialize( obj ), pos );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
        return false;
    }


    /**
     * Description of the Method
     *
     * @return
     * @param obj
     */
    IndexedDiskElementDescriptor appendObject( CacheElement obj )
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
            //return  success ? new DiskElement(pos, data) : null;
            return success ? ded : null;
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
        return null;
    }


    /**
     * Returns the raf length.
     *
     * @return
     * @exception IOException
     */
    long length()
        throws IOException
    {
        synchronized ( this )
        {
            return raf.length();
        }
    }


    /**
     * Closes the raf.
     *
     * @exception IOException
     */
    synchronized void close()
        throws IOException
    {
        raf.close();
        return;
    }


    /**
     * Sets the raf to empty.
     *
     * @exception IOException
     */
    synchronized void reset()
        throws IOException
    {
        raf.close();
        File f = new File( filepath );
        int i = 0;
        for ( ; i < 10 && !f.delete(); i++ )
        {
            try
            {
                Thread.currentThread().sleep( 1000 );
            }
            catch ( InterruptedException ex )
            {
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
        return;
    }


    /**
     * Returns the serialized form of the given object in a byte array.
     *
     * @return
     * @param obj
     * @exception IOException
     */
    static byte[] serialize( Serializable obj )
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


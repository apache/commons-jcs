package org.apache.jcs.engine.behavior;

/* ====================================================================
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
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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

/**
 * Description of the Interface
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IAttributes
{

//    // remove
//    //static int DISTRIBUTE = 1; // lateral
//    static int LATERAL = 1;
//    // lateral
//    //static int NOFLUSH = 2;
//    //static int REPLY = 3;
//    //static int SYNCHRONIZE = 4;
//    static int SPOOL = 5;
//    //static int GROUP_TTL_DESTROY = 6;
//    //static int ORIGINAL = 7;
//    static int REMOTE = 8;
//    // central rmi store
//    static int ETERNAL = 8;

    /**
     * Sets the version attribute of the IAttributes object
     *
     * @param version The new version value
     */
    public void setVersion( long version );


    /**
     * Sets the timeToLive attribute of the IAttributes object
     *
     * @param ttl The new timeToLive value
     */
    public void setTimeToLive( long ttl );


//    /**
//     * Sets the defaultTimeToLive attribute of the IAttributes object
//     *
//     * @param ttl The new defaultTimeToLive value
//     */
//    public void setDefaultTimeToLive( long ttl );


    /**
     * Sets the idleTime attribute of the IAttributes object
     *
     * @param idle The new idleTime value
     */
    public void setIdleTime( long idle );


    //public void setListener( int event, CacheEventListener listerner) {}

    /**
     * Size in bytes.
     *
     * @param size The new size value
     */
    public void setSize( int size );


    /**
     * Gets the size attribute of the IAttributes object
     *
     * @return The size value
     */
    public int getSize();

    /**
     * Gets the createTime attribute of the IAttributes object
     *
     * @return The createTime value
     */
    public long getCreateTime();


    //public CacheLoader getLoader( );

    /**
     * Gets the version attribute of the IAttributes object
     *
     * @return The version value
     */
    public long getVersion();


    /**
     * Gets the idleTime attribute of the IAttributes object
     *
     * @return The idleTime value
     */
    public long getIdleTime();


    /**
     * Gets the timeToLive attribute of the IAttributes object
     *
     * @return The timeToLive value
     */
    public long getTimeToLive();


//    /** Description of the Method */
//    public long timeToSeconds( int days, int hours, int minutes, int seconds )
//        throws InvalidArgumentException;

}

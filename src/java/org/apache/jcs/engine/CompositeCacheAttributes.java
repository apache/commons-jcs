package org.apache.jcs.engine;

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

import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class CompositeCacheAttributes implements ICompositeCacheAttributes, Cloneable
{

    // Allows for programmatic stopping of configuration information.  Shouldn't use.
    // cannot turn on service if it is not set in props.  Only stop.
    private boolean useLateral = true;
    private boolean useRemote = true;
    private boolean useDisk = true;

    private boolean useMemoryShrinker = false;

    private int maxObjs = 100;

    /**
     * maxMemoryIdleTimeSeconds
     */
    protected long maxMemoryIdleTimeSeconds = 60 * 120; // 2 hours
    /**
     * shrinkerIntervalSeconds
     */
    protected long shrinkerIntervalSeconds = 30;

    private String cacheName;

    private String memoryCacheName;


    /**
     * Constructor for the CompositeCacheAttributes object
     */
    public CompositeCacheAttributes()
    {
        // set this as the default so the configuration is a bit simpler
        memoryCacheName = "org.apache.jcs.engine.memory.lru.LRUMemoryCache";
    }


    /**
     * Sets the maxObjects attribute of the CompositeCacheAttributes object
     *
     * @param maxObjs The new maxObjects value
     */
    public void setMaxObjects( int maxObjs )
    {
        this.maxObjs = maxObjs;
    }


    /**
     * Gets the maxObjects attribute of the CompositeCacheAttributes object
     *
     * @return The maxObjects value
     */
    public int getMaxObjects()
    {
        return this.maxObjs;
    }


    /**
     * Sets the useDisk attribute of the CompositeCacheAttributes object
     *
     * @param useDisk The new useDisk value
     */
    public void setUseDisk( boolean useDisk )
    {
        this.useDisk = useDisk;
    }


    /**
     * Gets the useDisk attribute of the CompositeCacheAttributes object
     *
     * @return The useDisk value
     */
    public boolean getUseDisk()
    {
        return useDisk;
    }


    /**
     * Sets the useLateral attribute of the CompositeCacheAttributes object
     *
     * @param b The new useLateral value
     */
    public void setUseLateral( boolean b )
    {
        this.useLateral = b;
    }


    /**
     * Gets the useLateral attribute of the CompositeCacheAttributes object
     *
     * @return The useLateral value
     */
    public boolean getUseLateral()
    {
        return this.useLateral;
    }


    /**
     * Sets the useRemote attribute of the CompositeCacheAttributes object
     *
     * @param useRemote The new useRemote value
     */
    public void setUseRemote( boolean useRemote )
    {
        this.useRemote = useRemote;
    }


    /**
     * Gets the useRemote attribute of the CompositeCacheAttributes object
     *
     * @return The useRemote value
     */
    public boolean getUseRemote()
    {
        return this.useRemote;
    }


    /**
     * Sets the cacheName attribute of the CompositeCacheAttributes object
     *
     * @param s The new cacheName value
     */
    public void setCacheName( String s )
    {
        this.cacheName = s;
    }


    /**
     * Gets the cacheName attribute of the CompositeCacheAttributes object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return this.cacheName;
    }


    /**
     * Sets the memoryCacheName attribute of the CompositeCacheAttributes object
     *
     * @param s The new memoryCacheName value
     */
    public void setMemoryCacheName( String s )
    {
        this.memoryCacheName = s;
    }


    /**
     * Gets the memoryCacheName attribute of the CompositeCacheAttributes object
     *
     * @return The memoryCacheName value
     */
    public String getMemoryCacheName()
    {
        return this.memoryCacheName;
    }


    /**
     * Whether the memory cache should perform background memory shrinkage.
     *
     * @param useShrinker The new UseMemoryShrinker value
     */
    public void setUseMemoryShrinker( boolean useShrinker )
    {
        this.useMemoryShrinker = useShrinker;
    }

    /**
     * Whether the memory cache should perform background memory shrinkage.
     *
     * @return The UseMemoryShrinker value
     */
    public boolean getUseMemoryShrinker()
    {
        return this.useMemoryShrinker;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space.
     *
     * @param seconds The new MaxMemoryIdleTimeSeconds value
     */
    public void setMaxMemoryIdleTimeSeconds( long seconds )
    {
        this.maxMemoryIdleTimeSeconds = seconds;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space.
     *
     * @return The MaxMemoryIdleTimeSeconds value
     */
    public long getMaxMemoryIdleTimeSeconds()
    {
        return this.maxMemoryIdleTimeSeconds;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This sets the shrinker interval.
     *
     * @param seconds The new ShrinkerIntervalSeconds value
     */
    public void setShrinkerIntervalSeconds( long seconds )
    {
        this.shrinkerIntervalSeconds = seconds;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This gets the shrinker interval.
     *
     * @return The ShrinkerIntervalSeconds value
     */
    public long getShrinkerIntervalSeconds()
    {
        return this.shrinkerIntervalSeconds;
    }


    /**
     * Description of the Method
     *
     * @return
     */
    public ICompositeCacheAttributes copy()
    {
        try
        {
            ICompositeCacheAttributes cattr = ( CompositeCacheAttributes ) this.clone();
            //System.out.println( "cattr = " + cattr );
            return cattr;
        }
        catch ( Exception e )
        {
            System.err.println( e.toString() );
            return new CompositeCacheAttributes();
        }
    }


    /**
     * Description of the Method
     *
     * @return
     */
    public String toString()
    {
        StringBuffer dump = new StringBuffer();

        dump.append( "[ " )
            .append( "useLateral = " ).append( useLateral )
            .append( ", useRemote = " ).append( useRemote )
            .append( ", useDisk = " ).append( useDisk )
            .append( ", maxObjs = " ).append( maxObjs )
            .append( " ]" );

        return dump.toString();
    }

}
// end class

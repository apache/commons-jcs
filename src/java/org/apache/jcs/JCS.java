package org.apache.jcs;

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

import org.apache.jcs.access.GroupCacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.control.Cache;
import org.apache.jcs.engine.control.CacheHub;
import org.apache.jcs.engine.control.group.GroupCacheHub;

/**
 * Simple class for using JCS. To use JCS in your application, you can use the
 * static methods of this class to get access objects (instances of this class)
 * for your cache regions. Ideally this class should be all you need to import
 * to use JCS. One JCS should be created for each region you want to access. If
 * you have several regions, then get instances for each. For best performance
 * the getInstance call should be made in an initialization method.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @author <a href="mailto:jtaylor@apache.org">James Taylor</a>
 * @created February 13, 2002
 * @version $Id$
 */
public class JCS extends GroupCacheAccess
{
    private static String configFilename = null;

    private static CacheHub cacheMgr;

    /**
     * Protected constructor for use by the static factory methods.
     *
     * @param cacheControl Cache which the instance will provide access to
     */
    protected JCS( Cache cacheControl )
    {
        super( cacheControl );
    }

    /**
     * Get a JCS which accesses the provided region.
     *
     * @param region Region that return JCS will provide access to
     * @return A JCS which provides access to a given region.
     * @exception CacheException
     */
    public static JCS getInstance( String region )
        throws CacheException
    {
        ensureCacheManager();

        return new JCS( ( Cache ) cacheMgr.getCache( region ) );
    }

    /**
     * Get a JCS which accesses the provided region.
     *
     * @param region Region that return JCS will provide access to
     * @param icca CacheAttributes for region
     * @return A JCS which provides access to a given region.
     * @exception CacheException
     */
    public static JCS getInstance( String region,
                                   ICompositeCacheAttributes icca )
        throws CacheException
    {
        ensureCacheManager();

        return new JCS( ( Cache ) cacheMgr.getCache( region, icca ) );
    }

    /**
     * Gets an instance of GroupCacheHub and stores it in the cacheMgr class
     * field, if it is not already set. Unlike the implementation in
     * CacheAccess, the cache manager is a GroupCacheHub. NOTE: This can
     * will be moved up into GroupCacheAccess.
     */
    protected static void ensureCacheManager()
    {
        if ( cacheMgr == null )
        {
            synchronized ( JCS.class )
            {
                if ( cacheMgr == null )
                {
                    if ( configFilename == null )
                    {
                        cacheMgr = GroupCacheHub.getInstance();
                    }
                    else
                    {
                        cacheMgr = GroupCacheHub
                            .getInstance( configFilename );
                    }
                }
            }
        }
    }

    /**
     * Set the filename that the cache manager will be initialized with. Only
     * matters before the instance is initialized.
     */
    public static void setConfigFilename( String configFilename )
    {
        JCS.configFilename = configFilename;
    }
}

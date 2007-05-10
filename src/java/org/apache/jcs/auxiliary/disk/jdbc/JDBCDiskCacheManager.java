package org.apache.jcs.auxiliary.disk.jdbc;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;

/**
 * This manages instances of the jdbc disk cache. It maintains one for each
 * region. One for all regions would work, but this gives us more detailed stats
 * by region.
 */
public class JDBCDiskCacheManager
    extends JDBCDiskCacheManagerAbstractTemplate
{
    private static final long serialVersionUID = -8258856770927857896L;

    private static final Log log = LogFactory.getLog( JDBCDiskCacheManager.class );

    private static JDBCDiskCacheManager instance;

    private JDBCDiskCacheAttributes defaultJDBCDiskCacheAttributes;

    /**
     * Constructor for the HSQLCacheManager object
     * <p>
     * @param cattr
     */
    private JDBCDiskCacheManager( JDBCDiskCacheAttributes cattr )
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "Creating JDBCDiskCacheManager with " + cattr );
        }
        defaultJDBCDiskCacheAttributes = cattr;
    }

    /**
     * Gets the defaultCattr attribute of the HSQLCacheManager object
     * <p>
     * @return The defaultCattr value
     */
    public JDBCDiskCacheAttributes getDefaultJDBCDiskCacheAttributes()
    {
        return defaultJDBCDiskCacheAttributes;
    }

    /**
     * Gets the instance attribute of the HSQLCacheManager class
     * <p>
     * @param cattr
     * @return The instance value
     */
    public static JDBCDiskCacheManager getInstance( JDBCDiskCacheAttributes cattr )
    {
        synchronized ( JDBCDiskCacheManager.class )
        {
            if ( instance == null )
            {
                instance = new JDBCDiskCacheManager( cattr );
            }
        }
        clients++;
        return instance;
    }

    /**
     * Gets the cache attribute of the HSQLCacheManager object
     * <p>
     * @param cacheName
     * @return The cache value
     */
    public AuxiliaryCache getCache( String cacheName )
    {
        JDBCDiskCacheAttributes cattr = (JDBCDiskCacheAttributes) defaultJDBCDiskCacheAttributes.copy();
        cattr.setCacheName( cacheName );
        return getCache( cattr );
    }

    /**
     * Creates a JDBCDiskCache using the supplied attributes.
     * <p>
     * @param cattr
     * @return
     */
    protected AuxiliaryCache createJDBCDiskCache( JDBCDiskCacheAttributes cattr, TableState tableState )
    {
        AuxiliaryCache raf;
        raf = new JDBCDiskCache( cattr, tableState );
        return raf;
    }
}

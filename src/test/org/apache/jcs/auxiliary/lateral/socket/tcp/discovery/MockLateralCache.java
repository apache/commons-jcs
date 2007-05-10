package org.apache.jcs.auxiliary.lateral.socket.tcp.discovery;

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

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.auxiliary.lateral.LateralCache;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * For testing things that need a lateral cache
 *
 * @author Aaron Smuts
 *
 */
public class MockLateralCache
    extends LateralCache
    implements ICache
{
    private static final long serialVersionUID = 1L;

    /**
     * @param cattr
     */
    protected MockLateralCache( ILateralCacheAttributes cattr )
    {
        super( cattr );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.behavior.ICache#update(org.apache.jcs.engine.behavior.ICacheElement)
     */
    public void update( ICacheElement ce )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.behavior.ICache#get(java.io.Serializable)
     */
    public ICacheElement get( Serializable key )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.behavior.ICache#remove(java.io.Serializable)
     */
    public boolean remove( Serializable key )
        throws IOException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.behavior.ICache#removeAll()
     */
    public void removeAll()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.behavior.ICache#dispose()
     */
    public void dispose()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.behavior.ICache#getSize()
     */
    public int getSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.behavior.ICache#getStatus()
     */
    public int getStatus()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.behavior.ICache#getStats()
     */
    public String getStats()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.behavior.ICache#getCacheName()
     */
    public String getCacheName()
    {
        return super.getCacheName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.behavior.ICacheType#getCacheType()
     */
    public int getCacheType()
    {
        return super.getCacheType();
    }

}

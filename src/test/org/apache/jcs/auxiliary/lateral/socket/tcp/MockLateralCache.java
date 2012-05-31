package org.apache.jcs.auxiliary.lateral.socket.tcp;

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
 */
public class MockLateralCache<K extends Serializable, V extends Serializable>
    extends LateralCache<K, V>
    implements ICache<K, V>
{
    /** junk */
    private static final long serialVersionUID = 1L;

    /**
     * @param cattr
     */
    protected MockLateralCache( ILateralCacheAttributes cattr )
    {
        super( cattr );
    }

    /**
     * Nothing.
     * @param ce
     * @throws IOException
     */
    @Override
    protected void processUpdate( ICacheElement<K, V> ce )
        throws IOException
    {
        // nothing
    }

    /**
     * @param key
     * @return ICacheElement
     * @throws IOException
     */
    @Override
    protected ICacheElement<K, V> processGet( K key )
        throws IOException
    {
        return null;
    }

    /**
     * @param key
     * @return false
     * @throws IOException
     */
    @Override
    protected boolean processRemove( K key )
        throws IOException
    {
        return false;
    }

    /**
     * @throws IOException
     */
    @Override
    public void processRemoveAll()
        throws IOException
    {
        //nothing
    }

    /**
     * @throws IOException
     */
    @Override
    public void processDispose()
        throws IOException
    {
        // nothing
    }

    /** @return 0 */
    @Override
    public int getSize()
    {
        return 0;
    }

    /** @return 0 */
    @Override
    public int getStatus()
    {
        return 0;
    }

    /** @return String */
    @Override
    public String getStats()
    {
        return null;
    }

    /** @return String */
    @Override
    public String getCacheName()
    {
        return super.getCacheName();
    }

    /** @return type */
    @Override
    public int getCacheType()
    {
        return super.getCacheType();
    }
}

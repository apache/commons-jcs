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
package org.apache.commons.jcs3.jcache.extras;

import java.lang.reflect.Field;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

// TODO: *if needed* define @CacheDeifnition instead of relying on field types
public class InternalCacheExtension
    implements BeforeEachCallback, AfterEachCallback
{

    private CachingProvider cachingProvider;

    private CacheManager cacheManager;

    @Override
    public void beforeEach( ExtensionContext context )
        throws Exception
    {
        cachingProvider = Caching.getCachingProvider();
        cacheManager = cachingProvider.getCacheManager();

        Field cacheField = null;
        CompleteConfiguration<?, ?> config = null;

        for ( Field field : context.getTestInstance().get().getClass().getDeclaredFields() )
        {
            if ( Cache.class.isAssignableFrom( field.getType() ) )
            {
                field.setAccessible( true );
                cacheField = field;
            }
            else if ( Configuration.class.isAssignableFrom( field.getType() ) )
            {
                field.setAccessible( true );
                config = (CompleteConfiguration<?, ?>) field.get( context.getTestInstance().get() );
            }
        }

        if ( cacheField != null )
        {
            if ( config == null )
            {
                throw new IllegalStateException( "Define a Configuration field" );
            }
            cacheField.set( context.getTestInstance().get(), cacheManager.createCache( cacheField.getName(), config ) );
        }
    }

    @Override
    public void afterEach( ExtensionContext context )
    {
        if ( cacheManager != null )
        {
            cacheManager.close();
        }
        if ( cachingProvider != null )
        {
            cachingProvider.close();
        }
    }
}

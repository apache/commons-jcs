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
package org.apache.commons.jcs.jcache.extras.cdi;

import javax.cache.CacheManager;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;

public class CacheManagerBean implements Bean<CacheManager>, PassivationCapable
{
    private final Set<Type> types;
    private final Set<Annotation> qualifiers;
    private final CacheManager manager;
    private final String id;

    public CacheManagerBean(final CacheManager cacheManager)
    {
        manager = cacheManager;
        id = getClass().getName() + "-" + hashCode();

        types = new HashSet<Type>();
        types.add(CacheManager.class);
        types.add(Object.class);

        qualifiers = new HashSet<Annotation>();
        qualifiers.add(DefaultLiteral.INSTANCE);
        qualifiers.add(AnyLiteral.INSTANCE);
    }

    @Override
    public Set<Type> getTypes()
    {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers()
    {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return ApplicationScoped.class;
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public boolean isNullable()
    {
        return false;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints()
    {
        return emptySet();
    }

    @Override
    public Class<?> getBeanClass()
    {
        return CacheManager.class;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes()
    {
        return emptySet();
    }

    @Override
    public boolean isAlternative()
    {
        return false;
    }

    @Override
    public CacheManager create(CreationalContext<CacheManager> cacheManagerCreationalContext)
    {
        return manager;
    }

    @Override
    public void destroy(CacheManager cacheManager, CreationalContext<CacheManager> cacheManagerCreationalContext)
    {
        manager.close();
    }

    @Override
    public String getId()
    {
        return id;
    }
}

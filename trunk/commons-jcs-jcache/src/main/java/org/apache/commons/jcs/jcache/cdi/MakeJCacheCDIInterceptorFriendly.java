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
package org.apache.commons.jcs.jcache.cdi;

import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

// TODO: observe annotated type (or maybe sthg else) to cache data and inecjt this extension (used as metadata cache)
// to get class model and this way allow to add cache annotation on the fly - == avoid java pure reflection to get metadata
public class MakeJCacheCDIInterceptorFriendly implements Extension
{
    protected void discoverInterceptorBindings(final @Observes BeforeBeanDiscovery beforeBeanDiscoveryEvent)
    {
        beforeBeanDiscoveryEvent.addInterceptorBinding(CachePut.class);
        beforeBeanDiscoveryEvent.addInterceptorBinding(CacheResult.class);
        beforeBeanDiscoveryEvent.addInterceptorBinding(CacheRemove.class);
        beforeBeanDiscoveryEvent.addInterceptorBinding(CacheRemoveAll.class);
    }
}

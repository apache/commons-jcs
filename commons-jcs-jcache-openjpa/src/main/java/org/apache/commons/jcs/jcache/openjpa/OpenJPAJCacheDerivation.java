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
package org.apache.commons.jcs.jcache.openjpa;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.event.SingleJVMRemoteCommitProvider;
import org.apache.openjpa.lib.conf.AbstractProductDerivation;
import org.apache.openjpa.lib.conf.Configuration;

public class OpenJPAJCacheDerivation extends AbstractProductDerivation
{
    @Override
    public boolean beforeConfigurationLoad(Configuration conf)
    {
        if (OpenJPAConfiguration.class.isInstance(conf)) {
            final OpenJPAConfigurationImpl oconf = OpenJPAConfigurationImpl.class.cast(conf);
            oconf.dataCacheManagerPlugin.setAlias("jcache", OpenJPAJCacheDataCacheManager.class.getName());
            oconf.dataCachePlugin.setAlias("jcache", OpenJPAJCacheDataCache.class.getName());
            oconf.queryCachePlugin.setAlias("jcache", OpenJPAJCacheQueryCache.class.getName());
            oconf.remoteProviderPlugin.setAlias("none", SingleJVMRemoteCommitProvider .class.getName());
        }
        return false;
    }

    @Override
    public int getType()
    {
        return 0;
    }
}

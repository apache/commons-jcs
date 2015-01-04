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
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.lib.conf.Configurations;

import java.util.Map;

public class OpenJPAJCacheDerivation extends AbstractProductDerivation
{
    private static final String JCACHE_NAME = "jcache";

    @Override
    public boolean beforeConfigurationLoad(final Configuration conf)
    {
        if (OpenJPAConfiguration.class.isInstance(conf)) {
            final OpenJPAConfigurationImpl oconf = OpenJPAConfigurationImpl.class.cast(conf);
            oconf.dataCacheManagerPlugin.setAlias(JCACHE_NAME, OpenJPAJCacheDataCacheManager.class.getName());
            oconf.dataCachePlugin.setAlias(JCACHE_NAME, OpenJPAJCacheDataCache.class.getName());
            oconf.queryCachePlugin.setAlias(JCACHE_NAME, OpenJPAJCacheQueryCache.class.getName());
        }
        return super.beforeConfigurationLoad(conf);
    }

    @Override
    public boolean beforeConfigurationConstruct(final ConfigurationProvider cp)
    {
        final Map<?, ?> props = cp.getProperties();
        final Object dcm = Configurations.getProperty("DataCacheManager", props);
        if (dcm != null && JCACHE_NAME.equals(dcm))
        {
            if (Configurations.getProperty("DataCache", props) == null)
            {
                cp.addProperty("openjpa.DataCache", JCACHE_NAME);
            }
            if (Configurations.getProperty("QueryCache", props) == null)
            {
                cp.addProperty("openjpa.QueryCache", JCACHE_NAME);
            }
            if (Configurations.getProperty("RemoteCommitProvider", props) == null)
            {
                cp.addProperty("openjpa.RemoteCommitProvider", SingleJVMRemoteCommitProvider.class.getName());
            }
        }
        return super.beforeConfigurationConstruct(cp);
    }

    @Override
    public int getType()
    {
        return TYPE_FEATURE;
    }
}

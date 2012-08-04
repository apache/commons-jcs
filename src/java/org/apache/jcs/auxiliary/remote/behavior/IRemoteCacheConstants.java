package org.apache.jcs.auxiliary.remote.behavior;

import org.apache.jcs.engine.behavior.ICacheServiceNonLocal;

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

/**
 * This holds constants that are used by the remote cache.
 */
public interface IRemoteCacheConstants
{
    /** Mapping to props file value */
    public final static String REMOTE_CACHE_SERVICE_VAL = ICacheServiceNonLocal.class.getName();

    /** The prefix for cache server config. */
    public final static String CACHE_SERVER_PREFIX = "jcs.remotecache";

    /**
     * I'm trying to migrate everything to use this prefix. All those below will be replaced. Any of
     * the RemoteCacheServerAttributes can be configured this way.
     */
    public final static String CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX = CACHE_SERVER_PREFIX + ".serverattributes";

    /**
     * This is the name of the class that will be used for an object specific socket factory.
     */
    public final static String CUSTOM_RMI_SOCKET_FACTORY_PROPERTY_PREFIX = CACHE_SERVER_PREFIX + ".customrmisocketfactory";

    /** Property prefix, should be jcs.remote but this would break existing config. */
    public final static String PROPERTY_PREFIX = "remote";    

    /** Mapping to props file value */
    public final static String SOCKET_TIMEOUT_MILLIS = PROPERTY_PREFIX + ".cache.rmiSocketFactoryTimeoutMillis";

    /** Mapping to props file value */
    public final static String REMOTE_CACHE_SERVICE_NAME = PROPERTY_PREFIX + ".cache.service.name";

    /** Mapping to props file value */
    public final static String TOMCAT_XML = PROPERTY_PREFIX + ".tomcat.xml";

    /** Mapping to props file value */
    public final static String TOMCAT_ON = PROPERTY_PREFIX + ".tomcat.on";

    /** Mapping to props file value */
    public final static String REMOTE_CACHE_SERVICE_PORT = PROPERTY_PREFIX + ".cache.service.port";

    /** Mapping to props file value */
    public final static String REMOTE_LOCAL_CLUSTER_CONSISTENCY = PROPERTY_PREFIX + ".cluster.LocalClusterConsistency";

    /** Mapping to props file value */
    public final static String REMOTE_ALLOW_CLUSTER_GET = PROPERTY_PREFIX + ".cluster.AllowClusterGet";
}

package org.apache.jcs.engine.stats.behavior;

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
 * This holds stat information on a region. It contains both auxiliary and core
 * stats.
 *
 * @author aaronsm
 *
 */
public interface ICacheStats
    extends IStats
{

    /**
     * Stats are for a region, though auxiliary data may be for more.
     *
     * @return The region name
     */
    public abstract String getRegionName();

    /**
     *
     * @param name
     */
    public abstract void setRegionName( String name );

    /**
     *
     * @return
     */
    public abstract IStats[] getAuxiliaryCacheStats();

    /**
     *
     * @param stats
     */
    public abstract void setAuxiliaryCacheStats( IStats[] stats );

}

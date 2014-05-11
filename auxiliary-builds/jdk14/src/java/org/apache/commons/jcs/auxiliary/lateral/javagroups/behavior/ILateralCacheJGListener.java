package org.apache.commons.jcs.auxiliary.lateral.javagroups.behavior;

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

import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheListener;

import java.io.IOException;
import java.io.Serializable;

/**
 * Listens for lateral cache event notification.
 *
 * @version $Id: ILateralCacheJGListener.java 224346 2005-06-04 02:01:59Z asmuts $
 */
public interface ILateralCacheJGListener
    extends ILateralCacheListener
{

    /** Description of the Method */
    public void init();

    /** Tries to get a requested item from the cache. */
    public Serializable handleGet( String cacheName, K key )
        throws IOException;

}

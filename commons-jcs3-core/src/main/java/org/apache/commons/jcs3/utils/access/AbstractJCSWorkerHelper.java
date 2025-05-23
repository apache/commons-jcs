package org.apache.commons.jcs3.utils.access;

import java.util.concurrent.atomic.AtomicBoolean;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * This is an abstract template for JCSWorkerHelper implementations. it simple has a convenience
 * method for setting the finished flag.
 */
public abstract class AbstractJCSWorkerHelper<V> implements JCSWorkerHelper<V>
{
    /** Finished flag. Can't we use wait notify? */
    private final AtomicBoolean finished = new AtomicBoolean();

    /**
     * Default
     */
    public AbstractJCSWorkerHelper()
    {
    }

    /**
     * @return finished
     */
    @Override
    public boolean isFinished()
    {
        return finished.get();
    }

    /**
     * @param isFinished
     */
    @Override
    public void setFinished( final boolean isFinished )
    {
        finished.set(isFinished);
    }
}

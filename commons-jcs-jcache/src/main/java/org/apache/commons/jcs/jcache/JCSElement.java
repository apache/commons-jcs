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
package org.apache.commons.jcs.jcache;

import javax.cache.expiry.Duration;
import java.io.Serializable;

public class JCSElement<V> implements Serializable
{
    private final V element;
    private volatile long end;

    public JCSElement(final V element, final Duration duration)
    {
        this.element = element;
        update(duration);
    }

    public boolean isExpired()
    {
        return end != -1 && (end == 0 || Times.now() > end);
    }

    public V getElement()
    {
        return element;
    }

    public void update(final Duration duration)
    {
        if (duration == null || duration.isEternal())
        {
            end = -1;
        }
        else if (duration.isZero())
        {
            end = 0;
        }
        else
        {
            end = duration.getAdjustedTime(Times.now());
        }
    }
}

package org.apache.jcs.admin;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** Stores info on a cache element for the template */
public class CacheElementInfo
{
    String key = null;

    boolean eternal = false;

    String createTime = null;

    long maxLifeSeconds = -1;

    long expiresInSeconds = -1;

    /**
     * @return a string representation of the key
     */
    public String getKey()
    {
        return this.key;
    }

    /**
     * @return true if the item does not expire
     */
    public boolean isEternal()
    {
        return this.eternal;
    }

    /**
     * @return the time the object was created
     */
    public String getCreateTime()
    {
        return this.createTime;
    }

    /**
     * Ignored if isEternal
     * 
     * @return the longest this object can live.
     */
    public long getMaxLifeSeconds()
    {
        return this.maxLifeSeconds;
    }

    /**
     * Ignored if isEternal
     * 
     * @return how many seconds until this object expires.
     */
    public long getExpiresInSeconds()
    {
        return this.expiresInSeconds;
    }
}

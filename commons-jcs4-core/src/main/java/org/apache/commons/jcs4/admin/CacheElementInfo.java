package org.apache.commons.jcs4.admin;

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
 * Stores info on a cache element for the template
 */
public record CacheElementInfo(
    /** Element key */
    String key,

    /** Is it eternal */
    boolean eternal,

    /** When it was created */
    String createTime,

    /** Max life */
    long maxLifeSeconds,

    /** When it will expire */
    long expiresInSeconds
)
{
    /**
     * @return string info on the item
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\nCacheElementInfo " );
        buf.append( "\n Key [" ).append( key() ).append( "]" );
        buf.append( "\n Eternal [" ).append( eternal() ).append( "]" );
        buf.append( "\n CreateTime [" ).append( createTime() ).append( "]" );
        buf.append( "\n MaxLifeSeconds [" ).append( maxLifeSeconds() ).append( "]" );
        buf.append( "\n ExpiresInSeconds [" ).append( expiresInSeconds() ).append( "]" );

        return buf.toString();
    }
}

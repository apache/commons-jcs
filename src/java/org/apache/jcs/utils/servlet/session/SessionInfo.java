package org.apache.jcs.utils.servlet.session;


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


import java.io.Serializable;

/**
 * Session information wrapper.
 *
 */
public class SessionInfo implements Serializable
{

    final long creationTime;
    long lastAccessedTime;

    /**
     * At first login this might be twice the set amount, depending on the
     * update ration.
     */
    int maxInactiveInterval = 60 * 60 * 1000;
    // 30 minutes after first hour

    /** Constructor for the SessionInfo object */
    SessionInfo()
    {
        lastAccessedTime = creationTime = System.currentTimeMillis();
    }


    /** Prints data to string. */
    public String toString()
    {
        return "[creationTime=" + creationTime
             + ", lastAccessedTime=" + lastAccessedTime
             + ", maxInactiveInterval=" + maxInactiveInterval
             + "]"
            ;
    }

}
// end SessionInfo

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


/**
 * Constants used by the session tracker.
 *
 */
public interface ISessionConstants
{
    /*
     * probably not necessary
     */
    /** Description of the Field */
    public final static String SESS_CACHE_NAME = "sessionCache";
    /** Description of the Field */
    public final static int SESS_CACHE_SIZE = 2000;
    /** Description of the Field */
    public final static String SESS_INFO_CACHE_NAME = "sessionInfoCache";
    /** Description of the Field */
    public final static int SESS_INFO_CACHE_SIZE = 2000;
    // 30 minutes after first hour
    /** Description of the Field */
    public final static int DFLT_INACTIVE_INTERVAL = 60 * 60 * 1000;
}


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


import javax.servlet.http.HttpSession;

/**
 * Used to contain an http session and/or the associated information.
 *
 */
class MetaHttpSession
{
    /** The corresponding cookie id of the http session. */
    final String session_id;
    /** The http session. */
    final HttpSession sess;


    /**
     * Constructs with the given session id, and the http session. Both values
     * can be null.
     *
     * @param session_id
     * @param sess
     */
    MetaHttpSession( String session_id, HttpSession sess )
    {
        this.session_id = session_id;
        this.sess = sess;
    }


    /**
     * Returns true iff both the session id and the http session are not null.
     */
    boolean valid()
    {
        return session_id != null && sess != null;
    }
}


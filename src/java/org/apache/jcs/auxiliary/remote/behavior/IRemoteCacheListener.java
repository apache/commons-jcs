package org.apache.jcs.auxiliary.remote.behavior;

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

import java.io.IOException;

import java.rmi.Remote;

import org.apache.jcs.engine.behavior.ICacheListener;

/**
 * Listens for remote cache event notification ( rmi callback ).
 *  
 */
public interface IRemoteCacheListener
    extends ICacheListener, Remote
{

    /** Description of the Field */
    public final static int SERVER_LISTENER = 0;

    /** Description of the Field */
    public final static int CLIENT_LISTENER = 0;

    /**
     * Get the id to be used by this manager.
     * 
     * @return long
     */
    public long getListenerId()
        throws IOException;

    /**
     * Set the id to be used by this manager. The remote cache server identifies
     * clients by this id. The value will be set by the server through the
     * remote cache listener.
     * 
     * @param id
     */
    public void setListenerId( long id )
        throws IOException;

    /**
     * Gets the remoteType attribute of the IRemoteCacheListener object
     * 
     * @return The remoteType value
     */
    public int getRemoteType()
        throws IOException;

}

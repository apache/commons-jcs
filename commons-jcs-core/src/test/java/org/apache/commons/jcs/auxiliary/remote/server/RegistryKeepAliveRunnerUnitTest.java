package org.apache.commons.jcs.auxiliary.remote.server;

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

import junit.framework.TestCase;
import org.apache.commons.jcs.auxiliary.MockCacheEventLogger;

/** Unit tests for the registry keep alive runner. */
public class RegistryKeepAliveRunnerUnitTest
    extends TestCase
{
    /** Verify that we get the appropriate event log */
    public void testCheckAndRestoreIfNeeded_failure()
    {
        // SETUP
        String host = "localhost";
        int port = 1234;
        String service = "doesn'texist";
        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        RegistryKeepAliveRunner runner = new RegistryKeepAliveRunner( host, port, service );
        runner.setCacheEventLogger( cacheEventLogger );

        // DO WORK
        runner.checkAndRestoreIfNeeded();

        // VERIFY
        // 1 for the lookup, one for the rebind since the server isn't created yet
        assertEquals( "error tally", 2, cacheEventLogger.errorEventCalls );
        //System.out.println( cacheEventLogger.errorMessages );
    }
}

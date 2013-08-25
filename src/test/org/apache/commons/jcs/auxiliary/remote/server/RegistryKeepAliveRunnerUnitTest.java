package org.apache.commons.jcs.auxiliary.remote.server;

import junit.framework.TestCase;

import org.apache.commons.jcs.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs.auxiliary.remote.server.RegistryKeepAliveRunner;

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

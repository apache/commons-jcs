package org.apache.jcs.auxiliary.remote.server;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.remote.server.behavior.IRemoteCacheServerAttributes;

/**
 * Tests for the remote cache server attributes.
 * <p>
 * @author Aaron Smuts
 */
public class RemoteCacheServerAttributesUnitTest
    extends TestCase
{

    /**
     * Verify that we get a string, even if not attributes are set.
     */
    public void testToString()
    {
        RemoteCacheServerAttributes attributes = new RemoteCacheServerAttributes();
        assertNotNull( "Should have a string.", attributes.toString() );
    }

    /**
     * Verify that the type is set correctly and that the correct name is returned for the type.
     */
    public void testSetRemoteTypeName_local()
    {
        RemoteCacheServerAttributes attributes = new RemoteCacheServerAttributes();
        attributes.setRemoteTypeName( "LOCAL" );
        assertEquals( "Wrong type.", IRemoteCacheServerAttributes.LOCAL, attributes.getRemoteType() );
        assertEquals( "Wrong name", "LOCAL", attributes.getRemoteTypeName() );
    }

    /**
     * Verify that the type is set correctly and that the correct name is returned for the type.
     */
    public void testSetRemoteTypeName_cluster()
    {
        RemoteCacheServerAttributes attributes = new RemoteCacheServerAttributes();
        attributes.setRemoteTypeName( "CLUSTER" );
        assertEquals( "Wrong type.", IRemoteCacheServerAttributes.CLUSTER, attributes.getRemoteType() );
        assertEquals( "Wrong name", "CLUSTER", attributes.getRemoteTypeName() );
    }
}

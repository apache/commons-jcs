package org.apache.jcs.utils.discovery;

import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * Tests for the sender.
 */
public class UDPDiscoverySenderUnitTest
    extends TestCase
{
    /** multicast address to send/receive on */
    private static final String ADDRESS = "228.4.5.9";

    /** multicast address to send/receive on */
    private static final int PORT = 5555;

    /** imaginary host address for sending */
    private static final String SENDING_HOST = "imaginary host address";

    /** imaginary port for sending */
    private static final int SENDING_PORT = 1;

    /** receiver instance for tests */
    private UDPDiscoveryReceiver receiver;

    /** sender instance for tests */
    private UDPDiscoverySender sender;

    /**
     * Set up the receiver. Maybe better to just code sockets here? Set up the sender for sending
     * the message.
     * <p>
     * @throws Exception on error
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();
        receiver = new UDPDiscoveryReceiver( null, ADDRESS, PORT );
        sender = new UDPDiscoverySender( ADDRESS, PORT );
    }

    /**
     * Kill off the sender and receiver.
     * <p>
     * @throws Exception on error
     */
    protected void tearDown()
        throws Exception
    {
        receiver.shutdown();
        sender.destroy();
        super.tearDown();
    }

    /**
     * Test sending a live messages.
     * <p>
     * @throws Exception on error
     */
    public void testPassiveBroadcast()
        throws Exception
    {
        // SETUP
        ArrayList cacheNames = new ArrayList();       
        
        // DO WORK
        sender.passiveBroadcast( SENDING_HOST, SENDING_PORT, cacheNames, 1L );

        // VERIFY
        // grab the sent message
        Object obj = receiver.waitForMessage() ;

        assertTrue( "unexpected crap received", obj instanceof UDPDiscoveryMessage );

        UDPDiscoveryMessage msg = (UDPDiscoveryMessage) obj;
        assertEquals( "wrong host", SENDING_HOST, msg.getHost() );
        assertEquals( "wrong port", SENDING_PORT, msg.getPort() );
        assertEquals( "wrong message type", UDPDiscoveryMessage.PASSIVE_BROADCAST, msg.getMessageType() );
    }

    /**
     * Test sending a remove broadcast.
     * <p>
     * @throws Exception on error
     */
    public void testRemoveBroadcast()
        throws Exception
    {
        // SETUP
        ArrayList cacheNames = new ArrayList();
        
        // DO WORK
        sender.removeBroadcast( SENDING_HOST, SENDING_PORT, cacheNames, 1L );

        // VERIFY
        // grab the sent message
        Object obj = receiver.waitForMessage();

        assertTrue( "unexpected crap received", obj instanceof UDPDiscoveryMessage );

        UDPDiscoveryMessage msg = (UDPDiscoveryMessage) obj;
        assertEquals( "wrong host", SENDING_HOST, msg.getHost() );
        assertEquals( "wrong port", SENDING_PORT, msg.getPort() );
        assertEquals( "wrong message type", UDPDiscoveryMessage.REMOVE_BROADCAST, msg.getMessageType() );
    }

    /**
     * Test sending a request broadcast.
     * <p>
     * @throws Exception on error
     */
    public void testRequestBroadcast()
        throws Exception
    {
        // DO WORK
        sender.requestBroadcast();

        // VERIFY
        // grab the sent message
        Object obj = receiver.waitForMessage();

        assertTrue( "unexpected crap received", obj instanceof UDPDiscoveryMessage );

        UDPDiscoveryMessage msg = (UDPDiscoveryMessage) obj;
        assertEquals( "wrong message type", UDPDiscoveryMessage.REQUEST_BROADCAST, msg.getMessageType() );
    }
}

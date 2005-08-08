package org.apache.jcs.auxiliary.lateral.socket.tcp;

import org.apache.jcs.JCS;
import org.apache.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCacheManager;

import junit.framework.TestCase;

/**
 * Basic unit tests for the sending and receiving portions of the lateral cache.
 * 
 * @author Aaron Smuts
 *  
 */
public class TestTCPLateral
    extends TestCase
{

    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestTCPLateralCache.ccf" );
    }

    /**
     * Make sure we can send a bunch to the listener. This would be better if we
     * could plugin a Mock CacheManger. The listener will instantiate on on its
     * own. We have to configure one before that.
     * 
     * @throws Exception
     */
    public void SkiptestSimpleSend()
        throws Exception
    {

        JCS jcs = JCS.getInstance( "test" );

        LateralCacheAttributes lac = new LateralCacheAttributes();
        lac.setTransmissionType( LateralCacheAttributes.TCP );
        lac.setTcpServer( "localhost" + ":" + 8111 );
        lac.setTcpListenerPort( 8111 );

        ICompositeCacheManager cacheMgr = CompositeCacheManager.getInstance();

        // start the listener
        LateralTCPListener listener = (LateralTCPListener) LateralTCPListener.getInstance( lac, cacheMgr );

        // send to the listener
        LateralTCPSender lur = new LateralTCPSender( lac );

        int numMes = 100;
        for ( int i = 0; i < numMes; i++ )
        {
            String message = "adsfasasfasfasdasf";
            CacheElement ce = new CacheElement( "test", "test", message );
            LateralElementDescriptor led = new LateralElementDescriptor( ce );
            led.command = LateralElementDescriptor.UPDATE;
            led.requesterId = 1;
            lur.send( led );
        }

        Thread.sleep( 300 );

        System.out.println( "PutCount = " + listener.getPutCnt() );
        assertEquals( "Should have received " + numMes + " by now.", numMes, listener.getPutCnt() );

    }

}

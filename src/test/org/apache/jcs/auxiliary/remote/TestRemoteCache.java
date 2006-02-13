package org.apache.jcs.auxiliary.remote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.jcs.auxiliary.remote.server.RemoteCacheServerFactory;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCacheManagerMockImpl;

import junit.framework.TestCase;

/**
 * @author asmuts
 */
public class TestRemoteCache
    extends TestCase
{

    private final static Log log = LogFactory.getLog( TestRemoteCache.class );

    /**
     * 
     *
     */
    public TestRemoteCache()
    {
        super();
        try
        {
            System.out.println( "main> creating registry on the localhost" );
            RemoteUtils.createRegistry( 1101 );

            RemoteCacheServerFactory.startup( "localhost", 1101, "/TestRemoteServer.ccf" );
            
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestRemoteClient.ccf" );
    }
    
    /**
     * @throws Exception 
     * 
     *
     */
    public void skiptestSimpleSend() throws Exception
    {
        log.info( "testSimpleSend" );
        System.out.println( "testSimpleSend" );
        
        JCS cache = JCS.getInstance( "testCache" );

        log.info( "cache = " + cache );

        for ( int i = 0; i < 1000; i++ )
        {
            System.out.println( "puttting " + i );
            cache.put( "key" + i, "data" + i );
            System.out.println( "put " + i );
            log.info( "put " + i );
        }
    }
    
    
    /**
     * @throws Exception
     */
    public void testService() throws Exception 
    {


        Thread.sleep( 100 );
        
        ICompositeCacheManager cacheMgr = new CompositeCacheManagerMockImpl();
        
        RemoteCacheAttributes rca = new RemoteCacheAttributes();
        rca.setRemoteHost( "localhost" );
        rca.setRemotePort( 1101 );
                
        RemoteCacheManager mgr = RemoteCacheManager.getInstance( rca, cacheMgr );
        AuxiliaryCache cache = mgr.getCache( "testCache" );
        
        int numMes = 100;
        for ( int i = 0; i < numMes; i++ )
        {
            String message = "adsfasasfasfasdasf";
            CacheElement ce = new CacheElement( "key" + 1, "data" + i, message );
            cache.update( ce );
            System.out.println( "put " + ce );
        }

        //Thread.sleep( 2000 );

        /*
        // the receiver instance.
        JCS cacheReceiver = JCS.getInstance( "testCache" );

        log.info( "cache = " + cache );

        for ( int i = 0; i < numMes; i++ )
        {
            System.out.println( "getting " + i );
            Object data = cacheReceiver.get( "key" + i );
            System.out.println( i + " = " + data );
        }        
        */
    }
}

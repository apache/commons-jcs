package org.apache.jcs.auxiliary.lateral;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCache;

//import org.apache.jcs.auxiliary.*;

/**
 * Constructs a LateralCacheNoWaitFacade for the given configuration. Each
 * lateral service / local relationship is managed by one manager. This manager
 * canl have multiple caches. The remote relationships are consolidated and
 * restored via these managers. The facade provides a front to the composite
 * cache so the implmenetation is transparent.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class LateralCacheFactory implements AuxiliaryCacheFactory
{
    private final static Log log =
        LogFactory.getLog( LateralCacheFactory.class );

    private String name;

    /**
     * Interface method. Allows classforname construction, making caches
     * pluggable.
     *
     * @return
     * @param iaca
     */
    public AuxiliaryCache createCache( AuxiliaryCacheAttributes iaca,
                                       CompositeCache cache )
    {
        LateralCacheAttributes lac = ( LateralCacheAttributes ) iaca;
        ArrayList noWaits = new ArrayList();

        if ( lac.getTransmissionType() == lac.UDP )
        {
            LateralCacheManager lcm = LateralCacheManager.getInstance( lac );
            ICache ic = lcm.getCache( lac.getCacheName() );
            if ( ic != null )
            {
                noWaits.add( ic );
            }
        }
        else if ( lac.getTransmissionType() == lac.JAVAGROUPS )
        {
            LateralCacheManager lcm = LateralCacheManager.getInstance( lac );
            ICache ic = lcm.getCache( lac.getCacheName() );
            if ( ic != null )
            {
                noWaits.add( ic );
            }
        }
        else if ( lac.getTransmissionType() == lac.TCP )
        {

            //pars up the tcp servers and set the tcpServer value and
            // get the manager and then get the cache
            //Iterator it = lac.tcpServers.iterator();
            //while( it.hasNext() ) {

            StringTokenizer it = new StringTokenizer( lac.tcpServers, "," );
            while ( it.hasMoreElements() )
            {
                //String server = (String)it.next();
                String server = ( String ) it.nextElement();
                if ( log.isDebugEnabled() )
                {
                  log.debug( "tcp server = " +  server );
                }
                lac.setTcpServer( server );
                LateralCacheManager lcm = LateralCacheManager.getInstance( lac );
                ICache ic = lcm.getCache( lac.getCacheName() );
                if ( ic != null )
                {
                    noWaits.add( ic );
                }
                else
                {
                    //p( "noWait is null" );
                }
            }

        }
        else if ( lac.getTransmissionType() == lac.XMLRPC )
        {

            //pars up the tcp servers and set the tcpServer value and
            // get the manager and then get the cache
            //Iterator it = lac.tcpServers.iterator();
            //while( it.hasNext() ) {

            StringTokenizer it = new StringTokenizer( lac.getHttpServers(), "," );
            while ( it.hasMoreElements() )
            {
                //String server = (String)it.next();
                String server = ( String ) it.nextElement();
                //p( "tcp server = " +  server );
                lac.setHttpServer( server );
                LateralCacheManager lcm = LateralCacheManager.getInstance( lac );
                ICache ic = lcm.getCache( lac.getCacheName() );
                if ( ic != null )
                {
                    noWaits.add( ic );
                }
                else
                {
                    log.warn( "noWait is null" );
                }
            }

        }
        else if ( lac.getTransmissionType() == lac.HTTP )
        {
            StringTokenizer it = new StringTokenizer( lac.getHttpServers(), "," );
            while ( it.hasMoreElements() )
            {
                String server = ( String ) it.nextElement();
                lac.setHttpServer( server );
                LateralCacheManager lcm = LateralCacheManager.getInstance( lac );
                ICache ic = lcm.getCache( lac.getCacheName() );
                if ( ic != null )
                {
                    noWaits.add( ic );
                }
            }
        }

        LateralCacheNoWaitFacade lcnwf = new LateralCacheNoWaitFacade( ( LateralCacheNoWait[] ) noWaits.toArray( new LateralCacheNoWait[ 0 ] ), iaca.getCacheName() );

        return lcnwf;
    }
    // end createCache

    /**
     * Gets the name attribute of the LateralCacheFactory object
     *
     * @return The name value
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name attribute of the LateralCacheFactory object
     *
     * @param name The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }
}

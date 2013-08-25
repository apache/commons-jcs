package org.apache.commons.jcs.utils.net;

import java.net.UnknownHostException;

import org.apache.commons.jcs.utils.net.HostNameUtil;

import junit.framework.TestCase;

/** Tests for the host name util. */
public class HostNameUtilUnitTest
    extends TestCase
{
    /**
     * It's nearly impossible to unit test the getLocalHostLANAddress method.
     * <p>
     * @throws UnknownHostException 
     */
    public void testGetLocalHostAddress_Simple() throws UnknownHostException
    {
        // DO WORK
        String result = HostNameUtil.getLocalHostAddress();
        
        // VERIFY
        //System.out.print( result );
        assertNotNull( "Should have a host address.", result );
    }
}

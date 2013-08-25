package org.apache.commons.jcs.utils.net;

import org.apache.commons.jcs.utils.net.AddressUtil;

import junit.framework.TestCase;

/**
 * Basic AddressUtil Test class.
 */
public class AddressUtilUnitTest
    extends TestCase
{
    /**
     * test the basics
     */
    public void testOctetBasicAddressParsing()
    {
        String tempStr = AddressUtil.obtainFinalThreeDigitsOfAddressAsString();
        assertNotNull( "some result shoudl come back", tempStr );
        assertTrue( "shoudl not be default", !tempStr.equals( "000" ) );
    }
}

package org.apache.jcs.utils.net;

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

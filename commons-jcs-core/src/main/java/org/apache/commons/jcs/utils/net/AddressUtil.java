package org.apache.commons.jcs.utils.net;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.UnknownHostException;

/** Utility for getting info on the local ip address. */
public final class AddressUtil
{
    /** log instance */
    private static final Log log = LogFactory.getLog( AddressUtil.class );

    /** the default returned string value for the last octet */
    public static final String DEFAULT_INTERNET_ADDRESS_OCTET_AS_STRING = "000";

    /** the default returned string value for the whole ip */
    public static final String DEFAULT_INTERNET_ADDRESS_AS_STRING = "0.0.0.0";

    /** current address octet as string */
    private static String currentAddressOctetAsString = null;

    /**
     * private constructor.
     */
    private AddressUtil()
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "private constructor" );
        }
    }

    /**
     * This method looks up the host machines internet address, parses out the last octet and
     * formats that number as a 3 character string.
     * @return a string containing the digits of the ip address
     */
    public static synchronized String obtainFinalThreeDigitsOfAddressAsString()
    {
        if ( currentAddressOctetAsString == null )
        {
            currentAddressOctetAsString = obtainFinalThreeDigitsOfAddressAsStringLookup();
        }

        return currentAddressOctetAsString;
    }

    /**
     * This method looks up the host machines internet address, parses out the last octet and
     * formats that number as a 3 character string.
     * <p>
     * @return a string containing the digits of the ip address
     */
    private static String obtainFinalThreeDigitsOfAddressAsStringLookup()
    {
        String retval = DEFAULT_INTERNET_ADDRESS_OCTET_AS_STRING;
        try
        {
            String ipAsString = HostNameUtil.getLocalHostAddress();

            if ( log.isInfoEnabled() )
            {
                log.info( "obtainFinalThreeDigitsOfAddressAsStringLookup running; ipstring [" + ipAsString + "]" );
            }

            int lastdot = ipAsString.lastIndexOf( "." );
            if ( lastdot != -1 )
            {
                if ( lastdot != ipAsString.length() )
                {
                    ipAsString = ipAsString.substring( lastdot + 1 );

                    // pad it out to 3 characters
                    switch ( ipAsString.length() )
                    {
                        case 1:
                            ipAsString = "00" + ipAsString;
                            break;
                        case 2:
                            ipAsString = "0" + ipAsString;
                            break;
                        case 3:
                            if ( log.isDebugEnabled() )
                            {
                                log.debug( "3 digits is as we expect" );
                            }
                            break;
                        default:
                            log.warn( "detected invalid ip octet length [" + ipAsString + "] will return default" );
                            ipAsString = DEFAULT_INTERNET_ADDRESS_OCTET_AS_STRING;

                            break;
                    }
                    retval = ipAsString;
                }
                else
                {
                    log.warn( "ip ends in . ip: " + ipAsString + "  returning default: "
                        + DEFAULT_INTERNET_ADDRESS_OCTET_AS_STRING );
                }
            }
            else
            {
                log.warn( "could not find a . in address: " + ipAsString + "  returning default: "
                    + DEFAULT_INTERNET_ADDRESS_OCTET_AS_STRING );
            }
        }
        catch ( UnknownHostException e1 )
        {
            log.warn( "problem getting host address.  returning default: " + DEFAULT_INTERNET_ADDRESS_OCTET_AS_STRING );
        }
        return retval;
    }
}

package org.apache.jcs.auxiliary.remote;

import java.rmi.RemoteException;

import junit.framework.TestCase;

/**
 * Simple tests for remote utils.  It is difficult to verify most of the things is does.
 *
 * @author Aaron Smuts
 *
 */
public class RemoteUtilsUnitTest
    extends TestCase
{

    /**
     * Call create registry twice.
     * 
     * <p>
     * The exception is in the security manager setting.
     *
     */
    public void testCreateRegistryTwice()
    {
        try
        {
            RemoteUtils.createRegistry( 1102 );
        }
        catch ( RemoteException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try
        {
            RemoteUtils.createRegistry( 1102 );
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    
}

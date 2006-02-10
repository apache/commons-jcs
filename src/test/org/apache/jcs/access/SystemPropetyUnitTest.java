package org.apache.jcs.access;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.control.CompositeCacheManager;

import junit.framework.TestCase;

/**
 * 
 *
 * @author Aaron Smuts
 *
 */
public class SystemPropetyUnitTest
    extends TestCase
{

    /**
     * Verify that we use a system property for a ${FOO} string in a value.
     * @throws Exception 
     *
     */
    public void testSystemPropertyInValueDelimeter() throws Exception
    {
            
        int maxMemory = 1234;
        System.getProperties().setProperty( "MY_SYSTEM_PROPERTY_DISK_DIR", "system_set" );
        System.getProperties().setProperty( "MY_SYSTEM_PROPERTY_MAX_SIZE", String.valueOf( maxMemory ) );
                
        JCS.setConfigFilename( "/TestSystemProperties.ccf" );
        
        JCS cache = JCS.getInstance( "test1" );
        assertEquals( "We should have used the system property for the memory size", maxMemory, cache.getCacheAttributes().getMaxObjects() );
        
    }
    
    /**
     * Verify that we use a system property for a ${FOO} string in a value.  We define a propety in the
     * cache.ccf file, but we do not have it as a system property.  The default value should be used, if one exists.
     * @throws Exception 
     *
     */
    public void testSystemPropertyMissingInValueDelimeter() throws Exception
    {
        System.getProperties().setProperty( "MY_SYSTEM_PROPERTY_DISK_DIR", "system_set" );
  
        CompositeCacheManager mgr = CompositeCacheManager.getUnconfiguredInstance();
        mgr.configure( "/TestSystemProperties.ccf" );
                
        JCS cache = JCS.getInstance( "missing" );
        // TODO check against the actual default def
        assertEquals( "We should have used the default property for the memory size", 100, cache.getCacheAttributes().getMaxObjects() );
        
    }
    
}

package org.apache.jcs.auxiliary.lateral;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;

import junit.framework.TestCase;

/**
 * Tests for LateralCacheNoWaitFacade.
 */
public class LateralCacheNoWaitFacadeUnitTest
    extends TestCase
{
    /**
     * Verify that we can remove an item.
     */
    public void testAddThenRemoveNoWait_InList()
    {
        // SETUP
        LateralCacheNoWait[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( "testCache1" );
        
        LateralCacheNoWaitFacade facade = new LateralCacheNoWaitFacade( noWaits, cattr );
        
        LateralCache cache = new LateralCache( cattr );
        LateralCacheNoWait noWait = new LateralCacheNoWait( cache );
        
        // DO WORK
        facade.addNoWait( noWait );

        // VERIFY
        assertTrue( "Should be in the list.", facade.containsNoWait( noWait ) );
        
        // DO WORK
        facade.removeNoWait( noWait );
        
        // VERIFY
        assertEquals( "Should have 0", 0, facade.noWaits.length );               
        assertFalse( "Should not be in the list. ", facade.containsNoWait( noWait ) );
    }
    
    /**
     * Verify that we can remove an item.
     */
    public void testAddThenRemoveNoWait_InListSize2()
    {
        // SETUP
        LateralCacheNoWait[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( "testCache1" );
        
        LateralCacheNoWaitFacade facade = new LateralCacheNoWaitFacade( noWaits, cattr );
        
        LateralCache cache = new LateralCache( cattr );
        LateralCacheNoWait noWait = new LateralCacheNoWait( cache );
        LateralCacheNoWait noWait2 = new LateralCacheNoWait( cache );
        
        // DO WORK
        facade.addNoWait( noWait );
        facade.addNoWait( noWait2 );

        // VERIFY
        assertEquals( "Should have 2", 2, facade.noWaits.length );        
        assertTrue( "Should be in the list.", facade.containsNoWait( noWait ) );
        assertTrue( "Should be in the list.", facade.containsNoWait( noWait2 ) );
        
        // DO WORK
        facade.removeNoWait( noWait );
        
        // VERIFY        
        assertEquals( "Should only have 1", 1, facade.noWaits.length );        
        assertFalse( "Should not be in the list. ", facade.containsNoWait( noWait ) );
        assertTrue( "Should be in the list.", facade.containsNoWait( noWait2 ) );
    }
    
    /**
     * Verify that we can remove an item.
     */
    public void testAdd_InList()
    {
        // SETUP
        LateralCacheNoWait[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( "testCache1" );
        
        LateralCacheNoWaitFacade facade = new LateralCacheNoWaitFacade( noWaits, cattr );
        
        LateralCache cache = new LateralCache( cattr );
        LateralCacheNoWait noWait = new LateralCacheNoWait( cache );
        
        // DO WORK
        facade.addNoWait( noWait );
        facade.addNoWait( noWait );

        // VERIFY
        assertTrue( "Should be in the list.", facade.containsNoWait( noWait ) );
        assertEquals( "Should only have 1", 1, facade.noWaits.length );
    }
    
    /**
     * Verify that we can remove an item.
     */
    public void testAddThenRemoveNoWait_NotInList()
    {
        // SETUP
        LateralCacheNoWait[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( "testCache1" );
        
        LateralCacheNoWaitFacade facade = new LateralCacheNoWaitFacade( noWaits, cattr );
        
        LateralCache cache = new LateralCache( cattr );
        LateralCacheNoWait noWait = new LateralCacheNoWait( cache );       
        
        // DO WORK
        facade.removeNoWait( noWait );
        
        // VERIFY
        assertFalse( "Should not be in the list.", facade.containsNoWait( noWait ) );
    }    
}

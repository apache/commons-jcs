package org.apache.jcs.auxiliary.lateral;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;

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
        @SuppressWarnings("unchecked")
        LateralCacheNoWait<String, String>[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( "testCache1" );

        LateralCacheNoWaitFacade<String, String> facade = new LateralCacheNoWaitFacade<String, String>( null, noWaits, cattr );

        LateralCache<String, String> cache = new LateralCache<String, String>( cattr );
        LateralCacheNoWait<String, String> noWait = new LateralCacheNoWait<String, String>( cache );

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
        @SuppressWarnings("unchecked")
        LateralCacheNoWait<String, String>[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( "testCache1" );

        LateralCacheNoWaitFacade<String, String> facade = new LateralCacheNoWaitFacade<String, String>( null, noWaits, cattr );

        LateralCache<String, String> cache = new LateralCache<String, String>( cattr );
        LateralCacheNoWait<String, String> noWait = new LateralCacheNoWait<String, String>( cache );
        LateralCacheNoWait<String, String> noWait2 = new LateralCacheNoWait<String, String>( cache );

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
        @SuppressWarnings("unchecked")
        LateralCacheNoWait<String, String>[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( "testCache1" );

        LateralCacheNoWaitFacade<String, String> facade = new LateralCacheNoWaitFacade<String, String>( null, noWaits, cattr );

        LateralCache<String, String> cache = new LateralCache<String, String>( cattr );
        LateralCacheNoWait<String, String> noWait = new LateralCacheNoWait<String, String>( cache );

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
        @SuppressWarnings("unchecked")
        LateralCacheNoWait<String, String>[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( "testCache1" );

        LateralCacheNoWaitFacade<String, String> facade = new LateralCacheNoWaitFacade<String, String>( null, noWaits, cattr );

        LateralCache<String, String> cache = new LateralCache<String, String>( cattr );
        LateralCacheNoWait<String, String> noWait = new LateralCacheNoWait<String, String>( cache );

        // DO WORK
        facade.removeNoWait( noWait );

        // VERIFY
        assertFalse( "Should not be in the list.", facade.containsNoWait( noWait ) );
    }
}

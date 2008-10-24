package org.apache.jcs.utils.match;

import java.util.Set;

import junit.framework.TestCase;

/** Unit tests for the key matcher. */
public class KeyMatcherUtilUnitTest
    extends TestCase
{
    /**
     * Verify that the matching method works.
     */
    public void testGetMatchingKeysFromArray_AllMatch()
    {
        // SETUP
        int numToInsertPrefix1 = 10;
        int araySize = numToInsertPrefix1;
        Object[] keyArray = new Object[araySize];

        String keyprefix1 = "MyPrefixC";

        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix1; i++ )
        {
            keyArray[i] = keyprefix1 + String.valueOf( i );
        }

        // DO WORK
        Set result1 = KeyMatcherUtil.getMatchingKeysFromArray( keyprefix1 + ".", keyArray );

        // VERIFY
        assertEquals( "Wrong number returned 1: " + result1, numToInsertPrefix1, result1.size() );
    }

    /**
     * Verify that the matching method works.
     */
    public void testGetMatchingKeysFromArray_AllMatchFirstNull()
    {
        // SETUP
        int numToInsertPrefix1 = 10;
        int araySize = numToInsertPrefix1 + 1;
        Object[] keyArray = new Object[araySize];

        String keyprefix1 = "MyPrefixC";

        // insert with prefix1
        for ( int i = 1; i < numToInsertPrefix1 + 1; i++ )
        {
            keyArray[i] = keyprefix1 + String.valueOf( i );
        }

        // DO WORK
        Set result1 = KeyMatcherUtil.getMatchingKeysFromArray( keyprefix1 + "\\S+", keyArray );

        // VERIFY
        assertEquals( "Wrong number returned 1: " + result1, numToInsertPrefix1, result1.size() );
    }

    /**
     * Verify that the matching method works.
     */
    public void testGetMathcingKeysFromArray_TwoTypes()
    {
        // SETUP
        int numToInsertPrefix1 = 10;
        int numToInsertPrefix2 = 50;
        int araySize = numToInsertPrefix1 + numToInsertPrefix2;
        Object[] keyArray = new Object[araySize];

        String keyprefix1 = "MyPrefixA";
        String keyprefix2 = "MyPrefixB";

        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix1; i++ )
        {
            keyArray[i] = keyprefix1 + String.valueOf( i );
        }

        // insert with prefix2
        for ( int i = numToInsertPrefix1; i < numToInsertPrefix2 + numToInsertPrefix1; i++ )
        {
            keyArray[i] = keyprefix2 + String.valueOf( i );
        }

        // DO WORK
        Set result1 = KeyMatcherUtil.getMatchingKeysFromArray( keyprefix1 + ".+", keyArray );
        Set result2 = KeyMatcherUtil.getMatchingKeysFromArray( keyprefix2 + ".+", keyArray );

        // VERIFY
        assertEquals( "Wrong number returned 1: " + result1, numToInsertPrefix1, result1.size() );
        assertEquals( "Wrong number returned 2: " + result2, numToInsertPrefix2, result2.size() );
    }
}

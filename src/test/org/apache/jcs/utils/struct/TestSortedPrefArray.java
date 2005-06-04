package org.apache.jcs.utils.struct;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import junit.framework.TestCase;

/**
 * Tests the SortedPrefArray used by the recycle bin.
 * 
 * @author aaronsm
 *  
 */
public class TestSortedPrefArray
    extends TestCase
{

    /**
     * Constructor for the TestSimpleLoad object
     * 
     * @param testName
     *            Description of the Parameter
     */
    public TestSortedPrefArray( String testName )
    {
        super( testName );
    }

    /**
     * Description of the Method
     * 
     * @param args
     *            Description of the Parameter
     */
    public static void main( String args[] )
    {
        String[] testCaseName = { TestSortedPrefArray.class.getName() };
        junit.textui.TestRunner.main( testCaseName );
    }

    /**
     * 
     * 
     * @exception Exception
     */
    public void testLargePref()
        throws Exception
    {

        int maxSize = 25;

        SortedPreferentialArray array = new SortedPreferentialArray( maxSize );
        //array.setPreferLarge( false );
        array.setPreferLarge( true );
        String[] elem = {
            "10",
            "11",
            "01",
            "02",
            "03",
            "04",
            "05",
            "08",
            "07",
            "06",
            "09",
            "12",
            "13",
            "15",
            "14",
            "20",
            "25",
            "29",
            "28",
            "16",
            "17",
            "96",
            "00",
            "72",
            "39",
            "55",
            "44",
            "26",
            "22",
            "59",
            "38",
            "16",
            "27" };

        // put more than the max in a random order
        for ( int i = 0; i < elem.length; i++ )
        {
            array.add( elem[i] );
            System.out.println( array.dumpArray() );
        }

        assertEquals( "Size was not as expected.", maxSize, array.size() );

        // this is a fragile test, since it relies on a hardcoded array
        String smallest = (String) array.getSmallest();
        assertEquals( "smallest should be 08", "08", smallest );

        String largest = (String) array.getLargest();
        assertEquals( "Largest should be 96", "96", largest );

        // this should take 96;
        String taken = (String) array.takeNearestLargerOrEqual( "95" );
        assertEquals( "Taken should be 96", "96", taken );
        assertEquals( "Size was not as expected.", ( maxSize - 1 ), array.size() );

        System.out.println( array.dumpArray() );

    }

    /**
     * Verify that we don't get an error when taking from an empty array.
     * 
     * @throws Exception
     */
    public void testEmptyTake()
        throws Exception
    {

        int maxSize = 25;
        SortedPreferentialArray array = new SortedPreferentialArray( maxSize );
        array.setPreferLarge( true );
        for ( int i = 0; i < maxSize; i++ )
        {
            String taken = (String) array.takeNearestLargerOrEqual( String.valueOf( i ) );
            assertNull( "taken should be null, since nothing was in the array", taken );
        }

    }

    /**
     * Verify that we don't get a null pointer if we insert a null.
     * 
     * @throws Exception
     */
    public void testNullInsertion()
        throws Exception
    {

        int maxSize = 25;
        SortedPreferentialArray array = new SortedPreferentialArray( maxSize );
        array.setPreferLarge( true );

        String[] elem = {
            "10",
            "11",
            "01",
            "02",
            "03",
            "04",
            "05",
            "08",
            "07",
            "06",
            "09",
            "12",
            "13",
            "15",
            "14",
            "20",
            "25",
            "29",
            "28",
            "16",
            "17",
            "96",
            "00",
            "72",
            "39",
            "55",
            "44",
            "26",
            "22",
            "59",
            "38",
            "16",
            "27" };

        // put more than the max in a random order
        for ( int i = 0; i < elem.length; i++ )
        {
            array.add( elem[i] );
        }
        System.out.println( array.dumpArray() );

        assertEquals( "Size was not as expected.", maxSize, array.size() );

        try
        {
            // should not get an error
            array.add( null );
        }
        catch ( NullPointerException e )
        {
            fail( "Got a null pointer inserting a null" );
        }

    }

    /**
     * Verify that we don't get an npe when taking with a null
     * 
     * @throws Exception
     */
    public void testNullTake()
        throws Exception
    {

        int maxSize = 25;
        SortedPreferentialArray array = new SortedPreferentialArray( maxSize );
        array.setPreferLarge( true );
        try
        {
            String taken = (String) array.takeNearestLargerOrEqual( null );
            assertNull( "taken should be null, since nothing was in the array", taken );
        }
        catch ( NullPointerException e )
        {
            fail( "Got a null pointer trying to take with a null" );
        }

    }

}

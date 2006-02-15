package org.apache.jcs.admin;

import junit.framework.TestCase;

/**
 * Tests for the counting only output stream.
 *
 * @author Aaron Smuts
 *
 */
public class CountingStreamUnitTest
    extends TestCase
{

    /**
     * Write a single byte and verify the count.
     * 
     * @throws Exception
     */
    public void testSingleByte() throws Exception
    {
        CountingOnlyOutputStream out = new CountingOnlyOutputStream();
        out.write( 1 );
        assertEquals( "Wrong number of bytes written.", 1, out.getCount() );
        out.write( 1 );
        assertEquals( "Wrong number of bytes written.", 2, out.getCount() );
    }
    
    /**
     * This should count the size of the array.
     * 
     * @throws Exception
     */
    public void testByteArray() throws Exception
    {
        CountingOnlyOutputStream out = new CountingOnlyOutputStream();
        byte[] array = new byte[]{1,2,3,4,5};
        out.write( array );
        assertEquals( "Wrong number of bytes written.", array.length, out.getCount() );        
    } 
    
    /**
     * This should count the len -- the tird arg
     * 
     * @throws Exception
     */
    public void testByteArrayLenCount() throws Exception
    {
        CountingOnlyOutputStream out = new CountingOnlyOutputStream();
        byte[] array = new byte[]{1,2,3,4,5};
        int len = 3;
        out.write( array, 0, len );
        assertEquals( "Wrong number of bytes written.", len, out.getCount() );        
    }     
}

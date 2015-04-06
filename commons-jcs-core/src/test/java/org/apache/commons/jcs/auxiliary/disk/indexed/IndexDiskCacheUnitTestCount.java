package org.apache.commons.jcs.auxiliary.disk.indexed;

import java.io.IOException;

import org.apache.commons.jcs.auxiliary.disk.behavior.IDiskCacheAttributes.DiskLimitType;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;

public class IndexDiskCacheUnitTestCount extends IndexDiskCacheUnitTest {

	@Override
	public IndexedDiskCacheAttributes getCacheAttributes() {
		IndexedDiskCacheAttributes ret = new IndexedDiskCacheAttributes();
		ret.setDiskLimitType(DiskLimitType.COUNT);
		return ret;
	}
	  public void testRecycleBin()
		        throws IOException
		    {
		        IndexedDiskCacheAttributes cattr = getCacheAttributes();
		        cattr.setCacheName( "testRemoveItems" );
		        cattr.setMaxRecycleBinSize( 2 );
		        cattr.setOptimizeAtRemoveCount( 7 );
		        cattr.setMaxKeySize( 5 );
		        cattr.setMaxPurgatorySize( 0 );
		        cattr.setDiskPath( "target/test-sandbox/BreakIndexTest" );
		        IndexedDiskCache<String, String> disk = new IndexedDiskCache<String, String>( cattr );

		        String[] test = { "a", "bb", "ccc", "dddd", "eeeee", "ffffff", "ggggggg", "hhhhhhhhh", "iiiiiiiiii" };
		        String[] expect = { null, "bb", "ccc", null, null, "ffffff", null, "hhhhhhhhh", "iiiiiiiiii" };

		        //System.out.println( "------------------------- testRecycleBin " );

		        for ( int i = 0; i < 6; i++ )
		        {
		            ICacheElement<String, String> element = new CacheElement<String, String>( "testRecycleBin", "key:" + test[i], test[i] );
		            //System.out.println( "About to add " + "key:" + test[i] + " i = " + i );
		            disk.processUpdate( element );
		        }

		        for ( int i = 3; i < 5; i++ )
		        {
		            //System.out.println( "About to remove " + "key:" + test[i] + " i = " + i );
		            disk.remove( "key:" + test[i] );
		        }

		        // there was a bug where 7 would try to be put in the empty slot left by 4's removal, but it
		        // will not fit.
		        for ( int i = 7; i < 9; i++ )
		        {
		            ICacheElement<String, String> element = new CacheElement<String, String>( "testRecycleBin", "key:" + test[i], test[i] );
		            //System.out.println( "About to add " + "key:" + test[i] + " i = " + i );
		            disk.processUpdate( element );
		        }

		        try
		        {
		            for ( int i = 0; i < 9; i++ )
		            {
		                ICacheElement<String, String> element = disk.get( "key:" + test[i] );
		                if ( element != null )
		                {
		                    //System.out.println( "element = " + element.getVal() );
		                }
		                else
		                {
		                    //System.out.println( "null --" + "key:" + test[i] );
		                }

		                String expectedValue = expect[i];
		                if ( expectedValue == null )
		                {
		                    assertNull( "Expected a null element", element );
		                }
		                else
		                {
		                    assertNotNull( "The element for key [" + "key:" + test[i] + "] should not be null. i = " + i,
		                                   element );
		                    assertEquals( "Elements contents do not match expected", element.getVal(), expectedValue );
		                }
		            }
		        }
		        catch ( Exception e )
		        {
		            e.printStackTrace();
		            fail( "Should not get an exception: " + e.toString() );
		        }

		        disk.removeAll();
		    }
}

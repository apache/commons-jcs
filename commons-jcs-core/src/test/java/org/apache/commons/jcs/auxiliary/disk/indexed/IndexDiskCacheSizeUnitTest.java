package org.apache.commons.jcs.auxiliary.disk.indexed;

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

import java.io.IOException;

import org.apache.commons.jcs.auxiliary.disk.DiskTestObject;
import org.apache.commons.jcs.auxiliary.disk.behavior.IDiskCacheAttributes.DiskLimitType;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;

public class IndexDiskCacheSizeUnitTest extends IndexDiskCacheUnitTestAbstract {

	@Override
	public IndexedDiskCacheAttributes getCacheAttributes() {
		IndexedDiskCacheAttributes ret = new IndexedDiskCacheAttributes();
		ret.setDiskLimitType(DiskLimitType.SIZE);
		return ret;
	}
	  public void testRecycleBin()
		        throws IOException
		    {
		        IndexedDiskCacheAttributes cattr = getCacheAttributes();
		        cattr.setCacheName( "testRemoveItems" );
		        cattr.setOptimizeAtRemoveCount( 7 );
		        cattr.setMaxKeySize( 8); // 1kb DiskTestObject takes 1420 bytes, so 5*1420 = 7100, so to keep 5 ojbects, we need max key size of 8
		        cattr.setMaxPurgatorySize( 0 );
		        cattr.setDiskPath( "target/test-sandbox/BreakIndexTest" );
		        IndexedDiskCache<String, DiskTestObject> disk = new IndexedDiskCache<String, DiskTestObject>( cattr );

		        String[] test = { "a", "bb", "ccc", "dddd", "eeeee", "ffffff", "ggggggg", "hhhhhhhhh", "iiiiiiiiii" };
		        String[] expect = { null, "bb", "ccc", null, null, "ffffff", null, "hhhhhhhhh", "iiiiiiiiii" };
		        DiskTestObject value = DiskTestObjectUtil.createCacheElementsWithTestObjects( 1, 1, cattr .getCacheName())[0].getVal();
		        //System.out.println( "------------------------- testRecycleBin " );

		        for ( int i = 0; i < 6; i++ )
		        {
		            ICacheElement<String, DiskTestObject> element = new CacheElement<String, DiskTestObject>( "testRecycleBin", "key:" + test[i], value);
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
		            ICacheElement<String, DiskTestObject> element = new CacheElement<String, DiskTestObject>( "testRecycleBin", "key:" + test[i], value);
		            //System.out.println( "About to add " + "key:" + test[i] + " i = " + i );
		            disk.processUpdate( element );
		        }

		        try
		        {
		            for ( int i = 0; i < 9; i++ )
		            {
		                ICacheElement<String, DiskTestObject> element = disk.get( "key:" + test[i] );
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
		                    assertEquals( "Elements contents do not match expected", element.getVal(), value );
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

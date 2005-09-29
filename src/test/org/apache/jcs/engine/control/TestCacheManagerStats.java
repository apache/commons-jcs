package org.apache.jcs.engine.control;

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

import org.apache.jcs.JCS;
import org.apache.jcs.engine.stats.behavior.ICacheStats;

import junit.framework.TestCase;

/**
 * @author Aaron Smuts
 *
 */
public class TestCacheManagerStats
    extends TestCase
{

    public void testSimpleGetStats() throws Exception
    {
        JCS cache = JCS.getInstance( "testCache1" );

        // 1 miss, 1 hit, 1 put
        cache.get( "testKey" );
        cache.put( "testkey", "testdata" );
        cache.get( "testKey" );
        
        CompositeCacheManager mgr = CompositeCacheManager.getInstance();
        String statsString = mgr.getStats();
        
        System.out.println( statsString );
        
        assertTrue( "Should have the cacheName in here.", statsString.indexOf("testCache1") != -1 );
        
        ICacheStats[] stats = mgr.getStatistics();
        int statsLen = stats.length;
        for ( int i = 0; i < statsLen; i++ )
        {
            //buf.append( stats[i] );
            // TODO finish
        }
    }
    
}

package org.apache.commons.jcs4;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.jcs4.access.GroupCacheAccess;
import org.apache.commons.jcs4.access.exception.CacheException;
import org.apache.commons.jcs4.utils.timing.ElapsedTimer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test Case for JCS-73, modeled after the Groovy code by Alexander Kleymenov
 */
class JCSConcurrentCacheAccessUnitTest
{
    /**
     * Worker thread
     */
	private void work(final GroupCacheAccess<Integer, String> cache, final String name, final CountDownLatch latch)
	{
        // put value in the cache
        try
        {
            cache.putInGroup(Integer.valueOf(0), group, String.valueOf(0));
        }
        catch (final CacheException e)
        {
            // continue
        }

		for (int idx = 1; idx < LOOPS; idx++)
		{
			// get previously stored value
            String res = cache.getFromGroup(Integer.valueOf(idx-1), group);

            if (res == null)
            {
                // null value got inspite of the fact it was placed in cache!
                System.out.println("ERROR: for " + idx + " in " + name);
                errcount.incrementAndGet();

                // try to get the value again:
                int n = 5;
                while (n-- > 0)
                {
                    res = cache.getFromGroup(Integer.valueOf(idx-1), group);
                    if (res != null)
                    {
                        // the value finally appeared in cache
                    	System.out.println("ERROR FIXED for " + idx + ": " + res + " " + name);
                    	errcount.decrementAndGet();
                        break;
                    }

                    System.out.println("ERROR STILL PERSISTS for " + idx + " in " + name);
                    try
                    {
						Thread.sleep(1000);
					}
                    catch (final InterruptedException e)
					{
						// continue
					}
                }
            }

            if (!String.valueOf(idx-1).equals(res))
            {
                valueMismatchList.add(String.format("Values do not match: %s - %s", String.valueOf(idx-1), res));
            }

			 // put value in the cache
	        try
	        {
				cache.putInGroup(Integer.valueOf(idx), group, String.valueOf(idx));
			}
	        catch (final CacheException e)
	        {
	        	// continue
			}
		}

		latch.countDown();
	}

    private final static int THREADS = 20;
    private final static int LOOPS = 10000;

    /**
     * the group name
     */
    protected String group = "group";

    /**
     * the error count
     */
    protected AtomicInteger errcount;

    /**
     * Collect all value mismatches
     */
    protected List<String> valueMismatchList;

    @BeforeEach
    void setUp()
        throws Exception
	{
        JCS.setConfigFilename( "/TestJCS-73.ccf" );
        errcount = new AtomicInteger();
        valueMismatchList = new CopyOnWriteArrayList<>();
	}

    private void testConcurrentAccess(String cacheName)
            throws Exception
    {
        System.out.println(cacheName);
        final GroupCacheAccess<Integer, String> cache = JCS.getGroupCacheInstance(cacheName);
        final CountDownLatch latch = new CountDownLatch(THREADS);
        final ElapsedTimer timer = new ElapsedTimer();

        for (int i = 0; i < THREADS; i++)
        {
            final String threadName = "Thread-" + i;
            new Thread(() -> work(cache, threadName, latch)).start();
        }

        latch.await(THREADS, TimeUnit.SECONDS);
        double ms = timer.getElapsedTime();
        System.out.println(cacheName + ": " + ms + " ms");
        System.out.println(cacheName + ": " + 1000.0 * THREADS * LOOPS / ms + " ops/s");

        assertEquals( 0, errcount.intValue(), cacheName + " Error count should be 0" );
        for (final String msg : valueMismatchList)
        {
            System.out.println(msg);
        }
        assertEquals( 0, valueMismatchList.size(), cacheName + " Value mismatch count should be 0" );
        errcount.set(0);
        valueMismatchList.clear();
        cache.dispose();
    }

    /**
     *
     * @throws Exception
     */
    @Test
    void testConcurrentAccess()
        throws Exception
    {
        testConcurrentAccess("lhm_cache");
        testConcurrentAccess("soft_cache");
        testConcurrentAccess("lru_cache");
        testConcurrentAccess("mru_cache");
        testConcurrentAccess("lru_cache_with_disk");
    }
}

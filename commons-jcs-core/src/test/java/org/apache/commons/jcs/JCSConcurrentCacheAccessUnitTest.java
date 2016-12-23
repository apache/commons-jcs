package org.apache.commons.jcs;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.commons.jcs.access.GroupCacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;

/**
 * Test Case for JCS-73, modeled after the Groovy code by Alexander Kleymenov
 *
 * @author Thomas Vandahl
 *
 */
public class JCSConcurrentCacheAccessUnitTest extends TestCase
{
    private final static int THREADS = 10;
    private final static int LOOPS = 10000;

    /**
     * the cache instance
     */
    protected GroupCacheAccess<Integer, String> cache;

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

    @Override
	protected void setUp() throws Exception
	{
        super.setUp();
        JCS.setConfigFilename( "/TestJCS-73.ccf" );
        cache = JCS.getGroupCacheInstance( "cache" );
        errcount = new AtomicInteger(0);
        valueMismatchList = Collections.synchronizedList(new ArrayList<String>());
	}

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        cache.clear();
        cache.dispose();
    }

    /**
     * Worker thread
     */
    protected class Worker extends Thread
    {
    	@Override
		public void run()
		{
			String name = getName();

			for (int idx = 0; idx < LOOPS; idx++)
			{
				if (idx > 0)
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
		                    catch (InterruptedException e)
							{
								// continue
							}
		                }
		            }

		            if (!String.valueOf(idx-1).equals(res))
		            {
		                valueMismatchList.add(String.format("Values do not match: %s - %s", String.valueOf(idx-1), res));
		            }
				}

				 // put value in the cache
		        try
		        {
					cache.putInGroup(Integer.valueOf(idx), group, String.valueOf(idx));
				}
		        catch (CacheException e)
		        {
		        	// continue
				}

//		        if ((idx % 1000) == 0)
//		        {
//		        	System.out.println(name + " " + idx);
//		        }
			}

		}
    }

	/**
     *
     * @throws Exception
     */
    public void testConcurrentAccess()
        throws Exception
    {
    	Worker[] worker = new Worker[THREADS];

        for (int i = 0; i < THREADS; i++)
        {
        	worker[i] = new Worker();
        	worker[i].start();
        }

        for (int i = 0; i < THREADS; i++)
        {
        	worker[i].join();
        }

        assertEquals("Error count should be 0",  0, errcount.intValue());
        for (String msg : valueMismatchList)
        {
            System.out.println(msg);
        }
        assertEquals("Value mismatch count should be 0",  0, valueMismatchList.size());
    }

}

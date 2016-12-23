package org.apache.commons.jcs.utils.threadpool;

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

import junit.framework.TestCase;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * This test is experimental. I'm trying to find out if the max size setting will result in the
 * removal of threads.
 * <p>
 * @author Aaron Smuts
 */
public class ThreadPoolUnitTest
    extends TestCase
{
    /**
     * Make sure that the max size setting takes effect before the idle time is reached.
     * <p>
     * We just want to ensure that we can adjust the max size of an active pool.
     * <p>
     * http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/PooledExecutor.html#setMaximumPoolSize(int)
     * @throws Exception
     */
    public void testMaxReduction()
        throws Exception
    {
        //ThreadPoolManager.setPropsFileName( "thread_pool_test.properties" );
        ThreadPoolExecutor pool = ThreadPoolManager.getInstance().getPool( "maxtest" );

        //System.out.println( "pool = " + pool );
        pool.setMaximumPoolSize( 5 );
        //System.out.println( "current size before execute = " + pool.getPool().getPoolSize() );

        // add 6
        for ( int i = 1; i < 30; i++ )
        {
            final int cnt = i;
            pool.execute( new Runnable()
            {

                @Override
                public void run()
                {
                    try
                    {
                        //System.out.println( cnt );
//                        System.out.println( "count = " + cnt + " before sleep current size = " + myPool.getPoolSize() );
                        Thread.sleep( 200 / cnt );
                        //System.out.println( "count = " + cnt + " after sleep current size = " + myPool.getPool().getPoolSize() );
                    }
                    catch ( InterruptedException e )
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } );
        }

        //System.out.println( "current size = " + pool.getPool().getPoolSize() );
        pool.setMaximumPoolSize( 4 );
        //Thread.sleep( 200 );
        //System.out.println( "current size after set size to 4= " + pool.getPool().getPoolSize() );
        Thread.sleep( 200 );
        //System.out.println( "current size again after sleep = " + pool.getPool().getPoolSize() );
        assertEquals( "Pool size should have been reduced.", 4, pool.getPoolSize() );
    }
}

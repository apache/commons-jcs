package org.apache.commons.jcs4.engine.control.event;

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/** Tests the lifecycle of the element-event worker owned by the queue. */
class ElementEventQueueUnitTest
{
    private static final String THREAD_PREFIX = "JCS-ElementEventQueue-";

    @Test
    void testDisposeStopsOwnedWorkerThread()
        throws InterruptedException
    {
        final Set<Long> threadsBefore = eventQueueThreadIds();
        final ElementEventQueue queue = new ElementEventQueue();
        final Thread worker = waitForNewWorker( threadsBefore );

        assertNotNull( worker, "The element-event queue did not start its worker" );

        queue.dispose();
        worker.join( 2000 );

        assertFalse( worker.isAlive(), "The element-event worker is still alive after dispose" );

        // Disposal is a lifecycle operation and must be safe when invoked more than once.
        queue.dispose();
    }

    private static Set<Long> eventQueueThreadIds()
    {
        final Set<Long> result = new HashSet<>();
        for ( final Thread thread : Thread.getAllStackTraces().keySet() )
        {
            if (thread.getName().startsWith( THREAD_PREFIX ))
            {
                result.add( thread.getId() );
            }
        }
        return result;
    }

    private static Thread waitForNewWorker( final Set<Long> threadsBefore )
        throws InterruptedException
    {
        final long deadline = System.currentTimeMillis() + 2000;
        do
        {
            for ( final Thread thread : Thread.getAllStackTraces().keySet() )
            {
                if (thread.getName().startsWith( THREAD_PREFIX ) && !threadsBefore.contains( thread.getId() ))
                {
                    return thread;
                }
            }
            Thread.sleep( 10 );
        }
        while (System.currentTimeMillis() < deadline);

        return null;
    }
}

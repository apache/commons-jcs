package org.apache.commons.jcs3.engine.control.event;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.engine.ElementAttributes;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.apache.commons.jcs3.engine.control.event.behavior.IElementEvent;
import org.apache.commons.jcs3.engine.control.event.behavior.IElementEventHandler;
import org.junit.Before;
import org.junit.Test;

/**
 * This test suite verifies that the basic ElementEvent are called as they should be.
 */
public class SimpleEventHandlingUnitTest
{
    /** Items to test with */
    private static final int items = 2000;

    /** Event handler instance */
    private MyEventHandler meh;

    /**
     * Test setup with expected configuration parameters.
     */
    @Before
    public void setUp()
    {
        JCS.setConfigFilename( "/TestSimpleEventHandling.ccf" );
        this.meh = new MyEventHandler();
    }

    /**
     * Verify that the spooled event is called as expected.
     * <p>
     * @throws Exception Description of the Exception
     */
    @Test
    public void testSpoolEvent()
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( "WithDisk" );
        // this should add the event handler to all items as they are created.
        final IElementAttributes attributes = jcs.getDefaultElementAttributes();
        attributes.addElementEventHandler( meh );
        jcs.setDefaultElementAttributes( attributes );

        // DO WORK
        // put them in
        for ( int i = 0; i < items; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }
        // wait a bit for it to finish
        Thread.sleep( items / 20 );

        // VERIFY
        // test to see if the count is right
        assertTrue( "The number of ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE events [" + meh.getSpoolCount()
            + "] does not equal the number expected [" + items + "]", meh.getSpoolCount() >= items );
    }

    /**
     * Test overflow with no disk configured for the region.
     * <p>
     * @throws Exception
     */
    @Test
    public void testSpoolNoDiskEvent()
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( "NoDisk" );

        // this should add the event handler to all items as they are created.
        final IElementAttributes attributes = jcs.getDefaultElementAttributes();
        attributes.addElementEventHandler( meh );
        jcs.setDefaultElementAttributes( attributes );

        // put them in
        for ( int i = 0; i < items; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        // wait a bit for it to finish
        Thread.sleep( items / 20 );

        // test to see if the count is right
        assertTrue( "The number of ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE events  [" + meh.getSpoolNoDiskCount()
            + "] does not equal the number expected.", meh.getSpoolNoDiskCount() >= items );

    }

    /**
     * Test the ELEMENT_EVENT_SPOOLED_NOT_ALLOWED event.
     * @throws Exception
     */
    @Test
    public void testSpoolNotAllowedEvent()
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( "DiskButNotAllowed" );
        // this should add the event handler to all items as they are created.
        final IElementAttributes attributes = jcs.getDefaultElementAttributes();
        attributes.addElementEventHandler( meh );
        jcs.setDefaultElementAttributes( attributes );

        // put them in
        for ( int i = 0; i < items; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        // wait a bit for it to finish
        Thread.sleep( items / 20 );

        // test to see if the count is right
        assertTrue( "The number of ELEMENT_EVENT_SPOOLED_NOT_ALLOWED events [" + meh.getSpoolNotAllowedCount()
            + "] does not equal the number expected.", meh.getSpoolNotAllowedCount() >= items );

    }

    /**
     * Test the ELEMENT_EVENT_SPOOLED_NOT_ALLOWED event.
     * @throws Exception
     */
    @Test
    public void testSpoolNotAllowedEventOnItem()
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( "DiskButNotAllowed" );
        // this should add the event handler to all items as they are created.
        //IElementAttributes attributes = jcs.getDefaultElementAttributes();
        //attributes.addElementEventHandler( meh );
        //jcs.setDefaultElementAttributes( attributes );

        // put them in
        for ( int i = 0; i < items; i++ )
        {
            final IElementAttributes attributes = jcs.getDefaultElementAttributes();
            attributes.addElementEventHandler( meh );
            jcs.put( i + ":key", "data" + i, attributes );
        }

        // wait a bit for it to finish
        Thread.sleep( items / 20 );

        // test to see if the count is right
        assertTrue( "The number of ELEMENT_EVENT_SPOOLED_NOT_ALLOWED events [" + meh.getSpoolNotAllowedCount()
            + "] does not equal the number expected.", meh.getSpoolNotAllowedCount() >= items );

    }

    /**
     * Test the ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST event.
     * @throws Exception
     */
    @Test
    public void testExceededMaxlifeOnrequestEvent()
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( "Maxlife" );
        // this should add the event handler to all items as they are created.
        final IElementAttributes attributes = jcs.getDefaultElementAttributes();
        attributes.addElementEventHandler( meh );
        jcs.setDefaultElementAttributes( attributes );

        // put them in
        for ( int i = 0; i < 200; i++ )
        {
            jcs.put( i + ":key", "data" + i);
        }

        // wait a bit for the items to expire
        Thread.sleep(attributes.getMaxLife() * 1000 + 100);

        for ( int i = 0; i < 200; i++ )
        {
            final String value = jcs.get( i + ":key");
            assertNull("Item should be null for key " + i + ":key, but is " + value, value);
        }

        // wait a bit for it to finish
        Thread.sleep( 100 );

        // test to see if the count is right
        assertTrue( "The number of ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST events [" + meh.getExceededMaxlifeCount()
            + "] does not equal the number expected.", meh.getExceededMaxlifeCount() >= 200 );
    }

    /**
     * Test the ELEMENT_EVENT_EXCEEDED_IDLETIME_ONREQUEST event.
     * @throws Exception
     */
    @Test
    public void testExceededIdletimeOnrequestEvent()
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( "Idletime" );
        // this should add the event handler to all items as they are created.
        final IElementAttributes attributes = jcs.getDefaultElementAttributes();
        attributes.addElementEventHandler( meh );
        jcs.setDefaultElementAttributes( attributes );

        // put them in
        for ( int i = 0; i < 200; i++ )
        {
            jcs.put( i + ":key", "data" + i);
        }

        // update access time
        for ( int i = 0; i < 200; i++ )
        {
            final String value = jcs.get( i + ":key");
            assertNotNull("Item should not be null for key " + i + ":key", value);
        }

        // wait a bit for the items to expire
        Thread.sleep(attributes.getIdleTime() * 1000 + 100);

        for ( int i = 0; i < 200; i++ )
        {
            final String value = jcs.get( i + ":key");
            assertNull("Item should be null for key " + i + ":key, but is " + value, value);
        }

        // wait a bit for it to finish
        Thread.sleep( 100 );

        // test to see if the count is right
        assertTrue( "The number of ELEMENT_EVENT_EXCEEDED_IDLETIME_ONREQUEST events [" + meh.getExceededIdletimeCount()
            + "] does not equal the number expected.", meh.getExceededIdletimeCount() >= 200 );
    }

    /**
     * Test that cloned ElementAttributes have different creation times.
     * @throws Exception
     */
    @Test
    public void testElementAttributesCreationTime()
        throws Exception
    {
    	final ElementAttributes elem1 = new ElementAttributes();
    	final long ctime1 = elem1.getCreateTime();

    	Thread.sleep(10);

    	final IElementAttributes elem2 = elem1.clone();
    	final long ctime2 = elem2.getCreateTime();

    	assertFalse("Creation times should be different", ctime1 == ctime2);
    }

    /**
     * Simple event counter used to verify test results.
     */
    public static class MyEventHandler
        implements IElementEventHandler
    {
        /** times spool called */
        private int spoolCount;

        /** times spool not allowed */
        private int spoolNotAllowedCount;

        /** times spool without disk */
        private int spoolNoDiskCount;

        /** times exceeded maxlife */
        private int exceededMaxlifeCount;

        /** times exceeded idle time */
        private int exceededIdletimeCount;

        /**
         * @param event
         */
        @Override
        public synchronized <T> void handleElementEvent( final IElementEvent<T> event )
        {
            //System.out.println( "Handling Event of Type " +
            // event.getElementEvent() );

            switch (event.getElementEvent())
            {
                case SPOOLED_DISK_AVAILABLE:
                //System.out.println( "Handling Event of Type
                // ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE, " + getSpoolCount() );
                spoolCount++;
                break;

                case SPOOLED_NOT_ALLOWED:
                spoolNotAllowedCount++;
                break;

                case SPOOLED_DISK_NOT_AVAILABLE:
                spoolNoDiskCount++;
                break;

                case EXCEEDED_MAXLIFE_ONREQUEST:
                exceededMaxlifeCount++;
                break;

                case EXCEEDED_IDLETIME_ONREQUEST:
                exceededIdletimeCount++;
                break;

                case EXCEEDED_IDLETIME_BACKGROUND:
                break;

                case EXCEEDED_MAXLIFE_BACKGROUND:
                break;
            }
        }

        /**
         * @return Returns the spoolCount.
         */
        protected int getSpoolCount()
        {
            return spoolCount;
        }

        /**
         * @return Returns the spoolNotAllowedCount.
         */
        protected int getSpoolNotAllowedCount()
        {
            return spoolNotAllowedCount;
        }

        /**
         * @return Returns the spoolNoDiskCount.
         */
        protected int getSpoolNoDiskCount()
        {
            return spoolNoDiskCount;
        }

        /**
         * @return the exceededMaxlifeCount
         */
        protected int getExceededMaxlifeCount()
        {
            return exceededMaxlifeCount;
        }

        /**
         * @return the exceededIdletimeCount
         */
        protected int getExceededIdletimeCount()
        {
            return exceededIdletimeCount;
        }
    }

}

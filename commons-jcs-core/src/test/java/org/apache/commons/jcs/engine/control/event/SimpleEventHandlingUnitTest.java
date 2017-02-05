package org.apache.commons.jcs.engine.control.event;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;
import org.apache.commons.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.commons.jcs.engine.control.event.behavior.IElementEventHandler;

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

/**
 * This test suite verifies that the basic ElementEvent are called as they should be.
 */
public class SimpleEventHandlingUnitTest
    extends TestCase
{
    /** Items to test with */
    private static int items = 20000;

    /**
     * Test setup with expected configuration parameters.
     */
    @Override
    public void setUp()
    {
        JCS.setConfigFilename( "/TestSimpleEventHandling.ccf" );
    }

    /**
     * Verify that the spooled event is called as expected.
     * <p>
     * @throws Exception Description of the Exception
     */
    public void testSpoolEvent()
        throws Exception
    {
        // SETUP
        MyEventHandler meh = new MyEventHandler();

        CacheAccess<String, String> jcs = JCS.getInstance( "WithDisk" );
        // this should add the event handler to all items as they are created.
        IElementAttributes attributes = jcs.getDefaultElementAttributes();
        attributes.addElementEventHandler( meh );
        jcs.setDefaultElementAttributes( attributes );

        // DO WORK
        // put them in
        for ( int i = 0; i <= items; i++ )
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
    public void testSpoolNoDiskEvent()
        throws Exception
    {
        CacheAccess<String, String> jcs = JCS.getInstance( "NoDisk" );

        MyEventHandler meh = new MyEventHandler();

        // this should add the event handler to all items as they are created.
        IElementAttributes attributes = jcs.getDefaultElementAttributes();
        attributes.addElementEventHandler( meh );
        jcs.setDefaultElementAttributes( attributes );

        // put them in
        for ( int i = 0; i <= items; i++ )
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
    public void testSpoolNotAllowedEvent()
        throws Exception
    {
        MyEventHandler meh = new MyEventHandler();

        CacheAccess<String, String> jcs = JCS.getInstance( "DiskButNotAllowed" );
        // this should add the event handler to all items as they are created.
        IElementAttributes attributes = jcs.getDefaultElementAttributes();
        attributes.addElementEventHandler( meh );
        jcs.setDefaultElementAttributes( attributes );

        // put them in
        for ( int i = 0; i <= items; i++ )
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
    public void testSpoolNotAllowedEventOnItem()
        throws Exception
    {
        MyEventHandler meh = new MyEventHandler();

        CacheAccess<String, String> jcs = JCS.getInstance( "DiskButNotAllowed" );
        // this should add the event handler to all items as they are created.
        //IElementAttributes attributes = jcs.getDefaultElementAttributes();
        //attributes.addElementEventHandler( meh );
        //jcs.setDefaultElementAttributes( attributes );

        // put them in
        for ( int i = 0; i <= items; i++ )
        {
            IElementAttributes attributes = jcs.getDefaultElementAttributes();
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
    public void testExceededMaxlifeOnrequestEvent()
        throws Exception
    {
        MyEventHandler meh = new MyEventHandler();

        CacheAccess<String, String> jcs = JCS.getInstance( "Maxlife" );
        // this should add the event handler to all items as they are created.
        IElementAttributes attributes = jcs.getDefaultElementAttributes();
        attributes.addElementEventHandler( meh );
        jcs.setDefaultElementAttributes( attributes );

        // put them in
        for ( int i = 0; i < 200; i++ )
        {
            jcs.put( i + ":key", "data" + i);
        }

        // wait a bit for the items to expire
        Thread.sleep( 3000 );

        for ( int i = 0; i < 200; i++ )
        {
            String value = jcs.get( i + ":key");
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
    public void testExceededIdletimeOnrequestEvent()
        throws Exception
    {
        MyEventHandler meh = new MyEventHandler();

        CacheAccess<String, String> jcs = JCS.getInstance( "Idletime" );
        // this should add the event handler to all items as they are created.
        IElementAttributes attributes = jcs.getDefaultElementAttributes();
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
            String value = jcs.get( i + ":key");
            assertNotNull("Item should not be null for key " + i + ":key", value);
        }

        // wait a bit for the items to expire
        Thread.sleep( 1500 );

        for ( int i = 0; i < 200; i++ )
        {
            String value = jcs.get( i + ":key");
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
    public void testElementAttributesCreationTime()
        throws Exception
    {
    	ElementAttributes elem1 = new ElementAttributes();
    	long ctime1 = elem1.getCreateTime();
    	
    	Thread.sleep(10);

    	IElementAttributes elem2 = elem1.clone();
    	long ctime2 = elem2.getCreateTime();
    	
    	assertFalse("Creation times should be different", ctime1 == ctime2);
    }
    
    /**
     * Simple event counter used to verify test results.
     */
    public static class MyEventHandler
        implements IElementEventHandler
    {
        /** times spool called */
        private int spoolCount = 0;

        /** times spool not allowed */
        private int spoolNotAllowedCount = 0;

        /** times spool without disk */
        private int spoolNoDiskCount = 0;

        /** times exceeded maxlife */
        private int exceededMaxlifeCount = 0;

        /** times exceeded idle time */
        private int exceededIdletimeCount = 0;

        /**
         * @param event
         */
        @Override
        public synchronized <T> void handleElementEvent( IElementEvent<T> event )
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

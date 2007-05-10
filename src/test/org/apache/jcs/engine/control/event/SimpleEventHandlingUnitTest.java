package org.apache.jcs.engine.control.event;

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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;

/**
 * This test suite verifies that the basic ElementEvent are called as they
 * should be.
 *
 *
 * @version $Id: TestSimpleEventHandling.java,v 1.1 2005/02/01 00:01:59 asmuts
 *          Exp $
 */
public class SimpleEventHandlingUnitTest
    extends TestCase
{

    private static int items = 20000;

    /**
     * Constructor for test case.
     *
     * @param testName
     *            Description of the Parameter
     */
    public SimpleEventHandlingUnitTest( String testName )
    {
        super( testName );
    }

    /**
     * Run at command line.
     *
     * @param args
     *            Description of the Parameter
     */
    public static void main( String args[] )
    {
        String[] testCaseName = { SimpleEventHandlingUnitTest.class.getName() };
        junit.textui.TestRunner.main( testCaseName );
    }

    /**
     * A unit test suite for JUnit
     *
     * @return The test suite
     */
    public static Test suite()
    {
        return new TestSuite( SimpleEventHandlingUnitTest.class );
    }

    /**
     * Test setup with expected configuration parameters.
     *
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestSimpleEventHandling.ccf" );
    }

    /**
     * Verify that the spooled event is called as expected.
     *
     *
     * @exception Exception
     *                Description of the Exception
     */
    public void testSpoolEvent()
        throws Exception
    {
        MyEventHandler meh = new MyEventHandler();

        JCS jcs = JCS.getInstance( "WithDisk" );
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
        assertTrue( "The number of ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE events [" + meh.getSpoolCount()
            + "] does not equal the number expected [" + items + "]", meh.getSpoolCount() >= items );

    }

    /**
     * Test overflow with no disk configured for the region.
     *
     * @throws Exception
     */
    public void testSpoolNoDiskEvent()
        throws Exception
    {
        JCS jcs = JCS.getInstance( "NoDisk" );

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
     *
     * @throws Exception
     */
    public void testSpoolNotAllowedEvent()
        throws Exception
    {
        MyEventHandler meh = new MyEventHandler();

        JCS jcs = JCS.getInstance( "DiskButNotAllowed" );
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
     *
     * @throws Exception
     */
    public void testSpoolNotAllowedEventOnItem()
        throws Exception
    {
        MyEventHandler meh = new MyEventHandler();

        JCS jcs = JCS.getInstance( "DiskButNotAllowed" );
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
     * Simple event counter used to verify test results.
     *
     * @author aaronsm
     *
     */
    public class MyEventHandler
        implements IElementEventHandler
    {

        private int spoolCount = 0;

        private int spoolNotAllowedCount = 0;

        private int spoolNoDiskCount = 0;

        /*
         * (non-Javadoc)
         *
         * @see org.apache.jcs.engine.control.event.behavior.IElementEventHandler#handleElementEvent(org.apache.jcs.engine.control.event.behavior.IElementEvent)
         */
        public synchronized void handleElementEvent( IElementEvent event )
        {
            //System.out.println( "Handling Event of Type " +
            // event.getElementEvent() );

            if ( event.getElementEvent() == IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE )
            {
                //System.out.println( "Handling Event of Type
                // ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE, " + getSpoolCount() );
                setSpoolCount( getSpoolCount() + 1 );
            }
            else if ( event.getElementEvent() == IElementEventConstants.ELEMENT_EVENT_SPOOLED_NOT_ALLOWED )
            {
                setSpoolNotAllowedCount( getSpoolNotAllowedCount() + 1 );
            }
            else if ( event.getElementEvent() == IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE )
            {
                setSpoolNoDiskCount( getSpoolNoDiskCount() + 1 );
            }
        }

        /**
         * @param spoolCount
         *            The spoolCount to set.
         */
        protected void setSpoolCount( int spoolCount )
        {
            this.spoolCount = spoolCount;
        }

        /**
         * @return Returns the spoolCount.
         */
        protected int getSpoolCount()
        {
            return spoolCount;
        }

        /**
         * @param spoolNotAllowedCount
         *            The spoolNotAllowedCount to set.
         */
        protected void setSpoolNotAllowedCount( int spoolNotAllowedCount )
        {
            this.spoolNotAllowedCount = spoolNotAllowedCount;
        }

        /**
         * @return Returns the spoolNotAllowedCount.
         */
        protected int getSpoolNotAllowedCount()
        {
            return spoolNotAllowedCount;
        }

        /**
         * @param spoolNoDiskCount
         *            The spoolNoDiskCount to set.
         */
        protected void setSpoolNoDiskCount( int spoolNoDiskCount )
        {
            this.spoolNoDiskCount = spoolNoDiskCount;
        }

        /**
         * @return Returns the spoolNoDiskCount.
         */
        protected int getSpoolNoDiskCount()
        {
            return spoolNoDiskCount;
        }

    }

}

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;

/**
 *
 * @author aaronsm
 *
 */
public class ElementEventHandlerMockImpl
    implements IElementEventHandler
{

    /**
     * Times called.
     */
    private int callCount = 0;

    private final static Log log = LogFactory.getLog( ElementEventHandlerMockImpl.class );

    // ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE
    private int spoolCount = 0;

    // ELEMENT_EVENT_SPOOLED_NOT_ALLOWED
    private int spoolNotAllowedCount = 0;

    // ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE
    private int spoolNoDiskCount = 0;

    // ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND
    private int exceededMaxLifeBackgroundCount = 0;

    // ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND
    private int exceededIdleTimeBackgroundCount = 0;

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.control.event.behavior.IElementEventHandler#handleElementEvent(org.apache.jcs.engine.control.event.behavior.IElementEvent)
     */
    public synchronized void handleElementEvent( IElementEvent event )
    {

        setCallCount( getCallCount() + 1 );

        if ( log.isDebugEnabled() )
        {
            log.debug( "HANDLER -- HANDLER -- HANDLER -- ---EVENT CODE = " + event.getElementEvent() );
            log.debug( "/n/n EVENT CODE = " + event.getElementEvent() + " ***************************" );
        }

        if ( event.getElementEvent() == IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE )
        {
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
        else if ( event.getElementEvent() == IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND )
        {
            setExceededMaxLifeBackgroundCount( getExceededMaxLifeBackgroundCount() + 1 );
        }
        else if ( event.getElementEvent() == IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND )
        {
            setExceededIdleTimeBackgroundCount( getExceededIdleTimeBackgroundCount() + 1 );
        }
    }

    /**
     * @param spoolCount
     *            The spoolCount to set.
     */
    public void setSpoolCount( int spoolCount )
    {
        this.spoolCount = spoolCount;
    }

    /**
     * @return Returns the spoolCount.
     */
    public int getSpoolCount()
    {
        return spoolCount;
    }

    /**
     * @param spoolNotAllowedCount
     *            The spoolNotAllowedCount to set.
     */
    public void setSpoolNotAllowedCount( int spoolNotAllowedCount )
    {
        this.spoolNotAllowedCount = spoolNotAllowedCount;
    }

    /**
     * @return Returns the spoolNotAllowedCount.
     */
    public int getSpoolNotAllowedCount()
    {
        return spoolNotAllowedCount;
    }

    /**
     * @param spoolNoDiskCount
     *            The spoolNoDiskCount to set.
     */
    public void setSpoolNoDiskCount( int spoolNoDiskCount )
    {
        this.spoolNoDiskCount = spoolNoDiskCount;
    }

    /**
     * @return Returns the spoolNoDiskCount.
     */
    public int getSpoolNoDiskCount()
    {
        return spoolNoDiskCount;
    }

    /**
     * @param exceededMaxLifeBackground The exceededMaxLifeBackground to set.
     */
    public void setExceededMaxLifeBackgroundCount( int exceededMaxLifeBackground )
    {
        this.exceededMaxLifeBackgroundCount = exceededMaxLifeBackground;
    }

    /**
     * @return Returns the exceededMaxLifeBackground.
     */
    public int getExceededMaxLifeBackgroundCount()
    {
        return exceededMaxLifeBackgroundCount;
    }

    /**
     * @param callCount The callCount to set.
     */
    public void setCallCount( int callCount )
    {
        this.callCount = callCount;
    }

    /**
     * @return Returns the callCount.
     */
    public int getCallCount()
    {
        return callCount;
    }

    /**
     * @param exceededIdleTimeBackground The exceededIdleTimeBackground to set.
     */
    public void setExceededIdleTimeBackgroundCount( int exceededIdleTimeBackground )
    {
        this.exceededIdleTimeBackgroundCount = exceededIdleTimeBackground;
    }

    /**
     * @return Returns the exceededIdleTimeBackground.
     */
    public int getExceededIdleTimeBackgroundCount()
    {
        return exceededIdleTimeBackgroundCount;
    }
}

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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs4.engine.control.event.behavior.ElementEventType;
import org.apache.commons.jcs4.engine.control.event.behavior.IElementEvent;
import org.apache.commons.jcs4.engine.control.event.behavior.IElementEventHandler;
import org.apache.commons.jcs4.log.Log;

/**
 */
public class ElementEventHandlerMockImpl
    implements IElementEventHandler
{
    /** The logger */
    private static final Log log = Log.getLog( ElementEventHandlerMockImpl.class );

    /** Times called. */
    private int callCount;

    /** ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE */
    private int spoolCount;

    /** ELEMENT_EVENT_SPOOLED_NOT_ALLOWED */
    private int spoolNotAllowedCount;

    /** ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE */
    private int spoolNoDiskCount;

    /** ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND */
    private int exceededMaxLifeBackgroundCount;

    /** ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND */
    private int exceededIdleTimeBackgroundCount;

    /**
     * @return the callCount.
     */
    public int getCallCount()
    {
        return callCount;
    }

    /**
     * @return the exceededIdleTimeBackground.
     */
    public int getExceededIdleTimeBackgroundCount()
    {
        return exceededIdleTimeBackgroundCount;
    }

    /**
     * @return the exceededMaxLifeBackground.
     */
    public int getExceededMaxLifeBackgroundCount()
    {
        return exceededMaxLifeBackgroundCount;
    }

    /**
     * @return the spoolCount.
     */
    public int getSpoolCount()
    {
        return spoolCount;
    }

    /**
     * @return the spoolNoDiskCount.
     */
    public int getSpoolNoDiskCount()
    {
        return spoolNoDiskCount;
    }

    /**
     * @return the spoolNotAllowedCount.
     */
    public int getSpoolNotAllowedCount()
    {
        return spoolNotAllowedCount;
    }

    /**
     * @param event
     */
    @Override
    public synchronized <T> void handleElementEvent( final IElementEvent<T> event )
    {
        setCallCount( getCallCount() + 1 );

        if ( log.isDebugEnabled() )
        {
            log.debug( "HANDLER -- HANDLER -- HANDLER -- ---EVENT CODE = " + event.getElementEvent() );
            log.debug( "/n/n EVENT CODE = " + event.getElementEvent() + " ***************************" );
        }

        if ( event.getElementEvent() == ElementEventType.SPOOLED_DISK_AVAILABLE )
        {
            setSpoolCount( getSpoolCount() + 1 );
        }
        else if ( event.getElementEvent() == ElementEventType.SPOOLED_NOT_ALLOWED )
        {
            setSpoolNotAllowedCount( getSpoolNotAllowedCount() + 1 );
        }
        else if ( event.getElementEvent() == ElementEventType.SPOOLED_DISK_NOT_AVAILABLE )
        {
            setSpoolNoDiskCount( getSpoolNoDiskCount() + 1 );
        }
        else if ( event.getElementEvent() == ElementEventType.EXCEEDED_MAXLIFE_BACKGROUND )
        {
            setExceededMaxLifeBackgroundCount( getExceededMaxLifeBackgroundCount() + 1 );
        }
        else if ( event.getElementEvent() == ElementEventType.EXCEEDED_IDLETIME_BACKGROUND )
        {
            setExceededIdleTimeBackgroundCount( getExceededIdleTimeBackgroundCount() + 1 );
        }
    }

    /**
     * @param callCount The callCount to set.
     */
    public void setCallCount( final int callCount )
    {
        this.callCount = callCount;
    }

    /**
     * @param exceededIdleTimeBackground The exceededIdleTimeBackground to set.
     */
    public void setExceededIdleTimeBackgroundCount( final int exceededIdleTimeBackground )
    {
        this.exceededIdleTimeBackgroundCount = exceededIdleTimeBackground;
    }

    /**
     * @param exceededMaxLifeBackground The exceededMaxLifeBackground to set.
     */
    public void setExceededMaxLifeBackgroundCount( final int exceededMaxLifeBackground )
    {
        this.exceededMaxLifeBackgroundCount = exceededMaxLifeBackground;
    }

    /**
     * @param spoolCount The spoolCount to set.
     */
    public void setSpoolCount( final int spoolCount )
    {
        this.spoolCount = spoolCount;
    }

    /**
     * @param spoolNoDiskCount The spoolNoDiskCount to set.
     */
    public void setSpoolNoDiskCount( final int spoolNoDiskCount )
    {
        this.spoolNoDiskCount = spoolNoDiskCount;
    }

    /**
     * @param spoolNotAllowedCount The spoolNotAllowedCount to set.
     */
    public void setSpoolNotAllowedCount( final int spoolNotAllowedCount )
    {
        this.spoolNotAllowedCount = spoolNotAllowedCount;
    }
}

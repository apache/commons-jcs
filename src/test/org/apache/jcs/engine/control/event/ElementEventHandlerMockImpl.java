package org.apache.jcs.engine.control.event;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.engine.control.event.behavior.IElementEvent;
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
    public int callCount = 0;
    
    private final static Log log = LogFactory.getLog( ElementEventHandlerMockImpl.class );

    public void handleElementEvent( IElementEvent event )
    {
        callCount++;
        
        log.debug( "HANDLER -- HANDLER -- HANDLER -- ---EVENT CODE = " + event.getElementEvent() );

        log.debug( "/n/n EVENT CODE = " + event.getElementEvent() + " ***************************" );
        //return "Done";
    }
}

package org.apache.jcs.engine.control.event;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;


public class TestElementEventHandler implements IElementEventHandler {


  private final static Log log = LogFactory.getLog( TestElementEventHandler.class );


  public Serializable handleElementEvent(IElementEvent event )
  {
    log.debug( "HANDLER -- HANDLER -- HANDLER -- ---EVENT CODE = " + event.getElementEvent() );

    log.debug( "/n/n EVENT CODE = " + event.getElementEvent() + " ***************************"  );
    return "Done";
  }
}
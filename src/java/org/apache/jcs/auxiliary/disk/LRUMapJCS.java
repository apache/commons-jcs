package org.apache.jcs.auxiliary.disk;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.collections.map.LRUMap;

/**
  *  Extension of LRUMap for logging of removals.
  *  Can switch this back to a HashMap easily.
  */
public class LRUMapJCS extends LRUMap
{

  private static final Log log =
        LogFactory.getLog( LRUMapJCS.class );

 public LRUMapJCS()
 {
   super();
 }

 public LRUMapJCS( int maxKeySize )
 {
   super( maxKeySize );
 }

 protected void processRemovedLRU( Object key, Object value )
 {
   if ( log.isDebugEnabled() )
   {
     log.debug( "Removing key: '" + key + "' from key store." );
     log.debug( "Key store size: '" + this.size() + "'." );
   }

 }
}

package org.apache.jcs.engine.match.behavior;

import java.io.Serializable;
import java.util.Set;

/** Key matchers need to implement this interface. */
public interface IKeyMatcher
{
    /**
     * Creates a pattern and find matches on the array.
     * <p>
     * @param pattern
     * @param keyArray
     * @return Set of the matching keys
     */
    Set<Serializable> getMatchingKeysFromArray( String pattern, Object[] keyArray );
}

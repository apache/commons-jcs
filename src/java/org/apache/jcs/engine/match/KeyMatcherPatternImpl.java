package org.apache.jcs.engine.match;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jcs.engine.match.behavior.IKeyMatcher;

/** This implementation of the KeyMatcher uses standard Java Pattern matching. */
public class KeyMatcherPatternImpl
    implements IKeyMatcher
{
    /**
     * Creates a pattern and find matches on the array.
     * <p>
     * @param pattern
     * @param keyArray
     * @return Set of the matching keys
     */
    public Set getMatchingKeysFromArray( String pattern, Object[] keyArray )
    {
        Pattern compiledPattern = Pattern.compile( pattern );

        Set matchingKeys = new HashSet();

        // Look for matches
        for ( int i = 0; i < keyArray.length; i++ )
        {
            Object key = keyArray[i];
            // TODO we might want to match on the toString.
            if ( key instanceof String )
            {
                Matcher matcher = compiledPattern.matcher( (String) key );
                if ( matcher.matches() )
                {
                    matchingKeys.add( key );
                }
            }
        }
        return matchingKeys;
    }
}

package org.apache.jcs.utils.match;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * We'll probably want to make an interface for this so new implementations can be plugged in.
 */
public class KeyMatcherUtil
{
    /**
     * Creates a pattern and find matches on the array.
     * <p>
     * @param pattern
     * @param keyArray
     * @return Set of the matching keys
     */
    public static Set getMatchingKeysFromArray( String pattern, Object[] keyArray )
    {
        Pattern compiledPattern = Pattern.compile( pattern );

        Set matchingKeys = new HashSet();

        // Look for matches
        for ( int i = 0; i < keyArray.length; i++ )
        {
            Object key = keyArray[i];
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

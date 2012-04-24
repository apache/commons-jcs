package org.apache.jcs.engine.match;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jcs.engine.match.behavior.IKeyMatcher;

/** This implementation of the KeyMatcher uses standard Java Pattern matching. */
public class KeyMatcherPatternImpl<K extends Serializable>
    implements IKeyMatcher<K>
{
    /** TODO serialVersionUID */
    private static final long serialVersionUID = 6667352064144381264L;

    /**
     * Creates a pattern and find matches on the array.
     * <p>
     * @param pattern
     * @param keyArray
     * @return Set of the matching keys
     */
    public Set<K> getMatchingKeysFromArray( String pattern, Set<K> keyArray )
    {
        Pattern compiledPattern = Pattern.compile( pattern );

        Set<K> matchingKeys = new HashSet<K>();

        // Look for matches
        for (K key : keyArray)
        {
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

package org.apache.commons.jcs.jcache.lang;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

public class Lang3Substitutor implements Subsitutor
{
    private static final StrSubstitutor SUBSTITUTOR = new StrSubstitutor(StrLookup.systemPropertiesLookup());

    @Override
    public String substitute(final String value)
    {
        return SUBSTITUTOR.replace(value);
    }
}

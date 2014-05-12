package org.apache.commons.jcs.jcache.lang;

import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.HashMap;
import java.util.Map;

public class Lang3Substitutor implements Subsitutor
{
    private static final StrSubstitutor SUBSTITUTOR = new StrSubstitutor(new HashMap<String, Object>() {{
        putAll(Map.class.cast(System.getProperties()));
        putAll(System.getenv());
    }});

    @Override
    public String substitute(final String value)
    {
        return SUBSTITUTOR.replace(value);
    }
}

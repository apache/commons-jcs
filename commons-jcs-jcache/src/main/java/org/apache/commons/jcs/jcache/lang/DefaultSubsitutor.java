package org.apache.commons.jcs.jcache.lang;

public class DefaultSubsitutor implements Subsitutor
{
    @Override
    public String substitute(final String value)
    {
        if (value.startsWith("${") && value.endsWith("}")) {
            return System.getProperty(value.substring("${".length(), value.length() - 1), value);
        }
        return value;
    }
}

package org.apache.commons.jcs.jcache;

public class Asserts
{
    public static void assertNotNull(final Object value, final String name)
    {
        if (value == null)
        {
            throw new NullPointerException(name + " is null");
        }
    }

    private Asserts()
    {
        // no-op
    }
}

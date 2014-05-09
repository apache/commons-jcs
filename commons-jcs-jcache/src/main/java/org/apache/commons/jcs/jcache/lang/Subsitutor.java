package org.apache.commons.jcs.jcache.lang;

public interface Subsitutor
{
    String substitute(String value);

    public static class Helper {
        public static final Subsitutor INSTANCE;
        static {
            Subsitutor value = null;
            for (final String name : new String[]
            { // ordered by features
                    "org.apache.commons.jcs.jcache.lang.Lang3Substitutor",
                    "org.apache.commons.jcs.jcache.lang.DefaultSubsitutor"
            })
            {
                try
                {
                    value = Subsitutor.class.cast(
                            Subsitutor.class.getClassLoader().loadClass(name).newInstance());
                    value.substitute("${java.version}"); // ensure it works
                }
                catch (final Throwable e) // not Exception otherwise NoClassDefFoundError
                {
                    // no-op: next
                }
            }
            if (value == null) {
                throw new IllegalStateException("Can't find a " + Subsitutor.class.getName());
            }
            INSTANCE = value;
        }
    }
}

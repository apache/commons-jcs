package org.apache.jcs.access.exception;

/** Thrown if there is some severe configuration problem that makes the cache nonfunctional. */
public class ConfigurationException
    extends CacheException
{
    /** Don't change. */
    private static final long serialVersionUID = 6881044536186097055L;

    /** Constructor for the ConfigurationException object */
    public ConfigurationException()
    {
        super();
    }

    /**
     * Constructor for the ConfigurationException object.
     * <p>
     * @param message
     */
    public ConfigurationException( String message )
    {
        super( message );
    }
}

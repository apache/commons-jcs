package org.apache.jcs.config;

/**
 * This class is based on the log4j class org.apache.log4j.config.PropertySetter
 * that was made by Anders Kristensen
 *
 * @author asmuts
 * @created January 15, 2002
 */

/**
 * Thrown when an error is encountered whilst attempting to set a property using
 * the {@link PropertySetter} utility class.
 *
 * @author Anders Kristensen
 * @created January 15, 2002
 * @since 1.1
 */
public class PropertySetterException extends Exception
{
    /** Description of the Field */
    protected Throwable rootCause;


    /**
     * Constructor for the PropertySetterException object
     *
     * @param msg
     */
    public PropertySetterException( String msg )
    {
        super( msg );
    }


    /**
     * Constructor for the PropertySetterException object
     *
     * @param rootCause
     */
    public PropertySetterException( Throwable rootCause )
    {
        super();
        this.rootCause = rootCause;
    }


    /**
     * Returns descriptive text on the cause of this exception.
     *
     * @return The message value
     */

    public String getMessage()
    {
        String msg = super.getMessage();
        if ( msg == null && rootCause != null )
        {
            msg = rootCause.getMessage();
        }
        return msg;
    }
}


package org.apache.jcs;

import org.apache.commons.lang.exception.NestableException;
import org.apache.jcs.engine.control.CacheHub;
import org.apache.jcs.engine.control.group.GroupCacheHub;
import org.apache.stratum.configuration.Configuration;
import org.apache.stratum.configuration.ConfigurationConverter;
import org.apache.stratum.lifecycle.Configurable;
import org.apache.stratum.lifecycle.Initializable;

/**
 * Component wrapper for JCS, initializes the GroupCacheHub. The cache can then
 * be accessed through {@link JCS}.
 *
 * @author <a href="mailto:jtaylor@apache.org">James Taylor</a>
 * @version $Id$
 */
public class JCSComponent
    implements Configurable, Initializable
{
    Configuration configuration = null;

    /** @see Configurable#configure */
    public void configure( Configuration configuration )
        throws NestableException
    {
        this.configuration = configuration;
    }

    /** @see Initializable#initialize */
    public void initialize() throws Exception
    {
        CacheHub instance = GroupCacheHub.getUnconfiguredInstance();

        instance.configure(
            ConfigurationConverter.getProperties( configuration ) );
    }
}

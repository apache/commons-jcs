package org.apache.jcs.engine.behavior;

/**
 * For the framework. Insures methods a MemoryCache needs to access.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICacheHub extends ICacheType
{

    /** Description of the Method */
    public void spoolToDisk( ICacheElement ice );

}

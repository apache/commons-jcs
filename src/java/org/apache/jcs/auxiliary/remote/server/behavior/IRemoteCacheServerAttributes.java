package org.apache.jcs.auxiliary.remote.server.behavior;

import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;

//import org.apache.jcs.auxiliary.*;

/**
 * Description of the Interface
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IRemoteCacheServerAttributes extends AuxiliaryCacheAttributes
{

    /*
     * A remote cache is either a local cache or a cluster cache.
     */
    /** Description of the Field */
    public static int LOCAL = 0;
    /** Description of the Field */
    public static int CLUSTER = 1;


    /**
     * Gets the remoteTypeName attribute of the IRemoteCacheAttributes object
     *
     * @return The remoteTypeName value
     */
    public String getRemoteTypeName();


    /**
     * Sets the remoteTypeName attribute of the IRemoteCacheAttributes object
     *
     * @param s The new remoteTypeName value
     */
    public void setRemoteTypeName( String s );


    /**
     * Gets the remoteType attribute of the IRemoteCacheAttributes object
     *
     * @return The remoteType value
     */
    public int getRemoteType();


    /**
     * Sets the remoteType attribute of the IRemoteCacheAttributes object
     *
     * @param p The new remoteType value
     */
    public void setRemoteType( int p );


    /**
     * Gets the remoteHost attribute of the IRemoteCacheAttributes object
     *
     * @return The remoteHost value
     */
    public String getRemoteHost();


    /**
     * Sets the remoteHost attribute of the IRemoteCacheAttributes object
     *
     * @param s The new remoteHost value
     */
    public void setRemoteHost( String s );


    /**
     * Gets the remotePort attribute of the IRemoteCacheAttributes object
     *
     * @return The remotePort value
     */
    public int getRemotePort();


    /**
     * Sets the remotePort attribute of the IRemoteCacheAttributes object
     *
     * @param p The new remotePort value
     */
    public void setRemotePort( int p );


    /**
     * Gets the localPort attribute of the IRemoteCacheAttributes object
     *
     * @return The localPort value
     */
    public int getServicePort();


    /**
     * Sets the localPort attribute of the IRemoteCacheAttributes object
     *
     * @param p The new localPort value
     */
    public void setServicePort( int p );


    /**
     * Gets the clusterServers attribute of the IRemoteCacheAttributes object
     *
     * @return The clusterServers value
     */
    public String getClusterServers();


    /**
     * Sets the clusterServers attribute of the IRemoteCacheAttributes object
     *
     * @param s The new clusterServers value
     */
    public void setClusterServers( String s );


    /**
     * Gets the removeUponRemotePut attribute of the IRemoteCacheAttributes
     * object
     *
     * @return The removeUponRemotePut value
     */
    public boolean getRemoveUponRemotePut();


    /**
     * Sets the removeUponRemotePut attribute of the IRemoteCacheAttributes
     * object
     *
     * @param r The new removeUponRemotePut value
     */
    public void setRemoveUponRemotePut( boolean r );


    /**
     * Gets the getOnly attribute of the IRemoteCacheAttributes object
     *
     * @return The getOnly value
     */
    public boolean getGetOnly();


    /**
     * Sets the getOnly attribute of the IRemoteCacheAttributes object
     *
     * @param r The new getOnly value
     */
    public void setGetOnly( boolean r );

    /**
     * Should cluster updates be propogated to the locals
     *
     * @return The localClusterConsistency value
     */
    public boolean getLocalClusterConsistency();

    /**
     * Should cluster updates be propogated to the locals
     *
     * @param r The new localClusterConsistency value
     */
    public void setLocalClusterConsistency( boolean r );


    /**
     * Should cluster updates be propogated to the locals
     *
     * @return The localClusterConsistency value
     */
    public boolean getAllowClusterGet();

    /**
     * Should cluster updates be propogated to the locals
     *
     * @param r The new localClusterConsistency value
     */
    public void setAllowClusterGet( boolean r );

    /**
     * Gets the ConfigFileName attribute of the IRemoteCacheAttributes object
     *
     * @return The clusterServers value
     */
    public String getConfigFileName();


    /**
     * Sets the ConfigFileName attribute of the IRemoteCacheAttributes object
     *
     * @param s The new clusterServers value
     */
    public void setConfigFileName( String s );

}


package org.apache.jcs.auxiliary.remote.behavior;

import java.rmi.Remote;

import org.apache.jcs.engine.behavior.ICacheObserver;

/**
 * Used to register interest in receiving remote cache changes.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IRemoteCacheObserver extends Remote, ICacheObserver
{

}

package org.apache.jcs.auxiliary.remote;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheObserver;

import org.apache.jcs.engine.CacheWatchRepairable;

/**
 * Same as CacheWatcherWrapper but implements the IRemoteCacheWatch interface.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheWatchRepairable extends CacheWatchRepairable implements IRemoteCacheObserver
{
}

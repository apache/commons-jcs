package org.apache.jcs.auxiliary.lateral;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheObserver;

import org.apache.jcs.engine.CacheWatchRepairable;

/**
 * Same as CacheWatcherWrapper but implements the IRemoteCacheWatch interface.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class LateralCacheWatchRepairable extends CacheWatchRepairable implements ILateralCacheObserver
{
}

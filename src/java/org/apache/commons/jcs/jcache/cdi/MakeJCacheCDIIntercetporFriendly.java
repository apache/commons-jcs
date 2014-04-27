package org.apache.commons.jcs.jcache.cdi;

import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

// TODO: observe annotated type (or maybe sthg else) to cache data and inecjt this extension (used as metadata cache)
// to get class model and this way allow to add cache annotation on the fly - == avoid java pure reflection to get metadata
public class MakeJCacheCDIIntercetporFriendly implements Extension {
    protected void discoverInterceptorBindings(final @Observes BeforeBeanDiscovery beforeBeanDiscoveryEvent) {
        beforeBeanDiscoveryEvent.addInterceptorBinding(CachePut.class);
        beforeBeanDiscoveryEvent.addInterceptorBinding(CacheResult.class);
        beforeBeanDiscoveryEvent.addInterceptorBinding(CacheRemove.class);
        beforeBeanDiscoveryEvent.addInterceptorBinding(CacheRemoveAll.class);
    }
}

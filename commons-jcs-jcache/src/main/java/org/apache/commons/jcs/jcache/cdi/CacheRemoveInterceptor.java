package org.apache.commons.jcs.jcache.cdi;

import javax.cache.annotation.CacheRemove;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@CacheRemove
@Interceptor
public class CacheRemoveInterceptor {
    @AroundInvoke
    public Object cache(final InvocationContext ic) throws Exception {
        throw new UnsupportedOperationException("TODO");
    }
}

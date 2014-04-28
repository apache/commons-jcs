package org.apache.commons.jcs.jcache.cdi;

import javax.cache.annotation.CacheResult;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@CacheResult
@Interceptor
public class CacheResultInterceptor {
    @AroundInvoke
    public Object cache(final InvocationContext ic) throws Exception {
        throw new UnsupportedOperationException("TODO");
    }
}

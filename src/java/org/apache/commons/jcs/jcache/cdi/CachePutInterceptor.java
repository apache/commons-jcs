package org.apache.commons.jcs.jcache.cdi;

import javax.cache.annotation.CachePut;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@CachePut
@Interceptor
public class CachePutInterceptor {
    @AroundInvoke
    public Object cache(final InvocationContext ic) throws Exception {
        throw new UnsupportedOperationException("TODO");
    }
}

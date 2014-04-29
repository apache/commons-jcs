package org.apache.commons.jcs.jcache.cdi;

import javax.cache.annotation.CacheRemoveAll;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@CacheRemoveAll
@Interceptor
public class CacheRemoveAllInterceptor
{
    @AroundInvoke
    public Object cache(final InvocationContext ic) throws Exception
    {
        throw new UnsupportedOperationException("TODO");
    }
}

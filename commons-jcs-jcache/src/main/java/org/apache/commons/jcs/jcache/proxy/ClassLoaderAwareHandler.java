package org.apache.commons.jcs.jcache.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ClassLoaderAwareHandler implements InvocationHandler
{
    private final ClassLoader loader;
    private final Object delegate;

    public ClassLoaderAwareHandler(final ClassLoader loader, final Object delegate)
    {
        this.loader = loader;
        this.delegate = delegate;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
    {
        final Thread th = Thread.currentThread();
        final ClassLoader old = th.getContextClassLoader();
        th.setContextClassLoader(loader);
        try
        {
            if (Object.class == method.getDeclaringClass())
            {
                if (isEquals(method, args))
                {
                    return doEquals(method, args);
                }
            }
            return method.invoke(delegate, args);
        }
        catch (final InvocationTargetException ite)
        {
            throw ite.getCause();
        }
        finally
        {
            th.setContextClassLoader(old);
        }
    }

    private Object doEquals(final Method method, final Object[] args) throws IllegalAccessException, InvocationTargetException
    {
        if (args[0] == null)
        {
            return false;
        }
        if (Proxy.isProxyClass(args[0].getClass()))
        {
            final InvocationHandler handler = Proxy.getInvocationHandler(args[0]);
            if (ClassLoaderAwareHandler.class.isInstance(handler))
            {
                return delegate.equals(ClassLoaderAwareHandler.class.cast(handler).delegate);
            }
        }
        return method.invoke(delegate, args);
    }

    private boolean isEquals(final Method method, final Object[] args)
    {
        return "equals".equals(method.getName()) && args != null && args.length == 1;
    }

    public static <T> T newProxy(final ClassLoader loader, final Object delegate, final Class<?>... apis)
    {
        return (T) Proxy.newProxyInstance(loader, apis, new ClassLoaderAwareHandler(loader, delegate));
    }

    public Object getDelegate()
    {
        return delegate;
    }
}

package org.apache.commons.jcs.jcache.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ExceptionWrapperHandler<T> implements InvocationHandler {
    private final T delegate;
    private final Constructor<? extends RuntimeException> wrapper;

    public ExceptionWrapperHandler(final T delegate, final Class<? extends RuntimeException> exceptionType) {
        this.delegate = delegate;
        try {
            this.wrapper = exceptionType.getConstructor(Throwable.class);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        try {
            return method.invoke(delegate, args);
        } catch (final InvocationTargetException ite) {
            final Throwable e = ite.getCause();
            if (RuntimeException.class.isInstance(e)) {
                final RuntimeException re;
                try {
                    re = wrapper.newInstance(e);
                } catch (final Exception e1) {
                    throw new IllegalArgumentException(e1);
                }
                throw re;
            }
            throw e;
        }
    }

    public static <T> T newProxy(final ClassLoader loader, final T delegate, final Class<? extends RuntimeException> exceptionType, final Class<T> apis) {
        return (T) Proxy.newProxyInstance(loader, new Class<?>[] { apis }, new ExceptionWrapperHandler<T>(delegate, exceptionType));
    }
}

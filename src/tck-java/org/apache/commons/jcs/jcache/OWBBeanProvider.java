package org.apache.commons.jcs.jcache;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.spi.ContainerLifecycle;

import javax.cache.annotation.BeanProvider;
import javax.enterprise.inject.spi.Bean;
import java.util.Set;

// TODO: move it over a tck module and remove owb dependency in main pom
public class OWBBeanProvider implements BeanProvider {
    private final BeanManagerImpl bm;

    public OWBBeanProvider() {
        final WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        final ContainerLifecycle lifecycle = webBeansContext.getService(ContainerLifecycle.class);
        lifecycle.startApplication(null);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                lifecycle.stopApplication(null);
            }
        });
        bm = webBeansContext.getBeanManagerImpl();
    }

    @Override
    public <T> T getBeanByType(final Class<T> tClass) {
        if (tClass == null) {
            throw new IllegalArgumentException("no bean class specified");
        }

        final Set<Bean<?>> beans = bm.getBeans(tClass);
        if (beans.isEmpty()) {
            throw new IllegalStateException("no bean of type " + tClass.getName());
        }
        final Bean<?> bean = bm.resolve(beans);
        return (T) bm.getReference(bean, bean.getBeanClass(), bm.createCreationalContext(bean));
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.jcs.jcache;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.spi.ContainerLifecycle;

import java.util.Set;
import javax.cache.annotation.BeanProvider;
import javax.enterprise.inject.spi.Bean;

public class OWBBeanProvider implements BeanProvider
{
    private final BeanManagerImpl bm;

    public OWBBeanProvider()
    {
        final WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        final ContainerLifecycle lifecycle = webBeansContext.getService(ContainerLifecycle.class);
        lifecycle.startApplication(null);
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                lifecycle.stopApplication(null);
            }
        });
        bm = webBeansContext.getBeanManagerImpl();
    }

    @Override
    public <T> T getBeanByType(final Class<T> tClass)
    {
        if (tClass == null)
        {
            throw new IllegalArgumentException("no bean class specified");
        }

        final Set<Bean<?>> beans = bm.getBeans(tClass);
        if (beans.isEmpty())
        {
            throw new IllegalStateException("no bean of type " + tClass.getName());
        }
        final Bean<?> bean = bm.resolve(beans);
        return (T) bm.getReference(bean, bean.getBeanClass(), bm.createCreationalContext(bean));
    }
}

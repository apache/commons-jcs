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
package org.apache.commons.jcs.jcache.openjpa;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Persistence;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class OpenJPAJCacheDataCacheTest
{
    @Test
    public void cacheMe()
    {
        final Properties props = new Properties();
        props.setProperty("openjpa.MetaDataFactory", "jpa(Types=" + MyEntity.class.getName() + ")");
        props.setProperty("openjpa.ConnectionDriverName", EmbeddedDriver.class.getName());
        props.setProperty("openjpa.ConnectionURL", "jdbc:derby:memory:test;create=true");
        props.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema");
        props.setProperty("openjpa.DataCacheManager", "jcache");
        props.setProperty("openjpa.DataCache", "jcache");
        props.setProperty("openjpa.RuntimeUnenhancedClasses", "supported");

        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("test-jcache", props);
        final OpenJPAConfiguration conf = OpenJPAEntityManagerFactorySPI.class.cast(emf).getConfiguration();

        final EntityManager em = emf.createEntityManager();

        final MyEntity entity = new MyEntity();
        entity.setName("cacheMe1");
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
        assertNotNull(conf.getDataCacheManagerInstance().getDataCache("default"));

        assertThat(conf.getDataCacheManagerInstance(), instanceOf(OpenJPAJCacheDataCacheManager.class));
        assertThat(conf.getDataCacheManagerInstance().getDataCache("default"), instanceOf(OpenJPAJCacheDataCache.class));
        assertTrue(conf.getDataCacheManagerInstance().getDataCache("default").contains(
                JPAFacadeHelper.toOpenJPAObjectId(conf.getMetaDataRepositoryInstance()
                        .getCachedMetaData(MyEntity.class), entity.getId())
        ));

        em.close();

        emf.close();
    }

    @Entity
    public static class MyEntity
    {
        @Id
        @GeneratedValue
        private long id;
        private String name;

        public long getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public void setName(final String name)
        {
            this.name = name;
        }
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.jcs3.jcache.openjpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.datacache.QueryKey;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.junit.jupiter.api.Test;

class OpenJPAJCacheDataCacheTest
{
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

    private static final Properties props = new Properties()
    {private static final long serialVersionUID = 498881935058549341L;

    {
        setProperty("openjpa.MetaDataFactory", "jpa(Types=" + MyEntity.class.getName() + ")");
        setProperty("openjpa.ConnectionDriverName", EmbeddedDriver.class.getName());
        setProperty("openjpa.ConnectionURL", "jdbc:derby:memory:test;create=true");
        setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema");
        setProperty("openjpa.DataCacheManager", "jcache");
        setProperty("openjpa.RuntimeUnenhancedClasses", "supported");

        // implicit
        // setProperty("openjpa.DataCache", "jcache");
        // setProperty("openjpa.QueryCache", "jcache");
    }};

    @Test
    void testEntity()
    {
        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("test-jcache", props);
        final OpenJPAConfiguration conf = OpenJPAEntityManagerFactorySPI.class.cast(emf).getConfiguration();

        final EntityManager em = emf.createEntityManager();

        final MyEntity entity = new MyEntity();
        entity.setName("cacheMe1");
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
        assertNotNull(conf.getDataCacheManagerInstance().getDataCache("default"));

        assertInstanceOf(OpenJPAJCacheDataCacheManager.class, conf.getDataCacheManagerInstance());
        assertInstanceOf(OpenJPAJCacheDataCache.class, conf.getDataCacheManagerInstance().getDataCache("default"));
        assertTrue(conf.getDataCacheManagerInstance().getDataCache("default").contains(JPAFacadeHelper.toOpenJPAObjectId(conf.getMetaDataRepositoryInstance().getCachedMetaData(MyEntity.class), entity.getId())));

        em.close();
        emf.close();
    }

    @Test
    void testQuery()
    {
        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("test-jcache", props);
        final OpenJPAConfiguration conf = OpenJPAEntityManagerFactorySPI.class.cast(emf).getConfiguration();

        final EntityManager em = emf.createEntityManager();

        final MyEntity entity = new MyEntity();
        entity.setName("cacheMe1");
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
        final Query query = em.createQuery("select e from OpenJPAJCacheDataCacheTest$MyEntity e where e.id = :id");
        assertEquals(1, query.setParameter("id", entity.getId()).getResultList().size());
        assertNotNull(conf.getDataCacheManagerInstance().getDataCache("default"));

        assertInstanceOf(OpenJPAJCacheDataCacheManager.class, conf.getDataCacheManagerInstance());
        assertInstanceOf(OpenJPAJCacheDataCache.class, conf.getDataCacheManagerInstance().getDataCache("default"));
        assertTrue(conf.getDataCacheManagerInstance().getDataCache("default").contains(JPAFacadeHelper.toOpenJPAObjectId(conf.getMetaDataRepositoryInstance().getCachedMetaData(MyEntity.class), entity.getId())));

        final Map<Object, Object> args = Map.of("id", entity.getId());
        final QueryKey qk = QueryKey.newInstance(query.unwrap(org.apache.openjpa.kernel.Query.class), args);
        assertNotNull(conf.getDataCacheManagerInstance().getSystemQueryCache().get(qk));

        em.close();
        emf.close();
    }
}

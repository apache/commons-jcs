/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Apache Commons JCS (Java Caching System) - Core Module
 *
 * Provides a distributed, versatile caching system with support for:
 * - In-memory caching with multiple eviction policies (LRU, MRU, FIFO, Soft References)
 * - Disk-based caching (Indexed, JDBC-based with HSQL and MySQL support)
 * - Remote caching (TCP sockets and HTTP)
 * - Lateral caching (UDP multicast discovery)
 * - Event-based notifications
 * - Statistics and monitoring
 *
 * @author Apache Commons JCS Team
 */
module org.apache.commons.jcs4.core {

    // Core public API exports
    exports org.apache.commons.jcs4;
    exports org.apache.commons.jcs4.access;
    exports org.apache.commons.jcs4.access.behavior;
    exports org.apache.commons.jcs4.access.exception;
    exports org.apache.commons.jcs4.admin;
    exports org.apache.commons.jcs4.engine.behavior;
    exports org.apache.commons.jcs4.engine.control;
    exports org.apache.commons.jcs4.engine.control.event;
    exports org.apache.commons.jcs4.engine.control.event.behavior;
    exports org.apache.commons.jcs4.engine.control.group;
    exports org.apache.commons.jcs4.engine.logging;
    exports org.apache.commons.jcs4.engine.logging.behavior;
    exports org.apache.commons.jcs4.engine.memory.behavior;
    exports org.apache.commons.jcs4.engine.stats;
    exports org.apache.commons.jcs4.engine.stats.behavior;

    // Auxiliary cache exports
    exports org.apache.commons.jcs4.auxiliary;
    exports org.apache.commons.jcs4.auxiliary.disk;
    exports org.apache.commons.jcs4.auxiliary.disk.behavior;
    exports org.apache.commons.jcs4.auxiliary.disk.block;
    exports org.apache.commons.jcs4.auxiliary.disk.indexed;
    exports org.apache.commons.jcs4.auxiliary.disk.jdbc;
    exports org.apache.commons.jcs4.auxiliary.disk.jdbc.dsfactory;
    exports org.apache.commons.jcs4.auxiliary.disk.jdbc.hsql;
    exports org.apache.commons.jcs4.auxiliary.disk.jdbc.mysql;
    exports org.apache.commons.jcs4.auxiliary.lateral;
    exports org.apache.commons.jcs4.auxiliary.lateral.behavior;
    exports org.apache.commons.jcs4.auxiliary.lateral.socket.tcp;
    exports org.apache.commons.jcs4.auxiliary.lateral.socket.tcp.behavior;
    exports org.apache.commons.jcs4.auxiliary.remote;
    exports org.apache.commons.jcs4.auxiliary.remote.behavior;
    exports org.apache.commons.jcs4.auxiliary.remote.server.behavior;
    exports org.apache.commons.jcs4.auxiliary.remote.http.behavior;
    exports org.apache.commons.jcs4.auxiliary.remote.http.client.behavior;

    // Utility exports
    exports org.apache.commons.jcs4.utils.access;
    exports org.apache.commons.jcs4.utils.discovery.behavior;
    exports org.apache.commons.jcs4.utils.serialization;

    // Internal/optional exports (for subclasses and extensions)
    exports org.apache.commons.jcs4.engine;
    exports org.apache.commons.jcs4.engine.memory;
    exports org.apache.commons.jcs4.engine.memory.lru;
    exports org.apache.commons.jcs4.engine.memory.mru;
    exports org.apache.commons.jcs4.engine.memory.fifo;
    exports org.apache.commons.jcs4.engine.memory.soft;
    exports org.apache.commons.jcs4.engine.memory.shrinking;
    exports org.apache.commons.jcs4.engine.memory.util;
    exports org.apache.commons.jcs4.engine.match;
    exports org.apache.commons.jcs4.engine.match.behavior;
    exports org.apache.commons.jcs4.auxiliary.remote.http.client;
    exports org.apache.commons.jcs4.auxiliary.remote.http.server;
    exports org.apache.commons.jcs4.auxiliary.remote.server;
    exports org.apache.commons.jcs4.auxiliary.remote.util;
    exports org.apache.commons.jcs4.auxiliary.remote.value;
    exports org.apache.commons.jcs4.utils.servlet;
    exports org.apache.commons.jcs4.utils.discovery;

    // Java platform modules - required
    requires java.base;
    requires java.management;
    requires java.desktop;
    requires transitive java.rmi;
    requires transitive java.sql;
    requires transitive java.naming;

    // Optional dependencies for remote HTTP caching
    requires static jakarta.servlet;

    // Optional dependencies for JDBC disk cache
    requires static org.apache.commons.dbcp2;

    // Optional dependencies for remote HTTP caching
    requires static org.apache.httpcomponents.httpclient;
    requires static org.apache.httpcomponents.httpcore;

    // Optional dependencies for JSON serializer
    requires static com.fasterxml.jackson.databind;
    opens org.apache.commons.jcs4.utils.serialization to com.fasterxml.jackson.databind;
    opens org.apache.commons.jcs4.engine to com.fasterxml.jackson.databind;

    // Uses and provides clauses
    uses org.apache.commons.jcs4.auxiliary.AuxiliaryCacheFactory;
}

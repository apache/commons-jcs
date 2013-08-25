package org.apache.commons.jcs.yajcache.lang.annotation;

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

/**
 * Thread Safety Types.
 *
 * http://www-106.ibm.com/developerworks/java/library/j-jtp09263.html
 *
 * @author Hanson Char
 */
// @CopyRightApache
// http://www.netbeans.org/issues/show_bug.cgi?id=53704
public enum ThreadSafetyType {
    /**  Immutable objects are guaranteed to be thread-safe. */
    IMMUTABLE,
    /**
     * Instances of this class are mutable, but all methods contain
     * sufficient internal synchronization that instances may be used
     * concurrently without the need for external synchronization.
     */
    SAFE,
    /**
     * Conditionally thread-safe classes are those for which each individual
     * operation may be thread-safe, but certain sequences of operations may
     * require external synchronization. The most common example of
     * conditional thread safety is traversing an iterator returned from
     * Hashtable or Vector -- the fail-fast iterators returned by these
     * classes assume that the underlying collection will not be mutated
     * while the iterator traversal is in progress. To ensure that other
     * threads will not mutate the collection during traversal, the
     * iterating thread should be sure that it has exclusive access to
     * the collection for the entirety of the traversal. Typically,
     * exclusive access is ensured by synchronizing on a lock -- and the
     * class's documentation should specify which lock that is
     * (typically the object's intrinsic monitor).
     */
    CONDITIONAL,
    /**
     * Thread-compatible classes are not thread-safe, but can be used
     * safely in concurrent environments by using synchronization
     * appropriately.
     */
    COMPATIBLE,
    /**
     * Thread-hostile classes are those that cannot be rendered safe to
     * use concurrently, regardless of what external synchronization is
     * invoked.
     */
    HOSTILE
}

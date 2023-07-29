
Yet Another Java Cache 
----------------------
http://yajcache.sourceforge.net

Currently supports both in jdk1.5 

a) soft reference memory-only cache; and 
b) soft reference memory-file cache with unlimited overflow.

Features
--------
* zero configuration 
* zero thread instantiation
* minimal memory impact (controlled by GC via SoftReference)
* no synchronized block
* no synchronized method
* minimal synchronization done via distributed (ie keyed) ReadWrite locks
with zero synchronization
* all unused ReadWrite locks are automatically removed by GC 
via WeakReference
* the cache instance itself (ICache or ICacheSafe) can be used 
anywhere a map instance can be used
* optional ICacheSafe to provide thread-safe objects 
for cache get/put via either Serializable or Java Bean patterns
* Intelligent guess to avoid deep clone whenever possible when
ICacheSafe is used
* String-only key constraint to avoid mutability issues
* Fully parameterized cache value type
* junit test cases, including emulation of hard-to-test
data race conditions

Usage
-----
All caches should be retrieved or removed via

    org.apache.commons.jcs.yajcache.core.CacheManager

Pre-requisite
-------------
1) jdk 1.5.0_01+ installed
1) Ant 1.6.2+ installed
2) copy lib/junit-3.8.1.jar to your <ANT_HOME>/lib/
3) For memory-file cache, the library needs to create a root directory 

           /tmp/yajcache/

and have complete access privilege of everything under it.

Build
-----
Simply type:

ant

Alternatively, if you've got NetBeans 4.0, life is even easier.
Simply open up the "cache" project folder, and build it.

Cheers,
Hanson Char

hchar@apache.org

package org.apache.commons.jcs3.admin;

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

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;

/**
 * Helper class to test the JMX registration
 */
public class TestJMX
{
	public static void main(final String[] args) throws Exception
	{
		final CacheAccess<String, String> cache = JCS.getInstance("test");

		cache.put("key", "value");
        System.out.println("Waiting...");
        Thread.sleep(Long.MAX_VALUE);
	}
}

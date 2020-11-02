<%--
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
--%>
<!DOCTYPE html>
<%@page import="java.util.List"%>
<%@page import="org.apache.commons.jcs3.JCS"%>
<%@page import="org.apache.commons.jcs3.access.CacheAccess" %>
<%@page import="org.apache.commons.jcs3.admin.CacheElementInfo" %>
<%@page import="org.apache.commons.jcs3.admin.CacheRegionInfo" %>
<%@page import="org.apache.commons.jcs3.engine.behavior.ICacheElement" %>

<jsp:useBean id="jcsBean" scope="request" class="org.apache.commons.jcs3.admin.JCSAdminBean" />

<html lang="en">
<head>
  <script>
  function decision(message, url)
  {
    if(confirm(message))
    {
      location.href = url;
    }
  }
  </script>
  <style>
  body {
    font-family: Arial, Helvetica, sans-serif;
    font-size: small;
  }
  
  h1 {
    font-size: large;
    color: navy;
  }

  h2 {
    font-size: medium;
    color: navy;
  }

  a {
    color: navy;
    font-weight: 700;
    text-decoration: none;
  }

  /* Change color on hover */
  a:hover {
    background-color: navy;
    color: white;
  }
  
  pre {
    font-size: medium;
  }
  
  table, th, td {
    border: thin solid #a0a0a0;
    font-size: small;
  }
  
  table {
    border-collapse: collapse;
  }
  
  tr:nth-child(odd) {
    background: #EEE;
  }

  th, td {
    padding: 4pt;
  }
  
  th {
    background-color: navy;
    color: white;
    font-weight: 700;
    text-align: left;
  }  
  </style>
  <title>JCS Administration</title>
</head>
<body>
<%
	final String CACHE_NAME_PARAM = "cacheName";
    final String ACTION_PARAM = "action";
    final String CLEAR_ALL_REGIONS_ACTION = "clearAllRegions";
    final String CLEAR_REGION_ACTION = "clearRegion";
    final String REMOVE_ACTION = "remove";
    final String DETAIL_ACTION = "detail";
    final String REGION_SUMMARY_ACTION = "regionSummary";
    final String ITEM_ACTION = "item";
    final String KEY_PARAM = "key";

    final String DEFAULT_TEMPLATE_NAME = "DEFAULT";
    final String REGION_DETAIL_TEMPLATE_NAME = "DETAIL";
    final String ITEM_TEMPLATE_NAME = "ITEM";
    final String REGION_SUMMARY_TEMPLATE_NAME = "SUMMARY";

	String templateName = DEFAULT_TEMPLATE_NAME;

	// Get cacheName for actions from request (might be null)
	String cacheName = request.getParameter(CACHE_NAME_PARAM);

	if (cacheName != null)
	{
	    cacheName = cacheName.trim();
	}

	// If an action was provided, handle it
	String action = request.getParameter(ACTION_PARAM);

	if (action == null)
	{
	    // do nothing
	}
	else if (action.equals(CLEAR_ALL_REGIONS_ACTION))
	{
		jcsBean.clearAllRegions();
	}
	else if (action.equals(CLEAR_REGION_ACTION) && cacheName != null)
	{
		jcsBean.clearRegion(cacheName);
	}
	else if (action.equals(REMOVE_ACTION))
	{
		for (String key : request.getParameterValues(KEY_PARAM))
		{
			jcsBean.removeItem(cacheName, key);
		}

		templateName = REGION_DETAIL_TEMPLATE_NAME;
	}
	else if (action.equals(DETAIL_ACTION))
	{
		templateName = REGION_DETAIL_TEMPLATE_NAME;
	}
	else if (action.equals(ITEM_ACTION))
	{
		templateName = ITEM_TEMPLATE_NAME;
	}
	else if (action.equals(REGION_SUMMARY_ACTION))
	{
		templateName = REGION_SUMMARY_TEMPLATE_NAME;
	}

    ///////////////////////////////////////////////////////////////////////////////////
	//handle display

	if (templateName == ITEM_TEMPLATE_NAME)
	{
	    String key = request.getParameter(KEY_PARAM);

	    if (key != null)
	    {
	        key = key.trim();
	    }

	    CacheAccess<Object, Object> cache = JCS.getInstance(cacheName);
		ICacheElement<?, ?> element = cache.getCacheElement(key);
%>
<h1>Item for key [<%=key%>] in region [<%=cacheName%>]</h1>
<p><a href="?action=detail&cacheName=<%=cacheName%>">Region Detail</a>
| <a href="?">All Regions</a></p>
<pre>
<%=element%>
</pre>
<%
	}
	else if (templateName == REGION_SUMMARY_TEMPLATE_NAME)
	{
%>
<h1>Summary for region [<%=cacheName%>]</h1>
<p><a href="?">All Regions</a></p>
<%
        CacheAccess<?, ?> cache = JCS.getInstance(cacheName);
        String stats = cache.getStats();
%>
<h2>Statistics for region [<%=cacheName%>]</h2>
<pre>
<%=stats%>
</pre>
<%
	}
	else if (templateName == REGION_DETAIL_TEMPLATE_NAME)
	{
%>
<h1>Detail for region [<%=cacheName%>]</h1>
<p><a href="?">All Regions</a></p>
<table>
  <tr>
    <th>Key</th>
    <th>Eternal?</th>
    <th>Create time</th>
    <th>Max Life (s)</th>
    <th>Till Expiration (s)</th>
  </tr>
<%
        for (CacheElementInfo element : jcsBean.buildElementInfo(cacheName))
        {
%>
  <tr>
    <td><%=element.getKey()%></td>
    <td><%=element.isEternal()%></td>
    <td><%=element.getCreateTime()%></td>
    <td><%=element.getMaxLifeSeconds()%></td>
    <td><%=element.getExpiresInSeconds()%></td>
    <td>
      <a href="?action=item&cacheName=<%=cacheName%>&key=<%=element.getKey()%>"> View </a>
      | <a href="?action=remove&cacheName=<%=cacheName%>&key=<%=element.getKey()%>"> Remove </a>
    </td>
  </tr>
<%
        }

        CacheAccess<?, ?> cache = JCS.getInstance(cacheName);
        String stats = cache.getStats();
%>
</table>
<h2>Statistics for region [<%=cacheName%>]</h2>
<pre>
<%=stats%>
</pre>
<%
    }
    else
    {
%>
<h1>Cache Regions</h1>
<p>
These are the regions which are currently defined in the cache. 'Items' and
'Bytes' refer to the elements currently in memory (not spooled). You can clear
all items for a region by selecting 'Clear' next to the desired region
below. You can also <a href="javascript:decision('Clicking OK will clear all the data from all regions!','?action=clearAllRegions')">Clear all regions</a>
which empties the entire cache.
</p>
<form action="?"><p>
  <input type="hidden" name="action" value="item" />
  Retrieve <input type="text" name="key" autofocus="autofocus" placeholder="(key)" /> 
  from region <select name="cacheName">
<%
        List<CacheRegionInfo> records = jcsBean.buildCacheInfo();

        for (CacheRegionInfo record : records)
        {
%>
    <option value="<%=record.getCacheName()%>"><%=record.getCacheName()%></option>
<%
        }
%>
  </select>
  <input type="submit" value="Retrieve" />
</p></form>
<table>
  <tr>
    <th>Cache Name</th>
    <th>Items</th>
    <th>Bytes</th>
    <th>Status</th>
    <th>Memory Hits</th>
    <th>Aux Hits</th>
    <th>Not Found Misses</th>
    <th>Expired Misses</th>
    <th>Actions</th>
  </tr>
<%
        for (CacheRegionInfo record : records)
        {
%>
  <tr>
    <td><%=record.getCacheName()%></td>
    <td><%=record.getCacheSize()%></td>
    <td><%=record.getByteCount()%></td>
    <td><%=record.getCacheStatus()%></td>
    <td><%=record.getHitCountRam()%></td>
    <td><%=record.getHitCountAux()%></td>
    <td><%=record.getMissCountNotFound()%></td>
    <td><%=record.getMissCountExpired()%></td>
    <td>
      <a href="?action=regionSummary&cacheName=<%=record.getCacheName()%>">Summary</a>
      | <a href="?action=detail&cacheName=<%=record.getCacheName()%>">Detail</a>
      | <a href="javascript:decision('Clicking OK will remove all the data from the region [<%=record.getCacheName()%>]!','?action=clearRegion&cacheName=<%=record.getCacheName()%>')">Clear</a>
    </td>
  </tr>
<%
        }
%>
</table>
<%
    }
%>
</body>
</html>

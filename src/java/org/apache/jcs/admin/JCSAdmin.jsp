<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.Iterator" %>

<%@ page import="org.apache.jcs.admin.*" %>


<jsp:useBean id="jcsBean" scope="request" class="org.apache.jcs.admin.JCSAdminBean" />

<html>

<head><title> JCS Admin Servlet </title></head>

<body>


<%

			String CACHE_NAME_PARAM = "cacheName";
			String ACTION_PARAM = "action";
		 	String CLEAR_ALL_REGIONS_ACTION = "clearAllRegions";
		 	String CLEAR_REGION_ACTION = "clearRegion";
		 	String REMOVE_ACTION = "remove";
		 	String DETAIL_ACTION = "detail";			
			String KEY_PARAM = "key";
			String SILENT_PARAM = "silent";

     		String DEFAULT_TEMPLATE_NAME = "DEFAULT";
     		String REGION_DETAIL_TEMPLATE_NAME = "DETAIL";
     		
			String templateName = DEFAULT_TEMPLATE_NAME;

			
			HashMap context = new HashMap();
		
			// Get cacheName for actions from request (might be null)
			String cacheName = request.getParameter( CACHE_NAME_PARAM );

			// If an action was provided, handle it
			String action = request.getParameter( ACTION_PARAM );

			if ( action != null )
			{
				if ( action.equals( CLEAR_ALL_REGIONS_ACTION ) )
				{
					jcsBean.clearAllRegions();
				}
				else if ( action.equals( CLEAR_REGION_ACTION ) )
				{
					if ( cacheName == null )
					{
						// Not Allowed
					}
					else
					{
						jcsBean.clearRegion( cacheName );
					}
				}
				else if ( action.equals( REMOVE_ACTION ) )
				{
					String[] keys = request.getParameterValues( KEY_PARAM );

					for ( int i = 0; i < keys.length; i++ )
					{
						jcsBean.removeItem( cacheName, keys[ i ] );
					}

					templateName = REGION_DETAIL_TEMPLATE_NAME;
				}
				else if ( action.equals( DETAIL_ACTION ) )
				{
					templateName = REGION_DETAIL_TEMPLATE_NAME;
				}
			}

			if ( request.getParameter( SILENT_PARAM ) != null )
			{
				// If silent parameter was passed, no output should be produced.

				//return null;
			}
			else
			{
				// Populate the context based on the template

				if ( templateName == REGION_DETAIL_TEMPLATE_NAME )
				{
					//context.put( "cacheName", cacheName );
					context.put( "elementInfoRecords", jcsBean.buildElementInfo( cacheName ) );
				}
				else if ( templateName == DEFAULT_TEMPLATE_NAME )
				{
					context.put( "cacheInfoRecords", jcsBean.buildCacheInfo() );
				}

			}
	
	
			//handle display
			if ( templateName == REGION_DETAIL_TEMPLATE_NAME ) {
%>

<h1> Keys for region: $cacheName </h1>

<table border="1" cellpadding="5" >
    <tr>
        <th> Key </th>
        <th> Eternal? </th>
        <th> Create time </th>
        <th> Max Life (s) </th>
        <th> Till Expiration (s) </th>
    </tr>
<%

	List list = (List)context.get( "elementInfoRecords" );
    Iterator it = list.iterator();
    while ( it.hasNext() ) {
    	CacheElementInfo element = (CacheElementInfo)it.next();
    
%>
        <tr>
            <td> <%=element.getKey()%> </td>
            <td> <%=element.isEternal()%> </td>
            <td> <%=element.getCreateTime()%> </td>
            <td> <%=element.getMaxLifeSeconds()%> </td>
            <td> <%=element.getExpiresInSeconds()%> </td>
            <td> <a href="?action=remove&cacheName=<%=cacheName%>&key=<%=element.getKey()%>"> Remove </a> </td>
        </tr>

<%
    }
		
			} else {

%>

<h1> Cache Regions </h1>

<p>These are the regions which are currently defined in the cache. 'Items' and
'Bytes' refer to the elements currently in memory (not spooled). You can clear
all items for a region by selecting 'Remove all' next to the desired region
below. You can also <a href="?action=clearAllRegions">Clear all regions</a>
which empties the entire cache.</p>

<table border="1" cellpadding="5" >
    <tr>
        <th> Cache Name </th>
        <th> Items </th>
        <th> Bytes </th>
        <th> Status </th>
        <th> Memory Hits </th>
        <th> Aux Hits </th>
        <th> Not Found Misses </th>
        <th> Expired Misses </th>
    </tr>

<%
	List list = (List)context.get( "cacheInfoRecords" );
    Iterator it = list.iterator();
    while (it.hasNext() ) {
    	CacheRegionInfo record = (CacheRegionInfo)it.next();

%>
        <tr>
            <td> <%=record.getCache().getCacheName()%> </td>
            <td> <%=record.getCache().getSize()%> </td>
            <td> <%=record.getByteCount()%> </td>
            <td> <%=record.getStatus()%> </td>
            <td> <%=record.getCache().getHitCountRam()%> </td>
            <td> <%=record.getCache().getHitCountAux()%> </td>
            <td> <%=record.getCache().getMissCountNotFound()%> </td>
            <td> <%=record.getCache().getMissCountExpired()%> </td>
            <td>   
                <a href="?action=detail&cacheName=<%=record.getCache().getCacheName()%>"> Detail </a>
                | <a href="?action=clearRegion&cacheName=<%=record.getCache().getCacheName()%>"> Remove all </a>
            </td>
        </tr>
<%  
    }
	
			}
		
%>

</table>

</body>

</html>

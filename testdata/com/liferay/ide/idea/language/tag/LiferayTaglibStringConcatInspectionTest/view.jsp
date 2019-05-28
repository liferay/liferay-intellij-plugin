<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>

<%
    String bar = "bar";
%>
<aui:input name=<warning descr="JSP expessions and string values cannot be concatenated inside the attribute">"foo<%=bar%>"</warning> />
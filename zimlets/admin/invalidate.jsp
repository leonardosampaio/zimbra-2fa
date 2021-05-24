<%@ page import="br.com.sampaio.Utils" %>
<%
response.setContentType("application/json");

new Utils().invalidateSecretKey(request.getParameter("email"));

%>

{"status":"invalidated"}
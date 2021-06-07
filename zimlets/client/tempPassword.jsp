<%@ page import="br.com.sampaio.SinglePasswordTempStore" %>
<%@ page import="br.com.sampaio.AccessType" %>
<%
response.setContentType("application/json");

SinglePasswordTempStore instance = null;
if (request.getServletContext().getAttribute("singlePasswordTempStore") == null)
{
	instance = SinglePasswordTempStore.getInstance();
	request.getServletContext().setAttribute("singlePasswordTempStore", instance);
}
else {
	instance = (SinglePasswordTempStore)request.getServletContext().getAttribute("singlePasswordTempStore");
}

String email = request.getParameter("email");

%>

{"tempPassword":"<%=instance.getPassword(email, AccessType.WEB)%>"}
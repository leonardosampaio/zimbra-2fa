<%@ page import="br.com.sampaio.Utils" %>
<%@ page import="br.com.sampaio.SinglePasswordTempStore" %>
<%
response.setContentType("application/json");

String email = request.getParameter("email");

new Utils().invalidateSecretKey(email);

SinglePasswordTempStore instance = null;
if (request.getServletContext().getAttribute("singlePasswordTempStore") == null)
{
	instance = SinglePasswordTempStore.getInstance();
	request.getServletContext().setAttribute("singlePasswordTempStore", instance);
}
else {
	instance = (SinglePasswordTempStore)request.getServletContext().getAttribute("singlePasswordTempStore");
}

instance.invalidatePassword(email);

%>

{"status":"invalidated"}
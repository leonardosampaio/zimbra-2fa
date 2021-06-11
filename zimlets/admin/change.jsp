<%@ page import="br.com.sampaio.Utils" %>
<%

String status = "success";

try {
	response.setContentType("application/json");

	String email = request.getParameter("email");
	String password = request.getParameter("password");

	new Utils().changePassword(email,password);
}
catch (Exception e)
{
	status = "error";
	e.printStackTrace();
}
%>

{"status":"<%=status%>"}
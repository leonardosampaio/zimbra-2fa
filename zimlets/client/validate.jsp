<%@ page import="br.com.sampaio.Utils" %>
<%
response.setContentType("application/json");

Utils utils = new Utils();

String email = request.getParameter("email");
String code = request.getParameter("code");

String status = "error";

SinglePasswordTempStore instance = null;
if (request.getServletContext().getAttribute("singlePasswordTempStore") == null)
{
	instance = SinglePasswordTempStore.getInstance();
	request.getServletContext().setAttribute("singlePasswordTempStore", instance);
}
else {
	instance = (SinglePasswordTempStore)request.getServletContext().getAttribute("singlePasswordTempStore");
}

String singleAppPassword = instance.getPassword(email);

try
{
    if (!utils.hasValidSecretKey(email) && utils.validateCode(email, code))
    {
        status = "validated";
    }   
}
catch (Exception e)
{
    e.printStackTrace();
}

%>

{"status":"<%=status%>","singleAppPassword":"<%=singleAppPassword%>"}
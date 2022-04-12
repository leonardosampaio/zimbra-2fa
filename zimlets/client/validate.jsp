<%@ page import="br.com.sampaio.Utils" %>
<%@ page import="br.com.sampaio.SinglePasswordTempStore" %>
<%
response.setContentType("application/json");

Utils utils = new Utils();

String hostname = request.getServerName();
String email    = request.getParameter("email");
String code     = request.getParameter("code");

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

String singleAppPassword = "";

try
{
    if (utils.validateCode(hostname, email, code))
    {
        singleAppPassword = instance.getPassword(email);
        status = "validated";
    }   
}
catch (Exception e)
{
    e.printStackTrace();
}

%>

{"status":"<%=status%>","singleAppPassword":"<%=singleAppPassword%>"}
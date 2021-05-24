<%@ page import="br.com.sampaio.Utils" %>
<%
response.setContentType("application/json");

Utils utils = new Utils();

String email = request.getParameter("email");
String code = request.getParameter("code");

String status = "error";

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

{"status":"<%=status%>"}
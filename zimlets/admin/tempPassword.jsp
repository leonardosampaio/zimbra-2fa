<%@ page import="br.com.sampaio.RandomString" %>
<%@ page import="java.util.concurrent.ThreadLocalRandom" %>
<%@ page import="com.zimbra.cs.account.Provisioning" %>
<%@ page import="com.zimbra.soap.type.AccountBy" %>
<%
response.setContentType("application/json");

String email = request.getParameter("email");
String tempPassword = new RandomString(8, ThreadLocalRandom.current()).nextString();

String status = "success";

try {
	Provisioning provisioningInstance = Provisioning.getInstance();
	provisioningInstance.setPassword(provisioningInstance.get(AccountBy.name,email), tempPassword);
}
catch (Exception e)
{
	status = "error";
}

%>

{"status":"<%=status%>", "email":"<%=email%>", "tempPassword":"<%=tempPassword%>"}
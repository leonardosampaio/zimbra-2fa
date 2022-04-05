<%@ page import="br.com.sampaio.Utils" %>
<%@ page import="br.com.sampaio.SinglePasswordTempStore" %>
<%
response.setContentType("application/json");

Utils utils = new Utils();

String email = request.getParameter("email");
String reactivate = request.getParameter("reactivate");
String companyName = "Zimbra 2FA "+email.split("@")[1];

String b64Png = "";
String status = "valid";
String singleAppPassword = "";

SinglePasswordTempStore instance = null;
if (request.getServletContext().getAttribute("singlePasswordTempStore") == null)
{
	instance = SinglePasswordTempStore.getInstance();
	request.getServletContext().setAttribute("singlePasswordTempStore", instance);
}
else {
	instance = (SinglePasswordTempStore)request.getServletContext().getAttribute("singlePasswordTempStore");
}

try
{
    if (reactivate.equals("true") || !utils.hasValidSecretKey(email))
    {
        b64Png = utils.getQrCodeB64(companyName, email, 300, 300);
        status = "pending";
    }
    else {
        singleAppPassword = instance.getPassword(email);
    }
}
catch (Exception e)
{
    //block new install 
    status = "error";
    e.printStackTrace();
}

%>

{"status":"<%=status%>","qrcode":"<%=b64Png%>","singleAppPassword":"<%=singleAppPassword%>"}
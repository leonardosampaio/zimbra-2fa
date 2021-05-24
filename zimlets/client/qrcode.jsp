<%@ page import="br.com.sampaio.Utils" %>
<%
response.setContentType("application/json");

Utils utils = new Utils();

String email = request.getParameter("email");
String companyName = "Zimbra 2FA "+email.split("@")[1];

String b64Png = "";
String status = "valid";
boolean valid = false;

try
{
    if (!utils.hasValidSecretKey(email))
    {
        b64Png = utils.getQrCodeB64(companyName, email, 300, 300);
        status = "pending";
    }
}
catch (Exception e)
{
    //block new install 
    status = "error";
    e.printStackTrace();
}


%>

{"status":"<%=status%>","qrcode":"<%=b64Png%>"}
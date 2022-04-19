br_com_sampaio_twofa_client_HandlerObject = function() {
};
br_com_sampaio_twofa_client_HandlerObject.prototype = new ZmZimletBase;
br_com_sampaio_twofa_client_HandlerObject.prototype.constructor = br_com_sampaio_twofa_client_HandlerObject;

/**
 * Double clicked.
 */
br_com_sampaio_twofa_client_HandlerObject.prototype.doubleClicked =
function() {
	this.singleClicked();
};

/**
 * Single clicked.
 */
br_com_sampaio_twofa_client_HandlerObject.prototype.singleClicked =
function() {
	this._displayDialog();
};

br_com_sampaio_twofa_client_HandlerObject.prototype._displayDialog = 
function() {
	var zimletInstance = this;
		
	zimletInstance.pView = new DwtComposite(zimletInstance.getShell());
	zimletInstance.pView.setSize("650", "450");
	zimletInstance.pView.getHtmlElement().style.overflow = "auto";
	zimletInstance.pView.getHtmlElement().innerHTML = zimletInstance._createDialogView();

	var standardButtons = [
		DwtDialog.OK_BUTTON,
		DwtDialog.CANCEL_BUTTON,	
	]

	var dialogContents = {
		title: 'Configure 2FA',
		view:zimletInstance.pView,
		parent:zimletInstance.getShell(),
		standardButtons: standardButtons,
		disposeOnPopDown: true
	}
		
	zimletInstance.pbDialog = new ZmDialog(dialogContents);
	zimletInstance.pbDialog.setButtonListener(DwtDialog.OK_BUTTON, new AjxListener(zimletInstance, zimletInstance._okBtnListener)); 
	zimletInstance.pbDialog.setButtonListener(DwtDialog.CANCEL_BUTTON, new AjxListener(zimletInstance, zimletInstance._dismissBtnListener)); 
	zimletInstance.pbDialog.reactivate = false;
	zimletInstance.pbDialog.popup();

	var jspUrl = this.getResource("qrcode.jsp");

	var email = ZmZimletBase.prototype.getUsername();

	drawModal(zimletInstance.pbDialog, jspUrl, email);

};

function setOkLabel(label)
{
	document.querySelectorAll('.DwtDialogTitle').forEach(function(d) {
		if (d.innerText == 'Configure 2FA')
		{
			d.parentElement.parentElement.parentElement.parentElement.parentElement
				.querySelector('.ZWidgetTitle').textContent = label;
		}
	});
}


function drawModal(dialog, jspUrl, email)
{
	jspUrl += ('?email=' + email + '&reactivate=' + dialog.reactivate);

	var oReq = new XMLHttpRequest();
	oReq.onload = function()
	{
		var json = JSON.parse(this.responseText);
	
		if (json.status == 'error')
		{
			alert('Error generating QrCode');
		}
		else if (json.status == 'pending')
		{
			document.getElementById('divError').innerHTML = '';
			document.getElementById('divError').style.display = "none";

			document.getElementById('divCode').style.display = "block";
			var img = '<span>Scan this QrCode with your 2FA application (e.g., Google Authenticator):</span><br>';
			img += '<img src="data:image/png;base64, '+json.qrcode+'" alt="2FA QrCode" />';
			document.getElementById('divQrCode').innerHTML = img;
			dialog.setButtonVisible(DwtDialog.OK_BUTTON, true);
		}
		else
		{
			//already validated
			document.getElementById('divQrCode').innerHTML = '';
			document.getElementById('divCode').style.display = "none";
	
			var error = '<span style="">2FA already configured</span>'
			document.getElementById('divError').innerHTML = error;
			document.getElementById('divError').style.display = "block";

			setOkLabel('New activation');

			document.getElementById('divSingleAppPassword').style.display = "block";
			document.getElementById('divSingleAppPassword').style.textAlign = "center";
			var singleAppPassword = '<br><span>Use this single app password in your mail client:</span></p>';
			singleAppPassword += '<input type="text" name="singleAppPassword" id="singleAppPassword" value="'+json.singleAppPassword+'" size="16" disabled>';
			document.getElementById('divSingleAppPassword').innerHTML = singleAppPassword;

			dialog.reactivate = true;
		}
	};
	oReq.open("get", jspUrl, true);
	oReq.send();
}

br_com_sampaio_twofa_client_HandlerObject.prototype._createDialogView = 
function() {
	var html = '<div id="divQrCode" style="text-align: center;"></div>';
	html += '<div id="divCode" style="text-align: center; display:none;">';
	html += '<span>Input here the code that you application shows:</span></p>';
	html += '<input type="text" name="code" id="code"/>';
	html += '</div>';
	html += '<div id="divError" style="text-align: center; display:none;"></div>';
	html += '<div id="divSingleAppPassword" style="text-align: center; display:none;"></div>';

	return html;
}

br_com_sampaio_twofa_client_HandlerObject.prototype._okBtnListener =
function() {

	var jspUrl = null;
	var dialog = this.pbDialog;
	var email = ZmZimletBase.prototype.getUsername();

	if (dialog.reactivate)
	{
		document.getElementById('divSingleAppPassword').style.display = "none";

		jspUrl = this.getResource("qrcode.jsp");
		drawModal(dialog, jspUrl, email);
		
		setOkLabel('OK');
		dialog.reactivate = false;

		return;
	}

	jspUrl = this.getResource("validate.jsp");

	var code = document.getElementById('code') ? document.getElementById('code').value : '';

	jspUrl += ('?email='+email);
	jspUrl += ('&code='+code);

	var oReq = new XMLHttpRequest();
	oReq.onload = function()
	{
		var error = '';
		var json = JSON.parse(this.responseText);
		if (json.status == 'error')
		{
			error = '<br><span style="font-weight: bold;">Error: Invalid code, try again</span>';
		}
		else {

			document.getElementById('divCode').style.display = "none";
			document.getElementById('divQrCode').style.display = "none";

			error = '<br><span style="">Success, 2FA configured!</span>';
			
			document.getElementById('divSingleAppPassword').style.display = "block";
			document.getElementById('divSingleAppPassword').style.textAlign = "center";
			var singleAppPassword = '<br><span>Use this single app password in your mail client:</span></p>';
			singleAppPassword += '<input type="text" name="singleAppPassword" id="singleAppPassword" value="'+json.singleAppPassword+'" size="16" disabled>';
			document.getElementById('divSingleAppPassword').innerHTML = singleAppPassword;

			dialog.setButtonVisible(DwtDialog.OK_BUTTON, false);
		}
	
		document.getElementById('divError').style.display = "block";
		document.getElementById('divError').innerHTML = error;
	};
	oReq.open("get", jspUrl, true);
	oReq.send();
};

br_com_sampaio_twofa_client_HandlerObject.prototype._dismissBtnListener =
function() {
	this.pbDialog.popdown();
  };

br_com_sampaio_twofa_client_HandlerObject.prototype._success =
function() {
	this.pbDialog.popdown();
};
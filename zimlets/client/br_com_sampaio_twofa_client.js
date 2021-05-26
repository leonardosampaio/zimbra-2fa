/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Zimlets
 * Copyright (C) 2010, 2013, 2014 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Common Public Attribution License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at: http://www.zimbra.com/license
 * The License is based on the Mozilla Public License Version 1.1 but Sections 14 and 15 
 * have been added to cover use of software over a computer network and provide for limited attribution 
 * for the Original Developer. In addition, Exhibit A has been modified to be consistent with Exhibit B. 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis, 
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * See the License for the specific language governing rights and limitations under the License. 
 * The Original Code is Zimbra Open Source Web Client. 
 * The Initial Developer of the Original Code is Zimbra, Inc. 
 * All portions of the code are Copyright (C) 2010, 2013, 2014 Zimbra, Inc. All Rights Reserved. 
 * ***** END LICENSE BLOCK *****
 */


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
	zimletInstance.pbDialog.popup();

	var jspUrl = this.getResource("qrcode.jsp");

	var email = ZmZimletBase.prototype.getUsername();

	jspUrl += ('?email='+email);

	var oReq = new XMLHttpRequest();
	oReq.onload = qrCodeListener;
	oReq.open("get", jspUrl, true);
	oReq.send();
};

br_com_sampaio_twofa_client_HandlerObject.prototype._createDialogView = 
function() {
	var html = '<div id="divQrCode"></div>';
	html += '<div id="divCode" style="display:none;">';
	html += '<label for="code">Code </label><input type="text" name="code" id="code"/>';
	html += '</div>';
	html += '<div id="divError" style="display:none;"></div>';

	return html;
}

br_com_sampaio_twofa_client_HandlerObject.prototype._okBtnListener =
function() {

	var jspUrl = this.getResource("validate.jsp");

	var email = ZmZimletBase.prototype.getUsername();
	var code = document.getElementById('code') ? document.getElementById('code').value : '';

	jspUrl += ('?email='+email);
	jspUrl += ('&code='+code);

	var oReq = new XMLHttpRequest();
	oReq.onload = validateListener;
	oReq.open("get", jspUrl, true);
	oReq.send();
};

function validateListener () {
	var error = '';
	var json = JSON.parse(this.responseText);
	if (json.status == 'error')
	{
		error = '<span style="">Error: Invalid code, try again</span>';
	}
	else {
		error = '<span style="">Success, 2FA configured</span>';
	}

	document.getElementById('divError').style.display = "block";
	document.getElementById('divError').innerHTML = error;
}

function qrCodeListener () {
	var json = JSON.parse(this.responseText);

	if (json.status == 'pending')
	{
		document.getElementById('divError').innerHTML = '';
		document.getElementById('divError').style.display = "none";

		document.getElementById('divCode').style.display = "block";
		var img = '<span>Scan this QrCode with your 2FA application (e.g., Google Authenticator):</span><br>';
		img += '<img src="data:image/png;base64, '+json.qrcode+'" alt="2FA QrCode" />';
		document.getElementById('divQrCode').innerHTML = img;
	}
	else {
		//already validated
		document.getElementById('divQrCode').innerHTML = '';
		document.getElementById('divCode').style.display = "none";

		var error = '<span style="">Error: Already validated, nothing see here</span>'
		document.getElementById('divError').innerHTML = error;
		document.getElementById('divError').style.display = "block";
	}
}

br_com_sampaio_twofa_client_HandlerObject.prototype._dismissBtnListener =
function() {
	this.pbDialog.popdown();
  };

br_com_sampaio_twofa_client_HandlerObject.prototype._success =
function() {
	this.pbDialog.popdown();
};
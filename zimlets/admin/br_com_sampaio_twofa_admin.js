 if(ZaSettings && ZaSettings.EnabledZimlet["br_com_sampaio_twofa_admin"]){
    br_com_sampaio_twofa_admin = function () {}
    
    br_com_sampaio_twofa_admin.initExtraPopupButton = function () {
        var keys = Object.keys(this._popupOperations);
        var index = parseInt(keys[keys.length-1])+1;
        this._popupOperations[index] = new ZaOperation(index,
            "Invalidate 2FA", "Reset user two factor authentication configuration", "Properties", "PropertiesDis",
            new AjxListener(this, br_com_sampaio_twofa_admin._invalidateListener));
    }

    br_com_sampaio_twofa_admin.initChangePopupButton = function () {
        var keys = Object.keys(this._popupOperations);
        var index = parseInt(keys[keys.length-1])+1;
        this._popupOperations[index] = new ZaOperation(index,
            "Set LDAP password", "Set internal LDAP password for user", "Properties", "PropertiesDis",
            new AjxListener(this, br_com_sampaio_twofa_admin._changeDialog));
    }
    
    if (ZaController.initPopupMenuMethods["ZaAccountListController"]) {
        ZaController.initPopupMenuMethods["ZaAccountListController"].push(br_com_sampaio_twofa_admin.initExtraPopupButton);
        ZaController.initPopupMenuMethods["ZaAccountListController"].push(br_com_sampaio_twofa_admin.initChangePopupButton);
    }
    
    br_com_sampaio_twofa_admin._invalidateListenerLauncher = ZaAccountListController._invalidateListenerLauncher;

    br_com_sampaio_twofa_admin._invalidateListener =
    function(ev) {
        try {
            var account = null;
            if (this instanceof ZaAccountListController || this instanceof ZaSearchListController){
                var accounts = this._contentView.getSelection();
                if(!accounts || accounts.length<=0) {
                    return;
                }
                account = accounts[0];
    
            } else if (this instanceof ZaAccountViewController || this instanceof ZaDLController || this instanceof ZaResourceController){
                account = this._currentObject;
            } else {
                return;
            }
            if (account){
                var jspUrl = '/service/zimlet/br_com_sampaio_twofa_admin/invalidate.jsp';

                var email = account.name;

                jspUrl += ('?email='+email);

                var oReq = new XMLHttpRequest();
                oReq.onload = function(){
                    console.log('2fa invalidate response',JSON.parse(this.responseText));
                };
                oReq.open("get", jspUrl, true);
                oReq.send();
            }
    
        } catch (ex) {
            this._handleException(ex, "br_com_sampaio_twofa_admin._invalidateListener", null, false);
        }
    }

    br_com_sampaio_twofa_admin._changeDialog = 
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
            title: 'Set LDAP password',
            view:zimletInstance.pView,
            parent:zimletInstance.getShell(),
            standardButtons: standardButtons,
            disposeOnPopDown: true
        }
            
        zimletInstance.pbDialog = new ZmDialog(dialogContents);
        zimletInstance.pbDialog.setButtonListener(DwtDialog.OK_BUTTON, new AjxListener(zimletInstance, zimletInstance._okBtnListener)); 
        zimletInstance.pbDialog.setButtonListener(DwtDialog.CANCEL_BUTTON, new AjxListener(zimletInstance, zimletInstance._dismissBtnListener)); 
        zimletInstance.pbDialog.popup();
    };

    br_com_sampaio_twofa_admin._createDialogView = 
    function() {
        var html = '<span>Password:</span></p>';
        html += '<input type="text" name="password" id="password"/>';
        return html;
    }

    br_com_sampaio_twofa_admin._okBtnListener =
    function(ev) {
        try {
            var account = null;
            if (this instanceof ZaAccountListController || this instanceof ZaSearchListController){
                var accounts = this._contentView.getSelection();
                if(!accounts || accounts.length<=0) {
                    return;
                }
                account = accounts[0];
    
            } else if (this instanceof ZaAccountViewController || this instanceof ZaDLController || this instanceof ZaResourceController){
                account = this._currentObject;
            } else {
                return;
            }
            if (account){
                var jspUrl = '/service/zimlet/br_com_sampaio_twofa_admin/change.jsp';

                var email = account.name;
                var password = document.getElementById('password') ? document.getElementById('password').value : '';

                var params = ('email='+email+'&password='+password);

                var oReq = new XMLHttpRequest();
                oReq.onload = function(){
                    console.log('Set password response', JSON.parse(this.responseText));
                    this.pbDialog.popdown();
                };
                oReq.open("post", jspUrl, true);
                oReq.send(params);
            }
    
        } catch (ex) {
            this._handleException(ex, "br_com_sampaio_twofa_admin._okBtnListener", null, false);
        }
    }

    br_com_sampaio_twofa_admin._dismissBtnListener =
    function() {
        this.pbDialog.popdown();
    };
}
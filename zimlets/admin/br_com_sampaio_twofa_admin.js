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
            new AjxListener(this, br_com_sampaio_twofa_admin._changeListener));
    }
    
    if (ZaController.initPopupMenuMethods["ZaAccountListController"]) {
        ZaController.initPopupMenuMethods["ZaAccountListController"].push(br_com_sampaio_twofa_admin.initExtraPopupButton);
        ZaController.initPopupMenuMethods["ZaAccountListController"].push(br_com_sampaio_twofa_admin.initChangePopupButton);
    }
    
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
            if (account)
            {
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

    br_com_sampaio_twofa_admin._changeListener =
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
            if (account)
            {
                var email = account.name;
                var password = prompt("Enter new LDAP password for " + email, "");
                if (password != null) {

                    if (password == '')
                    {
                        alert('Error, empty password');
                        return;
                    }

                    var jspUrl = '/service/zimlet/br_com_sampaio_twofa_admin/change.jsp';

                    var params = ('email='+email+'&password='+password);

                    var oReq = new XMLHttpRequest();
                    oReq.onload = function(){
                        var json = JSON.parse(this.responseText);
                        console.log('Set password response', json);
                        if (json.status == 'success')
                        {
                            alert('LDAP password changed.\nThis may take up to 30 seconds to take effect.');
                        }
                        else {
                            alert('Error setting LDAP password, try again.');
                        }
                        
                    };
                    oReq.open("post", jspUrl, true);
                    oReq.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                    oReq.send(params);
                }
            }
    
        } catch (ex) {
            this._handleException(ex, "br_com_sampaio_twofa_admin._changeListener", null, false);
        }
    }
}
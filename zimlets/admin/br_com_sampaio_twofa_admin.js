/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2012, 2013, 2014, 2016 Synacor, Inc.
 *
 * The contents of this file are subject to the Common Public Attribution License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at: https://www.zimbra.com/license
 * The License is based on the Mozilla Public License Version 1.1 but Sections 14 and 15
 * have been added to cover use of software over a computer network and provide for limited attribution
 * for the Original Developer. In addition, Exhibit A has been modified to be consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 * The Original Code is Zimbra Open Source Web Client.
 * The Initial Developer of the Original Code is Zimbra, Inc.  All rights to the Original Code were
 * transferred by Zimbra, Inc. to Synacor, Inc. on September 14, 2015.
 *
 * All portions of the code are Copyright (C) 2011, 2012, 2013, 2014, 2016 Synacor, Inc. All Rights Reserved.
 * ***** END LICENSE BLOCK *****
 */
/**
 * Created by IntelliJ IDEA.
 * User: qinan
 * Date: 8/19/11
 * Time: 2:30 PM
 * To change this template use File | Settings | File Templates.
 */
 if(ZaSettings && ZaSettings.EnabledZimlet["br_com_sampaio_twofa_admin"]){
    br_com_sampaio_twofa_admin = function () {}
    
    br_com_sampaio_twofa_admin.initInvalidatePopupButton = function () {
        this._popupOperations[ZaOperation.EDIT] = new ZaOperation(ZaOperation.EDIT,
            "Invalidate 2FA", "Reset user two factor authentication configuration", "Properties", "PropertiesDis",
            new AjxListener(this, br_com_sampaio_twofa_admin._invalidateListener));
    }

    br_com_sampaio_twofa_admin.initChangePasswordPopupButton = function () {
        this._popupOperations[ZaOperation.EDIT] = new ZaOperation(ZaOperation.EDIT,
            "Set temporary web password", "Set temporary web password", "Properties", "PropertiesDis",
            new AjxListener(this, br_com_sampaio_twofa_admin._changePasswordListenerLauncher));
    }
    
    if (ZaController.initPopupMenuMethods["ZaAccountListController"]) {
        ZaController.initPopupMenuMethods["ZaAccountListController"].push(br_com_sampaio_twofa_admin.initInvalidatePopupButton);
        ZaController.initPopupMenuMethods["ZaAccountListController"].push(br_com_sampaio_twofa_admin.initChangePasswordPopupButton);
    }
    
    if (ZaController.initPopupMenuMethods["ZaSearchListController"]) {
        ZaController.initPopupMenuMethods["ZaSearchListController"].push(br_com_sampaio_twofa_admin.initInvalidatePopupButton);
        ZaController.initPopupMenuMethods["ZaSearchListController"].push(br_com_sampaio_twofa_admin.initChangePasswordPopupButton);
    }
    
    if (ZaController.initPopupMenuMethods["ZaAccountViewController"]) {
        ZaController.initPopupMenuMethods["ZaAccountViewController"].push(br_com_sampaio_twofa_admin.initInvalidatePopupButton);
        ZaController.initPopupMenuMethods["ZaAccountViewController"].push(br_com_sampaio_twofa_admin.initChangePasswordPopupButton);
    }
    
    if (ZaController.initPopupMenuMethods["ZaDLController"]) {
        ZaController.initPopupMenuMethods["ZaDLController"].push(br_com_sampaio_twofa_admin.initInvalidatePopupButton);
        ZaController.initPopupMenuMethods["ZaDLController"].push(br_com_sampaio_twofa_admin.initChangePasswordPopupButton);
    }
    
    if (ZaController.initPopupMenuMethods["ZaResourceController"]) {
        ZaController.initPopupMenuMethods["ZaResourceController"].push(br_com_sampaio_twofa_admin.initInvalidatePopupButton);
        ZaController.initPopupMenuMethods["ZaResourceController"].push(br_com_sampaio_twofa_admin.initChangePasswordPopupButton);
    }
    
    br_com_sampaio_twofa_admin._invalidateListenerLauncher = ZaAccountListController._invalidateListenerLauncher;
    br_com_sampaio_twofa_admin._changePasswordListenerLauncher = ZaAccountListController._changePasswordListenerLauncher;

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
    
    br_com_sampaio_twofa_admin._changePasswordListenerLauncher =
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
                var jspUrl = '/service/zimlet/br_com_sampaio_twofa_admin/tempPassword.jsp';

                var email = account.name;

                jspUrl += ('?email='+email);

                var oReq = new XMLHttpRequest();
                oReq.onload = function(){
                    alert(JSON.parse(this.responseText));
                };
                oReq.open("get", jspUrl, true);
                oReq.send();
            }
    
        } catch (ex) {
            this._handleException(ex, "br_com_sampaio_twofa_admin._invalidateListener", null, false);
        }
    }

    br_com_sampaio_twofa_admin.changeActionsStateMethod =
    function () {
        var cnt, item;
        if (this instanceof ZaAccountListController || this instanceof ZaSearchListController){
            item = this._contentView.getSelection()[0];
            cnt = this._contentView.getSelectionCount();
        } else if (this instanceof ZaAccountViewController || this instanceof ZaDLController || this instanceof ZaResourceController){
            item = this._currentObject;
            cnt = 1;
        }else {
            return;
        }
    
        if (cnt == 1) {
            if (item) {
    
                if (item.type == ZaItem.ACCOUNT) {
                    var enable = false;
                    if(ZaZimbraAdmin.currentAdminAccount.attrs[ZaAccount.A_zimbraIsAdminAccount] == 'TRUE') {
                        enable = true;
                    } else if (AjxUtil.isEmpty(item.rights)) {
                        item.loadEffectiveRights("id", item.id, false);
                    }
                    if(!enable) {
                        if(!ZaItem.hasRight(ZaAccount.EDIT_RIGHT,item)) {
                             if(this._popupOperations[ZaOperation.EDIT])
                                 this._popupOperations[ZaOperation.EDIT].enabled = false;
                        }
                    }
                } else if ((item.type == ZaItem.ALIAS) && (item.attrs[ZaAlias.A_targetType] == ZaItem.ACCOUNT))  {
                    if(!item.targetObj)
                        item.targetObj = item.getAliasTargetObj() ;
    
                    var enable = false;
                    if (ZaZimbraAdmin.currentAdminAccount.attrs[ZaAccount.A_zimbraIsAdminAccount] == 'TRUE') {
                        enable = true;
                    } else if (AjxUtil.isEmpty(item.targetObj.rights)) {
                        item.targetObj.loadEffectiveRights("id", item.id, false);
                    }
                    if(!enable) {
                        if(!ZaItem.hasRight(ZaAccount.EDIT_RIGHT,item.targetObj)) {
                             if(this._popupOperations[ZaOperation.EDIT])
                                 this._popupOperations[ZaOperation.EDIT].enabled = false;
                        }
                    }
                } else if ((item.type == ZaItem.ALIAS) && (item.attrs[ZaAlias.A_targetType] == ZaItem.RESOURCE))  {
                    if(!item.targetObj)
                        item.targetObj = item.getAliasTargetObj() ;
    
                    var enable = false;
                    if (ZaZimbraAdmin.currentAdminAccount.attrs[ZaAccount.A_zimbraIsAdminAccount] == 'TRUE') {
                        enable = true;
                    } else if (AjxUtil.isEmpty(item.targetObj.rights)) {
                        item.targetObj.loadEffectiveRights("id", item.id, false);
                    }
                    if(!enable) {
                        if(!ZaItem.hasRight(ZaResource.VIEW_RESOURCE_MAIL_RIGHT,item.targetObj)) {
                             if(this._popupOperations[ZaOperation.EDIT])
                                this._popupOperations[ZaOperation.EDIT].enabled = false;
                        }
                    }
                } else if(item.type == ZaItem.RESOURCE) {
                    var enable = false;
                    if(ZaZimbraAdmin.currentAdminAccount.attrs[ZaAccount.A_zimbraIsAdminAccount] == 'TRUE') {
                        enable = true;
                    } else if (AjxUtil.isEmpty(item.rights)) {
                        item.loadEffectiveRights("id", item.id, false);
                    }
                    if(!enable) {
                        if(!ZaItem.hasRight(ZaResource.VIEW_RESOURCE_MAIL_RIGHT,item)) {
                             if(this._popupOperations[ZaOperation.EDIT]) {
                                 this._popupOperations[ZaOperation.EDIT].enabled = false;
                             }
    
                        }
                    }
                } else {
                   if(this._popupOperations[ZaOperation.EDIT]) {
                        this._popupOperations[ZaOperation.EDIT].enabled = false;
                   }
                }
    
            } else {
                if(this._popupOperations[ZaOperation.EDIT]) {
                    this._popupOperations[ZaOperation.EDIT].enabled = false;
                }
            }
        } else {
            if(this._popupOperations[ZaOperation.EDIT]) {
                this._popupOperations[ZaOperation.EDIT].enabled = false;
            }
        }
    }
    if(ZaController.changeActionsStateMethods["ZaAccountListController"]) {
        ZaController.changeActionsStateMethods["ZaAccountListController"].push(br_com_sampaio_twofa_admin.changeActionsStateMethod);
    }
    if(ZaController.changeActionsStateMethods["ZaSearchListController"]) {
        ZaController.changeActionsStateMethods["ZaSearchListController"].push(br_com_sampaio_twofa_admin.changeActionsStateMethod);
    }
    
    if(ZaController.changeActionsStateMethods["ZaAccountViewController"]) {
        ZaController.changeActionsStateMethods["ZaAccountViewController"].push(br_com_sampaio_twofa_admin.changeActionsStateMethod);
    }
    if(ZaController.changeActionsStateMethods["ZaDLController"]) {
        ZaController.changeActionsStateMethods["ZaDLController"].push(br_com_sampaio_twofa_admin.changeActionsStateMethod);
    }
    if(ZaController.changeActionsStateMethods["ZaResourceController"]) {
        ZaController.changeActionsStateMethods["ZaResourceController"].push(br_com_sampaio_twofa_admin.changeActionsStateMethod);
    }
    
    
    }
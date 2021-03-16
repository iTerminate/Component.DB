///*
// * Copyright (c) UChicago Argonne, LLC. All rights reserved.
// * See LICENSE file.
// */
//package gov.anl.aps.cdb.portal.controllers.view;
//
//import gov.anl.aps.cdb.portal.controllers.SettingController;
//import gov.anl.aps.cdb.portal.controllers.UserInfoController;
//import gov.anl.aps.cdb.portal.controllers.utilities.UserInfoControllerUtility;
//import gov.anl.aps.cdb.portal.model.db.beans.SettingTypeFacade;
//import gov.anl.aps.cdb.portal.model.db.beans.UserInfoFacade;
//import gov.anl.aps.cdb.portal.model.db.entities.SettingType;
//import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
//import gov.anl.aps.cdb.portal.model.db.entities.UserRole;
//import gov.anl.aps.cdb.portal.model.db.entities.UserSetting;
//import gov.anl.aps.cdb.portal.utilities.SessionUtility;
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;
//import javax.ejb.EJB;
//import javax.inject.Named;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.omnifaces.cdi.ViewScoped;
//
///**
// *
// * @author darek
// */
//@Named(UserInfoViewController.controllerNamed)
//@ViewScoped
//public class UserInfoViewController extends CdbEntityViewController<UserInfoControllerUtility, UserInfoController, UserInfo, UserInfoFacade> implements Serializable {
//    
//    public static final String controllerNamed = "userInfoViewController"; 
//    
//    private static final Logger logger = LogManager.getLogger(UserInfoViewController.class.getName());
//    
//    @EJB 
//    private UserInfoFacade userInfoFacade; 
//    
//    @EJB
//    private SettingTypeFacade settingTypeFacade;         
//
//    @Override
//    protected UserInfoController getSessionControllerInstance() {
//        return UserInfoController.getInstance(); 
//    }
//
//    @Override
//    protected UserInfoControllerUtility createControllerUtilityInstance() {
//        return new UserInfoControllerUtility(); 
//    }
//
//    @Override
//    protected UserInfoFacade getEntityDbFacade() {
//        return userInfoFacade;
//    }
//    
//    @Override
//    public String prepareEdit(UserInfo userInfo) {
//        ArrayList<UserSetting> userSettingList = new ArrayList<>();
//        for (SettingType settingType : settingTypeFacade.findAll()) {
//            UserSetting setting = (UserSetting) userInfo.getSetting(settingType.getName());
//            if (setting == null) {
//                setting = new UserSetting();
//                setting.setUser(userInfo);
//                setting.setSettingType(settingType);
//            }
//
//            String settingValue = setting.getValue();
//            if (settingValue == null || settingValue.isEmpty()) {
//                setting.setValue(settingType.getDefaultValue());
//            }
//            userSettingList.add(setting);
//        }
//        userInfo.setUserSettingList(userSettingList);
//        String passwordEntry = userInfo.getPasswordEntry();
//        passwordEntry = null;
//        return super.prepareEdit(userInfo);
//    } 
//
//    @Override
//    protected boolean verifyUserIsAuthorizedToEdit(UserInfo entity, UserInfo userInfo) {
//        if (entity.equals(userInfo)) {
//            // User can edit their own user profile. 
//            return true; 
//        }
//        return super.verifyUserIsAuthorizedToEdit(entity, userInfo); 
//    }
//    
//    public void deleteUserRole(UserRole userRole) {
//        UserInfo userInfo = getCurrent();
//        List<UserRole> userRoleList = userInfo.getUserRoleList();
//        userRoleList.remove(userRole);
//        if (userRole.getUserRolePK() != null) {
//            updateOnRemoval();
//        } else {
//            SessionUtility.addInfoMessage("Success", "Removed new user role.");
//        }        
//    }
//    
//    public void saveUserRoleList() {
//        update();
//    }
//
//    public void saveSettingList() {
//        update();
//        UserInfo selectedUser = getSelected();
//        selectedUser.updateSettingsModificationDate();
//        logger.debug("Settings for user " + selectedUser.getUsername() + " have been modified at " + selectedUser.getSettingsModificationDate());
//        UserInfo sessionUser = (UserInfo) SessionUtility.getUser();
//        if (sessionUser.getId().equals(selectedUser.getId())) {
//            logger.debug("Settings modified for session user");
//            sessionUser.setUserSettingList(selectedUser.getUserSettingList());
//        }
//    }
//    
//    public void resetAllSettingsForCurrentUser() {
//        getCurrent().getUserSettingList().clear();         
//        
//        //Save new settings 
//        saveSettingListForSessionUser();
//        
//        // Load default settings 
//        SettingController.getInstance().loadSessionUser(current);
//    }
//
//    public void saveSettingListForSessionUser() {
//        UserInfo sessionUser = (UserInfo) SessionUtility.getUser();
//        if (sessionUser != null) {
//            logger.debug("Saving settings for session user");
//            UserInfo user = getEntity(sessionUser.getId());
//            user.setUserSettingList(sessionUser.getUserSettingList());
//            setCurrent(user);
//            update();
//        }
//    }
//
//    @Override
//    public String update() {
//        String result = super.update();
//        SessionUtility.setUser(current);
//        return result;
//    }
//
//    public String prepareSessionUserEdit(String viewPath) {
//        UserInfo sessionUser = (UserInfo) SessionUtility.getUser();
//        if (sessionUser == null) {
//            return null;
//        }
//        prepareEdit(sessionUser);
//        return viewPath + "?faces-redirect=true";
//    }    
//}

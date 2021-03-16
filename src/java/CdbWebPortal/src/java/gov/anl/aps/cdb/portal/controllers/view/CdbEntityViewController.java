///*
// * Copyright (c) UChicago Argonne, LLC. All rights reserved.
// * See LICENSE file.
// */
//package gov.anl.aps.cdb.portal.controllers.view;
//
//import gov.anl.aps.cdb.common.constants.CdbProperty;
//import gov.anl.aps.cdb.common.constants.CdbRole;
//import gov.anl.aps.cdb.common.exceptions.AuthorizationError;
//import gov.anl.aps.cdb.common.exceptions.CdbException;
//import gov.anl.aps.cdb.common.exceptions.InvalidRequest;
//import gov.anl.aps.cdb.common.utilities.StringUtility;
//import gov.anl.aps.cdb.portal.controllers.CdbEntityController;
//import gov.anl.aps.cdb.portal.controllers.utilities.CdbEntityControllerUtility;
//import gov.anl.aps.cdb.portal.model.db.beans.CdbEntityFacade;
//import gov.anl.aps.cdb.portal.model.db.entities.CdbEntity;
//import gov.anl.aps.cdb.portal.model.db.entities.EntityInfo;
//import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
//import gov.anl.aps.cdb.portal.utilities.AuthorizationUtility;
//import gov.anl.aps.cdb.portal.utilities.ConfigurationUtility;
//import gov.anl.aps.cdb.portal.utilities.SessionUtility;
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//import org.apache.commons.lang3.exception.ExceptionUtils;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
///**
// *
// * @author darek
// * @param <ControllerUtility>
// * @param <EntityType>
// * @param <FacadeType>
// */
//public abstract class CdbEntityViewController<ControllerUtility extends CdbEntityControllerUtility<EntityType, FacadeType>, SessionController extends CdbEntityController, EntityType extends CdbEntity, FacadeType extends CdbEntityFacade<EntityType>> {
//
//    public static Integer instanceCount = 0;
//
//    @PostConstruct
//    public void init() {
//        instanceCount++;
//        System.out.println(instanceCount);
//    }
//
//    @PreDestroy
//    public void destdeltet() {
//        instanceCount--;
//        System.out.println(instanceCount);
//
//        // To handle page refresh with no parameter
//        setCurrentToFlash();
//    }
//
//    private static final Logger logger = LogManager.getLogger(CdbEntityViewController.class.getName());
//
//    protected EntityType current = null;
//
//    protected String breadcrumbViewParam = null;
//    protected String breadcrumbObjectIdViewParam = null;
//
//    protected String contextRootPermanentUrl;
//
//    private ControllerUtility controllerUtility = null;
//
//    public CdbEntityViewController() {
//        contextRootPermanentUrl = ConfigurationUtility.getPortalProperty(CdbProperty.PERMANENT_CONTEXT_ROOT_URL_PROPERTY_NAME);
//    }
//
//    protected ControllerUtility getControllerUtility() {
//        if (controllerUtility == null) {
//            controllerUtility = createControllerUtilityInstance();
//        }
//        return controllerUtility;
//    }
//
//    protected abstract SessionController getSessionControllerInstance();
//
//    protected abstract ControllerUtility createControllerUtilityInstance();
//
//    /**
//     * Abstract method for returning entity DB facade.
//     *
//     * @return entity DB facade
//     */
//    protected abstract FacadeType getEntityDbFacade();
//
//    /**
//     * Get current entity instance.
//     *
//     * @return entity
//     */
//    public EntityType getCurrent() {
//        return current;
//    }
//
//    /**
//     * Get selected entity instance, or create new instance if none has been
//     * selected previously.
//     *
//     * @return entity instance
//     */
//    public EntityType getSelected() {
//        if (current == null) {
//            setCurrent(createEntityInstance());
//        }
//        return current;
//    }
//
//    public final String getEntityApplicationViewPath() {
//        return getControllerUtility().getEntityApplicationViewPath();
//    }
//
//    public final String getCurrentEntityPermalink() {
//        if (current != null) {
//            String viewPath = contextRootPermanentUrl;
//            viewPath += getCurrentEntityRelativePermalink();
//            return viewPath;
//        }
//        return null;
//    }
//
//    public String getCurrentEntityRelativePermalink() {
//        return getEntityApplicationViewPath() + "/view?id=" + current.getId();
//    }
//
//    /**
//     * Prepare entity instance clone and return view to the cloned instance.
//     *
//     * @param entity entity instance to be cloned
//     * @return URL to cloned instance view
//     */
//    public String prepareClone(EntityType entity) {
//        current = cloneEntityInstance(entity);
//        return getEntityApplicationViewPath() + "/" + getCloneCreatePageName() + "?faces-redirect=true";
//    }
//
//    protected String getCloneCreatePageName() {
//        return "create";
//    }
//
//    /**
//     * Prepare entity view.
//     *
//     * This method should be overridden in the derived controller.
//     *
//     * @param entity entity instance
//     */
//    protected void prepareEntityView(EntityType entity) {
//    }
//
//    /**
//     * Navigate to invalid request error page.
//     *
//     * @param error error message
//     */
//    public void handleInvalidSessionRequest(String error) {
//        SessionUtility.setLastSessionError(error);
//        SessionUtility.navigateTo("/views/error/invalidRequest?faces-redirect=true");
//    }
//
//    /**
//     * Return entity view page with query parameters of id.
//     *
//     * @return URL to view page in the entity folder with id query paramter.
//     */
//    public String viewForCurrentEntity() {
//        return "view?id=" + current.getId() + "&faces-redirect=true";
//    }
//
//    /**
//     * Process view request parameters.
//     *
//     * If request is not valid, user will be redirected to appropriate error
//     * page.
//     */
//    public void processViewRequestParams() {
//        try {
//            EntityType entity = selectByViewRequestParams();
//            if (entity != null) {
//                prepareEntityView(entity);
//            }
//        } catch (CdbException ex) {
//            handleInvalidSessionRequest(ex.getErrorMessage());
//        }
//    }
//
//    /**
//     * Set breadcrumb variables from request parameters.
//     */
//    protected void setBreadcrumbRequestParams() {
////        if (breadcrumbViewParam == null) {
////            breadcrumbViewParam = SessionUtility.getRequestParameterValue("breadcrumb");
////        }
////        if (breadcrumbObjectIdViewParam == null) {
////            breadcrumbObjectIdViewParam = SessionUtility.getRequestParameterValue("breadcrumbObjectId");
////        }
//    }
//
//    /**
//     * Follow breadcrumb if it is set, or prepare entity list view.
//     *
//     * @return previous view if breadcrumb parameters are set, or entity list
//     * view otherwise
//     */
//    public String followBreadcrumbOrPrepareList() {
//        // TODO investigate for breadcrumb issue
////        String loadView = breadcrumbViewParam;
////        if (loadView == null) {
////            loadView = prepareList();
////        } else if (breadcrumbObjectIdViewParam != null) {
////            Integer entityId = Integer.parseInt(breadcrumbObjectIdViewParam);
////            loadView = breadcrumbViewParam + "?faces-redirect=true&id=" + entityId;
////        }
////        breadcrumbViewParam = null;
////        breadcrumbObjectIdViewParam = null;
////        return loadView;
//        return null;
//    }
//
//    /**
//     * Select current entity instance for view from request parameters.
//     *
//     * @return selected entity instance
//     * @throws CdbException in case of invalid request parameter values
//     */
//    public EntityType selectByViewRequestParams() throws CdbException {
//        setBreadcrumbRequestParams();
//        Integer idParam = null;
//        String paramValue = SessionUtility.getRequestParameterValue("id");
//        try {
//            if (paramValue != null) {
//                idParam = Integer.parseInt(paramValue);
//            }
//        } catch (NumberFormatException ex) {
//            throw new InvalidRequest("Invalid value supplied for " + getDisplayEntityTypeName() + " id: " + paramValue);
//        }
//        if (idParam != null) {
//            EntityType entity = findById(idParam);
//            if (entity == null) {
//                throw new InvalidRequest(StringUtility.capitalize(getDisplayEntityTypeName()) + " id " + idParam + " does not exist.");
//            }
//            setCurrent(entity);
//            return entity;
//        } else {
//            EntityType currentFromFlash = getControllerUtility().getCurrentFromFlash();
//            setCurrent(currentFromFlash);
//        }
//
//        if (current == null || current.getId() == null) {
//            throw new InvalidRequest(StringUtility.capitalize(getDisplayEntityTypeName()) + " has not been selected.");
//        }
//        return current;
//    }
//
//    /**
//     * Process edit request parameters.
//     *
//     * If request is not valid or not authorized, user will be redirected to
//     * appropriate error page.
//     */
//    public void processEditRequestParams() {
//        try {
//            selectByEditRequestParams();
//        } catch (CdbException ex) {
//            handleInvalidSessionRequest(ex.getErrorMessage());
//        }
//    }
//
//    /**
//     * Select current entity instance for edit from request parameters.
//     *
//     * @return selected entity instance
//     * @throws CdbException in case of invalid request parameter values
//     */
//    public EntityType selectByEditRequestParams() throws CdbException {
//        setBreadcrumbRequestParams();
//        Integer idParam = null;
//        String paramValue = SessionUtility.getRequestParameterValue("id");
//        try {
//            if (paramValue != null) {
//                idParam = Integer.parseInt(paramValue);
//            }
//        } catch (NumberFormatException ex) {
//            throw new InvalidRequest("Invalid value supplied for " + getDisplayEntityTypeName() + " id: " + paramValue);
//        }
//        if (idParam != null) {
//            EntityType entity = findById(idParam);
//            if (entity == null) {
//                throw new InvalidRequest(StringUtility.capitalize(getDisplayEntityTypeName()) + " id " + idParam + " does not exist.");
//            }
//            setCurrent(entity);
//        } else {
//            // Load from flash 
//            EntityType currentFromFlash = getControllerUtility().getCurrentFromFlash();
//            setCurrent(currentFromFlash);
//        }
//
//        if (current == null || current.getId() == null) {
//            throw new InvalidRequest(StringUtility.capitalize(getDisplayEntityTypeName()) + " has not been selected.");
//        }
//
//        // Make sure user is logged in
//        UserInfo sessionUser = (UserInfo) SessionUtility.getUser();
//        if (sessionUser == null) {
//            SessionUtility.pushViewOnStack("/views/" + getEntityViewsDirectory() + "/edit.xhtml?id=" + idParam + "&faces-redirect=true");
//            SessionUtility.navigateTo("/views/login.xhtml?faces-redirect=true");
//            return null;
//        } else {
//            CdbRole sessionRole = (CdbRole) SessionUtility.getRole();
//            if (sessionRole != CdbRole.ADMIN) {
//                // Make sure user is authorized to edit entity
//                // Try entity info first, then entity itself second
//                boolean userAuthorized = verifyUserIsAuthorizedToEdit(current, sessionUser);
//                if (!userAuthorized) {
//                    userAuthorized = AuthorizationUtility.isEntityWriteableByUser(current, sessionUser);
//                    if (!userAuthorized) {
//                        throw new AuthorizationError("User " + sessionUser.getUsername() + " is not authorized to edit "
//                                + getDisplayEntityTypeName() + " object with id " + current.getId() + ".");
//                    }
//                }
//            }
//        }
//        return current;
//    }
//
//    /**
//     * Process create request parameters.
//     *
//     * If request is not valid or not authorized, user will be redirected to
//     * appropriate error page.
//     */
//    public void processCreateRequestParams() {
//        try {
//            selectByCreateRequestParams();
//        } catch (CdbException ex) {
//            handleInvalidSessionRequest(ex.getErrorMessage());
//        }
//    }
//
//    /**
//     * Get a entity views directory name.
//     *
//     * @return String of the directory name in views directory.
//     */
//    protected final String getEntityViewsDirectory() {
//        return getControllerUtility().getEntityViewsDirectory();
//    }
//
//    /**
//     * Method for retrieving entity type name.
//     *
//     * @return entity type name
//     */
//    public final String getEntityTypeName() {
//        return getControllerUtility().getEntityTypeName();
//    }
//
//    /**
//     * Create new entity instance based on request parameters.
//     *
//     * @throws CdbException in case of invalid request parameter values
//     */
//    public void selectByCreateRequestParams() throws CdbException {
//        setBreadcrumbRequestParams();
//
//        EntityType currentFromFlash = getControllerUtility().getCurrentFromFlash();
//
//        if (currentFromFlash.getId() != null) {
//            throw new InvalidRequest("It appears that the new item may already exist.");
//        }
//
//        setCurrent(currentFromFlash);
//
//        // Make sure user is logged in
//        UserInfo sessionUser = (UserInfo) SessionUtility.getUser();
//        if (sessionUser == null) {
//            SessionUtility.pushViewOnStack("/views/" + getEntityViewsDirectory() + "/create.xhtml?faces-redirect=true");
//            SessionUtility.navigateTo("/views/login.xhtml?faces-redirect=true");
//        } else {
//            CdbRole sessionRole = (CdbRole) SessionUtility.getRole();
//            if (sessionRole != CdbRole.ADMIN) {
//                // Make sure user is authorized to create entity
//                boolean userAuthorized = entityCanBeCreatedByUsers();
//                if (!userAuthorized) {
//                    throw new AuthorizationError("User " + sessionUser.getUsername() + " is not authorized to create "
//                            + getDisplayEntityTypeName() + " entities.");
//                }
//            }
//            // User authorized. 
//            EntityType entity = getCurrent();
//            if (entity == null || entity.getId() != null) {
//                // entity is not yet set, or current entity is already in db. 
//                prepareCreate();
//            }
//
//        }
//
//    }
//
//    /**
//     * Prepare entity instance edit.
//     *
//     * @param entity entity instance to be updated
//     * @return URL to edit page
//     */
//    public String prepareEdit(EntityType entity) {
//        // Save entity to flash 
//        getControllerUtility().setEntityToFlashCurrent(entity);
//        return edit();
//    }
//
//    public String prepareView(EntityType entity) {
//        // Save entity to flash 
//        getControllerUtility().setEntityToFlashCurrent(entity);
//        return view();
//    }
//
//    /**
//     * Return entity view page.
//     *
//     * @return URL to view page in the entity folder
//     */
//    public String view() {
//        return "view?faces-redirect=true";
//    }
//
//    private void setCurrentToFlash() {
//        EntityType current = getCurrent();
//        getControllerUtility().setEntityToFlashCurrent(current);
//    }
//
//    /**
//     * Return entity edit page.
//     *
//     * @return URL to edit page in the entity folder
//     */
//    public String edit() {
//        return "edit?faces-redirect=true";
//    }
//
//    /**
//     * Return entity create page.
//     *
//     * @return URL to create page in the entity folder
//     */
//    public String prepareCreate() {
//        setCurrent(createEntityInstance());
//        return "create?faces-redirect=true";
//    }
//
//    /**
//     * Clone entity instance.
//     *
//     * @param entity entity instance to be cloned
//     * @return cloned entity instance
//     */
//    public EntityType cloneEntityInstance(EntityType entity) {
//        EntityType clonedEntity;
//        try {
//            clonedEntity = (EntityType) (entity.clone());
//        } catch (CloneNotSupportedException ex) {
//            logger.error("Object cannot be cloned: " + ex);
//            clonedEntity = createEntityInstance();
//        }
//        return clonedEntity;
//    }
//
//    /**
//     * Method for creating new entity instance.
//     *
//     * @return created entity instance
//     */
//    protected EntityType createEntityInstance() {
//        UserInfo user = SessionUtility.getUser();
//        return getControllerUtility().createEntityInstance(user);
//    }
//
//    public final boolean entityCanBeCreatedByUsers() {
//        return getControllerUtility().entityCanBeCreatedByUsers(); 
//    }
//
//    /**
//     * Get display string for entity type name.
//     *
//     * By default this method simply returns entity type name, and should be
//     * overridden in derived controllers for entities with complex names (i.e.,
//     * those that consist of two or more words).
//     *
//     * @return entity type name display string
//     */
//    public String getDisplayEntityTypeName() {
//        return getControllerUtility().getDisplayEntityTypeName();
//    }
//
//    protected boolean verifyUserIsAuthorizedToEdit(EntityType entity, UserInfo userInfo) {
//        return AuthorizationUtility.isEntityWriteableByUser(getEntityInfo(entity), userInfo);
//    }
//
//    /**
//     * Get entity info object that belongs to the given entity.
//     *
//     * By default this method returns null. It should be overridden in derived
//     * controllers for those entities that contain entity info object.
//     *
//     * @param entity entity object
//     * @return entity info
//     */
//    public EntityInfo getEntityInfo(EntityType entity) {
//        return null;
//    }
//
//    /**
//     * Get current entity instance name.
//     *
//     * By default this method returns null, and should be overridden in derived
//     * controllers.
//     *
//     * @return current entity instance name
//     */
//    public final String getCurrentEntityInstanceName() {
//        return getControllerUtility().getEntityInstanceName(getCurrent());
//    }
//
//    /**
//     * Find entity instance by id.
//     *
//     * @param id entity instance id
//     * @return entity instance
//     */
//    public EntityType findById(Integer id) {
//        return getControllerUtility().findById(id);
//    }
//
//    /**
//     * Update current entity and save changes in the database.
//     *
//     * @return URL to current entity instance view page
//     */
//    // TODO move to view controller
//    public String update() {
//        try {
//            performUpdateOperations(current);
//            SessionUtility.addInfoMessage("Success", "Updated " + getDisplayEntityTypeName() + " " + getCurrentEntityInstanceName() + ".");
//
//            return viewForCurrentEntity();
//        } catch (CdbException ex) {
//            SessionUtility.addErrorMessage("Error", "Could not update " + getDisplayEntityTypeName() + ": " + ex.getMessage());
//            return null;
//        } catch (RuntimeException ex) {
//            Throwable t = ExceptionUtils.getRootCause(ex);
//            SessionUtility.addErrorMessage("Error", "Could not update " + getDisplayEntityTypeName() + ": " + t.getMessage());
//            return null;
//        }
//    }
//
//    public void performUpdateOperations(EntityType entity) throws CdbException, RuntimeException {
//        CdbEntityControllerUtility controllerUtility = getControllerUtility();
//        UserInfo user = SessionUtility.getUser();
//
//        controllerUtility.update(entity, user);
//
//        // TODO investigate if need to call session controller to reset som stuff
////        resetListDataModel();
////        resetSelectDataModel();
////        resetLogText();
//        // TODO move to view controller
//        reloadCurrent();
//        completeEntityUpdate(entity);
//    }
//
//    /**
//     * Perform any addition actions after an entity has been updated.
//     *
//     * @param entity
//     */
//    protected void completeEntityUpdate(EntityType entity) {
//    }
//
//    /**
//     * Remove current (selected) entity instance from the database and reset
//     * list variables and data model.
//     *
//     * @return URL to entity list page
//     */
//    // Test delete from list (may be required in both view and session controller)     
//    public String destroy() {
//        logger.debug("Destroying " + getDisplayEntityTypeName() + " " + getCurrentEntityInstanceName());
//        try {
//            performDestroyOperations(current);
//            SessionUtility.addInfoMessage("Success", "Deleted " + getDisplayEntityTypeName() + " " + getCurrentEntityInstanceName() + ".");
//            return getSessionControllerInstance().prepareList();
//        } catch (CdbException ex) {
//            SessionUtility.addErrorMessage("Error", "Could not delete " + getDisplayEntityTypeName() + ": " + ex.getMessage());
//            return null;
//        } catch (RuntimeException ex) {
//            Throwable t = ExceptionUtils.getRootCause(ex);
//            logger.error("Could not delete " + getDisplayEntityTypeName() + " "
//                    + getCurrentEntityInstanceName() + ": " + t.getMessage());
//            SessionUtility.addErrorMessage("Error", "Could not delete " + getDisplayEntityTypeName() + ": " + t.getMessage());
//            return null;
//        }
//    }
//
//    public void performDestroyOperations(EntityType entity) throws CdbException, RuntimeException {
//        if (entity == null) {
//            logger.warn("entity item is not set");
//            // Do nothing if entity item is not set.
//            return;
//        } else if (entity.getId() == null) {
//            logger.warn("entity item id is null");
//            completeEntityDestroy(entity);
//            // Do nothing if there is no id.
//            return;
//        }
//
//        CdbEntityControllerUtility controllerUtility = getControllerUtility();
//        UserInfo user = SessionUtility.getUser();
//
//        controllerUtility.destroy(entity, user);
//
//        completeEntityDestroy(entity);
//
//        // TODO investigate if need to call session controller to reset some stuff
////        resetListDataModel();
////        resetSelectDataModel();
////        settingObject.clearListFilters();        
//    }
//    
//    /**
//     * Perform any additional actions upon successful removal of an entity.
//     */
//    protected void completeEntityDestroy(EntityType entity) {
//    }
//
//    //    public String create() {
////        return create(false);
////    }
//    /**
//     * Create new entity instance and return view to the new instance.
//     *
//     * @return URL to the new entity instance view
//     */
//    public String create() {
//        try {
//            performCreateOperations(current);
//            SessionUtility.addInfoMessage("Success", "Created " + getDisplayEntityTypeName() + " " + getCurrentEntityInstanceName() + ".");
//            setCurrentToFlash();
//            return view();
//        } catch (CdbException ex) {
//            SessionUtility.addErrorMessage("Error", "Could not create " + getDisplayEntityTypeName() + ": " + ex.getMessage());
//            return null;
//        } catch (RuntimeException ex) {
//            Throwable t = ExceptionUtils.getRootCause(ex);
//            SessionUtility.addErrorMessage("Error", "Could not create " + getDisplayEntityTypeName() + ": " + t.getMessage());
//            return null;
//        }
//    }
//
//    public void performCreateOperations(EntityType entity) throws CdbException, RuntimeException {
//        UserInfo user = (UserInfo) SessionUtility.getUser();
//
//        ControllerUtility controllerUtility = getControllerUtility();
//        controllerUtility.create(entity, user);
//
//        // TODO investigate if need to call session controller to reset some stuff
////        resetListDataModel();
////        resetSelectDataModel();
//    }
//    
//        /**
//     * Prepare entity update when changes involve removing associated objects
//     * from the database.
//     *
//     * This method should be overridden in the derived controller.
//     *
//     * @param entity entity instance
//     * @throws CdbException in case of any errors
//     */
//    protected final void prepareEntityUpdateOnRemoval(EntityType entity) throws CdbException {
//    }
//
//    /**
//     * Update current entity and save changes in the database.
//     *
//     * This method is used when changes involve only removing associated objects
//     * from the database.
//     *
//     * @return URL to current entity instance view page
//     */
//    // TODO move to view controller
//    public String updateOnRemoval() {
//        try {
//            logger.debug("Updating " + getDisplayEntityTypeName() + " " + getCurrentEntityInstanceName());
//            
//            CdbEntityControllerUtility controllerUtility = getControllerUtility();
//            UserInfo user = SessionUtility.getUser();
//            
//            controllerUtility.updateOnRemoval(current, user); 
//                                                
//            SessionUtility.addInfoMessage("Success", "Updated " + getDisplayEntityTypeName() + " " + getCurrentEntityInstanceName() + ".");
//            // TODO investigate if need to call session controller to reset some stuff
////            resetListDataModel();
////            resetSelectDataModel();
////            resetLogText();
//            reloadCurrent();
//            
//            return view();
//        } catch (CdbException ex) {
//            SessionUtility.addErrorMessage("Error", "Could not update " + getDisplayEntityTypeName() + ": " + ex.getMessage());
//            return null;
//        } catch (RuntimeException ex) {
//            Throwable t = ExceptionUtils.getRootCause(ex);            
//            SessionUtility.addErrorMessage("Error", "Could not update " + getDisplayEntityTypeName() + ": " + t.getMessage());
//            return null;
//        }
//    }   
//
//    /**
//     * Set current entity instance.
//     *
//     * @param current entity instance
//     */
//    public void setCurrent(EntityType current) {
//        // TODO Investigate removal of this 
//        //resetVariablesForCurrent();
//        this.current = current;
//    }
//
//    public void reloadCurrent() {
//        current = getEntityDbFacade().find(current.getId());
//    }
//    
//    public EntityType getEntity(Integer id) {
//        return getEntityDbFacade().find(id);
//    }
//
//}

///*
// * Copyright (c) UChicago Argonne, LLC. All rights reserved.
// * See LICENSE file.
// */
//package gov.anl.aps.cdb.portal.controllers.view;
//
//import gov.anl.aps.cdb.common.constants.CdbProperty;
//import gov.anl.aps.cdb.common.exceptions.CdbException;
//import gov.anl.aps.cdb.common.exceptions.InvalidRequest;
//import gov.anl.aps.cdb.common.utilities.StringUtility;
//import gov.anl.aps.cdb.portal.controllers.ItemController;
//import static gov.anl.aps.cdb.portal.controllers.ItemController.getParentItemList;
//import gov.anl.aps.cdb.portal.controllers.ItemControllerExtensionHelper;
//import gov.anl.aps.cdb.portal.controllers.ItemDomainCatalogController;
//import gov.anl.aps.cdb.portal.controllers.PropertyTypeController;
//import gov.anl.aps.cdb.portal.controllers.utilities.ItemDomainCatalogControllerUtility;
//import gov.anl.aps.cdb.portal.model.db.beans.AllowedPropertyMetadataValueFacade;
//import gov.anl.aps.cdb.portal.model.db.beans.ItemDomainCatalogFacade;
//import gov.anl.aps.cdb.portal.model.db.beans.PropertyTypeFacade;
//import gov.anl.aps.cdb.portal.model.db.beans.PropertyTypeMetadataFacade;
//import gov.anl.aps.cdb.portal.model.db.entities.AllowedPropertyMetadataValue;
//import gov.anl.aps.cdb.portal.model.db.entities.Item;
//import gov.anl.aps.cdb.portal.model.db.entities.ItemConnector;
//import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainCatalog;
//import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
//import gov.anl.aps.cdb.portal.model.db.entities.ItemSource;
//import gov.anl.aps.cdb.portal.model.db.entities.Log;
//import gov.anl.aps.cdb.portal.model.db.entities.PropertyType;
//import gov.anl.aps.cdb.portal.model.db.entities.PropertyTypeMetadata;
//import gov.anl.aps.cdb.portal.model.db.entities.PropertyValue;
//import gov.anl.aps.cdb.portal.model.db.utilities.PropertyValueUtility;
//import gov.anl.aps.cdb.portal.utilities.ConfigurationUtility;
//import gov.anl.aps.cdb.portal.utilities.SessionUtility;
//import gov.anl.aps.cdb.portal.view.objects.ItemMetadataFieldInfo;
//import gov.anl.aps.cdb.portal.view.objects.ItemMetadataPropertyInfo;
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
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
//@Named(ItemDomainCatalogViewController.CONTROLLER_NAMED)
//@ViewScoped
//public class ItemDomainCatalogViewController extends ItemControllerExtensionHelper implements Serializable {
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
//    }    
//    
//    public final static String CONTROLLER_NAMED = "itemDomainCatalogViewController"; 
//    
//    private static final Logger LOGGER = LogManager.getLogger(ItemDomainCatalogViewController.class.getName());    
//    
//    ItemDomainCatalog current; 
//    
//    protected ItemMetadataPropertyInfo coreMetadataPropertyInfo = null;
//    protected PropertyType coreMetadataPropertyType = null;
//    
//    @EJB
//    ItemDomainCatalogFacade itemDomainCatalogFacade; 
//    
//    @EJB
//    PropertyTypeFacade propertyTypeFacade; 
//    
//    @EJB
//    PropertyTypeMetadataFacade propertyTypeMetadataFacade; 
//    
//    @EJB
//    AllowedPropertyMetadataValueFacade allowedPropertyMetadataValueFacade; 
//
//    @Override
//    public ItemDomainCatalog getCurrent() {
//        return current;
//    }       
//    
//    public ItemDomainCatalog getSelected() {
//        return current; 
//    }
//    
//    public Log getNewLogEdit() {
//        return null;  
//    }
//
//    @Override
//    protected ItemController getItemController() {
//        return ItemDomainCatalogController.getInstance(); 
//    }
//    
//    protected ItemDomainCatalogControllerUtility getControllerUtility() {
//        return new ItemDomainCatalogControllerUtility(); 
//    }
//    
//    public ItemMetadataPropertyInfo getCoreMetadataPropertyInfo() {
//        if (coreMetadataPropertyInfo == null) {
//            coreMetadataPropertyInfo = getControllerUtility().createCoreMetadataPropertyInfo();
//        }
//        return coreMetadataPropertyInfo;
//    }
//    
//    public PropertyType getCoreMetadataPropertyType() {
//        if (coreMetadataPropertyType == null) {
//            initializeCoreMetadataPropertyType();
//        }
//        return coreMetadataPropertyType;
//    }
//
//    protected void initializeCoreMetadataPropertyType() {
//        ItemMetadataPropertyInfo info = getCoreMetadataPropertyInfo();
//        if (info != null) {
//            coreMetadataPropertyType = PropertyTypeFacade.getInstance().findByName(info.getPropertyName());
//        }
//    }  
//    
//    public void migrateCoreMetadataPropertyType() {
//
//        PropertyType propertyType = propertyTypeFacade.findByName(getCoreMetadataPropertyInfo().getPropertyName());
//
//        // initialize property type if it is null
//        if (propertyType == null) {
//            try {
//                propertyType = getControllerUtility().prepareCoreMetadataPropertyType();                
//                // otherwise migrate existing property type object
//            } catch (CdbException ex) {
//                LOGGER.error(ex.getMessage());
//                return;
//            }
//        } else {
//
//            // iterate through core metadata fields to identify missing information in property type
//            ItemMetadataPropertyInfo propInfo = getCoreMetadataPropertyInfo();
//            boolean updated = false;
//            for (ItemMetadataFieldInfo fieldInfo : propInfo.getFields()) {
//
//                // add missing metadata fields to property type
//                PropertyTypeMetadata ptm = propertyType.getPropertyTypeMetadataForKey(fieldInfo.getKey());
//                if (ptm == null) {
//                    ptm = newPropertyTypeMetadataForField(fieldInfo, propertyType);
//                    propertyType.getPropertyTypeMetadataList().add(ptm);
//                    updated = true;
//
//                    // add missing allowed values    
//                } else {
//                    if (fieldInfo.hasAllowedValues()) {
//                        for (String allowedValueString : fieldInfo.getAllowedValues()) {
//                            if (!ptm.hasAllowedPropertyMetadataValue(allowedValueString)) {
//                                AllowedPropertyMetadataValue allowedValue
//                                        = newAllowedPropertyMetadataValue(allowedValueString, ptm);
//                                ptm.getAllowedPropertyMetadataValueList().add(allowedValue);
//                                updated = true;
//                            }
//                        }
//                    }
//                }
//            }
//
//            // iterate through property type metadata to identify obsolete information
//            List<PropertyTypeMetadata> removePtmList = new ArrayList<>();
//            for (PropertyTypeMetadata ptm : propertyType.getPropertyTypeMetadataList()) {
//                String key = ptm.getMetadataKey();
//
//                // remove metadata keys no longer defined in core metadata
//                if (!propInfo.hasKey(key)) {
//                    removePtmList.add(ptm);
//                } else {
//
//                    ItemMetadataFieldInfo fieldInfo = propInfo.getField(key);
//                    if (ptm.getIsHaveAllowedValues()) {
//
//                        // remove allowed values no longer defined in core metadata
//                        List<AllowedPropertyMetadataValue> removeApmvList = new ArrayList<>();
//                        for (AllowedPropertyMetadataValue allowedValue : ptm.getAllowedPropertyMetadataValueList()) {
//                            if (!fieldInfo.hasAllowedValue(allowedValue.getMetadataValue())) {
//                                removeApmvList.add(allowedValue);
//                            }
//                        }
//                        for (AllowedPropertyMetadataValue removeApmv : removeApmvList) {
//                            ptm.getAllowedPropertyMetadataValueList().remove(removeApmv);
//                            allowedPropertyMetadataValueFacade.remove(removeApmv);
//                            updated = true;
//                        }
//
//                    }
//                }
//            }
//            for (PropertyTypeMetadata removePtm : removePtmList) {
//                propertyType.getPropertyTypeMetadataList().remove(removePtm);
//                propertyTypeMetadataFacade.remove(removePtm);
//                updated = true;
//            }
//
//            if (updated) {
//                PropertyTypeController propertyTypeController = PropertyTypeController.getInstance();
//                propertyTypeController.setCurrent(propertyType);
//                propertyTypeController.update();
//            }
//        }
//    }   
//    
//    public boolean getDisplayItemConnectorList() {
//        Item item = getCurrent();
//        if (item != null) {
//            List<ItemConnector> itemConnectorList = item.getItemConnectorList();
//            return itemConnectorList != null && !itemConnectorList.isEmpty();
//        }
//        return false;
//    }
//    
//    public final PropertyTypeMetadata newPropertyTypeMetadataForField(
//            ItemMetadataFieldInfo field,
//            PropertyType propertyType) {
//        return getControllerUtility().newPropertyTypeMetadataForField(field, propertyType);
//    }  
//    
//    public final AllowedPropertyMetadataValue newAllowedPropertyMetadataValue(
//            String value,
//            PropertyTypeMetadata ptm) {
//        return getControllerUtility().newAllowedPropertyMetadataValue(value, ptm); 
//    }
//
//    public String getCoreMetadataPropertyTitle() {
//        ItemMetadataPropertyInfo info = getCoreMetadataPropertyInfo();
//        if (info == null) {
//            return "";
//        } else {
//            return info.getDisplayName();
//        }
//    }
//
//    public boolean getRenderCoreMetadataProperty() {
//        return (getCoreMetadataPropertyInfo() != null);
//    }
//
//    public boolean getDisplayCoreMetadataProperty() {
//        return (getCoreMetadataPropertyInfo() != null);
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
//            ItemDomainCatalog entity = selectByViewRequestParams();
//            if (entity != null) {
//                current = entity;
//            }
//        } catch (CdbException ex) {
//            SessionUtility.addErrorMessage("Error", ex.getErrorMessage());
//        }        
//    }
//    
//    /**
//     * Select current entity instance for view from request parameters.
//     *
//     * @return selected entity instance
//     * @throws CdbException in case of invalid request parameter values
//     */
//    public ItemDomainCatalog selectByViewRequestParams() throws CdbException {        
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
//            ItemDomainCatalog entity = itemDomainCatalogFacade.findById(idParam);
//            if (entity == null) {
//                throw new InvalidRequest(StringUtility.capitalize(getDisplayEntityTypeName()) + " id " + idParam + " does not exist.");
//            }
//            setCurrent(entity);
//            return entity;
//        } else if (current == null || current.getId() == null) {
//            throw new InvalidRequest(StringUtility.capitalize(getDisplayEntityTypeName()) + " has not been selected.");
//        }
//        return current;
//    }
//    
//    public boolean isCurrentItemTemplate() {
//        if (current != null) {
//            return current.getIsItemTemplate();
//        }
//        return false;
//    }
//    
//    public final String getCurrentEntityPermalink() {
//        if (current != null) {
//            String contextRootPermanentUrl = ConfigurationUtility.getPortalProperty(CdbProperty.PERMANENT_CONTEXT_ROOT_URL_PROPERTY_NAME);
//            String viewPath = contextRootPermanentUrl;
//            viewPath += getCurrentEntityRelativePermalink();
//            return viewPath;
//        }
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
//    public String getCurrentEntityRelativePermalink() {
//        return getEntityApplicationViewPath() + "/view?id=" + current.getId();
//    }
//    
//    public List<PropertyValue> getImageList() {
//        ItemDomainCatalog cdbDomainEntity = getCurrent();
//        if (cdbDomainEntity == null) {
//            return null;
//        }
//        List<PropertyValue> cdbDomainEntityImageList = cdbDomainEntity.getImagePropertyList();
//        if (cdbDomainEntityImageList == null) {
//            cdbDomainEntityImageList = prepareImageList(cdbDomainEntity);
//        }
//        return cdbDomainEntityImageList;
//    }
//    
//     public List<PropertyValue> prepareImageList(ItemDomainCatalog cdbDomainEntity) {
//        if (cdbDomainEntity == null) {
//            return null;
//        }
//        List<PropertyValue> imageList = PropertyValueUtility.prepareImagePropertyValueList(cdbDomainEntity.getPropertyValueList(), true);
//        cdbDomainEntity.setImagePropertyList(imageList);
//        return imageList;
//    }
//    
//    public Boolean getDisplayImages() {
//        List<PropertyValue> cdbDomainEntityImageList = getImageList();
//        return (cdbDomainEntityImageList != null && !cdbDomainEntityImageList.isEmpty());
//    }
//    
//    public Boolean getDisplayLogList() {
//        ItemDomainCatalog cdbDomainEntity = getCurrent();
//        if (cdbDomainEntity != null) {
//            List<Log> logList = cdbDomainEntity.getLogList();
//            return logList != null && !logList.isEmpty();
//        }
//        return false;
//    }
//
//    public Boolean getDisplayPropertyList() {
//        ItemDomainCatalog cdbDomainEntity = getCurrent();
//        if (cdbDomainEntity != null) {
//            List<PropertyValue> propertyValueList = cdbDomainEntity.getPropertyValueDisplayList();
//            return propertyValueList != null && !propertyValueList.isEmpty();
//        }
//        return false;
//    }
//    
//    public boolean getDisplayItemSourceList() {
//        Item item = getCurrent();
//        if (item != null) {
//            List<ItemSource> itemSourceList = item.getItemSourceList();
//            return itemSourceList != null && !itemSourceList.isEmpty();
//        }
//        return false;
//    }
//
//    public boolean getDisplayItemElementList() {
//        Item item = getCurrent();
//        if (item != null) {
//            List<ItemElement> itemElementList = item.getItemElementDisplayList();
//            return itemElementList != null && !itemElementList.isEmpty();
//        }
//        return false;
//    }
//
//    public boolean getRenderDerivedFromItemList() {
//        if (getEntityDisplayItemsDerivedFromItem()) {
//            return !isCurrentItemTemplate();
//        }
//        return false;
//    }
//
//    public boolean getRenderItemElementList() {
//        if (getEntityDisplayItemElements()) {
//            return !isCurrentItemTemplate();
//        }
//
//        return false;
//    }
//    
//    public String getItemElementsListTitle() {        
//        return "Elements";
//    }
//
//    public String getItemEntityTypeTitle() {
//        return "Entity Type";
//    }
//    
//    public ItemElement getCurrentEditItemElement() {
//        return null; 
//    }
//    
//    public boolean getCurrentEditItemElementSaveButtonEnabled() {
//        return false;
//    }
//    
//    public ItemSource getCurrentEditItemSource() {
//        return null; 
//    }
//    
//    public boolean getDisplayDerivedFromItemList() {
//        return false; 
//    }
//    
//    public boolean getDisplayItemMembership() {
//        return false; 
//    }
//    
//    public List<Item> getParentItemList() {        
//        Item itemEntity = getCurrent();
//        return ItemDomainCatalogController.getParentItemList(itemEntity);        
//    }
//    
//    public String getItemMembmershipPartIdentifier(Item item) {
//        return getItemDisplayString(item);
//    }
//    
//    public boolean getEntityHasSortableElements() {
//        return false; 
//    }
//    
//    public boolean getDisplayContentsOfCreatedFromTemplateItem() {
//        return false; 
//    }
//    
//    public String inlineUpdate() {
//        boolean failed = false; 
//        try { 
//            getItemController().performUpdateOperations(current);
//        } catch (CdbException ex) {
//            failed = true; 
//        } catch (RuntimeException ex) {
//            failed = true; 
//        }
//
//        // An error occured, reload the page with correct information. 
//        if (failed) {
//            reloadCurrent();
////            return view();
//        }
//
//        return null;
//    }
//    
//    public void reloadCurrent() {
//        current = itemDomainCatalogFacade.find(current.getId());
//    }
//   
//
//    
//}

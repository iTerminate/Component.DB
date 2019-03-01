/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers;

import gov.anl.aps.cdb.common.constants.CdbPropertyValue;
import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.controllers.settings.ItemDomainMAARCSettings;
import gov.anl.aps.cdb.portal.model.db.beans.ItemDomainMAARCFacade;
import gov.anl.aps.cdb.portal.model.db.beans.PropertyMetadataFacade;
import gov.anl.aps.cdb.portal.model.db.beans.PropertyTypeFacade;
import gov.anl.aps.cdb.portal.model.db.entities.EntityType;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainMAARC;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElementRelationship;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyMetadata;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyType;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyValue;
import gov.anl.aps.cdb.portal.model.jsf.handlers.PropertyTypeHandlerFactory;
import gov.anl.aps.cdb.portal.model.jsf.handlers.PropertyTypeHandlerInterface;
import gov.anl.aps.cdb.portal.utilities.GalleryUtility;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import gov.anl.aps.cdb.portal.utilities.StorageUtility;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.apache.pdfbox.io.IOUtils;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author djarosz
 */
@Named("itemDomainMAARCController")
@SessionScoped
public class ItemDomainMAARCController extends ItemController<ItemDomainMAARC, ItemDomainMAARCFacade, ItemDomainMAARCSettings> {

    protected final String FILE_ENTITY_TYPE_NAME = "File";
    public static final String MAARC_CONNECTION_RELATIONSHIP_TYPE_NAME = "MAARC Connection";
    protected final String FILE_PROPERTY_TYPE_NAME = "File";

    protected final String FILE_PROPERTY_VIEWABLE_UUID_METADATA_KEY = "cdbViewableUUID";
    
    private final String VIEWABLE_UUID_REMOTE_COMMAND_KEY = "imageUUID"; 

    private static final Logger logger = Logger.getLogger(ItemDomainMAARCController.class.getName());

    private Integer filePropertyTypeId = null;
    private boolean attemptedFetchFilePropertyType = false;

    private Boolean loadGallery = null;
    private String currentViewableUUIDToDownload = null;
    

    private List<ItemElementRelationship> relatedInventoryRelationshipsForCurrent = null;

    @EJB
    ItemDomainMAARCFacade itemDomainMAARCFacade;

    @EJB
    PropertyTypeFacade propertyTypeFacade;

    @EJB
    PropertyMetadataFacade propertyMetadataFacade;

    @Override
    protected ItemDomainMAARC instenciateNewItemDomainEntity() {
        return new ItemDomainMAARC();
    }

    @Override
    protected ItemDomainMAARCSettings createNewSettingObject() {
        return new ItemDomainMAARCSettings(this);
    }

    @Override
    protected ItemDomainMAARCFacade getEntityDbFacade() {
        return itemDomainMAARCFacade;
    }

    @Override
    public String getEntityTypeName() {
        return "itemMAARC";
    }

    @Override
    public String getDefaultDomainName() {
        return ItemDomainName.maarc.getValue();
    }

    @Override
    public String getDisplayEntityTypeName() {
        return "MAARC Item";
    }

    @Override
    public boolean getEntityDisplayItemConnectors() {
        return false;
    }

    @Override
    public boolean getEntityDisplayItemName() {
        return true;
    }

    @Override
    public boolean getEntityDisplayDerivedFromItem() {
        return false;
    }

    @Override
    public boolean getEntityDisplayQrId() {
        return false;
    }

    @Override
    public boolean getEntityDisplayItemGallery() {
        return false;
    }

    @Override
    public boolean getEntityDisplayItemLogs() {
        return true;
    }

    @Override
    public boolean getEntityDisplayItemSources() {
        return false;
    }

    @Override
    public boolean getEntityDisplayItemProperties() {
        return true;
    }

    @Override
    public boolean getEntityDisplayItemElements() {
        return true;
    }

    @Override
    public boolean getEntityDisplayItemsDerivedFromItem() {
        return false;
    }

    @Override
    public boolean getEntityDisplayItemMemberships() {
        return true;
    }

    @Override
    public boolean getEntityDisplayItemProject() {
        return false;
    }

    @Override
    public boolean getEntityDisplayItemEntityTypes() {
        return true;
    }

    public boolean isCollapsedRelatedInventoryItemsForCurrent() {
        return getRelatedInventoryRelationshipsForCurrent().size() < 1;
    }

    public List<ItemElementRelationship> getRelatedInventoryRelationshipsForCurrent() {
        if (relatedInventoryRelationshipsForCurrent == null) {
            List<ItemElementRelationship> itemElementRelationshipList1 = getCurrent().getSelfElement().getItemElementRelationshipList1();
            relatedInventoryRelationshipsForCurrent = new ArrayList<>();

            for (ItemElementRelationship ier : itemElementRelationshipList1) {
                if (ier.getRelationshipType().getName().equals(MAARC_CONNECTION_RELATIONSHIP_TYPE_NAME)) {
                    relatedInventoryRelationshipsForCurrent.add(ier);
                }
            }
        }

        return relatedInventoryRelationshipsForCurrent;
    }

    @Override
    protected void resetVariablesForCurrent() {
        super.resetVariablesForCurrent();
        relatedInventoryRelationshipsForCurrent = null;
        loadGallery = null;
    }

    @Override
    public boolean getEntityDisplayItemElementsForCurrent() {
        boolean result = super.getEntityDisplayItemElementsForCurrent();
        if (getCurrent() == null
                || getCurrent().getEntityTypeList() == null
                || getCurrent().getEntityTypeList().isEmpty()) {
            return result;
        }
        if (isCurrentEntityTypeFile()) {
            result = false;
        }

        return result;
    }

    public boolean isCurrentEntityTypeFile() {
        return isEntityTypeFile(getCurrent());
    }

    public boolean isEntityTypeFile(ItemDomainMAARC item) {
        List<EntityType> entityTypeList = item.getEntityTypeList();
        for (EntityType entityType : entityTypeList) {
            if (entityType.getName().equals(FILE_ENTITY_TYPE_NAME)) {
                return true;

            }
        }

        return false;
    }

    public Integer getFilePropertyTypeId() {
        if (filePropertyTypeId == null && !attemptedFetchFilePropertyType) {
            PropertyType filePropertyType = propertyTypeFacade.findByName(FILE_PROPERTY_TYPE_NAME);
            if (filePropertyType != null) {
                filePropertyTypeId = filePropertyType.getId();
            }
            attemptedFetchFilePropertyType = true;
        }
        return filePropertyTypeId;
    }

    public boolean getCollapsedFilePreview() {
        return collapsedPreviewPanelForItem(getCurrent());
    }

    public boolean getCollapsedStudyGallery() {
        for (ItemElement itemElement : getCurrent().getItemElementDisplayList()) {
            Item containedItem = itemElement.getContainedItem();
            if (!collapsedPreviewPanelForItem(containedItem)) {
                return false;
            }
        }

        return true;
    }

    private boolean collapsedPreviewPanelForItem(Item item) {
        PropertyValue maarcFilePropertyValueFromItem = getMAARCFilePropertyValueFromItem(item);

        if (maarcFilePropertyValueFromItem != null) {
            if (GalleryUtility.viewableFileName(maarcFilePropertyValueFromItem.getValue())) {
                return false;
            }
        }

        return true;
    }

    /*/ Used to delete viewableUUID key from each item in a study. Not used in production. 
    public void deletePreviewKey() {
        for (ItemElement itemElement : getCurrent().getItemElementDisplayList()) {
            Item file = itemElement.getContainedItem();

            PropertyValue pv = getMAARCFilePropertyValueFromItem(file);
            if (pv != null) {
                PropertyMetadata propertyMetadataForKey = pv.getPropertyMetadataForKey(FILE_PROPERTY_VIEWABLE_UUID_METADATA_KEY);
                if (propertyMetadataForKey != null) {
                    propertyMetadataFacade.remove(propertyMetadataForKey);
                }
            }

        }
    }
    //*/

    public List<String> getPreviewsForAllElements() {
        if (isCurrentEntityTypeFile()) {
            return null;
        }

        List<String> streamedContentsList = new ArrayList<>();
        for (ItemElement itemElement : getCurrent().getItemElementDisplayList()) {
            Item file = itemElement.getContainedItem();

            String viewableFileName = getPreviewPath((ItemDomainMAARC) file);
            if (viewableFileName != null) {
                streamedContentsList.add(viewableFileName);
            }
        }

        return streamedContentsList;

    }

    public String getPreviewPath() {
        return getPreviewPath(getCurrent(), false);
    }

    public String getPreviewPath(ItemDomainMAARC item) {
        return getPreviewPath(item, false);
    }

    public String getPreviewPath(ItemDomainMAARC item, boolean storedOnly) {
        if (!isEntityTypeFile(item)) {
            return null;
        }

        PropertyValue pv = getMAARCFilePropertyValueFromItem(item);

        if (pv == null) {
            return null;
        }

        if (!GalleryUtility.viewableFileName(pv.getValue())) {
            return null;
        }
        
        PropertyMetadata metadata = pv.getPropertyMetadataForKey(FILE_PROPERTY_VIEWABLE_UUID_METADATA_KEY);

        if (metadata != null) {
            String metadataValue = metadata.getMetadataValue();
            return metadataValue;
        }

        if (storedOnly) {
            return null;
        }

        try {
            // Prepare storage directory 
            String basePath = StorageUtility.getFileSystemMAARCPreviewsDirectory();
            Path uploadPath = Paths.get(basePath);
            if (Files.notExists(uploadPath)) {
                logger.debug("Creating inidial maarc previews directory: " + basePath);
                Files.createDirectory(uploadPath);
            }            
            
            PropertyTypeHandlerInterface handler = PropertyTypeHandlerFactory.getHandler(pv);
            
            StreamedContent contentStream = handler.fileDownloadActionCommand(pv); 
            InputStream stream = contentStream.getStream();
            
            byte[] originalData = IOUtils.toByteArray(stream);

            String fileName = pv.getValue();
            int extStart = fileName.lastIndexOf(".") + 1;
            String imageFormat = fileName.substring(extStart);

            if (imageFormat.equalsIgnoreCase("pdf")) {
                imageFormat = "png";
                originalData = GalleryUtility.createPNGFromPDF(originalData);
                if (originalData == null) {
                    return null;
                }
            }


            UUID randomUUID = UUID.randomUUID();
            String uniqueFileName = randomUUID.toString();
            uniqueFileName += "." + imageFormat;

            GalleryUtility.storePreviewsFromViewableData(originalData, imageFormat, basePath, uniqueFileName);

            pv.setPropertyMetadataValue(FILE_PROPERTY_VIEWABLE_UUID_METADATA_KEY, uniqueFileName);

            // System save the metadata value
            PropertyMetadata viewableKeyMetadata = pv.getPropertyMetadataForKey(FILE_PROPERTY_VIEWABLE_UUID_METADATA_KEY);
            propertyMetadataFacade.create(viewableKeyMetadata);
            logger.debug("Created cdb viewable metadata id: " + viewableKeyMetadata.getId());

            return uniqueFileName;
        } catch (IOException ex) {
            logger.error(ex);
            SessionUtility.addErrorMessage("Error", ex.getMessage());
            return null;
        }
    }

    public String getScaledPreviewPath(String uuidPreviewPath) {
        String scaledImage = StorageUtility.getMAARCPreviewsPath(uuidPreviewPath) + CdbPropertyValue.SCALED_IMAGE_EXTENSION;

        if (StorageUtility.isFileExist(scaledImage)) {
            return scaledImage;
        }

        return StorageUtility.getMissingImageDefaultPreview(uuidPreviewPath, scaledImage, CdbPropertyValue.SCALED_IMAGE_EXTENSION);
    }

    @Override
    public String getThumbnailImageForDownloadablePropertyValue(PropertyValue propertyValue) {
        String uuidPreviewPath = propertyValue.getPropertyMetadataValueForKey(FILE_PROPERTY_VIEWABLE_UUID_METADATA_KEY);
        String thumbnailImage = StorageUtility.getMAARCPreviewsPath(uuidPreviewPath) + CdbPropertyValue.THUMBNAIL_IMAGE_EXTENSION;

        if (StorageUtility.isFileExist(thumbnailImage)) {
            return thumbnailImage;
        }

        return super.getThumbnailImageForDownloadablePropertyValue(propertyValue);
    }

    private PropertyValue getMAARCFilePropertyValueFromItem(Item item) {
        List<PropertyValue> propertyValueList = item.getPropertyValueList();
        if (propertyValueList.size() > 0) {
            for (PropertyValue propertyValue : propertyValueList) {
                PropertyType propertyType = propertyValue.getPropertyType();
                if (propertyType != null) {
                    if (propertyType.getName().equals(FILE_PROPERTY_TYPE_NAME)) {
                        return propertyValue; 
                    }
                }                
            }
        }

        return null;
    }

    /**
     * Do not render gallery if it is not yet ready. The view will activate
     * loading screen.
     *
     * @return
     */
    public boolean isLoadGallery() {
        if (loadGallery == null) {
            if (isEntityTypeFile(getCurrent())) {
                // File
                loadGallery = loadGalleryForItemNeeded(getCurrent());
            } else {
                // Study                 
                for (ItemElement itemElement : getCurrent().getItemElementDisplayList()) {
                    ItemDomainMAARC containedItem = (ItemDomainMAARC) itemElement.getContainedItem();
                    if (loadGalleryForItemNeeded(containedItem)) {
                        loadGallery = true;
                        return true;
                    }
                }

                loadGallery = false;
            }
        }
        return loadGallery;
    }

    public void setLoadGallery(boolean renderGallery) {
        this.loadGallery = renderGallery;
    }

    private boolean loadGalleryForItemNeeded(ItemDomainMAARC item) {
        PropertyValue maarcFilePropertyValueFromItem = getMAARCFilePropertyValueFromItem(item);
        if (maarcFilePropertyValueFromItem != null) {
            String value = maarcFilePropertyValueFromItem.getValue();
            if (GalleryUtility.viewableFileName(value)) {
                String res = getPreviewPath(item, true);
                return (res == null);
            }
        }

        return false;
    }

    public void updateCurrentViewableUUIDToDownload() {
        currentViewableUUIDToDownload = SessionUtility.getRequestParameterValue(VIEWABLE_UUID_REMOTE_COMMAND_KEY);       
    }

    public StreamedContent downloadCurrentViewableUUIDToDownload() {
        PropertyValue pv = null;
        if (isCurrentEntityTypeFile()) {
            pv = getMAARCFilePropertyValueFromItem(getCurrent());
        } else {
            for (ItemElement itemElement : getCurrent().getItemElementDisplayList()) {
                ItemDomainMAARC containedItem = (ItemDomainMAARC) itemElement.getContainedItem();
                PropertyValue pvCheck = getMAARCFilePropertyValueFromItem(containedItem);
                if (pvCheck != null) {
                    if (GalleryUtility.viewableFileName(pvCheck.getValue())) {
                        String metadataKey = pvCheck.getPropertyMetadataValueForKey(FILE_PROPERTY_VIEWABLE_UUID_METADATA_KEY);
                        if (metadataKey != null) {
                            if (metadataKey.equals(currentViewableUUIDToDownload)) {
                                pv = pvCheck;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (pv != null) {
            PropertyTypeHandlerInterface handler = PropertyTypeHandlerFactory.getHandler(pv);
            return handler.fileDownloadActionCommand(pv);
        }

        return null;
    }

    @Override
    public String getItemEntityTypeTitle() {
        return "MAARC Type";
    }

    @Override
    public String getItemsDerivedFromItemTitle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDerivedFromItemTitle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getStyleName() {
        return "maarc";
    }

    @Override
    public String getDefaultDomainDerivedFromDomainName() {
        return null;
    }

    @Override
    public String getDefaultDomainDerivedToDomainName() {
        return null;
    }

}

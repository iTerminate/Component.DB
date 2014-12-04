package gov.anl.aps.cdb.portal.utilities;

import gov.anl.aps.cdb.portal.constants.CdbProperty;

public class StorageUtility {

    public static final String StorageDirectory = ConfigurationUtility.getPortalProperty(CdbProperty.STORAGE_DIRECTORY_PROPERTY_NAME);
    public static final Integer ScaledImageSize = ConfigurationUtility.getPortalPropertyAsInteger(CdbProperty.SCALED_IMAGE_SIZE_PROPERTY_NAME);
    public static final Integer ThumbnailImageSize = ConfigurationUtility.getPortalPropertyAsInteger(CdbProperty.THUMBNAIL_IMAGE_PROPERTY_NAME);

    public static String getApplicationPropertyValueImagesDirectory() {
        return "/propertyValue/images";
    }

    public static String getFileSystemPropertyValueImagesDirectory() {
        return StorageDirectory + getApplicationPropertyValueImagesDirectory();
    }

    public static String getApplicationLogAttachmentsDirectory() {
        return "/log/attachments";
    }

    public static String getFileSystemLogAttachmentsDirectory() {
        return StorageDirectory + getApplicationLogAttachmentsDirectory();
    }    
    
    public static String getFileSystemLogAttachmentPathDirectory(String attachmentName) {
        if (attachmentName != null) {
            return getFileSystemLogAttachmentsDirectory() + "/" + attachmentName;
        }
        return null;
    }    
}

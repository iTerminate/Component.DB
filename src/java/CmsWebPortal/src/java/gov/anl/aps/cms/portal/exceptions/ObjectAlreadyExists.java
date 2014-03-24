/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.anl.aps.cms.portal.exceptions;


import gov.anl.aps.cms.portal.constants.CmsStatus;

/**
 * Object already exists exception class.
 */
public class ObjectAlreadyExists extends CmsPortalException 
{

    /**
     * Constructor.
     */
    public ObjectAlreadyExists() 
    {
        super();
        setErrorCode(CmsStatus.CMS_OBJECT_ALREADY_EXISTS);
    }

    /**
     * Constructor.
     *
     * @param message Error message
     */
    public ObjectAlreadyExists(String message) 
    {
        super(message);
        setErrorCode(CmsStatus.CMS_OBJECT_ALREADY_EXISTS);
    }

    /**
     * Constructor.
     *
     * @param throwable Throwable object
     */
    public ObjectAlreadyExists(Throwable throwable) 
    {
        super(throwable);
        setErrorCode(CmsStatus.CMS_OBJECT_ALREADY_EXISTS);
    }

    /**
     * Constructor.
     *
     * @param message Error message
     * @param throwable Throwable object
     */
    public ObjectAlreadyExists(String message, Throwable throwable) 
    {
        super(message, throwable);
        setErrorCode(CmsStatus.CMS_OBJECT_ALREADY_EXISTS);
    }

}
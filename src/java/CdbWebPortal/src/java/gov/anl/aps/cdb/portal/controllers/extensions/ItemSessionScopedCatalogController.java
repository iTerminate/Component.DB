/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers.extensions;

import gov.anl.aps.cdb.portal.controllers.ItemController;
import gov.anl.aps.cdb.portal.controllers.ItemDomainCatalogController;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *
 * @author darek
 */
@Named(ItemSessionScopedCatalogController.controllerNamed)
@SessionScoped
public class ItemSessionScopedCatalogController extends ItemSessionScopedController implements Serializable {
    
    public final static String controllerNamed = "itemSessionScopedCatalogController";
    
    @Override
    protected ItemController getItemController() {        
        return ItemDomainCatalogController.getInstance();        
    }
    
    public static ItemSessionScopedCatalogController getInstance() {
        return (ItemSessionScopedCatalogController) SessionUtility.findBean(controllerNamed);
    }   
    
}

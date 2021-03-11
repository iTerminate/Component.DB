/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers.extensions;

import gov.anl.aps.cdb.portal.controllers.ItemController;
import gov.anl.aps.cdb.portal.controllers.ItemDomainCableCatalogController;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *
 * @author djarosz
 */
@Named(ItemMultiEditDomainCableCatalogController.controllerNamed)
@SessionScoped
public class ItemMultiEditDomainCableCatalogController extends ItemMultiEditController implements Serializable {

    public final static String controllerNamed = "itemMultiEditDomainCableCatalogController";

    @Override
    protected ItemController getItemController() {
        return ItemDomainCableCatalogController.getInstance();
    }

    @Override
    protected String getControllerNamedConstant() {
        return controllerNamed;
    }

    public static ItemMultiEditDomainCableCatalogController getInstance() {
        return (ItemMultiEditDomainCableCatalogController) SessionUtility.findBean(controllerNamed);
    }

}

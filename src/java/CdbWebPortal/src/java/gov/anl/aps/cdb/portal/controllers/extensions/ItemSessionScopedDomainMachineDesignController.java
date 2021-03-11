/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers.extensions;

import gov.anl.aps.cdb.portal.controllers.ItemDomainMachineDesignController;
import gov.anl.aps.cdb.portal.model.ItemDomainMachineDesignTreeNode;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *
 * @author darek
 */
@Named(ItemSessionScopedDomainMachineDesignController.controllerNamed)
@SessionScoped
public class ItemSessionScopedDomainMachineDesignController extends ItemSessionScopedController implements Serializable {
    
    public final static String controllerNamed = "itemSessionScopedDomainMachineDesignController";
    
    private ItemDomainMachineDesignTreeNode machineDesignTreeRootTreeNode = null;

    @Override
    protected ItemDomainMachineDesignController getItemController() {
        return ItemDomainMachineDesignController.getInstance();
    }        

    public ItemDomainMachineDesignTreeNode getMachineDesignTreeRootTreeNode() {
        if (machineDesignTreeRootTreeNode == null) {
            machineDesignTreeRootTreeNode = getItemController().loadMachineDesignRootTreeNode();
        }
        return machineDesignTreeRootTreeNode;
    }
    
    public static ItemSessionScopedDomainMachineDesignController getInstance() {
        return (ItemSessionScopedDomainMachineDesignController) SessionUtility.findBean(controllerNamed); 
    }
    
}

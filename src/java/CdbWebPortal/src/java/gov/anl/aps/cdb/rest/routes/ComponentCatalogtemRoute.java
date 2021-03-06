/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.rest.routes;

import gov.anl.aps.cdb.common.exceptions.CdbException;
import gov.anl.aps.cdb.common.exceptions.InvalidArgument;
import gov.anl.aps.cdb.portal.controllers.utilities.ItemControllerUtility;
import gov.anl.aps.cdb.portal.controllers.utilities.ItemDomainCatalogControllerUtility;
import gov.anl.aps.cdb.portal.model.db.beans.ItemDomainCatalogFacade;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainCatalog;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
import gov.anl.aps.cdb.rest.authentication.Secured;
import gov.anl.aps.cdb.rest.entities.NewCatalogElementInformation;
import gov.anl.aps.cdb.rest.entities.NewCatalogInformation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ejb.EJB;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author djarosz
 */
@Path("/ComponentCatalogItems")
@Tag(name = "componentCatalogItems")
public class ComponentCatalogtemRoute extends ItemBaseRoute {

    private static final Logger LOGGER = LogManager.getLogger(ComponentCatalogtemRoute.class.getName());

    @EJB
    ItemDomainCatalogFacade facade;

    @PUT
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create catalog item.")
    @SecurityRequirement(name = "cdbAuth")
    @Secured
    public ItemDomainCatalog createCatalog(@RequestBody(required = true) NewCatalogInformation catalogInformation) throws InvalidArgument, CdbException {
        ItemDomainCatalogControllerUtility utility = new ItemDomainCatalogControllerUtility();
        UserInfo requestUser = getCurrentRequestUserInfo();

        ItemDomainCatalog createEntityInstance = utility.createEntityInstance(requestUser);

        catalogInformation.updateItemDomainCatalogWithInformation(createEntityInstance);

        utility.create(createEntityInstance, requestUser);

        return createEntityInstance;
    }

    @PUT
    @Path("/createElement/{catalogItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create catalog item element.")
    @SecurityRequirement(name = "cdbAuth")
    @Secured
    public ItemElement addCatalogElement(@PathParam("catalogItemId") int parentItemId,
            @RequestBody(required = true) NewCatalogElementInformation catalogElementInformation) throws CdbException {

        ItemDomainCatalog parentItem = facade.findById(parentItemId);
        ItemControllerUtility itemControllerUtility = parentItem.getItemControllerUtility();
        UserInfo currentRequestUserInfo = getCurrentRequestUserInfo();

        ItemElement newItemElement = itemControllerUtility.createItemElement(parentItem, currentRequestUserInfo);
        
        catalogElementInformation.updateItemElement(newItemElement);

        itemControllerUtility.saveNewItemElement(newItemElement, currentRequestUserInfo);
        
        // Return newly created element.
        parentItem = facade.findById(parentItemId);        
        for (ItemElement element : parentItem.getFullItemElementList()) {
            String name = newItemElement.getName();
            if (name.equals(element.getName())) {
                return element; 
            }
        }
        return null; 
    }

}

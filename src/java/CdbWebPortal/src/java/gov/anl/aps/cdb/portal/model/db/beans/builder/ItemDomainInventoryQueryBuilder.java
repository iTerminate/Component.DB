/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.model.db.beans.builder;

import gov.anl.aps.cdb.portal.constants.ItemElementRelationshipTypeNames;
import gov.anl.aps.cdb.portal.controllers.ItemDomainInventoryController;
import gov.anl.aps.cdb.portal.model.db.entities.Domain;
import java.util.Map;
import org.primefaces.model.SortOrder;

/**
 *
 * @author Dariusz
 */
public class ItemDomainInventoryQueryBuilder extends ItemQueryBuilder {

    private final String INVENTORY_STATUS_FIELD_NAME = "inventoryStatusValue";
    private final String LOCATION_RELATIONSHIP_FIELD_NAME = "locationString";
    private final String LOCATION_DETAILS_RELATIONSHIP_FIELD_NAME = "locationDetails";

    public ItemDomainInventoryQueryBuilder(Domain domain, Map filterMap, String sortField, SortOrder sortOrder) {
        super(domain, filterMap, sortField, sortOrder);
    }

    @Override
    protected void handleUnandeledFieldFilter(String key, String value) {
        super.handleUnandeledFieldFilter(key, value);

        switch (key) {
            case INVENTORY_STATUS_FIELD_NAME:
                String propertyTypeName = ItemDomainInventoryController.ITEM_DOMAIN_INVENTORY_STATUS_PROPERTY_TYPE_NAME;
                addPropertyWhereByTypeName(propertyTypeName, "pvlStatus", value);
                break;
            case LOCATION_RELATIONSHIP_FIELD_NAME:
                String locationRelationshipTypeName = ItemElementRelationshipTypeNames.itemLocation.getValue();
                addFirstRelationshipParentItemWhere(key, locationRelationshipTypeName, value);
                break;
            case LOCATION_DETAILS_RELATIONSHIP_FIELD_NAME:
                String locationRelationshipTypeName2 = ItemElementRelationshipTypeNames.itemLocation.getValue();
                addFirstRelationshipDetailsWhere(key, locationRelationshipTypeName2, value);

        }
    }

    @Override
    protected String handleUnhandeledSortField() {
        switch (sortField) {
            case INVENTORY_STATUS_FIELD_NAME:
                String key = preparePropertyQueryByPropertyTypeName(sortField,
                        ItemDomainInventoryController.ITEM_DOMAIN_INVENTORY_STATUS_PROPERTY_TYPE_NAME);
                return key + ".value";                
            case LOCATION_RELATIONSHIP_FIELD_NAME:
                String locationRelationshipTypeName = ItemElementRelationshipTypeNames.itemLocation.getValue();
                prepareFirstRelationshipQueryByRelationshipName(sortField, locationRelationshipTypeName);
                return sortField + ".secondItemElement.parentItem.name";
            case LOCATION_DETAILS_RELATIONSHIP_FIELD_NAME:
                String locationRelationshipTypeName2 = ItemElementRelationshipTypeNames.itemLocation.getValue();
                prepareFirstRelationshipQueryByRelationshipName(sortField, locationRelationshipTypeName2);
                return sortField + ".relationshipDetails";
        }
        return super.handleUnhandeledSortField();
    }

}

/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.model.db.entities;

import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author djarosz
 */
@Entity
@DiscriminatorValue(value = ItemDomainName.CABLE_CATALOG_ID + "")   
public class ItemDomainCableCatalog extends ItemDomainCatalogBase<ItemDomainCableInventory> {
    
    private transient String cableType = null;
    private transient double weight = 0;
    private transient double diameter = 0;
    private transient String source = null;
    private transient String url = null;

    @Override
    public Item createInstance() {
        return new ItemDomainCableCatalog(); 
    }

    public List<ItemDomainCableInventory> getCableInventoryItemList() {
        return (List<ItemDomainCableInventory>)(List<?>) super.getDerivedFromItemList();
    }
    
    public void setCableProperties(String t, double w, double d, String s, String u) {
        cableType = t;
        weight = w;
        diameter = d;
        source = s;
        url = u;
    }
    
    public String getCableType() {
        return cableType;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public double getDiameter() {
        return diameter;
    }
    
    public String getSource() {
        return source;
    }
    
    public String getUrl() {
        return url;
    }
}

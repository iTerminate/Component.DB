/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers;

import gov.anl.aps.cdb.common.constants.CdbRole;
import gov.anl.aps.cdb.portal.controllers.extensions.CableWizard;
import gov.anl.aps.cdb.common.exceptions.CdbException;
import gov.anl.aps.cdb.common.utilities.ObjectUtility;
import gov.anl.aps.cdb.portal.constants.EntityTypeName;
import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.constants.PortalStyles;
import gov.anl.aps.cdb.portal.controllers.extensions.BundleWizard;
import gov.anl.aps.cdb.portal.controllers.extensions.CircuitWizard;
import gov.anl.aps.cdb.portal.controllers.extensions.ItemSessionScopedController;
import gov.anl.aps.cdb.portal.controllers.extensions.ItemSessionScopedDomainMachineDesignController;
import gov.anl.aps.cdb.portal.controllers.settings.ItemDomainMachineDesignSettings;
import gov.anl.aps.cdb.portal.controllers.utilities.ItemDomainMachineDesignControllerUtility;
import gov.anl.aps.cdb.portal.import_export.import_.helpers.ImportHelperMachineHierarchy;
import gov.anl.aps.cdb.portal.import_export.import_.helpers.ImportHelperMachineTemplateInstantiation;
import gov.anl.aps.cdb.portal.import_export.import_.objects.ValidInfo;
import gov.anl.aps.cdb.portal.model.ItemDomainMachineDesignTreeNode;
import gov.anl.aps.cdb.portal.model.db.beans.ItemDomainMachineDesignFacade;
import gov.anl.aps.cdb.portal.model.db.beans.RelationshipTypeFacade;
import gov.anl.aps.cdb.portal.model.db.entities.CdbEntity;
import gov.anl.aps.cdb.portal.model.db.entities.Connector;
import gov.anl.aps.cdb.portal.model.db.entities.EntityType;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemConnector;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainCatalog;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainInventory;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainMachineDesign;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElementHistory;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElementRelationship;
import gov.anl.aps.cdb.portal.model.db.entities.Log;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyType;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyValue;
import gov.anl.aps.cdb.portal.model.db.entities.RelationshipType;
import gov.anl.aps.cdb.portal.model.db.entities.UserGroup;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
import gov.anl.aps.cdb.portal.utilities.AuthorizationUtility;
import gov.anl.aps.cdb.portal.utilities.SearchResult;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import gov.anl.aps.cdb.portal.view.objects.DomainImportExportInfo;
import gov.anl.aps.cdb.portal.view.objects.ImportExportFormatInfo;
import gov.anl.aps.cdb.portal.view.objects.KeyValueObject;
import gov.anl.aps.cdb.portal.view.objects.MachineDesignConnectorCableMapperItem;
import gov.anl.aps.cdb.portal.view.objects.MachineDesignConnectorListObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author djarosz
 */
@Named(ItemDomainMachineDesignController.controllerNamed)
@ViewScoped
public class ItemDomainMachineDesignController
        extends ItemController<ItemDomainMachineDesignControllerUtility, ItemDomainMachineDesign, ItemDomainMachineDesignFacade, ItemDomainMachineDesignSettings>
        implements ItemDomainCableDesignWizardClient {

    private static final Logger LOGGER = LogManager.getLogger(ItemDomainMachineDesignController.class.getName());

    public final static String controllerNamed = "itemDomainMachineDesignController";
    private final static String cableWizardRedirectSuccess
            = "/views/itemDomainMachineDesign/list?faces-redirect=true";

    private final static String pluginItemMachineDesignSectionsName = "itemMachineDesignDetailsViewSections";

    private List<ItemElementRelationship> relatedMAARCRelationshipsForCurrent = null;

    private MachineDesignConnectorCableMapperItem mdccmi;
    private List<MachineDesignConnectorListObject> mdConnectorList;

    private TreeNode searchResultsTreeNode;

    // <editor-fold defaultstate="collapsed" desc="Favorites toggle variables">
    private boolean favoritesShown = false;
    private ItemDomainMachineDesignTreeNode favoriteMachineDesignTreeRootTreeNode;
    
    // </editor-fold>   

    // <editor-fold defaultstate="collapsed" desc="Element edit variables ">
    private Item inventoryForElement = null;
    private Item catalogForElement = null;
    private Item originalForElement = null;
    protected DataModel installedInventorySelectionForCurrentElement;
    protected DataModel machineDesignTemplatesSelectionList;
    protected DataModel topLevelMachineDesignSelectionList;
    private List<KeyValueObject> machineDesignNameList = null;
    private String machineDesignName = null;
    private boolean displayCreateMachineDesignFromTemplateContent = false;
    // </editor-fold>   

    // <editor-fold defaultstate="collapsed" desc="Dual list view configuration variables ">
    private TreeNode selectedItemInListTreeTable = null;
    private TreeNode lastExpandedNode = null;

    private ItemDomainMachineDesignTreeNode currentMachineDesignListRootTreeNode = null;    
    private ItemDomainMachineDesignTreeNode machineDesignTemplateRootTreeNode = null;
    private boolean currentViewIsTemplate = false;   

    private boolean displayListConfigurationView = false;
    private boolean displayListViewItemDetailsView = false;
    private boolean displayAddMDPlaceholderListConfigurationPanel = true;
    private boolean displayAddMDFromTemplateConfigurationPanel = true;
    private boolean displayAddMDMoveExistingConfigurationPanel = true;
    private boolean displayAddCatalogItemListConfigurationPanel = true;
    private boolean displayAssignCatalogItemListConfigurationPanel = true;
    private boolean displayAssignInventoryItemListConfigurationPanel = true;
    private boolean displayAttachTemplateToMachine = true; 
    private boolean displayMachineDesignReorderOverlayPanel = true;
    private boolean displayAddCablePanel = true;
    private boolean displayAddCableCircuitPanel = true;
    private boolean displayAddCableBundlePanel = true;

    private List<ItemDomainCatalog> catalogItemsDraggedAsChildren = null;
    private TreeNode newCatalogItemsInMachineDesignModel = null;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Machine Design drag and drop variables">
    private static final String JS_SOURCE_MD_ID_PASSED_KEY = "sourceId";
    private static final String JS_SOURCE_MD_ELEMENT_ID_PASSED_KEY = "sourceElementId";
    private static final String JS_DESTINATION_MD_ID_PASSED_KEY = "destinationId";
    // </editor-fold>   

    // <editor-fold defaultstate="collapsed" desc="Delete support variables">
    private Boolean moveToTrashAllowed;
    private Boolean moveToTrashHasWarnings;    
    private TreeNode moveToTrashNode = new DefaultTreeNode();
    private String moveToTrashDisplayName = null;
    private String moveToTrashMessage = null;
    private List<ItemDomainMachineDesign> moveToTrashItemsToUpdate = null;
    private List<ItemElement> moveToTrashElementsToDelete = null;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Machine Design drag and drop implementation">
    public void onDropFromJS() {
        LoginController loginController = LoginController.getInstance();
        if (loginController.isLoggedIn() == false) {
            SessionUtility.addInfoMessage("Cannot complete move.", "Please login and try again.");
            return;
        }

        String sourceIdStr = SessionUtility.getRequestParameterValue(JS_SOURCE_MD_ID_PASSED_KEY);
        String sourceElementIdStr = SessionUtility.getRequestParameterValue(JS_SOURCE_MD_ELEMENT_ID_PASSED_KEY);
        String destinationIdStr = SessionUtility.getRequestParameterValue(JS_DESTINATION_MD_ID_PASSED_KEY);
        int sourceId = Integer.parseInt(sourceIdStr);
        int destId = Integer.parseInt(destinationIdStr);

        ItemElement currentItemElement = null;
        if (sourceElementIdStr.isEmpty() == false) {
            int sourceElementId = Integer.parseInt(sourceElementIdStr);
            currentItemElement = itemElementFacade.find(sourceElementId);
        }

        ItemDomainMachineDesign parent = findById(destId);
        ItemDomainMachineDesign child = findById(sourceId);

        // Permission check
        if (loginController.isEntityWriteable(parent.getEntityInfo()) == false) {
            SessionUtility.addErrorMessage("Insufficient privilages", "The user doesn't have permissions to item: " + parent.toString());
            return;
        }
        if (loginController.isEntityWriteable(child.getEntityInfo()) == false) {
            SessionUtility.addErrorMessage("Insufficient privilages", "The user doesn't have permissions to item: " + child.toString());
            return;
        }

        // Continue to reassignment of parent.
        setCurrent(parent);
        if (currentItemElement != null) {
            String uniqueName = getControllerUtility().generateUniqueElementNameForItem(parent);
            currentItemElement.setName(uniqueName);
            currentItemElement.setParentItem(parent);
        } else {
            // Dragging in top level
            UserInfo user = SessionUtility.getUser();
            currentItemElement = getControllerUtility().createItemElement(parent, user);
            currentItemElement.setContainedItem(child);
        }

        prepareAddItemElement(parent, currentItemElement);

        update();

        child = findById(sourceId);
        expandToSpecificMachineDesignItem(child);
    }

    // </editor-fold>   
    // <editor-fold defaultstate="collapsed" desc="Undocumented Fold">
    private String mdSearchString;
    private List<TreeNode> searchResultsList;
    private boolean searchCollapsed;

    protected ItemElement currentHierarchyItemElement;

    @EJB
    ItemDomainMachineDesignFacade itemDomainMachineDesignFacade;

    public static ItemDomainMachineDesignController getInstance() {
        return (ItemDomainMachineDesignController) SessionUtility.findBean(controllerNamed);
    }

    public boolean isItemInventory(Item item) {
        return item instanceof ItemDomainInventory;
    }

    public boolean isItemCatalog(Item item) {
        return item instanceof ItemDomainCatalog;
    }

    public boolean isItemMachineDesign(Item item) {
        return isItemMachineDesignStatic(item);
    }

    public static boolean isItemMachineDesignStatic(Item item) {
        return item instanceof ItemDomainMachineDesign;
    }

    public boolean isItemMachineDesignAndTemplate(Item item) {
        if (item instanceof ItemDomainMachineDesign) {
            return ((ItemDomainMachineDesign) item).getIsItemTemplate();
        }

        return false;
    }

    public String getMdNodeRepIcon(ItemElement ie) {
        Item containedItem = ie.getContainedItem();
        if (containedItem != null) {
            return getItemRepIcon(containedItem);
        }
        Item parentItem = ie.getParentItem();
        if (parentItem != null) {
            return getItemRepIcon(parentItem);
        }
        ItemConnector conn = ie.getMdConnector();
        if (conn != null) {
            return PortalStyles.itemConnectorIcon.getValue();
        }

        return "";
    }

    public String getItemRepIcon(Item item) {
        if (isItemMachineDesignAndTemplate(item)) {
            return PortalStyles.machineDesingTemplateIcon.getValue();
        } else {
            return item.getDomain().getDomainRepIcon();
        }
    }

    public boolean isCollapsedRelatedMAARCItemsForCurrent() {
        return getRelatedMAARCRelationshipsForCurrent().size() < 1;
    }

    public List<ItemElementRelationship> getRelatedMAARCRelationshipsForCurrent() {
        if (relatedMAARCRelationshipsForCurrent == null) {
            relatedMAARCRelationshipsForCurrent = ItemDomainMAARCController.getRelatedMAARCRelationshipsForItem(getCurrent());
        }

        return relatedMAARCRelationshipsForCurrent;
    }

    @Override
    public void resetListDataModel() {
        super.resetListDataModel();
        currentMachineDesignListRootTreeNode = null;
        machineDesignTemplateRootTreeNode = null;
        //machineDesignTreeRootTreeNode = null;        
    }
    // </editor-fold>   

    // <editor-fold defaultstate="collapsed" desc="Dual list view configuration implementation ">   
    public ItemDomainMachineDesignTreeNode getCurrentMachineDesignListRootTreeNode() {
        if (currentMachineDesignListRootTreeNode == null) {
            if (currentViewIsTemplate) {
                currentMachineDesignListRootTreeNode = getMachineDesignTemplateRootTreeNode();
            } else {
                if (favoritesShown) {
                    currentMachineDesignListRootTreeNode = getFavoriteMachineDesignTreeRootTreeNode();
                } else {
                    currentMachineDesignListRootTreeNode = getItemSessionScopedController().getMachineDesignTreeRootTreeNode();
                }
            }
        }
        return currentMachineDesignListRootTreeNode;
    } 

    @Override
    public ItemSessionScopedDomainMachineDesignController getItemSessionScopedController() {
        return ItemSessionScopedDomainMachineDesignController.getInstance(); 
    }

    public ItemDomainMachineDesignTreeNode getMachineDesignTemplateRootTreeNode() {
        if (machineDesignTemplateRootTreeNode == null) {
            List<ItemDomainMachineDesign> itemsWithoutParents;
            itemsWithoutParents = getEntityDbFacade().findByDomainNameWithNoParentsAndWithEntityType(getDefaultDomainName(), EntityTypeName.template.getValue());
            for (int i = itemsWithoutParents.size() - 1; i >= 0; i--) {
                ItemDomainMachineDesign item = itemsWithoutParents.get(i); 
                if (item.getIsItemDeleted()) {
                    itemsWithoutParents.remove(i); 
                }
            }            
            machineDesignTemplateRootTreeNode = loadMachineDesignRootTreeNode(itemsWithoutParents);
        }
        return machineDesignTemplateRootTreeNode;
    }

    public void loadMachineDesignFilters() {        
        ItemDomainMachineDesignTreeNode node = getCurrentMachineDesignListRootTreeNode();
        node.getConfig().setLoadAllChildren(true);
    }

    public boolean isFiltersLoaded() {
        ItemDomainMachineDesignTreeNode node = getCurrentMachineDesignListRootTreeNode();
        return node.getConfig().isLoadAllChildren();
    }

    /**
     * Override this function as default tree parents for the list of derived machine entity. 
     * 
     * @return parent list of machine items.
     */
    public List<ItemDomainMachineDesign> getDefaultTopLevelMachineList() {
        return getEntityDbFacade().findByDomainNameWithNoParentsAndEntityType(getDefaultDomainName());
    }

    public ItemDomainMachineDesignTreeNode loadMachineDesignRootTreeNode() {
        List<ItemDomainMachineDesign> defaultTopLevelMachineList = getDefaultTopLevelMachineList();
        return loadMachineDesignRootTreeNode(defaultTopLevelMachineList);
    }

    public ItemDomainMachineDesignTreeNode loadMachineDesignRootTreeNode(List<ItemDomainMachineDesign> itemsWithoutParents) {        
        ItemDomainMachineDesignTreeNode rootTreeNode = new ItemDomainMachineDesignTreeNode(itemsWithoutParents);       

        return rootTreeNode;
    }

    public void searchMachineDesign() {
        Pattern searchPattern = Pattern.compile(Pattern.quote(mdSearchString), Pattern.CASE_INSENSITIVE);

        TreeNode mdRoot = getCurrentMachineDesignListRootTreeNode();

        searchResultsList = new ArrayList();

        searchMachineDesign(mdRoot, searchPattern, searchResultsList);

        if (searchResultsList.size() > 0) {
            for (TreeNode node : searchResultsList) {
                TreeNode parent = node.getParent();
                while (parent != null) {
                    parent.setExpanded(true);
                    parent = parent.getParent();
                }
            }

            selectItemInTreeTable(searchResultsList.get(0));
        }
        searchCollapsed = true;
    }

    public void searchMachineDesign(TreeNode parentNode, Pattern searchPattern, List<TreeNode> results) {
        Object data = parentNode.getData();
        parentNode.setExpanded(false);
        if (data != null) {
            ItemElement ie = (ItemElement) data;
            Item parentItem = ie.getContainedItem();
            if (parentItem != null) {
                SearchResult search = parentItem.search(searchPattern);
                if (search.getObjectAttributeMatchMap().size() > 0) {
                    results.add(parentNode);
                    ie.setRowStyle(SearchResult.SEARCH_RESULT_ROW_STYLE);
                } else {
                    ie.setRowStyle(null);
                }
            }
        }

        for (TreeNode node : parentNode.getChildren()) {
            searchMachineDesign(node, searchPattern, results);
        }
    }

    public void selectNextResult() {
        if (searchResultsList != null && searchResultsList.size() > 0) {
            TreeNode selectedItemInListTreeTable = getSelectedItemInListTreeTable();
            int indx = 0;
            if (selectedItemInListTreeTable != null) {
                for (int i = 0; i < searchResultsList.size(); i++) {
                    TreeNode node = searchResultsList.get(i);
                    if (node.equals(selectedItemInListTreeTable)) {
                        indx = i + 1;
                        break;
                    }
                }

                // Last index
                if (indx == searchResultsList.size() - 1) {
                    indx = 0;
                }
            }

            TreeNode result = searchResultsList.get(indx);
            selectItemInTreeTable(result);
        }
    }

    public String getMdSearchString() {
        return mdSearchString;
    }

    public void setMdSearchString(String mdSearchString) {
        this.mdSearchString = mdSearchString;
    }

    public boolean isSearchCollapsed() {
        return searchCollapsed;
    }

    public void setSearchCollapsed(boolean searchCollapsed) {
        this.searchCollapsed = searchCollapsed;
    }

    public void expandSelectedTreeNode() {
        TreeNode selectedItemInListTreeTable = getSelectedItemInListTreeTable();
        if (selectedItemInListTreeTable != null) {
            boolean expanded = !selectedItemInListTreeTable.isExpanded();
            expandAllChildren(selectedItemInListTreeTable, expanded);
        } else {
            SessionUtility.addInfoMessage("No tree node is selected", "Select a tree node and try again.");
        }
    }

    private void expandAllChildren(TreeNode treeNode, boolean expanded) {
        treeNode.setExpanded(expanded);

        List<TreeNode> children = treeNode.getChildren();
        if (children != null) {
            for (TreeNode child : children) {
                expandAllChildren(child, expanded);
            }
        }
    }

    public TreeNode getSelectedItemInListTreeTable() {
        return selectedItemInListTreeTable;
    }

    public void setSelectedItemInListTreeTable(TreeNode selectedItemInListTreeTable) {
        this.selectedItemInListTreeTable = selectedItemInListTreeTable;
    }

    private void selectItemInTreeTable(TreeNode newSelection) {
        TreeNode selectedItemInListTreeTable = getSelectedItemInListTreeTable();
        if (selectedItemInListTreeTable != null) {
            selectedItemInListTreeTable.setSelected(false);
        }

        newSelection.setSelected(true);
        setSelectedItemInListTreeTable(newSelection);
    }

    public boolean isSelectedItemInListReorderable() {
        if (isSelectedItemInListTreeViewWriteable()) {
            ItemDomainMachineDesign selectedItem = getItemFromSelectedItemInTreeTable();
            List<ItemElement> itemElementDisplayList = selectedItem.getItemElementDisplayList();
            return itemElementDisplayList.size() > 1;
        }
        return false;
    }

    public boolean isSelectedItemInListTreeViewWriteable() {
        if (selectedItemInListTreeTable != null) {
            ItemDomainMachineDesign selectedItem = getItemFromSelectedItemInTreeTable();
            if (selectedItem != null) {
                LoginController instance = LoginController.getInstance();
                return instance.isEntityWriteable(selectedItem.getEntityInfo());
            }
        }
        return false;
    }

    public boolean isParentOfSelectedItemInListTreeViewWriteable() {
        if (selectedItemInListTreeTable != null) {
            ItemElement itemElement = (ItemElement) selectedItemInListTreeTable.getData();
            Item parentItem = itemElement.getParentItem();
            if (parentItem != null) {
                LoginController instance = LoginController.getInstance();
                return instance.isEntityWriteable(parentItem.getEntityInfo());
            } else {
                // Top level
                return isSelectedItemInListTreeViewWriteable();
            }
        }
        return false;
    }

    public String showDetailsForCurrentSelectedTreeNode() {
        updateCurrentUsingSelectedItemInTreeTable();

        ItemDomainMachineDesign item = getCurrent();

        if (item != null) {
            return viewForCurrentEntity() + "&mode=detail";
        }

        SessionUtility.addErrorMessage("Error", "Cannot load details for a non machine design.");
        return null;
    }

    public void resetListConfigurationVariables() {
        searchCollapsed = true;
        displayListConfigurationView = false;
        displayListViewItemDetailsView = false;
        displayAddMDPlaceholderListConfigurationPanel = false;
        displayAddMDFromTemplateConfigurationPanel = false;
        displayAddMDMoveExistingConfigurationPanel = false;
        displayAddCatalogItemListConfigurationPanel = false;
        displayAssignCatalogItemListConfigurationPanel = false;
        displayAssignInventoryItemListConfigurationPanel = false;
        displayAttachTemplateToMachine = false; 
        displayMachineDesignReorderOverlayPanel = false;
        displayAddCablePanel = false;
        displayAddCableCircuitPanel = false;
        displayAddCableBundlePanel = false;
        catalogItemsDraggedAsChildren = null;
        newCatalogItemsInMachineDesignModel = null;
        currentMachineDesignListRootTreeNode = null;
    }

    public ItemDomainMachineDesign createEntityInstanceForDualTreeView() {
        ItemDomainMachineDesign newInstance = createEntityInstance();

        Item selectedItem = getItemFromSelectedItemInTreeTable();
        newInstance.setItemProjectList(selectedItem.getItemProjectList());

        if (currentViewIsTemplate) {
            try {
                List<EntityType> entityTypeList = new ArrayList<>();
                EntityType templateEntity = entityTypeFacade.findByName(EntityTypeName.template.getValue());
                entityTypeList.add(templateEntity);
                newInstance.setEntityTypeList(entityTypeList);
            } catch (CdbException ex) {
                LOGGER.error(ex);
                SessionUtility.addErrorMessage("Error", ex.getErrorMessage());
                return null;
            }
        }

        return newInstance;
    }

    public void prepareAddPlaceholder() {
        ItemDomainMachineDesign newItem = createEntityInstanceForDualTreeView();
        prepareAddNewMachineDesignListConfiguration();

        displayAddMDPlaceholderListConfigurationPanel = true;
        currentEditItemElement.setContainedItem(newItem);
    }
    
    public void prepareAssignTemplate() {
        prepareAddNewMachineDesignListConfiguration();
        displayAttachTemplateToMachine = true;         
    }

    public void prepareAddMdFromPlaceholder() {
        prepareAddNewMachineDesignListConfiguration();
        displayAddMDFromTemplateConfigurationPanel = true;
        currentEditItemElementSaveButtonEnabled = true;
    }

    public void prepareAddMdFromCatalog() {
        prepareAddNewMachineDesignListConfiguration();
        displayAddCatalogItemListConfigurationPanel = true;
    }

    public void prepareAddMoveExistingMd() {
        prepareAddNewMachineDesignListConfiguration();
        displayAddMDMoveExistingConfigurationPanel = true;
    }

    public boolean isDisplayFollowInstructionOnRightOnBlockUI() {
        return displayAddMDMoveExistingConfigurationPanel
                || displayAddMDFromTemplateConfigurationPanel
                || displayAddMDPlaceholderListConfigurationPanel
                || displayAssignCatalogItemListConfigurationPanel
                || displayAssignInventoryItemListConfigurationPanel
                || displayAttachTemplateToMachine;
    }

    public boolean isDisplayListConfigurationView() {
        return displayListConfigurationView;
    }

    public boolean isDisplayListViewItemDetailsView() {
        return displayListViewItemDetailsView;
    }

    public boolean isDisplayAddMDPlaceholderListConfigurationPanel() {
        return displayAddMDPlaceholderListConfigurationPanel;
    }

    public boolean isDisplayAddMDFromTemplateConfigurationPanel() {
        return displayAddMDFromTemplateConfigurationPanel;
    }

    public boolean isDisplayAddMDMoveExistingConfigurationPanel() {
        return displayAddMDMoveExistingConfigurationPanel;
    }

    public boolean isDisplayAddCatalogItemListConfigurationPanel() {
        return displayAddCatalogItemListConfigurationPanel;
    }

    public boolean isDisplayAssignCatalogItemListConfigurationPanel() {
        return displayAssignCatalogItemListConfigurationPanel;
    }

    public boolean isDisplayAssignInventoryItemListConfigurationPanel() {
        return displayAssignInventoryItemListConfigurationPanel;
    }

    public boolean isDisplayAttachTemplateToMachine() {
        return displayAttachTemplateToMachine;
    }

    public boolean isDisplayMachineDesignReorderOverlayPanel() {
        return displayMachineDesignReorderOverlayPanel;
    }

    public boolean isDisplayAddCablePanel() {
        return displayAddCablePanel;
    }

    public boolean isDisplayAddCableCircuitPanel() {
        return displayAddCableCircuitPanel;
    }

    public boolean isDisplayAddCableBundlePanel() {
        return displayAddCableBundlePanel;
    }

    protected void updateCurrentUsingSelectedItemInTreeTable() {
        setCurrent(getItemFromSelectedItemInTreeTable());
        setCurrentHierarchyItemElement(getItemElementFromSelectedItemInTreeTable());
    }

    private ItemElement getItemElementFromSelectedItemInTreeTable() {
        if (selectedItemInListTreeTable != null) {
            ItemElement element = (ItemElement) selectedItemInListTreeTable.getData();
            return element;
        }
        return null;
    }

    private ItemDomainMachineDesign getItemFromSelectedItemInTreeTable() {
        ItemElement itemElement = getItemElementFromSelectedItemInTreeTable();
        if (itemElement != null) {
            Item item = itemElement.getContainedItem();

            if (item instanceof ItemDomainMachineDesign) {
                return (ItemDomainMachineDesign) item;
            }
        }

        return null;
    }

    public void unlinkContainedItem2ToDerivedFromItem() {
        ItemElement element = (ItemElement) selectedItemInListTreeTable.getData();

        ItemDomainMachineDesign mdItem = (ItemDomainMachineDesign) element.getContainedItem();

        Item assignedItem = mdItem.getAssignedItem();
        Item derivedFromItem = assignedItem.getDerivedFromItem();

        mdItem.setAssignedItem(derivedFromItem);

        setCurrent(mdItem);
        update();

        resetListDataModel();
        expandToSpecificTreeNode(selectedItemInListTreeTable);
    }

    public void unlinkContainedItem2FromSelectedItem() {
        ItemElement element = (ItemElement) selectedItemInListTreeTable.getData();

        ItemDomainMachineDesign mdItem = (ItemDomainMachineDesign) element.getContainedItem();
        Item originalContainedItem = mdItem.getAssignedItem();

        mdItem.setAssignedItem(null);

        updateTemplateReferenceElementContainedItem2(element, originalContainedItem, null);

        setCurrent(mdItem);
        update();

        resetListDataModel();
        expandToSpecificTreeNode(selectedItemInListTreeTable);
    }

    public void detachSelectedItemFromHierarchyInDualView() {
        ItemElement element = (ItemElement) selectedItemInListTreeTable.getData();

        Item containedItem = element.getContainedItem();
        Integer detachedDomainId = containedItem.getDomain().getId();

        ItemElementController instance = ItemElementController.getInstance();

        if (currentViewIsTemplate) {
            List<ItemElement> derivedFromItemElementList = element.getDerivedFromItemElementList();
            boolean needsUpdate = false;
            for (ItemElement itemElement : derivedFromItemElementList) {
                if (!containedItem.equals(itemElement.getContainedItem())) {
                    // fullfilled Element
                    itemElement.setDerivedFromItemElement(null);
                    needsUpdate = true;

                    try {
                        instance.performUpdateOperations(itemElement);
                    } catch (Exception ex) {
                        LOGGER.error(ex);
                        SessionUtility.addErrorMessage("Error", ex.getMessage());
                    }
                }
            }
            if (needsUpdate) {
                element = instance.findById(element.getId());
            }
        }

        instance.destroy(element);

        selectedItemInListTreeTable = selectedItemInListTreeTable.getParent();

        resetListDataModel();

        expandToSpecificTreeNode(selectedItemInListTreeTable);
        if (detachedDomainId == ItemDomainName.MACHINE_DESIGN_ID) {
            for (TreeNode node : getCurrentMachineDesignListRootTreeNode().getChildren()) {
                ItemElement ie = (ItemElement) node.getData();
                Item ci = ie.getContainedItem();
                if (containedItem.equals(ci)) {
                    node.setSelected(true);
                    selectedItemInListTreeTable = node;
                    break;
                }
            }
        }
    }

    @Deprecated
    /**
     * Templates are only created fully and only previously partially created md
     * from templates will utilize this.
     */
    public void prepareFullfilPlaceholder() {
        // Element with template to be fullfilled
        ItemElement templateElement = (ItemElement) selectedItemInListTreeTable.getData();

        // Select Parent where the template will be created 
        selectedItemInListTreeTable = selectedItemInListTreeTable.getParent();

        // Execute standard add template function 
        prepareAddMdFromPlaceholder();

        // Remove the template element                 
        getCurrent().removeItemElement(templateElement);

        // Select current template 
        templateToCreateNewItem = (ItemDomainMachineDesign) templateElement.getContainedItem();
        generateTemplateForElementMachineDesignNameVars();
    }

    public void prepareAssignInventoryMachineDesignListConfiguration() {
        currentEditItemElement = (ItemElement) selectedItemInListTreeTable.getData();
        catalogForElement = currentEditItemElement.getCatalogItem();

        prepareUpdateInstalledInventoryItem();

        displayAssignInventoryItemListConfigurationPanel = true;
        displayListConfigurationView = true;
    }

    public void assignInventoryMachineDesignListConfiguration() {
        updateInstalledInventoryItem();

        resetListConfigurationVariables();
        resetListDataModel();
        expandToSelectedTreeNodeAndSelect();
    }

    private void updateItemElement(ItemElement itemElement) {
        ItemElementController itemElementController = ItemElementController.getInstance();
        itemElementController.setCurrent(itemElement);
        itemElementController.update();
    }

    public void unassignInventoryMachineDesignListConfiguration() {
        ItemElement itemElement = (ItemElement) selectedItemInListTreeTable.getData();

        Item containedItem = itemElement.getContainedItem();

        if (containedItem instanceof ItemDomainInventory) {
            itemElement.setContainedItem(containedItem.getDerivedFromItem());

            updateItemElement(itemElement);
        }

        resetListConfigurationVariables();
        resetListDataModel();
        expandToSelectedTreeNodeAndSelect();

    }

    public void prepareAddNewMachineDesignListConfiguration() {
        updateCurrentUsingSelectedItemInTreeTable();

        displayListConfigurationView = true;

        prepareCreateSingleItemElementSimpleDialog();
    }

    public void completeAddNewMachineDesignListConfiguration() {
        resetListConfigurationVariables();
    }

    public String saveTreeListMachineDesignItem() {
        ItemElement ref = currentEditItemElement;
        saveCreateSingleItemElementSimpleDialog();

        for (ItemElement element : current.getItemElementDisplayList()) {
            if (element.getName().equals(ref.getName())) {
                ref = element;
                break;
            }
        }

        cloneNewTemplateElementForItemsDerivedFromItem(ref);

        expandToSelectedTreeNodeAndSelectChildItemElement(ref);

        return currentDualViewList();
    }

    private void cloneNewTemplateElementForItemsDerivedFromItem(ItemElement newTemplateElement) {
        if (currentViewIsTemplate) {
            Item parentItem = newTemplateElement.getParentItem();
            List<Item> items = getItemsCreatedFromTemplateItem(parentItem);
            cloneNewTemplateElementForItemsDerivedFromItem(newTemplateElement, items);
        }
    }

    private void cloneNewTemplateElementForItemsDerivedFromItem(ItemElement newTemplateElement, List<Item> derivedItems) {
        if (currentViewIsTemplate) {
            for (int i = 0; i < derivedItems.size(); i++) {
                Item item = derivedItems.get(i);
                String uniqueName = getControllerUtility().generateUniqueElementNameForItem((ItemDomainMachineDesign) item);
                ItemElement newItemElement = cloneCreateItemElement(newTemplateElement, item, true, true);
                newItemElement.setName(uniqueName);
                try {
                    ItemDomainMachineDesign origCurrent = current;
                    performUpdateOperations((ItemDomainMachineDesign) item);
                    // Update pointer to latest version of the item. 
                    derivedItems.set(i, current);
                    current = origCurrent;
                } catch (CdbException ex) {
                    SessionUtility.addErrorMessage("Error", ex.getErrorMessage());
                    LOGGER.error(ex);
                } catch (Exception ex) {
                    SessionUtility.addErrorMessage("Error", ex.getMessage());
                    LOGGER.error(ex);
                }
            }
        }
    }

    private void expandToSpecificMachineDesignItem(ItemDomainMachineDesign item) {

        TreeNode machineDesignTreeRootTreeNode = getCurrentMachineDesignListRootTreeNode();

        if (selectedItemInListTreeTable != null) {
            selectedItemInListTreeTable.setSelected(false);
            selectedItemInListTreeTable = null;
        }

        TreeNode selectedNode = expandToSpecificMachineDesignItem(machineDesignTreeRootTreeNode, item);
        selectedItemInListTreeTable = selectedNode;
    }

    /**
     * Expands the parent nodes in the supplied tree above the specified machine
     * design item. Returns the TreeNode for the specified item, so that the
     * caller can call select to highlight it if appropriate.
     *
     * @param machineDesignTreeRootTreeNode Root node of machine design
     * hierarchy.
     * @param item Child node to expand the nodes above.
     * @return
     */
    public static TreeNode expandToSpecificMachineDesignItem(
            TreeNode machineDesignTreeRootTreeNode,
            ItemDomainMachineDesign item) {

        Stack<ItemDomainMachineDesign> machineDesingItemStack = new Stack<>();

        machineDesingItemStack.push(item);

        List<Item> parentItemList = getParentItemList(item);

        while (parentItemList != null) {
            ItemDomainMachineDesign parentItem = null;
            for (Item ittrItem : parentItemList) {
                if (ittrItem instanceof ItemDomainMachineDesign) {
                    // Machine design items should only have one parent
                    parentItem = (ItemDomainMachineDesign) parentItemList.get(0);
                    break;
                }
            }

            if (parentItem != null) {
                machineDesingItemStack.push(parentItem);
                parentItemList = getParentItemList(parentItem);
            } else {
                parentItemList = null;
            }
        }

        List<TreeNode> children = machineDesignTreeRootTreeNode.getChildren();

        TreeNode result = null;

        while (children != null && machineDesingItemStack.size() > 0) {
            ItemDomainMachineDesign pop = machineDesingItemStack.pop();

            for (TreeNode treeNode : children) {
                ItemElement data = (ItemElement) treeNode.getData();
                Item containedItem = data.getContainedItem();
                if (isItemMachineDesignStatic(containedItem)) {
                    if (containedItem.equals(pop)) {
                        if (machineDesingItemStack.isEmpty()) {
                            result = treeNode;
                            treeNode.setSelected(true);
                            children = null;
                            break;
                        } else {
                            treeNode.setExpanded(true);
                            children = treeNode.getChildren();
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    private void expandToSelectedTreeNodeAndSelectChildItemElement(ItemElement element) {
        if (selectedItemInListTreeTable != null) {
            selectedItemInListTreeTable.setSelected(false);
        }

        expandToSpecificTreeNode(selectedItemInListTreeTable);

        for (TreeNode treeNode : lastExpandedNode.getChildren()) {
            if (treeNode.getData().equals(element)) {
                selectedItemInListTreeTable = treeNode;
                treeNode.setSelected(true);
                break;
            }
        }
    }

    private void expandToSpecificTreeNodeAndSelect(TreeNode treeNode) {
        expandToSpecificTreeNode(treeNode);
        selectedItemInListTreeTable = lastExpandedNode;
        lastExpandedNode.setSelected(true);
    }

    private void expandToSelectedTreeNodeAndSelect() {
        expandToSpecificTreeNodeAndSelect(selectedItemInListTreeTable);
    }

    private void expandToSpecificTreeNode(TreeNode treeNode) {
        lastExpandedNode = null;

        if (treeNode.getParent() != null) {
            // No need to get top most parent. 
            if (treeNode.getParent().getParent() != null) {
                expandToSpecificTreeNode(treeNode.getParent());
            }
        }
        if (lastExpandedNode == null) {
            lastExpandedNode = getCurrentMachineDesignListRootTreeNode();
        }

        ItemElement itemElement = (ItemElement) treeNode.getData();
        Item item = itemElement.getContainedItem();

        for (TreeNode ittrTreeNode : lastExpandedNode.getChildren()) {
            ItemElement element = (ItemElement) ittrTreeNode.getData();
            Item ittrItem = element.getContainedItem();
            if (item.equals(ittrItem)) {
                ittrTreeNode.setExpanded(true);
                lastExpandedNode = ittrTreeNode;
                break;
            }
        }
    }

    public void prepareAddNewCatalogListConfiguration() {
        updateCurrentUsingSelectedItemInTreeTable();

        displayListConfigurationView = true;
        displayAddCatalogItemListConfigurationPanel = true;
    }

    public void prepareReorderTopLevelMachineDesignElements() {
        ItemDomainMachineDesign mockTopLevelMachineDesign = new ItemDomainMachineDesign();
        mockTopLevelMachineDesign.setFullItemElementList(new ArrayList<>());
        mockTopLevelMachineDesign.setDomain(getDefaultDomain());

        TreeNode currentMachineDesignListRootTreeNode = getCurrentMachineDesignListRootTreeNode();

        List<TreeNode> currentTopLevels = currentMachineDesignListRootTreeNode.getChildren();

        for (TreeNode node : currentTopLevels) {
            ItemElement data = (ItemElement) node.getData();
            ItemElement mockItemElement = new ItemElement();
            mockItemElement.setName("MOCK element");
            mockItemElement.setContainedItem(data.getContainedItem());
            mockItemElement.setParentItem(mockTopLevelMachineDesign);

            mockTopLevelMachineDesign.getFullItemElementList().add(mockItemElement);
        }

        mockTopLevelMachineDesign.getFullItemElementList().sort(new Comparator<ItemElement>() {
            @Override
            public int compare(ItemElement o1, ItemElement o2) {
                Float sortOrder = o1.getSortOrder();
                Float sortOrder1 = o2.getSortOrder();

                if (ObjectUtility.equals(sortOrder, sortOrder1)) {
                    return 0;
                }

                if (sortOrder == null && sortOrder1 != null) {
                    return -1;
                }

                if (sortOrder != null && sortOrder1 == null) {
                    return 1;
                }

                if (sortOrder > sortOrder1) {
                    return 1;
                }
                return -1;
            }
        });

        setCurrent(mockTopLevelMachineDesign);

        displayMachineDesignReorderOverlayPanel = true;
    }

    public void prepareReorderMachineDesignElements() {
        updateCurrentUsingSelectedItemInTreeTable();

        displayMachineDesignReorderOverlayPanel = true;
    }

    public String revertReorderMachineDesignElement() {
        if (hasElementReorderChangesForCurrent) {
            resetListConfigurationVariables();
            resetListDataModel();
            expandToSelectedTreeNodeAndSelect();
        }
        return currentDualViewList();
    }

    public String saveReorderMachineDesignElement() {
        ItemDomainMachineDesign current = getCurrent();
        if (current.getId() == null) {
            // not in DB. Simulated list. Set sort order on self element. Perform multi-item save
            List<ItemDomainMachineDesign> itemsToUpdate = new ArrayList<>();
            List<ItemElement> itemElements = current.getItemElementDisplayList();
            for (ItemElement itemElement : itemElements) {
                Float sortOrder = itemElement.getSortOrder();

                ItemDomainMachineDesign sortedItem = (ItemDomainMachineDesign) itemElement.getContainedItem();
                ItemElement selfElement = sortedItem.getSelfElement();
                selfElement.setSortOrder(sortOrder);

                itemsToUpdate.add(sortedItem);
            }

            try {
                updateList(itemsToUpdate);
            } catch (CdbException ex) {
                LOGGER.error(ex);
                SessionUtility.addErrorMessage("Error", ex.getErrorMessage());
            }

            return currentDualViewList();
        }

        if (isItemMachineDesignAndTemplate(getCurrent())) {
            ItemDomainMachineDesign template = getCurrent();
            for (ItemElement ie : template.getItemElementDisplayList()) {
                for (ItemElement derivedIe : ie.getDerivedFromItemElementList()) {
                    derivedIe.setSortOrder(ie.getSortOrder());
                }
            }
        }
        update();
        expandToSelectedTreeNodeAndSelect();
        return currentDualViewList();
    }

    public void prepareAssignCatalogListConfiguration() {
        updateCurrentUsingSelectedItemInTreeTable();
        currentEditItemElement = (ItemElement) selectedItemInListTreeTable.getData();

        displayListConfigurationView = true;
        displayAssignCatalogItemListConfigurationPanel = true;
    }

    public String completeAssignCatalogListConfiguration() {
        if (currentEditItemElement != null
                && catalogForElement != null) {

            ItemDomainMachineDesign mdItem = (ItemDomainMachineDesign) currentEditItemElement.getContainedItem();

            Item assignedItem = mdItem.getAssignedItem();
            mdItem.setAssignedItem(catalogForElement);

            updateTemplateReferenceElementContainedItem2(currentEditItemElement, assignedItem, catalogForElement);

            setCurrent(mdItem);
            update();

            resetListConfigurationVariables();
            resetListDataModel();
            expandToSelectedTreeNodeAndSelect();
            return currentDualViewList();
        } else {
            SessionUtility.addErrorMessage("Error", "Please select catalog item and try again.");
        }
        return null;
    }

    /**
     * Updates contained item on elements created from template if update is
     * valid.
     *
     * @param currentElement
     * @param newAssignedItem
     */
    private void updateTemplateReferenceElementContainedItem2(ItemElement currentElement,
            Item originalContainedItem,
            Item newAssignedItem) {

        Item mdItem = currentElement.getContainedItem();

        if (currentViewIsTemplate && isItemMachineDesignAndTemplate(mdItem)) {
            List<ItemDomainMachineDesign> itemsCreatedFromThisTemplateItem = (List<ItemDomainMachineDesign>) (List<?>) mdItem.getItemsCreatedFromThisTemplateItem();
            List<ItemDomainMachineDesign> itemsToUpdate = new ArrayList<>();

            for (ItemDomainMachineDesign item : itemsCreatedFromThisTemplateItem) {
                Item assignedItem = item.getAssignedItem();

                // Verify if in sync with template
                if (ObjectUtility.equals(originalContainedItem, assignedItem)) {
                    item.setAssignedItem(newAssignedItem);
                    itemsToUpdate.add(item);
                }
            }

            try {
                updateList(itemsToUpdate);
            } catch (CdbException ex) {
                LOGGER.error(ex);
                SessionUtility.addErrorMessage("Error", ex.getMessage());
            }
        }
    }

    public String completeAddNewCatalogListConfiguration() {
        // Verify non empty names 
        for (ItemDomainCatalog item : catalogItemsDraggedAsChildren) {
            String mdName = item.getMachineDesignPlaceholderName();
            if (mdName == null || mdName.equals("")) {
                SessionUtility.addErrorMessage("Error", "Please provide machine design name for all items. '" + item.getName() + "' is missing machine design name.");
                return null;
            }
        }

        // Verify uniqueness
        List<ItemDomainMachineDesign> idmList = new ArrayList<>();
        for (ItemDomainCatalog item : catalogItemsDraggedAsChildren) {
            String name = item.getMachineDesignPlaceholderName();
            for (ItemDomainCatalog innerItem : catalogItemsDraggedAsChildren) {
                if (innerItem.getId() != item.getId()) {
                    if (name.equals(innerItem.getMachineDesignPlaceholderName())) {
                        SessionUtility.addErrorMessage("Error",
                                "Ensure all machine designs are unique. '"
                                + name + "' shows up twice.");
                        return null;
                    }
                }
            }

            ItemDomainMachineDesign newMachineDesign = createEntityInstanceForDualTreeView();
            newMachineDesign.setName(item.getMachineDesignPlaceholderName());
            idmList.add(newMachineDesign);
        }

        // Verify valid machine desings 
        for (ItemDomainMachineDesign md : idmList) {
            try {
                getControllerUtility().checkItem(md);
            } catch (CdbException ex) {
                SessionUtility.addErrorMessage("Error", ex.getErrorMessage());
                LOGGER.error(ex);
                return null;
            }
        }

        // Add new items
        List<ItemElement> newlyAddedItemElementList = new ArrayList<>();
        for (int i = 0; i < catalogItemsDraggedAsChildren.size(); i++) {
            ItemDomainMachineDesign mdItem = idmList.get(i);
            Item catalogItem = catalogItemsDraggedAsChildren.get(i);

            UserInfo user = SessionUtility.getUser();
            ItemElement newItemElement = getControllerUtility().createItemElement(current, user);

            newItemElement.setContainedItem(mdItem);
            mdItem.setAssignedItem(catalogItem);

            prepareAddItemElement(current, newItemElement);

            newlyAddedItemElementList.add(newItemElement);
        }

        update();

        // Add item elements to items created from the current template 
        if (currentViewIsTemplate) {
            // Remap to the added elements 
            List<ItemElement> dbMappedElements = new ArrayList<>();
            for (ItemElement ie : current.getItemElementDisplayList()) {
                for (int i = 0; i < newlyAddedItemElementList.size(); i++) {
                    ItemElement unmappedIE = newlyAddedItemElementList.get(i);
                    if (ie.getName().equals(unmappedIE.getName())) {
                        dbMappedElements.add(ie);
                        newlyAddedItemElementList.remove(i);
                        break;
                    }
                }
            }

            List<Item> items = getItemsCreatedFromTemplateItem(getCurrent());

            for (ItemElement ie : dbMappedElements) {
                cloneNewTemplateElementForItemsDerivedFromItem(ie, items);
            }
        }

        expandToSelectedTreeNodeAndSelect();

        return currentDualViewList();
    }

    public void onItemDrop(DragDropEvent ddEvent) {
        ItemDomainCatalog catalogItem = ((ItemDomainCatalog) ddEvent.getData());

        if (catalogItemsDraggedAsChildren == null) {
            catalogItemsDraggedAsChildren = new ArrayList<>();
        }

        if (newCatalogItemsInMachineDesignModel == null) {
            newCatalogItemsInMachineDesignModel = new DefaultTreeNode();
            TreeNode parent = new DefaultTreeNode(getCurrent());
            newCatalogItemsInMachineDesignModel.getChildren().add(parent);
            parent.setExpanded(true);
            lastExpandedNode = parent;
        }
        TreeNode newCatalogNode = new DefaultTreeNode(catalogItem);
        lastExpandedNode.getChildren().add(newCatalogNode);

        catalogItemsDraggedAsChildren.add(catalogItem);
    }

    public List<ItemDomainCatalog> getCatalogItemsDraggedAsChildren() {
        return catalogItemsDraggedAsChildren;
    }

    public TreeNode getNewCatalogItemsInMachineDesignModel() {
        return newCatalogItemsInMachineDesignModel;
    }

    @Override
    public void resetSearchVariables() {
        super.resetSearchVariables();
        searchResultsTreeNode = null;
    }

    public TreeNode getSearchResults(String searchString, boolean caseInsensitive) {
        this.performEntitySearch(searchString, caseInsensitive);
        return getHierarchicalSearchResults();
    }

    public TreeNode getHierarchicalSearchResults() {
        if (searchResultsTreeNode != null) {
            return searchResultsTreeNode;
        }
        LinkedList<SearchResult> searchResultList = getSearchResultList();
        TreeNode rootTreeNode = new DefaultTreeNode();
        if (searchResultList != null) {
            for (SearchResult result : searchResultList) {
                result.setRowStyle(SearchResult.SEARCH_RESULT_ROW_STYLE);

                ItemDomainMachineDesign mdItem = (ItemDomainMachineDesign) result.getCdbEntity();

                ItemDomainMachineDesign parent = mdItem.getParentMachineDesign();

                TreeNode resultNode = new DefaultTreeNode(result);

                List<ItemDomainMachineDesign> parents = new ArrayList<>();

                while (parent != null) {
                    parents.add(parent);
                    parent = parent.getParentMachineDesign();
                }

                TreeNode currentRoot = rootTreeNode;

                // Combine common parents 
                parentSearch:
                for (int i = parents.size() - 1; i >= 0; i--) {
                    ItemDomainMachineDesign currentParent = parents.get(i);

                    for (TreeNode node : currentRoot.getChildren()) {
                        Object data = node.getData();
                        SearchResult searchResult = (SearchResult) data;
                        CdbEntity cdbEntity = searchResult.getCdbEntity();
                        ItemDomainMachineDesign itemResult = (ItemDomainMachineDesign) cdbEntity;

                        if (itemResult.equals(currentParent)) {
                            currentRoot = node;
                            continue parentSearch;
                        }
                    }

                    // Need to create parentNode
                    SearchResult parentResult = new SearchResult(currentParent, currentParent.getId(), currentParent.getName());
                    parentResult.addAttributeMatch("Reason", "Parent of Result");

                    TreeNode newRoot = new DefaultTreeNode(parentResult);
                    newRoot.setExpanded(true);
                    currentRoot.getChildren().add(newRoot);
                    currentRoot = newRoot;
                }

                currentRoot.getChildren().add(resultNode);

                List<ItemElement> childElements = mdItem.getItemElementDisplayList();

                for (ItemElement childElement : childElements) {
                    Item mdChild = childElement.getContainedItem();
                    SearchResult childResult = new SearchResult(mdChild, mdChild.getId(), mdChild.getName());
                    childResult.addAttributeMatch("Reason", "Child of result");

                    TreeNode resultChildNode = new DefaultTreeNode(childResult);
                    resultNode.getChildren().add(resultChildNode);
                }
            }
        }
        searchResultsTreeNode = rootTreeNode;
        return searchResultsTreeNode;
    }

    private void syncMachineDesignConnectors(ItemDomainMachineDesign item) {
        List<ItemConnector> itemConnectorList = item.getItemConnectorList();
        List<ItemConnector> connectorsFromAssignedCatalogItem = getConnectorsFromAssignedCatalogItem(item);

        if (connectorsFromAssignedCatalogItem == null) {
            return;
        }

        if (itemConnectorList.size() == 0) {
            // Sync all connectors into machine design
            for (ItemConnector cconnector : connectorsFromAssignedCatalogItem) {
                ItemConnector mdConnector = cloneConnectorForMachineDesign(cconnector, item);

                itemConnectorList.add(mdConnector);
            }
        } else {
            // Verify if any new connections were created on the catalog             
            if (connectorsFromAssignedCatalogItem != null) {

                catConnFor:
                for (ItemConnector catalogItemConn : connectorsFromAssignedCatalogItem) {
                    for (ItemConnector mdItemConn : itemConnectorList) {
                        Connector mdConnector = mdItemConn.getConnector();
                        Connector catConnector = catalogItemConn.getConnector();

                        if (mdConnector.equals(catConnector)) {
                            continue catConnFor;
                        }
                    }
                    ItemConnector mdConnector = cloneConnectorForMachineDesign(catalogItemConn, item);
                    itemConnectorList.add(mdConnector);
                }
            }
        }
    }

    private ItemConnector cloneConnectorForMachineDesign(ItemConnector catalogConnector, ItemDomainMachineDesign mdItem) {
        ItemConnector mdConnector = new ItemConnector();

        mdConnector.setConnector(catalogConnector.getConnector());
        mdConnector.setItem(mdItem);

        return mdConnector;
    }

    public void prepareCableMappingDialog() {
        ItemDomainMachineDesign item = getItemFromSelectedItemInTreeTable();
        // Refresh item from DB
        item = findById(item.getId());

        setCurrent(item);
        prepareCableMappingDialogForCurrent();
    }

    public void prepareCableMappingDialogForCurrent() {
        mdccmi = new MachineDesignConnectorCableMapperItem(getMdConnectorListForCurrent());
    }

    public void saveCableMappingDialog() {
        update();
    }

    public MachineDesignConnectorCableMapperItem getMachineDesignConnectorCableMapperItem() {
        return mdccmi;
    }

    public void resetMachineDesignConnectorCableMapperItem() {
        mdccmi = null;
    }

    public void prepareWizardCable() {
        updateCurrentUsingSelectedItemInTreeTable();
        currentEditItemElement = (ItemElement) selectedItemInListTreeTable.getData();

        CableWizard cableWizard = CableWizard.getInstance();
        cableWizard.registerClient(this, cableWizardRedirectSuccess);
        cableWizard.setSelectionEndpoint1(selectedItemInListTreeTable);

        displayListConfigurationView = true;
        displayAddCablePanel = true;
    }

    public void prepareWizardCircuit() {
        updateCurrentUsingSelectedItemInTreeTable();
        currentEditItemElement = (ItemElement) selectedItemInListTreeTable.getData();

        // create model for wizard
        CircuitWizard circuitWizard = CircuitWizard.getInstance();
        circuitWizard.registerClient(this, cableWizardRedirectSuccess);
        circuitWizard.setSelectionEndpoint1(selectedItemInListTreeTable);

        displayListConfigurationView = true;
        displayAddCableCircuitPanel = true;
    }

    public void prepareWizardBundle() {
        updateCurrentUsingSelectedItemInTreeTable();
        currentEditItemElement = (ItemElement) selectedItemInListTreeTable.getData();

        // create model for wizard
        BundleWizard bundleWizard = BundleWizard.getInstance();
        bundleWizard.registerClient(this, cableWizardRedirectSuccess);
        bundleWizard.setSelectionEndpoint1(selectedItemInListTreeTable);

        displayListConfigurationView = true;
        displayAddCableBundlePanel = true;
    }

    public void cleanupCableWizard() {
        resetListConfigurationVariables();
        resetListDataModel();
        expandToSelectedTreeNodeAndSelect();
    }

    private static List<ItemConnector> getConnectorsFromAssignedCatalogItem(ItemDomainMachineDesign item) {
        Item assignedItem = item.getAssignedItem();

        Item catalogItem = null;
        if (assignedItem instanceof ItemDomainInventory) {
            catalogItem = ((ItemDomainInventory) assignedItem).getCatalogItem();
        } else if (assignedItem instanceof ItemDomainCatalog) {
            catalogItem = assignedItem;
        }

        if (catalogItem != null) {
            return catalogItem.getItemConnectorList();
        }
        return null;
    }

    public List<MachineDesignConnectorListObject> getMdConnectorListForCurrent() {
        if (mdConnectorList == null) {
            //Generate connector list
            ItemDomainMachineDesign item = getCurrent();
            mdConnectorList = getMdConnectorListForItem(item);
        }
        return mdConnectorList;
    }

    public List<MachineDesignConnectorListObject> getMdConnectorListForItem(ItemDomainMachineDesign item) {
        syncMachineDesignConnectors(item);
        return MachineDesignConnectorListObject.createMachineDesignConnectorList(item);
    }

    public boolean getDisplayMdConnectorList() {
        return getMdConnectorListForCurrent().size() > 0;
    }

    public List<ItemElementHistory> getCombinedItemElementHistory(ItemElement ie) {
        List<ItemElementHistory> itemElementHistories = new ArrayList<>();
        Item mdItem = ie.getContainedItem();
        ItemElement selfElement = mdItem.getSelfElement();

        List<ItemElementHistory> assignedItemHistory = selfElement.getItemElementHistoryList();
        List<ItemElementHistory> parentItemHistory = ie.getItemElementHistoryList();

        int currentAssignedItemInx = incrementValidIndxForHistory(-1, assignedItemHistory);
        int currentParentItemInx = incrementValidIndxForHistory(-1, parentItemHistory);

        int size = assignedItemHistory.size();
        if (parentItemHistory != null) {
            size = size + parentItemHistory.size();
        }

        Date lastParentDate = null;

        for (int i = 0; i < size; i++) {
            ItemElementHistory aih = null;
            if (currentAssignedItemInx != -1) {
                aih = assignedItemHistory.get(currentAssignedItemInx);
            }

            ItemElementHistory pih = null;
            if (currentParentItemInx != -1) {
                pih = parentItemHistory.get(currentParentItemInx);
            }

            // default 1 is to use the assigned item history. Parent item history is optional.
            int result = 1;
            if (currentAssignedItemInx != -1 && currentParentItemInx != -1) {
                // Both histories must be valid
                result = aih.getEnteredOnDateTime().compareTo(pih.getEnteredOnDateTime());
            } else if (currentParentItemInx != -1) {
                // parent item must be valid
                result = -1;
            }

            ItemElementHistory ieh = new ItemElementHistory();

            Item currentAssignedItem = null;
            Item currentParentItem = null;
            if (aih != null) {
                currentAssignedItem = aih.getContainedItem2();
            }
            if (pih != null) {
                currentParentItem = pih.getParentItem();
            }

            if (result == 0) {
                ieh.setEnteredOnDateTime(aih.getEnteredOnDateTime());
                ieh.setEnteredByUser(aih.getEnteredByUser());

                currentAssignedItemInx = incrementValidIndxForHistory(currentAssignedItemInx, assignedItemHistory);
                currentParentItemInx = incrementValidIndxForHistory(currentParentItemInx, parentItemHistory);

                lastParentDate = pih.getEnteredOnDateTime();

                i++;
            } else if (result > 0) {
                ieh.setEnteredOnDateTime(aih.getEnteredOnDateTime());
                ieh.setEnteredByUser(aih.getEnteredByUser());

                currentAssignedItemInx = incrementValidIndxForHistory(currentAssignedItemInx, assignedItemHistory);
            } else {
                ieh.setEnteredOnDateTime(pih.getEnteredOnDateTime());
                ieh.setEnteredByUser(pih.getEnteredByUser());

                lastParentDate = pih.getEnteredOnDateTime();

                currentParentItemInx = incrementValidIndxForHistory(currentParentItemInx, parentItemHistory);
            }

            if (lastParentDate == null && pih != null) {
                lastParentDate = pih.getEnteredOnDateTime();
            }

            // Add parent only if it has happened. 
            if (lastParentDate != null && ieh.getEnteredOnDateTime().compareTo(lastParentDate) >= 0) {
                ieh.setParentItem(currentParentItem);
            }
            ieh.setContainedItem2(currentAssignedItem);

            itemElementHistories.add(ieh);
        }

        return itemElementHistories;
    }

    private int incrementValidIndxForHistory(int currIndx, List<ItemElementHistory> ieh) {
        currIndx++;

        if (ieh == null || ieh.size() == currIndx) {
            return -1;
        }
        return currIndx;
    }

    // </editor-fold>    
    // <editor-fold defaultstate="collapsed" desc="Undocumented Fold">    
    public boolean isCollapseContentsOfInventoryItem() {
        return current.getDerivedFromItemList().size() == 0;
    }

    public boolean isInventory(ItemDomainMachineDesign item) {
        if (item == null) {
            return false;
        }
        String inventoryetn = EntityTypeName.inventory.getValue();
        return item.isItemEntityType(inventoryetn);
    }

    public void templateToCreateNewItemSelected(NodeSelectEvent nodeSelection) {
        TreeNode treeNode = nodeSelection.getTreeNode();
        treeNode.setSelected(false);

        ItemElement element = (ItemElement) treeNode.getData();
        Item parentItem = element.getContainedItem();

        templateToCreateNewItem = (ItemDomainMachineDesign) parentItem;
    }

    @Override
    public String prepareCreateTemplate() {
        String createRedirect = super.prepareCreate();

        ItemDomainMachineDesign current = getCurrent();
        String templateEntityTypeName = EntityTypeName.template.getValue();
        EntityType templateEntityType = entityTypeFacade.findByName(templateEntityTypeName);
        try {
            current.setEntityTypeList(new ArrayList<>());
        } catch (CdbException ex) {
            LOGGER.error(ex);
        }
        current.getEntityTypeList().add(templateEntityType);

        return createRedirect;

    }
    // </editor-fold>   

    // <editor-fold defaultstate="collapsed" desc="Favorites toggle impl">
    public ItemDomainMachineDesignTreeNode getFavoriteMachineDesignTreeRootTreeNode() {
        if (favoriteMachineDesignTreeRootTreeNode == null) {
            List<ItemDomainMachineDesign> favoriteItems = getFavoriteItems();

            if (favoriteItems == null) {
                favoriteMachineDesignTreeRootTreeNode = new ItemDomainMachineDesignTreeNode();
            } else {

                List<ItemDomainMachineDesign> parentFavorites = new ArrayList<>();

                for (ItemDomainMachineDesign item : favoriteItems) {

                    ItemDomainMachineDesign parentMachineDesign = item.getParentMachineDesign();
                    boolean parentFound = parentMachineDesign != null;
                    while (parentMachineDesign != null) {
                        ItemDomainMachineDesign ittrParent = parentMachineDesign.getParentMachineDesign();
                        if (ittrParent == null) {
                            item = parentMachineDesign;
                        }
                        parentMachineDesign = ittrParent;
                    }
                    if (parentFound) {
                        // Ensure mutliple top levels aren't added. 
                        if (parentFavorites.contains(item)) {
                            continue;
                        } else {
                            parentFavorites.add(item);
                        }

                        // Ensure multiple top levels aren't added when a child of a favorite is also a favorite. 
                        if (favoriteItems.contains(item)) {
                            continue;
                        }
                    }
                }

                loadMachineDesignRootTreeNode(parentFavorites); 
            }
            
        }       

        return favoriteMachineDesignTreeRootTreeNode;
    }

    public boolean isFavoritesShown() {
        return favoritesShown;
    }

    public void setFavoritesShown(boolean favoritesShown) {
        this.favoritesShown = favoritesShown;
    }
    // </editor-fold>   

    public boolean isCablesShown() {
        ItemDomainMachineDesignTreeNode.MachineTreeConfiguration config = getCurrentMachineDesignListRootTreeNode().getConfig();
        return config.isShowCables();
    }

    public void setCablesShown(boolean cablesShown) {        
        ItemDomainMachineDesignTreeNode.MachineTreeConfiguration config = getCurrentMachineDesignListRootTreeNode().getConfig();
        config.setShowCables(cablesShown); 
    }

    public String getPluginItemMachineDesignSectionsName() {
        return pluginItemMachineDesignSectionsName;
    }

    // <editor-fold defaultstate="collapsed" desc="Element creation implementation ">   
    // <editor-fold defaultstate="collapsed" desc="Functionality">
    public void newMachineDesignElementContainedItemValueChanged() {
        String name = currentEditItemElement.getContainedItem().getName();
        if (!name.equals("")) {
            currentEditItemElementSaveButtonEnabled = true;
        } else {
            currentEditItemElementSaveButtonEnabled = false;
        }

    }

    public void updateInstalledInventoryItem() {
        boolean updateNecessary = false;
        ItemDomainMachineDesign mdItem = (ItemDomainMachineDesign) currentEditItemElement.getContainedItem();
        Item assignedItem = mdItem.getAssignedItem();

        if (inventoryForElement != null) {
            if (assignedItem.equals(inventoryForElement)) {
                SessionUtility.addInfoMessage("No update", "Inventory selected is same as before");
            } else if (verifyValidUnusedInventoryItem(inventoryForElement)) {
                updateNecessary = true;
                mdItem.setAssignedItem(inventoryForElement);
            }
        } else if (assignedItem.getDomain().getId() == ItemDomainName.INVENTORY_ID) {
            // Item is unselected, select catalog item
            updateNecessary = true;
            mdItem.setAssignedItem(assignedItem.getDerivedFromItem());
        } else {
            SessionUtility.addInfoMessage("No update", "Inventory item not selected");
        }

        if (updateNecessary) {
            setCurrent(mdItem);
            update();
        }

        resetItemElementEditVariables();
    }

    private boolean verifyValidUnusedInventoryItem(Item inventoryItem) {
        for (ItemElement itemElement : inventoryItem.getItemElementMemberList2()) {
            Item item = itemElement.getParentItem();
            if (item instanceof ItemDomainMachineDesign) {
                SessionUtility.addWarningMessage("Inventory item used",
                        "Inventory item cannot be saved, used in: " + item.toString());
                return false;
            }
        }

        return true;

    }

    public void prepareUpdateInstalledInventoryItem() {
        resetItemElementEditVariables();
        catalogForElement = currentEditItemElement.getCatalogItem();
    }

    public DataModel getInstalledInventorySelectionForCurrentElement() {
        if (installedInventorySelectionForCurrentElement == null) {
            if (catalogForElement != null) {
                List<Item> derivedFromItemList = catalogForElement.getDerivedFromItemList();
                installedInventorySelectionForCurrentElement = new ListDataModel(derivedFromItemList);
            }

        }
        return installedInventorySelectionForCurrentElement;
    }

    public DataModel getMachineDesignTemplatesSelectionList() {
        if (machineDesignTemplatesSelectionList == null) {
            List<ItemDomainMachineDesign> machineDesignTemplates = itemDomainMachineDesignFacade.getMachineDesignTemplates();
            machineDesignTemplatesSelectionList = new ListDataModel(machineDesignTemplates);
        }
        return machineDesignTemplatesSelectionList;
    }

    public DataModel getTopLevelMachineDesignSelectionList() {
        if (topLevelMachineDesignSelectionList == null) {
            List<ItemDomainMachineDesign> itemsWithoutParents = getItemsWithoutParents();

            removeTopLevelParentOfItemFromList(current, itemsWithoutParents);
            removeEntityTypesFromList(itemsWithoutParents, !isCurrentViewIsTemplate());

            topLevelMachineDesignSelectionList = new ListDataModel(itemsWithoutParents);
        }
        return topLevelMachineDesignSelectionList;
    }

    protected void removeTopLevelParentOfItemFromList(Item item, List<ItemDomainMachineDesign> topLevelItems) {
        List<ItemElement> itemElementMemberList = item.getItemElementMemberList();

        if (itemElementMemberList != null) {
            if (itemElementMemberList.size() == 0) {
                // current item has no parents
                topLevelItems.remove(item);
            } else {
                // Be definition machine design item should only have one parent
                Item parentItem = null;

                while (itemElementMemberList.size() != 0) {
                    ItemElement parentElement = itemElementMemberList.get(0);
                    parentItem = parentElement.getParentItem();

                    itemElementMemberList = parentItem.getItemElementMemberList();
                }

                topLevelItems.remove(parentItem);
            }
        }
    }

    private void removeEntityTypesFromList(List<ItemDomainMachineDesign> itemList, boolean removeTemplate) {

        String templateEntityName = EntityTypeName.template.getValue();
        EntityType templateEntityType = entityTypeFacade.findByName(templateEntityName);

        String deletedEntityName = EntityTypeName.deleted.getValue();
        EntityType deletedEntityType = entityTypeFacade.findByName(deletedEntityName);

        String inventoryEntityName = EntityTypeName.inventory.getValue();
        EntityType inventoryEntityType = entityTypeFacade.findByName(inventoryEntityName);

        int index = 0;
        while (index < itemList.size()) {

            Item item = itemList.get(index);

            // remove template items or regular items depending on removeTemplate flag
            // remove all deleted items
            // remove all machine inventory
            if (((item.getEntityTypeList().contains(templateEntityType)) && (removeTemplate))
                    || ((!item.getEntityTypeList().contains(templateEntityType)) && (!removeTemplate))
                    || (item.getEntityTypeList().contains(deletedEntityType))
                    || (item.getEntityTypeList().contains(inventoryEntityType))) {

                itemList.remove(index);

            } else {
                index++;
            }
        }
    }

    public void resetItemElementEditVariables() {
        currentEditItemElementSaveButtonEnabled = false;
        displayCreateMachineDesignFromTemplateContent = false;

        installedInventorySelectionForCurrentElement = null;
        inventoryForElement = null;
        catalogForElement = null;
        inventoryForElement = null;
        templateToCreateNewItem = null;
        machineDesignTemplatesSelectionList = null;
        topLevelMachineDesignSelectionList = null;
        machineDesignNameList = null;
        machineDesignName = null;
    }

    @Override
    public void prepareCreateSingleItemElementSimpleDialog() {
        super.prepareCreateSingleItemElementSimpleDialog();
        resetItemElementEditVariables();

        int elementSize = current.getItemElementDisplayList().size();
        float sortOrder = elementSize;
        currentEditItemElement.setSortOrder(sortOrder);
    }

    public void verifyMoveExistingMachineDesignSelected() {
        if (currentEditItemElement.getContainedItem() != null) {
            currentEditItemElementSaveButtonEnabled = true;
        }
    }

    public void reassignTemplateVarsForSelectedMdCreatedFromTemplate() {
        ItemDomainMachineDesign itemFromSelectedItemInTreeTable = getItemFromSelectedItemInTreeTable();

        List<ItemDomainMachineDesign> itemsToUpdate = new ArrayList<>();
        reassignTemplateVarsForSelectedMdCreatedFromTemplateRecursivelly(itemFromSelectedItemInTreeTable, itemsToUpdate);

        try {
            updateList(itemsToUpdate);
        } catch (Exception ex) {
            LOGGER.error(ex);
        }

        expandToSpecificMachineDesignItem(itemFromSelectedItemInTreeTable);
    }

    private void reassignTemplateVarsForSelectedMdCreatedFromTemplateRecursivelly(ItemDomainMachineDesign item, List<ItemDomainMachineDesign> itemsToUpdate) {
        ItemDomainMachineDesign createdFromTemplate = (ItemDomainMachineDesign) item.getCreatedFromTemplate();

        if (createdFromTemplate != null) {
            setMachineDesginIdentifiersFromTemplateItem(createdFromTemplate, item);
            itemsToUpdate.add(item);
        }

        List<ItemElement> itemElementDisplayList = item.getItemElementDisplayList();

        for (ItemElement itemElement : itemElementDisplayList) {
            ItemDomainMachineDesign containedItem = (ItemDomainMachineDesign) itemElement.getContainedItem();
            reassignTemplateVarsForSelectedMdCreatedFromTemplateRecursivelly(containedItem, itemsToUpdate);
        }
    }

    public void generateTemplateVarsForSelectedMdCreatedFromTemplate() {
        ItemDomainMachineDesign selectedItem = getItemFromSelectedItemInTreeTable();

        machineDesignNameList = new ArrayList<>();

        generateTemplateVarsForSelectedMdCreatedFromTemplateRecursivelly(selectedItem);

    }

    private void generateTemplateVarsForSelectedMdCreatedFromTemplateRecursivelly(ItemDomainMachineDesign itemDomainMachineDesign) {
        ItemDomainMachineDesign createdFromTemplate = (ItemDomainMachineDesign) itemDomainMachineDesign.getCreatedFromTemplate();

        if (createdFromTemplate != null) {
            generateMachineDesignTemplateNameVars(createdFromTemplate);
        }

        List<ItemElement> itemElementDisplayList = itemDomainMachineDesign.getItemElementDisplayList();

        for (ItemElement itemElement : itemElementDisplayList) {
            ItemDomainMachineDesign containedItem = (ItemDomainMachineDesign) itemElement.getContainedItem();
            generateTemplateVarsForSelectedMdCreatedFromTemplateRecursivelly(containedItem);
        }
    }

    public void generateTemplateForElementMachineDesignNameVars() {
        if (templateToCreateNewItem != null) {

            machineDesignNameList = new ArrayList<>();

            generateMachineDesignTemplateNameVarsRecursivelly(templateToCreateNewItem);

            generateMachineDesignName();
        }
    }

    public void generateMachineDesignTemplateNameVarsRecursivelly(ItemDomainMachineDesign template) {
        generateMachineDesignTemplateNameVars(template);

        for (ItemElement ie : template.getItemElementDisplayList()) {
            ItemDomainMachineDesign machineDesignTemplate = (ItemDomainMachineDesign) ie.getContainedItem();
            if (machineDesignTemplate != null) {
                generateMachineDesignTemplateNameVarsRecursivelly(machineDesignTemplate);
            }
        }
    }

    private void generateMachineDesignTemplateNameVars(ItemDomainMachineDesign template) {
        String name = template.getName();
        String alternateName = template.getItemIdentifier1();
        appendMachineDesignNameList(name);
        appendMachineDesignNameList(alternateName);
    }

    private void appendMachineDesignNameList(String templateIdentifier) {
        if (templateIdentifier == null) {
            return;
        }
        int firstVar = templateIdentifier.indexOf('{');
        int secondVar;

        while (firstVar != -1) {
            templateIdentifier = templateIdentifier.substring(firstVar);
            secondVar = templateIdentifier.indexOf('}');

            String key = templateIdentifier.substring(1, secondVar);

            KeyValueObject keyValue = new KeyValueObject(key);

            if (machineDesignNameList.contains(keyValue) == false) {
                machineDesignNameList.add(keyValue);
            }

            templateIdentifier = templateIdentifier.substring(secondVar + 1);

            firstVar = templateIdentifier.indexOf('{');
        }
    }

    public void generateMachineDesignName() {
        machineDesignName = generateMachineDesignNameForTemplateItem(templateToCreateNewItem.getName());
    }

    private void setMachineDesginIdentifiersFromTemplateItem(ItemDomainMachineDesign templateItem, ItemDomainMachineDesign mdItem) {
        String machineDesignName = generateMachineDesignNameForTemplateItem(templateItem.getName());
        mdItem.setName(machineDesignName);
        String alternateName = generateMachineDesignNameForTemplateItem(templateItem.getItemIdentifier1());
        mdItem.setItemIdentifier1(alternateName);
    }

    public String generateMachineDesignNameForTemplateItem(String templateIdentifier) {
        if (templateIdentifier == null) {
            return templateIdentifier;
        }
        if (machineDesignNameList != null) {
            for (KeyValueObject kv : machineDesignNameList) {
                if (kv.getValue() != null && !kv.getValue().equals("")) {
                    String originalText = "{" + kv.getKey() + "}";
                    templateIdentifier = templateIdentifier.replace(originalText, kv.getValue());
                }
            }
        }

        return templateIdentifier;
    }

    @Override
    public void beforeValidateItemElement() throws CloneNotSupportedException, CdbException {
        super.beforeValidateItemElement();

        if (displayAddMDFromTemplateConfigurationPanel) {
            if (currentViewIsTemplate == false) {
                createMachineDesignFromTemplateForEditItemElement();
            } else {
                // Template link in multiple places        
                currentEditItemElement.setContainedItem(templateToCreateNewItem);

                // Add from top level only 
                if (templateToCreateNewItem.getParentMachineDesign() == null) {
                    throw new CdbException("Top level machine design templates will be moved into selected machine design. Use add top machine design.");
                }
            }
        }

        ItemDomainMachineDesign containedItem = (ItemDomainMachineDesign) currentEditItemElement.getContainedItem();

        List<ItemElement> itemElementMemberList = containedItem.getItemElementMemberList();
        if (itemElementMemberList == null) {
            containedItem.setItemElementMemberList(new ArrayList<>());
            itemElementMemberList = containedItem.getItemElementMemberList();
        }

        if (itemElementMemberList.contains(currentEditItemElement) == false) {
            containedItem.getItemElementMemberList().add(currentEditItemElement);
        }

        getControllerUtility().checkItem(containedItem);

    }
    
    public void assignTemplateToSelectedItem() {
        if (templateToCreateNewItem == null) {
            SessionUtility.addWarningMessage("No Template Selected", "Please select template and try again.");
            return;
        }
        TreeNode selectedItemInListTreeTable = getSelectedItemInListTreeTable();
        ItemDomainMachineDesignTreeNode machineNode = (ItemDomainMachineDesignTreeNode) selectedItemInListTreeTable;
        
        ItemElement element = machineNode.getElement();
        ItemDomainMachineDesign containedItem = (ItemDomainMachineDesign) element.getContainedItem();
                
        setMachineDesginIdentifiersFromTemplateItem(templateToCreateNewItem, containedItem);
        addCreatedFromTemplateRelationshipToItem(containedItem, templateToCreateNewItem);
        Item assignedItem = templateToCreateNewItem.getAssignedItem();
        containedItem.setAssignedItem(assignedItem);
        
        cloneCreateItemElements(containedItem, templateToCreateNewItem, true, true, true);
        containedItem.resetItemElementVars();
        
        try { 
            createMachineDesignFromTemplateHierachically(element);
        } catch (CdbException ex) {
            SessionUtility.addErrorMessage("Error", ex.getMessage());
            return;
        } catch (CloneNotSupportedException ex) {
            SessionUtility.addErrorMessage("Error", ex.getMessage());
            return;
        }
        
        updateCurrentUsingSelectedItemInTreeTable();
        update();
        
        resetListConfigurationVariables();
        resetListDataModel();
        expandToSelectedTreeNodeAndSelect();
    }

    private void createMachineDesignFromTemplateForEditItemElement() throws CdbException, CloneNotSupportedException {
        createMachineDesignFromTemplate(currentEditItemElement, templateToCreateNewItem);
        createMachineDesignFromTemplateHierachically(currentEditItemElement);
    }

    public void createMachineDesignFromTemplateHierachically(ItemElement itemElement) throws CdbException, CloneNotSupportedException {
        Item containedItem = itemElement.getContainedItem();
        ItemDomainMachineDesign subTemplate = (ItemDomainMachineDesign) containedItem;
        createMachineDesignFromTemplateHierachically(subTemplate);
    }

    protected void createMachineDesignFromTemplateHierachically(ItemDomainMachineDesign subTemplate) throws CdbException, CloneNotSupportedException {

        UserInfo ownerUser = subTemplate.getOwnerUser();
        UserGroup ownerGroup = subTemplate.getOwnerUserGroup();

        List<ItemElement> itemElementDisplayList = subTemplate.getItemElementDisplayList();
        for (ItemElement ie : itemElementDisplayList) {
            ItemDomainMachineDesign result = createMachineDesignFromTemplate(ie, ie, ownerUser, ownerGroup);
            if (result != null) {
                createMachineDesignFromTemplateHierachically(ie);
            }
        }
    }

    /**
     * This version of create md from template allows for machine designs
     * created from template to be sortable to match the template
     *
     * @param itemElement
     * @param templateElementItem
     * @return null when no template exists
     * @throws CdbException
     * @throws CloneNotSupportedException
     */
    private ItemDomainMachineDesign createMachineDesignFromTemplate(
            ItemElement itemElement,
            ItemElement templateElementItem,
            UserInfo ownerUser,
            UserGroup ownerGroup) throws CdbException, CloneNotSupportedException {
        
        ItemDomainMachineDesign templateItem = (ItemDomainMachineDesign) templateElementItem.getContainedItem();
        
        if (ItemDomainMachineDesign.isItemTemplate(templateItem) == false) {
            return null; 
        }        

        if (templateElementItem.getId() != null) {
            // The key derivedFromItemElement is used for sorting.
            itemElement.setDerivedFromItemElement(templateElementItem);
        }       

        return createMachineDesignFromTemplate(itemElement, templateItem, ownerUser, ownerGroup);
    }

    public ItemDomainMachineDesign createMachineDesignFromTemplate(ItemElement itemElement, ItemDomainMachineDesign templateItem) throws CdbException, CloneNotSupportedException {
        return createMachineDesignFromTemplate(itemElement, templateItem, null, null);
    }

    public ItemDomainMachineDesign createMachineDesignFromTemplate(
            ItemElement itemElement,
            ItemDomainMachineDesign templateItem,
            UserInfo ownerUser,
            UserGroup ownerGroup) throws CdbException, CloneNotSupportedException {
        cloneProperties = true;
        cloneCreateItemElementPlaceholders = false;

        ItemDomainMachineDesign createItemFromTemplate
                = createItemFromTemplate(templateItem, ownerUser, ownerGroup);

        Item assignedItem = templateItem.getAssignedItem();
        createItemFromTemplate.setAssignedItem(assignedItem);

        itemElement.setContainedItem(createItemFromTemplate);

        // No longer needed. Skip the standard template relationship process. 
        templateToCreateNewItem = null;

        return createItemFromTemplate;
    }

    protected ItemDomainMachineDesign createItemFromTemplate(ItemDomainMachineDesign templateItem) throws CdbException, CloneNotSupportedException {
        return createItemFromTemplate(templateItem, null, null);
    }

    protected ItemDomainMachineDesign createItemFromTemplate(
            ItemDomainMachineDesign templateItem,
            UserInfo ownerUser,
            UserGroup ownerGroup) throws CdbException, CloneNotSupportedException {

        ItemDomainMachineDesign clone = (ItemDomainMachineDesign) templateItem.clone(ownerUser, ownerGroup);
        cloneCreateItemElements(clone, templateItem, true, true);
        setMachineDesginIdentifiersFromTemplateItem(templateItem, clone);

        // ensure uniqueness of template creation.
        String viewUUID = clone.getViewUUID();
        clone.setItemIdentifier2(viewUUID);

        addCreatedFromTemplateRelationshipToItem(clone, templateItem);

        clone.setEntityTypeList(new ArrayList<>());

        return clone;
    }

    @Override
    public void failedValidateItemElement() {
        super.failedValidateItemElement();
        currentEditItemElement.setContainedItem(originalForElement);
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Accessors">   
    public String getMachineDesignName() {
        return machineDesignName;
    }
    
    public List<KeyValueObject> getMachineDesignNameList() {
        return machineDesignNameList;
    }

    public void setMachineDesignNameList(List<KeyValueObject> list) {
        this.machineDesignNameList = list;
    }

    public Item getInventoryForElement() {
        return inventoryForElement;
    }

    public void setInventoryForElement(Item inventoryForElement) {
        this.inventoryForElement = inventoryForElement;
    }

    public Item getCatalogForElement() {
        return catalogForElement;
    }

    public void setCatalogForElement(Item catalogForElement) {
        this.catalogForElement = catalogForElement;
    }

    public boolean isDisplayCreateMachineDesignFromTemplateContent() {
        return displayCreateMachineDesignFromTemplateContent;
    }

    // </editor-fold>    // </editor-fold>
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Base class overrides">                   
    @Override
    public String getItemListPageTitle() {
        return "Machine: Housing Hierarchy";
    }

    @Override
    public String getItemTemplateListPageTitle() {
        return "Machine Element Templates";
    }

    @Override
    public boolean getEntityHasSortableElements() {
        return true;
    }

    @Override
    public boolean entityCanBeCreatedByUsers() {
        return true;
    }

    private void resetListViewVariables() {
        currentViewIsTemplate = false;
    }

    @Override
    public void processPreRenderList() {
        super.processPreRenderList();
        resetListViewVariables();

        resetListConfigurationVariables();

        String paramValue = SessionUtility.getRequestParameterValue("id");
        if (paramValue != null) {
            Integer idParam = Integer.parseInt(paramValue);
            ItemDomainMachineDesign item = itemDomainMachineDesignFacade.findById(idParam);
            if (item != null) {
                currentViewIsTemplate = isItemMachineDesignAndTemplate(item);
                expandToSpecificMachineDesignItem(item);
            } else {
                SessionUtility.addErrorMessage("Error", "Machine design with id " + idParam + " couldn't be found.");
            }
        }
    }

    @Override
    public void processPreRenderTemplateList() {
        super.processPreRenderTemplateList();

        currentViewIsTemplate = true;
    }

    @Override
    protected void prepareEntityView(ItemDomainMachineDesign entity) {
        super.prepareEntityView(entity);

        // Cannot only show favorites when specific node is selected by id.
        favoritesShown = false;

        processPreRenderList();

        String redirect = "/list";

        if (loadViewModeUrlParameter()) {
            return;
        }

        if (currentViewIsTemplate) {
            redirect = "/templateList";
        }

        SessionUtility.navigateTo("/views/" + getEntityViewsDirectory() + redirect + ".xhtml?id=" + entity.getId() + "&faces-redirect=true");
    }

    protected boolean loadViewModeUrlParameter() {
        String viewMode = SessionUtility.getRequestParameterValue("mode");
        if (viewMode != null) {
            if (viewMode.equals("detail")) {
                displayListConfigurationView = true;
                displayListViewItemDetailsView = true;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void completeEntityUpdate(ItemDomainMachineDesign entity) {
        super.completeEntityUpdate(entity);

        if (displayListViewItemDetailsView) {
            expandToSpecificMachineDesignItem(entity);
        }
    }

    @Override
    protected void resetVariablesForCurrent() {
        super.resetVariablesForCurrent();

        relatedMAARCRelationshipsForCurrent = null;
        mdccmi = null;
        mdConnectorList = null;

        resetItemElementEditVariables();
    }

    public String getPrimaryImageThumbnailForMachineDesignItem(ItemElement itemElement) {
        String value = getPrimaryImageValueForMachineDesignItem(itemElement);
        if (!value.isEmpty()) {
            return PropertyValueController.getThumbnailImagePathByValue(value);
        }
        return value;
    }

    public Boolean isMachineDesignItemHasPrimaryImage(ItemElement itemElement) {
        return !getPrimaryImageValueForMachineDesignItem(itemElement).isEmpty();
    }

    public String getPrimaryImageValueForMachineDesignItem(ItemElement itemElement) {
        String value = "";
        if (itemElement != null) {
            Item containedItem = itemElement.getContainedItem();

            if (containedItem != null) {
                if (isItemMachineDesignStatic(containedItem)) {
                    value = super.getPrimaryImageValueForItem(containedItem);
                } else {
                    ItemController itemItemController = getItemItemController(containedItem);
                    value = itemItemController.getPrimaryImageValueForItem(containedItem);
                }
            }

            if (value.isEmpty()) {
                if (containedItem instanceof ItemDomainMachineDesign) {
                    Item assignedItem = ((ItemDomainMachineDesign) containedItem).getAssignedItem();
                    if (assignedItem != null) {
                        ItemController itemItemController = getItemItemController(assignedItem);
                        value = itemItemController.getPrimaryImageValueForItem(assignedItem);
                    }
                }
            }
        }
        return value;
    }

    public boolean isSelectedItemInTreeTableCreatedFromTemplate() {
        ItemDomainMachineDesign item = getItemFromSelectedItemInTreeTable();
        if (item != null) {
            return item.getCreatedFromTemplate() != null;
        }
        return false;
    }

    public boolean isCurrentViewIsStandard() {
        return (currentViewIsTemplate == false);
    }

    public boolean isCurrentViewIsTemplate() {
        return currentViewIsTemplate;
    }

    public String currentDualViewList() {
        if (currentViewIsTemplate) {
            return templateList();
        }

        return list();
    }

    public String getDetailsPageHeader() {
        String header = getDisplayEntityTypeName();
        if (isCurrentItemTemplate()) {
            header += " Template";
        }
        header += " Details";

        return header;
    }

    public ItemElement getCurrentHierarchyItemElementForItem(ItemDomainMachineDesign item) {
        List<ItemElement> itemElementMemberList = item.getItemElementMemberList();
        ItemElement hierarchyItemElement = item.getCurrentHierarchyItemElement();

        if (hierarchyItemElement == null) {
            Integer id = currentHierarchyItemElement.getId();

            if (id == null) {
                ItemElement ie = new ItemElement();
                ie.setContainedItem(item);

                item.setCurrentHierarchyItemElement(ie);
            } else {
                for (ItemElement ie : itemElementMemberList) {
                    Integer ieId = ie.getId();
                    if (ieId.equals(id)) {
                        item.setCurrentHierarchyItemElement(ie);
                    }
                }
            }
        }

        return item.getCurrentHierarchyItemElement();
    }

    private void setCurrentHierarchyItemElement(ItemElement currentHierarchyItemElement) {
        this.currentHierarchyItemElement = currentHierarchyItemElement;
    }

    @Override
    protected ItemDomainMachineDesignSettings createNewSettingObject() {
        return new ItemDomainMachineDesignSettings(this);
    }

    @Override
    protected ItemDomainMachineDesignFacade getEntityDbFacade() {
        return itemDomainMachineDesignFacade;
    }

    @Override
    public String getDefaultDomainName() {
        return ItemDomainName.machineDesign.getValue();
    }

    public boolean getRenderItemElementList() {
        if (getEntityDisplayItemElements()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean getEntityDisplayItemConnectors() {
        return true;
    }

    @Override
    public boolean getEntityDisplayDerivedFromItem() {
        return false;
    }

    @Override
    public boolean getEntityDisplayQrId(ItemDomainMachineDesign item) {
        return !item.getIsItemTemplate();
    }

    @Override
    public boolean getEntityDisplayItemGallery() {
        return true;
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
    public boolean getEntityDisplayItemEntityTypes() {
        return false;
    }

    @Override
    public boolean getEntityDisplayTemplates() {
        return true;
    }

    @Override
    public boolean getEntityDisplayDeletedItems() {
        return true;
    }

    @Override
    public String getItemsDerivedFromItemTitle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getStyleName() {
        return "machineDesign";
    }

    @Override
    public String getDefaultDomainDerivedFromDomainName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDefaultDomainDerivedToDomainName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getEntityDisplayImportButton() {
        return true;
    }

    @Override
    protected DomainImportExportInfo initializeDomainImportInfo() {

        List<ImportExportFormatInfo> formatInfo = new ArrayList<>();
        formatInfo.add(new ImportExportFormatInfo("Machine Hierarchy Creation", ImportHelperMachineHierarchy.class));
        formatInfo.add(new ImportExportFormatInfo("Machine Template Instantiation", ImportHelperMachineTemplateInstantiation.class));

        String completionUrl = "/views/itemDomainMachineDesign/list?faces-redirect=true";

        return new DomainImportExportInfo(formatInfo, completionUrl);
    }
    // </editor-fold>       

    // <editor-fold defaultstate="collapsed" desc="Delete support">   
    private void addChildrenForItemToHierarchyNode(
            ItemDomainMachineDesign item,
            TreeNode itemNode) {

        itemNode.setExpanded(true);

        List<ItemElement> childElements = item.getItemElementDisplayList();
        List<ItemDomainMachineDesign> childItems = childElements.stream()
                .map((child) -> (ItemDomainMachineDesign) child.getContainedItem())
                .collect(Collectors.toList());

        for (ItemDomainMachineDesign childItem : childItems) {
            //TreeNode childNode = new DefaultTreeNode(nodeType, childItem.getName(), itemNode);
            TreeNode childNode = new DefaultTreeNode(childItem, itemNode);
            childNode.setExpanded(true);
            // itemNode.getChildren().add(childNode);
            addChildrenForItemToHierarchyNode(childItem, childNode);
        }
    }

    protected void prepareItemHierarchyTree(
            TreeNode rootNode,
            ItemDomainMachineDesign rootItem) {

        if (rootItem != null) {
            rootNode.setExpanded(true);
            //TreeNode childNode = new DefaultTreeNode(nodeType, rootItem.getName(), rootNode);
            TreeNode childNode = new DefaultTreeNode(rootItem, rootNode);
            // rootNode.getChildren().add(childNode);
            addChildrenForItemToHierarchyNode(rootItem, childNode);
        }
    }

    public void collectItemsForDeletion(
            ItemDomainMachineDesign parentItem,
            List<ItemDomainMachineDesign> collectedItems,
            List<ItemElement> collectedElements,
            boolean isRootItem,
            boolean rootRelationshipOnly) {

        boolean isValid = true;
        String validString = "";

        List<ItemElement> displayList = parentItem.getItemElementDisplayList();
        for (ItemElement ie : displayList) {
            Item childItem = ie.getContainedItem();
            if (childItem instanceof ItemDomainMachineDesign) {
                // depth first ordering is important here, otherwise there are merge errors for deleted items
                collectItemsForDeletion((ItemDomainMachineDesign) childItem, collectedItems, collectedElements, false, rootRelationshipOnly);
                collectedItems.add((ItemDomainMachineDesign) childItem);
                if (!rootRelationshipOnly) {
                    collectedElements.add(ie);
                }
            }
        }

        if (isRootItem) {
            collectedItems.add(parentItem);

            // mark ItemElement for relationship from parent to its container for deletion
            List<ItemElement> memberList = parentItem.getItemElementMemberList();
            if (memberList.size() == 1) {
                ItemElement containerRelElement = memberList.get(0);
                collectedElements.add(containerRelElement);
            }
        }
    }

    public Boolean getMoveToTrashAllowed() {
        return moveToTrashAllowed;
    }

    public Boolean getMoveToTrashHasWarnings() {
        return moveToTrashHasWarnings;
    }

    public String getMoveToTrashDisplayName() {
        return moveToTrashDisplayName;
    }

    public String getMoveToTrashMessage() {
        return moveToTrashMessage;
    }

    public TreeNode getMoveToTrashNode() {
        return moveToTrashNode;
    }

    /**
     * Prepares dialog for move to trash operation.
     */
    public void prepareMoveToTrash() {

        updateCurrentUsingSelectedItemInTreeTable();

        ItemDomainMachineDesign itemToDelete = findById(getCurrent().getId());
        if (itemToDelete == null) {
            return;
        }

        moveToTrashNode = null;
        moveToTrashDisplayName = itemToDelete.getName();
        moveToTrashMessage = "";
        moveToTrashHasWarnings = false;

        // collect list of items to delete, for use here in applying restrictions
        // and in moveToTrash for executing the operation
        moveToTrashItemsToUpdate = new ArrayList<>();
        moveToTrashElementsToDelete = new ArrayList<>();
        collectItemsForDeletion(itemToDelete, moveToTrashItemsToUpdate, moveToTrashElementsToDelete, true, true);
        
        // check each item for restriction violations
        moveToTrashAllowed = true;
        CdbRole sessionRole = (CdbRole) SessionUtility.getRole();
        UserInfo sessionUser = (UserInfo) SessionUtility.getUser();
        for (ItemDomainMachineDesign itemToCheck : moveToTrashItemsToUpdate) {
            String errorString = "";
            String warningString = "";

            // don't allow move to trash for template with instances
            List<Item> templateInstances = itemToCheck.getItemsCreatedFromThisTemplateItem();
            if ((templateInstances != null) && (templateInstances.size() > 0)) {
                moveToTrashAllowed = false;
                errorString = errorString + "Item is a template with instances. ";
            }
            
            // don't allow move to trash for item in multiple assemblies
            List<ItemElement> childMemberList = itemToCheck.getItemElementMemberList();
            if (childMemberList.size() > 1) {
                // this code assumes that a child machine design item has only one 'membership'
                moveToTrashAllowed = false;
                errorString = errorString + "Item is a child of multiple parent items or templates (e.g., it might be an unfulfilled template placeholder).";
            }
            
            // check for various relationships
            List<ItemElementRelationship> relationshipList = itemToCheck.getFullRelationshipList();
            boolean hasCableRelationship = false;
            boolean hasMaarcRelationship = false;
            boolean hasOtherRelationship = false;
            for (ItemElementRelationship relationship : relationshipList) {
                RelationshipType relType = relationship.getRelationshipType();
                if (relType == null) {
                    // shouldn't happen, but....
                    continue;
                } else if (relType.equals(RelationshipTypeFacade.getRelationshipTypeLocation())) {
                    // ignore location relationships
                    continue;
                } else if (relType.equals(RelationshipTypeFacade.getRelationshipTypeTemplate())) {
                    // ignore template relationships, handling explicitly already
                    continue;
                } else if (relType.equals(RelationshipTypeFacade.getRelationshipTypeCable())) {
                    hasCableRelationship = true;
                } else if (relType.equals(RelationshipTypeFacade.getRelationshipTypeMaarc())) {
                    hasMaarcRelationship = true;
                } else {
                    moveToTrashAllowed = false;
                    String relTypeName = relType.getName();
                    if (relTypeName != null) {
                        errorString = errorString + "Item has " + relTypeName + " relationship. ";
                    } else {
                        hasOtherRelationship = true;
                    }
                }
            }
            if (hasCableRelationship) {
                // don't allow move to trash for cable endpoints
                moveToTrashAllowed = false;
                errorString = errorString + "Item is an endpoint for one or more cables. ";
            }
            if (hasMaarcRelationship) {
                // don't allow move to trash for MAARC relationships
                moveToTrashAllowed = false;
                errorString = errorString + "Item has one or more MAARC items. ";
            }
            if (hasOtherRelationship) {
                // don't allow move to trash for other relationships (generic check for now, add specific handling as encountered)
                errorString = errorString + "Item has one or more relationships of unspecified type. ";
            }

            // check permissions for current user
            if (sessionRole != CdbRole.ADMIN) {
                if (!AuthorizationUtility.isEntityWriteableByUser(itemToCheck, sessionUser)) {
                    moveToTrashAllowed = false;
                    errorString = errorString + "Current user does not have permission to delete item. ";
                }
            }

            // check if need to warn about property values
            List<PropertyValue> itemProperties = itemToCheck.getPropertyValueList();
            if (itemProperties != null && !itemProperties.isEmpty()) {
                moveToTrashHasWarnings = true;
                warningString = warningString
                        + "Item has property values and/or traveler templates/instances, including: (";
                for (PropertyValue propValue : itemProperties) {
                    PropertyType propType = propValue.getPropertyType();
                    if (propType != null && propType.getName() != null) {
                        warningString = warningString
                                + propValue.getPropertyType().getName() + ", ";
                    }
                    warningString = warningString.substring(0, warningString.length() - 2) + "). ";
                }
            }

            // check if need to warn about log entries
            List<Log> itemLogs = itemToCheck.getLogList();
            if (itemLogs != null && !itemLogs.isEmpty()) {
                moveToTrashHasWarnings = true;
                warningString = warningString + "Item has log entries. ";
            }

            if (!errorString.isEmpty()) {
                itemToCheck.setMoveToTrashErrorMsg(errorString);
            }
            if (!warningString.isEmpty()) {
                itemToCheck.setMoveToTrashWarningMsg(warningString);
            }
        }

        // build tree node hierarchy for dialog
        if (moveToTrashItemsToUpdate.size() > 1 || !moveToTrashAllowed || moveToTrashHasWarnings) {
            moveToTrashNode = new DefaultTreeNode();
            prepareItemHierarchyTree(moveToTrashNode, itemToDelete);
        }

        if (!moveToTrashAllowed) {
            moveToTrashMessage = "Unable to move '"
                    + itemToDelete.getName()
                    + "' to trash because there are problems with one or more items in hierarchy. "
                    + "Items with problems are shown in red in the table below. "
                    + "Consult item detail view for additional information. "
                    + "Click 'No' to cancel. ";
            if (moveToTrashHasWarnings) {
                moveToTrashMessage = moveToTrashMessage
                        + "WARNING: there are warnings for one or more items. "
                        + "Items with warnings can be moved to trash.";
            }
        } else {
            String itemDescription = "'" + itemToDelete.getName() + "'";
            if (!itemToDelete.getItemElementDisplayList().isEmpty()) {
                itemDescription = itemDescription
                        + " and its children (hierarchy shown in table below) ";
            }
            moveToTrashMessage = "Click 'Yes' to move "
                    + itemDescription
                    + " to trash. Click 'No' to cancel. "
                    + "NOTE: items restored from trash will appear as top-level items "
                    + "and not within their original container. ";
            if (moveToTrashHasWarnings) {
                moveToTrashMessage = moveToTrashMessage
                        + "WARNING: there are warnings for one or more items. "
                        + "Items with warnings are shown in green in the table below. "
                        + "Items with warnings can be moved to trash.";
            }
        }
    }

    /**
     * Executes move to trash operation initiated by 'Yes' button on dialog.
     */
    public void moveToTrash() {

        ItemDomainMachineDesign rootItemToDelete = findById(getCurrent().getId());
        if (rootItemToDelete == null) {
            return;
        }

        // mark all items as deleted entity type (moves them to "trash")
        for (ItemDomainMachineDesign item : moveToTrashItemsToUpdate) {
            item.setIsDeleted();
        }

        // remove relationship for root item to its parent and 
        // add container item to list of items to update
        if (moveToTrashElementsToDelete.size() > 1) {
            // should be 0 for a top-level item or 1 for internal node
            SessionUtility.addErrorMessage("Error", "Could not delete: " + rootItemToDelete + " - unexpected relationships exist in hierarchy");
            return;
        } else if (moveToTrashElementsToDelete.size() == 1) {
            ItemElement ie = moveToTrashElementsToDelete.get(0);
            Item childItem = ie.getContainedItem();
            Item ieParentItem = ie.getParentItem();
            ieParentItem.removeItemElement(ie);
            childItem.getItemElementMemberList().remove(ie);
            ie.setMarkedForDeletion(true);
            moveToTrashItemsToUpdate.add((ItemDomainMachineDesign) ieParentItem);
        }

        try {
            updateList(moveToTrashItemsToUpdate);
        } catch (CdbException ex) {
            // handled adequately by thrower
        }

        moveToTrashNode = null;
        moveToTrashMessage = null;
        moveToTrashItemsToUpdate = null;
        moveToTrashElementsToDelete = null;
        moveToTrashHasWarnings = false;

        ItemDomainMachineDesignDeletedItemsController.getInstance().resetListDataModel();
        ItemDomainMachineDesignDeletedItemsController.getInstance().resetSelectDataModel();
    }

    // </editor-fold>
    @Override
    protected ItemDomainMachineDesignControllerUtility createControllerUtilityInstance() {
        return new ItemDomainMachineDesignControllerUtility();
    }
}

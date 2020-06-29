/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers;

import gov.anl.aps.cdb.common.utilities.StringUtility;
import gov.anl.aps.cdb.portal.model.db.entities.Domain;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemCategory;
import gov.anl.aps.cdb.portal.model.db.entities.ItemConnector;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainMachineDesign;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.model.db.entities.ItemProject;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.TreeNode;

/**
 *
 * @author craig
 */
public abstract class ItemDomainCableDesignWizardBase {

    public class MembersDialog {

        private List<Item> selectionModelCables = new ArrayList<>();
        private Boolean disableButtonSelect = true;

        public List<Item> getSelectionModelCables() {
            return selectionModelCables;
        }

        public void setSelectionModelCables(List<Item> selectionModelCables) {
            this.selectionModelCables = selectionModelCables;
        }

        public Boolean getDisableButtonSelect() {
            return disableButtonSelect;
        }

        public void setDisableButtonSelect(Boolean disableButtonSelect) {
            this.disableButtonSelect = disableButtonSelect;
        }

        /**
         * Handles select events generated by the data table component. Must
         * call client side remoteCommand to update button state from oncomplete
         * attribute for this event tag.
         */
        public void selectListenerCables() {
            setEnablement();
        }

        public void actionListenerSelectSuccess() {
        }

        /**
         * Resets the dialog components when closing.
         */
        public void reset() {
            setSelectionModelCables(new ArrayList<>());
            setEnablement();
        }

        /**
         * Enables save button when a valid selection is made.
         */
        private void setEnablement() {

            if (selectionModelCables.isEmpty()) {
                disableButtonSelect = true;
            } else {
                disableButtonSelect = false;
            }
        }

        /**
         * Handles selected cables for members dialog's select button.
         */
        public String select(String remoteCommandSuccess) {

            // process selected cables
            if (!selectionModelCables.isEmpty()) {
                addMembers(selectionModelCables);
                SessionUtility.executeRemoteCommand(remoteCommandSuccess);
            }
            return null;
        }

        public void cancel() {
        }

    }

    protected static final String defaultRedirectSuccess = "/views/itemDomainCableDesign/view?faces-redirect=true";
    protected static final String tabEndpoint = "EndpointTab";
    protected static final String tabBasics = "CableBasicsTab";
    protected static final String tabReview = "CableReviewTab";
    protected static final String tabMembers = "MembersTab";

    protected ItemDomainCableDesignWizardClient client;
    protected TreeNode machineDesignTreeEndpoint1 = null;
    protected TreeNode machineDesignTreeEndpoint2 = null;
    protected String inputValueName = "";
    protected List<ItemProject> selectionProjectList = new ArrayList<>();
    protected List<ItemCategory> selectionTechnicalSystemList = new ArrayList<>();
    protected TreeNode selectionEndpoint1 = null;
    protected TreeNode selectionEndpoint2 = null;
    protected List<Item> members = new ArrayList<>();
    protected Boolean disableButtonPrev = true;
    protected Boolean disableButtonNext = true;
    protected Boolean disableButtonSave = true;
    protected Boolean disableButtonCancel = false;
    protected String currentTab = tabEndpoint;
    protected String redirectSuccess = "";
    protected MembersDialog dialogMembers = new MembersDialog();
    protected List<ItemCategory> availableTechnicalSystems = null;

    public ItemDomainCableDesignWizardBase() {
    }

    public void registerClient(ItemDomainCableDesignWizardClient client, String redirectSuccess) {
        this.client = client;
        this.redirectSuccess = redirectSuccess;
        reset();
    }

    /**
     * Returns the root node of the machine design tree that is used to populate
     * the wizard's endpoint selection tab.
     */
    public TreeNode getMachineDesignTreeEndpoint1() {
        if (machineDesignTreeEndpoint1 == null) {
            ItemDomainMachineDesignController controller = ItemDomainMachineDesignController.getInstance();
            machineDesignTreeEndpoint1 = controller.loadMachineDesignRootTreeNode(false);
        }
        return machineDesignTreeEndpoint1;
    }

    /**
     * @link ItemDomainCableDesignWizard#getMachineDesignTreeEndpoint1
     */
    public void setMachineDesignTreeEndpoint1(TreeNode machineDesignTree) {
        this.machineDesignTreeEndpoint1 = machineDesignTree;
    }

    /**
     * Returns the root node of the machine design tree that is used to populate
     * the wizard's endpoint selection tab.
     */
    public TreeNode getMachineDesignTreeEndpoint2() {
        if (machineDesignTreeEndpoint2 == null) {
            ItemDomainMachineDesignController controller = ItemDomainMachineDesignController.getInstance();
            machineDesignTreeEndpoint2 = controller.loadMachineDesignRootTreeNode(false);
        }
        return machineDesignTreeEndpoint2;
    }

    /**
     * @link ItemDomainCableDesignWizard#getMachineDesignTreeEndpoint2
     */
    public void setMachineDesignTreeEndpoint2(TreeNode machineDesignTree) {
        this.machineDesignTreeEndpoint2 = machineDesignTree;
    }

    /**
     * Returns the cable name. This is the model for the name input on the
     * "basics" tab.
     */
    public String getInputValueName() {
        return inputValueName;
    }

    /**
     * @link ItemDomainCableDesignWizard#getName
     */
    public void setInputValueName(String inputValueName) {
        this.inputValueName = inputValueName;
    }

    /**
     * Returns the selected list of projects for the cable.
     */
    public List<ItemProject> getSelectionProjectList() {
        return selectionProjectList;
    }

    /**
     * @link ItemDomainCableDesignWizard#getSelectionProjectList
     */
    public void setSelectionProjectList(List<ItemProject> projectList) {
        this.selectionProjectList = projectList;
    }

    /**
     * Returns project list as String for display.
     */
    public String getProjectsString() {
        return StringUtility.getStringifyCdbList(selectionProjectList);
    }

    public List<ItemCategory> getSelectionTechnicalSystemList() {
        return selectionTechnicalSystemList;
    }

    public void setSelectionTechnicalSystemList(List<ItemCategory> selectionTechnicalSystemList) {
        this.selectionTechnicalSystemList = selectionTechnicalSystemList;
    }

    /**
     * Returns technical system list as String for display.
     */
    public String getTechnicalSystemsString() {
        return StringUtility.getStringifyCdbList(selectionTechnicalSystemList);
    }

    public List<ItemCategory> getAvailableTechnicalSystems() {
        if (availableTechnicalSystems == null) {
            availableTechnicalSystems = ItemCategoryController.getInstance().getItemTypeCategoryEntityListByDomainName(getDomain().getName());
        }
        return availableTechnicalSystems;
    }

    /**
     * Returns the cable's first endpoint, intended to be set by the client
     * after creating an instance of this class.
     */
    public TreeNode getSelectionEndpoint1() {
        return selectionEndpoint1;
    }

    /**
     * @link ItemDomainCableDesignWizard#getEndpoint1
     */
    public void setSelectionEndpoint1(TreeNode selectionEndpoint1) {
        this.selectionEndpoint1 = selectionEndpoint1;
    }

    /**
     * Returns selectionEndpoint1 as a string.
     * @link ItemDomainCableDesignWizard#getSelectionEndpoint1
     */
    public String getEndpoint1String() {
        if (selectionEndpoint1 == null) {
            return new String();
        } else {
            return ((ItemElement) (selectionEndpoint1.getData())).getContainedItem().toString();
        }
    }

    /**
     * Returns the cable's second endpoint. This is set by the wizard's endpoint
     * selection tab, and is the selection model for the machine design tree
     * table.
     */
    public TreeNode getSelectionEndpoint2() {
        return selectionEndpoint2;
    }

    /**
     * Returns endpoint2 as a string.
     * @link ItemDomainCableDesignWizard#getSelectionEndpoint2
     */
    public String getEndpoint2String() {
        if (selectionEndpoint2 == null) {
            return new String();
        } else {
            return ((ItemElement) (selectionEndpoint2.getData())).getContainedItem().toString();
        }
    }

    /**
     * @link ItemDomainCableDesignWizard#getSelectedEndpoint
     */
    public void setSelectionEndpoint2(TreeNode selectionEndpoint2) {
        this.selectionEndpoint2 = selectionEndpoint2;
    }

    /**
     * Returns value model for list of member cables.
     */
    public List<Item> getMembers() {
        return members;
    }

    /**
     * Returns number of member cables.
     */
    public int getMemberCount() {
        return members.size();
    }

    protected void addMembers(List<Item> newMembers) {

        for (Item item : newMembers) {
            if (!members.contains(item)) {
                members.add(item);
            }
        }
        setEnablementForCurrentTab();
    }

    public void removeMember(Item member) {
        if (members.contains(member)) {
            members.remove(member);
            setEnablementForCurrentTab();
        }
    }

    public MembersDialog getDialogMembers() {
        return dialogMembers;
    }

    public void setDialogMembers(MembersDialog dialogMembers) {
        this.dialogMembers = dialogMembers;
    }

    /**
     * Returns true if the previous button should be disabled.
     */
    public Boolean getDisableButtonPrev() {
        return disableButtonPrev;
    }

    /**
     * @link ItemDomainCableDesignWizard#getDisableButtonPrev
     */
    public void setDisableButtonPrev(Boolean disableButtonPrev) {
        this.disableButtonPrev = disableButtonPrev;
    }

    /**
     * Returns true if the next button should be disabled.
     */
    public Boolean getDisableButtonNext() {
        return disableButtonNext;
    }

    /**
     * @link ItemDomainCableDesignWizard#getDisableButtonNext
     */
    public void setDisableButtonNext(Boolean disableButtonNext) {
        this.disableButtonNext = disableButtonNext;
    }

    /**
     * Returns true if the save button should be disabled.
     */
    public Boolean getDisableButtonSave() {
        return disableButtonSave;
    }

    /**
     * @link ItemDomainCableDesignWizard#getDisableButtonSave
     */
    public void setDisableButtonSave(Boolean disableButtonSave) {
        this.disableButtonSave = disableButtonSave;
    }

    /**
     * Returns true if the cancel button should be disabled.
     */
    public Boolean getDisableButtonCancel() {
        return disableButtonCancel;
    }

    /**
     * @link ItemDomainCableDesignWizard#getDisableButtonCancel
     */
    public void setDisableButtonCancel(Boolean disableButtonCancel) {
        this.disableButtonCancel = disableButtonCancel;
    }

    protected String getRedirectSuccess() {
        return redirectSuccess;
    }

    protected void setRedirectSuccess(String redirectSuccess) {
        this.redirectSuccess = redirectSuccess;
    }

    /**
     * Handles select events generated by the machine design tree table
     * component. Must call client side remoteCommand to update button state
     * from oncomplete attribute for this event tag.
     */
    public void selectListenerEndpoint1(NodeSelectEvent event) {
        setEnablementForCurrentTab();
    }

    /**
     * Handles select events generated by the machine design tree table
     * component. Must call client side remoteCommand to update button state
     * from oncomplete attribute for this event tag.
     */
    public void selectListenerEndpoint2(NodeSelectEvent event) {
        setEnablementForCurrentTab();
    }

    /**
     * Handles unselect events generated by the machine design tree table
     * component. Must call client side remoteCommand to update button state
     * from oncomplete attribute for this event tag.
     */
    public void unselectListenerEndpoint2(NodeUnselectEvent event) {
        setEnablementForCurrentTab();
    }

    /**
     * Handles select event on the p:selectCheckboxMenu for selecting project.
     */
    public void selectListenerProjectList() {
        setEnablementForCurrentTab();
    }

    /**
     * Handles unselect event on the p:selectCheckboxMenu for selecting project.
     */
    public void unselectListenerProjectList() {
        setEnablementForCurrentTab();
    }

    /**
     * Handles select event on the p:selectCheckboxMenu for selecting project.
     */
    public void selectListenerTechnicalSystemList() {
        setEnablementForCurrentTab();
    }

    /**
     * Handles keyup events for the inputValueName inputText component.
     */
    public void keyupListenerName() {
        setEnablementForCurrentTab();
    }

    public void prepareDialogMembers() {
        getDialogMembers().reset();
    }

    /**
     * Sets enable/disable state for the navigation buttons based on the current
     * tab and input elements.
     */
    public void setEnablementForCurrentTab() {
        setEnablement(currentTab);
    }

    /**
     * Sets enable/disable state for the navigation buttons based on the current
     * tab and input elements.
     */
    protected void setEnablement(String tab) {

        // default
        disableButtonPrev = true;
        disableButtonCancel = false;
        disableButtonSave = true;
        disableButtonNext = true;

        if (tab.endsWith("EndpointTab")) {
            disableButtonPrev = true;
            disableButtonCancel = false;
            disableButtonSave = true;
            if (selectionEndpoint2 != null) {
                Object data = selectionEndpoint2.getData();
                if (data instanceof ItemElement) {
                    ItemElement ie = (ItemElement) data;
                    Item containedItem = ie.getContainedItem();
                    if (containedItem != null) {
                        if (containedItem instanceof ItemDomainMachineDesign) {
                            // TODO: Check if same as endpoint 1
                            disableButtonNext = false;
                        } else {
                            disableButtonNext = true;
                            SessionUtility.addWarningMessage("Invalid Selection", "New connections can only be made directly to machine designs item.");
                        }
                    } else {
                        ItemConnector mdConnector = ie.getMdConnector();
                        if (mdConnector != null) {
                            SessionUtility.addWarningMessage("Select Machine design", "Use port mapping utility after the cable has been created.");
                            disableButtonNext = true;
                        } else {
                            SessionUtility.addWarningMessage("Invalid Selection", "New connections can only be made directly to machine designs item.");
                            disableButtonNext = true;
                        }
                    }
                } else {
                    disableButtonNext = false;
                }
            } else {
                disableButtonNext = true;
            }
        } else if (tab.endsWith("CableBasicsTab")) {
            disableButtonPrev = false;
            disableButtonCancel = false;
            disableButtonSave = true;
            if ((inputValueName.isEmpty()) || (selectionProjectList.isEmpty()) || (selectionTechnicalSystemList.isEmpty())) {
                disableButtonNext = true;
            } else {
                disableButtonNext = false;
            }
        } else if (tab.endsWith("MembersTab")) {
            disableButtonPrev = false;
            disableButtonCancel = false;
            disableButtonSave = true;
            if (!members.isEmpty()) {
                disableButtonNext = false;
            } else {
                disableButtonNext = true;
            }
        } else if (tab.endsWith("CableReviewTab")) {
            disableButtonPrev = false;
            disableButtonCancel = false;
            disableButtonSave = false;
            disableButtonNext = true;
        } else {
            // override in subclass
            setEnablement_(tab);
        }
    }

    /**
     * Handles FlowEvents generated by the wizard component. Determines next tab
     * based on current tab, defaults to visiting all tabs but implements
     * special cases. Skips cableDetailsTab when the type is "unspecified" since
     * that tab is empty.
     */
    public String onFlowProcess(FlowEvent event) {

        String nextStep = event.getNewStep();
        String currStep = event.getOldStep();

        nextStep = nextTab_(currStep, nextStep);

        setEnablement(nextStep);

        currentTab = nextStep;

        return nextStep;
    }

    /**
     * Resets models for wizard components.
     */
    protected void reset() {
        currentTab = "EndpointTab";
        machineDesignTreeEndpoint1 = null;
        machineDesignTreeEndpoint2 = null;
        inputValueName = "";
        selectionEndpoint1 = null;
        selectionEndpoint2 = null;
        selectionProjectList = null;
        selectionTechnicalSystemList = null;
        members.clear();
        reset_();
    }

    protected void cleanupClient() {
        if (client != null) {
            client.cleanupCableWizard();
        }
        client = null;
        redirectSuccess = "";
    }

    /**
     * Implements the cancel operation, invoked by the wizard's "Cancel"
     * navigation button.
     */
    public String cancel() {
        cleanupClient();
        this.reset();
        cancel_();
        return "list";
    }

    protected Domain getDomain() {
        return ItemDomainCableDesignController.getInstance().getDefaultDomain();
    }

    /**
     * Allows custom enablement behavior, called on tab change. Subclasses
     * should override for custom behavior. Default implementation does nothing.
     */
    protected void setEnablement_(String tab) {
    }

    /**
     * Returns custom tab navigation based on wizard state. Subclasses should
     * override for custom behavior.
     */
    protected String nextTab_(String currStep, String nextStep) {
        return nextStep;
    }

    /**
     * Performs custom reset behavior, subclasses should override as
     * appropriate. Default implementation does nothing.
     */
    protected void reset_() {
    }

    /**
     * Performs custom cancel behavior, subclasses should override as
     * appropriate. Default implementation does nothing.
     */
    public void cancel_() {
    }

    /**
     * Implements the save operation, invoked by the wizard's "Save" navigation
     * button.
     */
    public abstract String save();
}

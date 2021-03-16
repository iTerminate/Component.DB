///*
// * Copyright (c) UChicago Argonne, LLC. All rights reserved.
// * See LICENSE file.
// */
//package gov.anl.aps.cdb.portal.controllers.view;
//
//import gov.anl.aps.cdb.portal.controllers.ConnectorTypeController;
//import gov.anl.aps.cdb.portal.controllers.utilities.ConnectorTypeControllerUtility;
//import gov.anl.aps.cdb.portal.model.db.beans.ConnectorTypeFacade;
//import gov.anl.aps.cdb.portal.model.db.entities.ConnectorType;
//import java.io.Serializable;
//import javax.ejb.EJB;
//import javax.inject.Named;
//import org.omnifaces.cdi.ViewScoped;
//
///**
// *
// * @author darek
// */
//@Named(ConnectorTypeViewController.controllerNamed)
//@ViewScoped
//public class ConnectorTypeViewController extends CdbEntityViewController<ConnectorTypeControllerUtility, ConnectorTypeController, ConnectorType, ConnectorTypeFacade> implements Serializable{
//    
//    public static final String controllerNamed = "connectorTypeViewController"; 
//    
//    @EJB
//    ConnectorTypeFacade connectorTypeFacade; 
//
//    @Override
//    protected ConnectorTypeController getSessionControllerInstance() {
//        return ConnectorTypeController.getInstance(); 
//    }
//
//    @Override
//    protected ConnectorTypeControllerUtility createControllerUtilityInstance() {
//        return new ConnectorTypeControllerUtility(); 
//    }
//
//    @Override
//    protected ConnectorTypeFacade getEntityDbFacade() {
//        return connectorTypeFacade; 
//    }
//    
//}

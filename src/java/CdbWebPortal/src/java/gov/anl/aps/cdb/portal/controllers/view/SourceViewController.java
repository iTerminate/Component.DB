///*
// * Copyright (c) UChicago Argonne, LLC. All rights reserved.
// * See LICENSE file.
// */
//package gov.anl.aps.cdb.portal.controllers.view;
//
//import gov.anl.aps.cdb.portal.controllers.SourceController;
//import gov.anl.aps.cdb.portal.controllers.utilities.SourceControllerUtility;
//import gov.anl.aps.cdb.portal.model.db.beans.SourceFacade;
//import gov.anl.aps.cdb.portal.model.db.entities.Source;
//import java.io.Serializable;
//import javax.ejb.EJB;
//import javax.inject.Named;
//import org.omnifaces.cdi.ViewScoped;
//
///**
// *
// * @author darek
// */
//@Named(SourceViewController.CONTROLLER_NAMED)
//@ViewScoped
//public class SourceViewController extends CdbEntityViewController<SourceControllerUtility, SourceController, Source, SourceFacade> implements Serializable {
//
//    public static final String CONTROLLER_NAMED = "sourceViewController"; 
//    
//    @EJB
//    SourceFacade sourceFacade; 
//    
//    @Override
//    protected SourceControllerUtility createControllerUtilityInstance() {
//        return new SourceControllerUtility(); 
//    }
//
//    @Override
//    protected SourceFacade getEntityDbFacade() {
//        return sourceFacade;
//    }
//
//    @Override
//    protected SourceController getSessionControllerInstance() {
//        return SourceController.getInstance(); 
//    }
//    
//}

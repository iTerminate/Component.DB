#!/usr/bin/env python

"""
Copyright (c) UChicago Argonne, LLC. All rights reserved.
See LICENSE file.
"""
from cdb.common.exceptions.invalidSession import InvalidSession
from sqlalchemy.orm.exc import NoResultFound
from sqlalchemy.exc import OperationalError

from cdb.common.db.entities.itemItemProject import ItemItemProject
from cdb.common.db.entities.itemProject import ItemProject
from cdb.common.exceptions.objectAlreadyExists import ObjectAlreadyExists
from cdb.common.exceptions.objectNotFound import ObjectNotFound
from cdb.common.exceptions.invalidArgument import InvalidArgument
from cdb.common.exceptions.dbError import DbError
from cdb.common.db.impl.entityInfoHandler import EntityInfoHandler
from cdb.common.db.impl.entityTypeHandler import EntityTypeHandler
from cdb.common.db.entities.item import Item
from cdb.common.db.entities.itemElement import ItemElement
from cdb.common.db.entities.itemEntityType import ItemEntityType
from cdb.common.db.entities.itemSource import ItemSource
from cdb.common.db.entities.itemCategory import ItemCategory
from cdb.common.db.entities.propertyValue import PropertyValue
from cdb.common.db.entities.propertyType import PropertyType
from cdb.common.db.entities.domain import Domain
from cdb.common.db.entities.itemType import ItemType
from cdb.common.db.entities.itemItemCategory import ItemItemCategory
from cdb.common.db.entities.itemItemType import ItemItemType
from cdb.common.db.entities.itemElementProperty import ItemElementProperty
from cdb.common.db.entities.itemElementRelationship import ItemElementRelationship
from cdb.common.db.entities.itemConnector import ItemConnector
from cdb.common.db.entities.itemElementLog import ItemElementLog
from cdb.common.db.entities.itemElementRelationshipHistory import ItemElementRelationshipHistory
from cdb.common.db.impl.cdbDbEntityHandler import CdbDbEntityHandler
from cdb.common.db.impl.domainHandler import DomainHandler
from cdb.common.db.impl.sourceHandler import SourceHandler
from cdb.common.db.impl.logHandler import LogHandler
from cdb.common.db.impl.relationshipTypeHandler import RelationshipTypeHandler
from cdb.common.db.impl.resourceTypeHandler import ResourceTypeHandler
from cdb.common.db.impl.userInfoHandler import UserInfoHandler
from cdb.common.db.impl.propertyValueHandler import PropertyValueHandler
from cdb.common.db.impl.permissionHandler import PermissionHandler


class ItemHandler(CdbDbEntityHandler):

    def __init__(self):
        CdbDbEntityHandler.__init__(self)
        self.entityInfoHandler = EntityInfoHandler()
        self.domainHandler = DomainHandler()
        self.entityTypeHandler = EntityTypeHandler()
        self.sourceHandler = SourceHandler()
        self.logHandler = LogHandler()
        self.propertyValueHandler = PropertyValueHandler()
        self.relationshipTypeHandler = RelationshipTypeHandler()
        self.resourceTypeHandler = ResourceTypeHandler()
        self.userInfoHandler = UserInfoHandler()
        self.permissionHandler = PermissionHandler()

    def getItemById(self, session, id):
        return self._findDbObjById(session, Item, id)

    def getItemByQrId(self, session, qrId):
        entityDisplayName = self._getEntityDisplayName(Item)

        try:
            dbItem = session.query(Item).filter(Item.qr_id==qrId).one()
            return dbItem
        except NoResultFound, ex:
            raise ObjectNotFound('No %s with QR-Id: %s found.'
                                 % (entityDisplayName, qrId))

    def getItemElementsByName(self, session, name):
        return self._findDbObjByName(session, ItemElement, name)

    def getItemElementById(self, session, id):
        return self._findDbObjById(session, ItemElement, id)

    def getItemElementsByItemId(self, session, itemId):
        entityDisplayName = self._getEntityDisplayName(ItemCategory)

        try:
            dbItemElements = session.query(ItemElement).filter(ItemElement.parent_item_id==itemId).all()
            return dbItemElements
        except NoResultFound, ex:
            raise ObjectNotFound('No %s with item id: %s found.'
                                 % (entityDisplayName, itemId))

    def addItemCategory(self, session, itemCategoryName, description, domainName):
        entityDisplayName = self._getEntityDisplayName(ItemCategory)

        self.logger.debug('Adding %s %s' % (entityDisplayName, itemCategoryName))

        dbDomain = self.domainHandler.findDomainByName(session, domainName)

        try:
            self.getItemCategoryByName(session, itemCategoryName, dbDomain.id)
            raise ObjectAlreadyExists('%s %s for domain %s already exists.' % (entityDisplayName, itemCategoryName, dbDomain.name))
        except ObjectNotFound, ex:
            # ok
            pass

        # Create Entity Db Object
        dbItemCategory = ItemCategory(name=itemCategoryName)
        if description:
            dbItemCategory.description = description

        dbItemCategory.domain = dbDomain

        session.add(dbItemCategory)
        session.flush()

        self.logger.debug('Inserted %s id %s' % (entityDisplayName, dbItemCategory.id))
        return dbItemCategory

    def addItemProject(self, session, itemProjectName, description):
        return self._addSimpleNameDescriptionTable(session, ItemProject, itemProjectName, description)

    def getItemProjects(self, session):
        self.logger.debug('Retrieving item projects.')
        dbItemProjects = session.query(ItemProject).all()
        return dbItemProjects

    def addItemType(self, session, itemTypeName, description, domainName):
        entityDisplayName = self._getEntityDisplayName(ItemType)

        self.logger.debug('Adding %s %s' % (entityDisplayName, itemTypeName))

        dbDomain = self.domainHandler.findDomainByName(session, domainName)

        try:
            self.getItemTypeByName(session, itemTypeName, dbDomain.id)
            raise ObjectAlreadyExists('%s %s for domain %s already exists.' % (entityDisplayName, itemTypeName, dbDomain.name))
        except ObjectNotFound, ex:
            # ok
            pass

        # Create Entity Db Object
        dbItemType = ItemType(name=itemTypeName)
        if description:
            dbItemType.description = description

        dbItemType.domain = dbDomain

        session.add(dbItemType)
        session.flush()

        self.logger.debug('Inserted %s id %s' % (entityDisplayName, dbItemType.id))
        return dbItemType

    def getItemCategoryByName(self, session, name, domainId):
        entityDisplayName = self._getEntityDisplayName(ItemCategory)

        try:
            dbItem = session.query(ItemCategory).filter(ItemCategory.name==name)\
                .filter(ItemCategory.domain_id == domainId).one()
            return dbItem
        except NoResultFound, ex:
            raise ObjectNotFound('No %s with name: %s, domain id: %s exists.'
                                 % (entityDisplayName, name, domainId))

    def getItemProjectByName(self, session, itemProjectName):
        return self._findDbObjByName(session, ItemProject, itemProjectName)

    def getItemTypeByName(self, session, name, domainId):
        entityDisplayName = self._getEntityDisplayName(ItemType)

        try:
            dbItem = session.query(ItemType).filter(ItemType.name==name)\
                .filter(ItemType.domain_id == domainId).one()
            return dbItem
        except NoResultFound, ex:
            raise ObjectNotFound('No %s with name: %s, domain id: %s exists.'
                                 % (entityDisplayName, name, domainId))

    def getItemConnectorById(self, session, id):
        return self._findDbObjById(session, ItemConnector, id)

    def getItemElementRelationshipById(self, session, id):
        return self._findDbObjById(session, ItemElementRelationship, id)

    def getItemByUniqueAttributes(self, session, domainId, name, itemIdentifier1, itemIdentifier2, derivedFromItemId):
        entityDisplayName = self._getEntityDisplayName(Item)

        try:
            dbItem = session.query(Item).filter(Item.domain_id==domainId)\
                .filter(Item.name==name)\
                .filter(Item.item_identifier1==itemIdentifier1)\
                .filter(Item.item_identifier2==itemIdentifier2)\
                .filter(Item.derived_from_item_id==derivedFromItemId).one()
            return dbItem
        except NoResultFound, ex:
            raise ObjectNotFound('No %s with name: %s, item identifier 1: %s, item identifier 2: %s in domain id %s exists.'
                                 % (entityDisplayName, name, itemIdentifier1, itemIdentifier1, domainId))

    def addItem(self, session, domainName, name, createdByUserId, ownerUserId, ownerGroupId,
                itemIdentifier1 = None, itemIdentifier2 = None, qrId = None, description = None,
                isGroupWriteable=True, createdOnDataTime=None, lastModifiedOnDateTime=None,
                derivedFromItemId=None, entityTypeNames=None):

        # Create entity info
        entityInfoArgs = (createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable, createdOnDataTime, lastModifiedOnDateTime)

        domain = self.domainHandler.findDomainByName(session, domainName)
        try:
            if self.getItemByUniqueAttributes(session, domain.id, name, itemIdentifier1, itemIdentifier2, derivedFromItemId):
                raise ObjectAlreadyExists("Item with attributes already exists: "
                                          "(domain=%s, name='%s', item_identifier1=%s, item_identifier2=%s and derivedFromItemId=%s)"
                                          % (domain.name, name, itemIdentifier1, itemIdentifier2, derivedFromItemId))
        except ObjectNotFound:
            pass

        # Create item
        dbItem = Item(name=name)
        dbItem.domain = domain
        dbItem.derived_from_item_id = derivedFromItemId
        dbItem.qr_id = qrId
        dbItem.item_identifier1 = itemIdentifier1
        dbItem.item_identifier2 = itemIdentifier2

        try:
            session.add(dbItem)
            session.flush()
        except OperationalError, err:
            raise DbError(err.message)

        # Add self element
        self.addItemElement(session, None, dbItem.id, None, False, description, *entityInfoArgs, selfElementCreation=True)

        self.logger.debug('Inserted item id %s' % dbItem.id)
        if entityTypeNames is not None:
            if type(entityTypeNames) is list:
                for entityTypeName in entityTypeNames:
                    self.addItemEntityType(session, None, entityTypeName, dbItem)
            elif type(entityTypeNames) is str:
                self.addItemEntityType(session, None, entityTypeNames, dbItem)


        return dbItem

    def getSelfElementByItemId(self, session, itemId):
        entityDisplayName = self._getEntityDisplayName(ItemElement)
        try:
            dbItemElement = session.query(ItemElement)\
                .filter(ItemElement.parent_item_id==itemId)\
                .filter(ItemElement.name==None)\
                .filter(ItemElement.derived_from_item_element_id==None).one()
            return dbItemElement
        except NoResultFound, ex:
            raise ObjectNotFound('No self %s with item id %s exists.' % (entityDisplayName, itemId))

    def getItemsOfDomain(self, session, domainName):
        entityDisplayName = self._getEntityDisplayName(Item)
        try:
            query = session.query(Item).join(Domain)
            query = query.filter(Domain.name==domainName)

            dbItems = query.all()
            return dbItems

        except NoResultFound, ex:
            raise ObjectNotFound("No %ss with domain %s found." % (entityDisplayName, domainName))

    def getItemsDerivedFromItem(self, session, derivedItemId):
        entityDisplayName = self._getEntityDisplayName(Item)

        try:
            query = session.query(Item).join(Domain)
            query = query.filter(Item.derived_from_item_id==derivedItemId)

            dbItems = query.all()
            return dbItems
        except NoResultFound, ex:
            raise ObjectNotFound("No %ss derived from item id %s found." % (entityDisplayName, derivedItemId))



    def getItemsWithPropertyTypeName(self, session, propertyTypeName, itemDomainName = None, itemDerivedFromItemId = None, propertyValueMatch = None):
        entityDisplayName = self._getEntityDisplayName(Item)
        try:
            query = session.query(Item)\
                .join(ItemElement.parentItem)\
                .join(ItemElementProperty)\
                .join(PropertyValue)\
                .join(PropertyType)\
                .filter(PropertyType.name == propertyTypeName)

            if itemDerivedFromItemId is not None:
                query = query.filter(Item.derived_from_item_id == itemDerivedFromItemId)

            if propertyValueMatch is not None:
                query = query.filter(PropertyValue.value == propertyValueMatch)

            if itemDomainName is not None:
                query = query.join(Domain)
                query = query.filter(Domain.name == itemDomainName)

            dbItems = query.all()
            return dbItems

        except NoResultFound, ex:
            raise ObjectNotFound("No %ss with property type %s found."  % (entityDisplayName, propertyTypeName))



    def addItemEntityType(self, session, itemId, entityTypeName, item=None):
        dbItemEntityType = ItemEntityType()

        dbEntityType = self.entityTypeHandler.findEntityTypeByName(session, entityTypeName)

        if not item:
            item = self.getItemById(session, itemId)

        dbAllowedEntityTypeDomains = self.domainHandler.getAllowedEntityTypeDomain(session, item.domain_id)
        found = False;
        for allowedEntityTypeDomain in dbAllowedEntityTypeDomains:
            allowedEntityType = allowedEntityTypeDomain.entityType
            if entityTypeName == allowedEntityType.name:
                found = True
                break

        if not found:
            raise InvalidArgument("Entity type name: %s cannot be added to domain of item." % entityTypeName)

        dbItemEntityType.item = item

        dbItemEntityType.entityType = dbEntityType

        session.add(dbItemEntityType)
        session.flush()

        self.logger.debug('Inserted Item Entity Type for item id %s' % dbItemEntityType.item.id)

        return dbItemEntityType

    def addItemElement(self, session, name, parentItemId, containedItemId, isRequired, description,
                createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable , createdOnDataTime=None, lastModifiedOnDateTime=None, selfElementCreation=False):
        dbItemElements = session.query(ItemElement).filter(ItemElement.name==name).all()
        for element in dbItemElements:
            if element.parent_item_id == parentItemId and element.name == name:
                raise ObjectAlreadyExists('Item Element with name %s already exists.' % name)

        if not selfElementCreation:
            parentSelfElement = self.getSelfElementByItemId(session, parentItemId)
            self.permissionHandler.verifyPermissionsForWriteToItemElement(session, createdByUserId, dbItemElementObject=parentSelfElement)
            entityInfo = parentSelfElement.entityInfo
            parentSelfElement.entityInfo = self.entityInfoHandler.updateEntityInfo(session, entityInfo, createdByUserId)
            session.add(parentSelfElement)

        # Create entity info
        dbEntityInfo = self.entityInfoHandler.createEntityInfo(session, createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable, createdOnDataTime, lastModifiedOnDateTime)

        # Create item
        dbItemElement = ItemElement(name=name, description=description, is_required=isRequired)
        dbItemElement.entityInfo = dbEntityInfo

        if parentItemId:
            dbItemElement.parentItem = self.getItemById(session, parentItemId)
        if containedItemId:
            dbItemElement.containedItem = self.getItemById(session, containedItemId)

        try:
            session.add(dbItemElement)
            session.flush()
        except OperationalError, err:
            raise DbError(err.message)

        self.logger.debug('Inserted item Element id %s' % dbItemElement.id)

        return dbItemElement

    def updateItemElement(self, session, itemElementId, lastModifiedUserId, containedItemId = -1, isRequired = -1, name = None, description = None,
                          ownerUserId = None, ownerGroupId = None, isGroupWriteable = None):

        dbItemElement = self.getItemElementById(session, itemElementId)

        self.permissionHandler.verifyPermissionsForWriteToItemElement(session, lastModifiedUserId, dbItemElementObject=dbItemElement)

        self.entityInfoHandler.updateEntityInfo(session, dbItemElement.entityInfo, lastModifiedUserId,
                                                               ownerUserId, ownerGroupId, isGroupWriteable)

        if containedItemId != -1:
            if containedItemId is None:
                dbItemElement.containedItem = None
            else:
                dbItemElement.containedItem = self.getItemById(session, containedItemId)

        if name is not None:
            dbItemElement.name = name

        if isRequired != -1:
            dbItemElement.is_required = isRequired

        if description is not None:
            dbItemElement.description = description

        try:
            session.add(dbItemElement)
            session.flush()
        except OperationalError, err:
            raise DbError(err.message)

        self.logger.debug('Updated item Element id %s' % dbItemElement.id)

        return dbItemElement

    def addItemSource(self, session, itemId, sourceName, partNumber, cost, description, isVendor, isManufacturer, contactInfo, url):
        dbItem = self.getItemById(session, itemId)
        dbSource = self.sourceHandler.findSourceByName(session, sourceName)

        dbItemSource = ItemSource()
        dbItemSource.item = dbItem
        dbItemSource.source = dbSource
        if partNumber:
            dbItemSource.part_number = partNumber
        if cost:
            dbItemSource.cost = cost
        if description:
            dbItemSource.description = description
        if isVendor:
            dbItemSource.is_vendor = isVendor
        if isManufacturer:
            dbItemSource.is_manufacturer = isManufacturer
        if contactInfo:
            dbItemSource.contact_info = contactInfo
        if url:
            dbItemSource.url = url

        session.add(dbItemSource)
        session.flush()
        self.logger.debug('Inserted item source %s' % dbItemSource.id)

        return dbItemSource

    def addItemItemCategory(self, session, itemId, itemCategoryName):
        dbItem = self.getItemById(session, itemId)
        domainId = dbItem.domain.id

        dbCategory = self.getItemCategoryByName(session, itemCategoryName, domainId)

        dbItemItemCategory = ItemItemCategory()
        dbItemItemCategory.item = dbItem
        dbItemItemCategory.category = dbCategory

        session.add(dbItemItemCategory)
        session.flush()

        self.logger.debug('Added category %s for item id %s' % (itemCategoryName, itemId))

        return dbItemItemCategory

    def addItemItemProject(self, session, itemId, itemProjectName):
        dbItem = self.getItemById(session, itemId)

        dbProject = self.getItemProjectByName(session, itemProjectName)

        dbItemItemProject = ItemItemProject()
        dbItemItemProject.item = dbItem
        dbItemItemProject.project = dbProject

        session.add(dbItemItemProject)
        session.flush()

        self.logger.debug('Added category %s for item id %s' % (itemProjectName, itemId))

        return dbItemItemProject

    def addItemItemType(self, session, itemId, itemTypeName):
        dbItem = self.getItemById(session, itemId)
        domainId = dbItem.domain.id

        dbType = self.getItemTypeByName(session, itemTypeName, domainId)

        dbItemItemType = ItemItemType()
        dbItemItemType.item = dbItem
        dbItemItemType.type = dbType

        session.add(dbItemItemType)

        session.flush()
        self.logger.debug('Added type %s for item id %s' % (itemTypeName, itemId))
        return dbItemItemType

    def addItemElementLog(self, session, itemElementId, text, enteredByUserId, effectiveFromDateTime, effectiveToDateTime, logTopicName, enteredOnDateTime = None, systemLogLevelName = None):
        dbItemElement = self.getItemElementById(session, itemElementId)
        self.permissionHandler.verifyPermissionsForWriteToItemElement(session, enteredByUserId, dbItemElementObject=dbItemElement)
        dbLog = self.logHandler.addLog(session, text, enteredByUserId, effectiveFromDateTime, effectiveToDateTime, logTopicName, enteredOnDateTime)

        if systemLogLevelName is not None:
            self.logHandler.addSystemLog(session, dbLog, systemLogLevelName)

        dbItemElementLog = ItemElementLog()
        dbItemElementLog.itemElement = dbItemElement
        dbItemElementLog.log = dbLog

        entityInfo = dbItemElement.entityInfo
        dbItemElement.entityInfo = self.entityInfoHandler.updateEntityInfo(session, entityInfo, enteredByUserId)

        session.add(dbItemElement)
        session.add(dbItemElementLog)
        session.flush()
        self.logger.debug('Added log for itemElement id %s' % (itemElementId))
        return dbItemElementLog

    def addItemElementProperty(self, session, itemElementId, propertyTypeName, tag, value, units, description, enteredByUserId, isUserWriteable, isDynamic, displayValue, targetValue, enteredOnDateTime = None):
        dbItemElement = self.getItemElementById(session, itemElementId)
        self.permissionHandler.verifyPermissionsForWriteToItemElement(session, enteredByUserId, dbItemElementObject=dbItemElement)
        dbPropertyValue = self.propertyValueHandler.createPropertyValue(session, propertyTypeName, tag, value, units, description, enteredByUserId, isUserWriteable, isDynamic, displayValue,targetValue, enteredOnDateTime)

        session.add(dbPropertyValue)

        dbItemElementProperty = ItemElementProperty()
        dbItemElementProperty.itemElement = dbItemElement
        dbItemElementProperty.propertyValue = dbPropertyValue

        entityInfo = dbItemElement.entityInfo
        dbItemElement.entityInfo = self.entityInfoHandler.updateEntityInfo(session, entityInfo, enteredByUserId)

        session.add(dbItemElement)
        session.add(dbItemElementProperty)

        session.flush()

        self.logger.debug('Added property value (type: %s) for item element id %s' % (propertyTypeName, itemElementId))
        return dbItemElementProperty

    def addItemElementRelationship(self, session, firstItemElementId, secondItemElementId, firstItemConnectorId, secondItemConnectorId,
                                   linkItemElementId, relationshipTypeName, relationshipDetails, resourceTypeName, label, description ):
        if firstItemElementId is None:
            raise InvalidArgument("First item element Id must be specified for a item element relationship")

        firstItemElement = self.getItemElementById(session, firstItemElementId)

        dbItemElementRelationship = ItemElementRelationship()
        dbItemElementRelationship.firstItemElement = firstItemElement

        if secondItemElementId is not None:
            secondItemElement = self.getItemElementById(session, secondItemElementId)
            dbItemElementRelationship.secondItemElement = secondItemElement

        relationshipType = self.relationshipTypeHandler.getRelationshipTypeByName(session, relationshipTypeName)
        dbItemElementRelationship.relationshipType = relationshipType
        dbItemElementRelationship.relationship_details = relationshipDetails
        dbItemElementRelationship.label = label
        dbItemElementRelationship.description = description

        if firstItemConnectorId:
            dbItemElementRelationship.firstItemConnector = self.getItemConnectorById(firstItemConnectorId)
        if secondItemConnectorId:
            dbItemElementRelationship.secondItemConnector = self.getItemConnectorById(secondItemConnectorId)
        if linkItemElementId:
            dbItemElementRelationship.linkItemElement = self.getItemElementById(linkItemElementId)
        if resourceTypeName:
            dbItemElementRelationship.resourceType = self.resourceTypeHandler.getResourceTypeByName(session, resourceTypeName)

        session.add(dbItemElementRelationship)
        session.flush()

        entityDisplayName = self._getEntityDisplayName(ItemElementRelationship)
        self.logger.debug('Inserted %s %s' % (entityDisplayName, dbItemElementRelationship.id))

        return dbItemElementRelationship

    def addItemElementRelationshipHistory(self, session, itemRelationshipId, firstItemElementId, secondItemElementId, firstItemConnectorId, secondItemConnectorId,
                                   linkItemElementId, relationshipDetails, resourceTypeName, label, description, enteredByUserId, enteredOnDateTime ):
        firstItemElement = self.getItemElementById(session, firstItemElementId)
        secondItemElement = self.getItemElementById(session, secondItemElementId)
        dbItemElementRelationship = self.getItemElementRelationshipById(session, itemRelationshipId)

        dbItemElementRelationshipHistory = ItemElementRelationshipHistory()
        dbItemElementRelationshipHistory.firstItemElement = firstItemElement
        dbItemElementRelationshipHistory.secondItemElement = secondItemElement
        dbItemElementRelationshipHistory.itemElementRelationship = dbItemElementRelationship
        dbItemElementRelationshipHistory.relationship_details = relationshipDetails
        dbItemElementRelationshipHistory.label = label
        dbItemElementRelationshipHistory.description = description
        dbItemElementRelationshipHistory.enteredByUserInfo = self.userInfoHandler.getUserInfoById(session, enteredByUserId)
        dbItemElementRelationshipHistory.entered_on_date_time = enteredOnDateTime

        if firstItemConnectorId:
            dbItemElementRelationshipHistory.firstItemConnector = self.getItemConnectorById(firstItemConnectorId)
        if secondItemConnectorId:
            dbItemElementRelationshipHistory.secondItemConnector = self.getItemConnectorById(secondItemConnectorId)
        if linkItemElementId:
            dbItemElementRelationshipHistory.linkItemElement = self.getItemElementById(linkItemElementId)
        if resourceTypeName:
            dbItemElementRelationshipHistory.resourceType = self.resourceTypeHandler.getResourceTypeByName(session, resourceTypeName)

        session.add(dbItemElementRelationshipHistory)
        session.flush()

        entityDisplayName = self._getEntityDisplayName(ItemElementRelationshipHistory)
        self.logger.debug('Inserted %s %s' % (entityDisplayName, dbItemElementRelationshipHistory.id))

        return dbItemElementRelationshipHistory

    def getParentItems(self, session, itemId):
        query = session.query(Item)\
            .join(ItemElement.parentItem)\
            .filter(ItemElement.contained_item_id == itemId)

        dbItems = query.all()
        return dbItems







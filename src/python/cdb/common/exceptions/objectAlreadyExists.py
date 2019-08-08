#!/usr/bin/env python

#
# Object already exists error class.
#

#######################################################################

from cdb.common.constants import cdbStatus 
from cdb.common.exceptions.cdbException import CdbException

#######################################################################

class ObjectAlreadyExists(CdbException):
    def __init__ (self, error='', **kwargs):
        CdbException.__init__(self, error, cdbStatus.CDB_OBJECT_ALREADY_EXISTS, **kwargs)

#!/usr/bin/python
#
# Copyright (C) 2008 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Contains extensions to ElementWrapper objects used with Google Contacts."""

__author__ = 'dbrattli (Dag Brattli)'


import atom
import gdata


## Constants from http://code.google.com/apis/gdata/elements.html ##
REL_HOME = 'http://schemas.google.com/g/2005#home'
REL_WORK = 'http://schemas.google.com/g/2005#work'
REL_OTHER = 'http://schemas.google.com/g/2005#other'


IM_AIM = 'http://schemas.google.com/g/2005#AIM' # AOL Instant Messenger protocol
IM_MSN = 'http://schemas.google.com/g/2005#MSN' # MSN Messenger protocol
IM_YAHOO = 'http://schemas.google.com/g/2005#YAHOO' # Yahoo Messenger protocol
IM_SKYPE = 'http://schemas.google.com/g/2005#SKYPE' # Skype protocol
IM_QQ = 'http://schemas.google.com/g/2005#QQ' # QQ protocol
# Google Talk protocol
IM_GOOGLE_TALK = 'http://schemas.google.com/g/2005#GOOGLE_TALK'
IM_ICQ = 'http://schemas.google.com/g/2005#ICQ' # ICQ protocol
IM_JABBER = 'http://schemas.google.com/g/2005#JABBER' # Jabber protocol


PHOTO_LINK_REL = 'http://schemas.google.com/contacts/2008/rel#photo'
PHOTO_EDIT_LINK_REL = 'http://schemas.google.com/contacts/2008/rel#edit-photo'


PHONE_CAR = 'http://schemas.google.com/g/2005#car' #  Number of a car phone.
PHONE_FAX = 'http://schemas.google.com/g/2005#fax'
# Unknown or unspecified type, such as a business phone number that doesn't
# belong to a particular person. 
PHONE_GENERAL = 'http://schemas.google.com/g/2005#general'
PHONE_HOME = REL_HOME
PHONE_HOME_FAX = 'http://schemas.google.com/g/2005#home_fax' 
# Phone number that makes sense only in a context known to the user (such as
# an enterprise PBX).
PHONE_INTERNAL = 'http://schemas.google.com/g/2005#internal-extension'
PHONE_MOBILE = 'http://schemas.google.com/g/2005#mobile' 
# A special type of number for which no other rel value makes sense. 
# For example, a TTY device. label can be used to indicate the actual type.
PHONE_OTHER = REL_OTHER
PHONE_PAGER = 'http://schemas.google.com/g/2005#pager'
PHONE_SATELLITE = 'http://schemas.google.com/g/2005#satellite'
PHONE_VOIP = 'http://schemas.google.com/g/2005#voip'
PHONE_WORK = REL_WORK
PHONE_WORK_FAX = 'http://schemas.google.com/g/2005#work_fax'


CONTACTS_NAMESPACE = 'http://schemas.google.com/contact/2008'


class OrgName(atom.AtomBase):
  _tag = 'orgName'
  _namespace = gdata.GDATA_NAMESPACE
  _children = atom.AtomBase._children.copy()
  _attributes = atom.AtomBase._attributes.copy()

  def __init__(self, text=None, 
      extension_elements=None, extension_attributes=None):
    self.text = text
    self.extension_elements = extension_elements or []
    self.extension_attributes = extension_attributes or {}


class OrgTitle(atom.AtomBase):
  _tag = 'orgTitle'
  _namespace = gdata.GDATA_NAMESPACE
  _children = atom.AtomBase._children.copy()
  _attributes = atom.AtomBase._attributes.copy()

  def __init__(self, text=None, 
      extension_elements=None, extension_attributes=None):
    self.text = text
    self.extension_elements = extension_elements or []
    self.extension_attributes = extension_attributes or {}


class Organization(atom.AtomBase):
  _tag = 'organization'
  _namespace = gdata.GDATA_NAMESPACE
  _children = atom.AtomBase._children.copy()
  _attributes = atom.AtomBase._attributes.copy()

  _attributes['rel'] = 'rel'
  _attributes['label'] = 'label'
  _attributes['primary'] = 'primary'
  
  _children['{%s}orgName' % gdata.GDATA_NAMESPACE] = ('org_name', OrgName)
  _children['{%s}orgTitle' % gdata.GDATA_NAMESPACE] = ('org_title', OrgTitle)

  def __init__(self, rel=None, primary='false', org_name=None, org_title=None, 
      label=None, text=None, extension_elements=None, 
      extension_attributes=None):
    self.rel = rel or REL_OTHER
    self.primary = primary
    self.org_name = org_name
    self.org_title = org_title
    self.label = label
    self.text = text
    self.extension_elements = extension_elements or []
    self.extension_attributes = extension_attributes or {}


class PostalAddress(atom.AtomBase):
  _tag = 'postalAddress'
  _namespace = gdata.GDATA_NAMESPACE
  _children = atom.AtomBase._children.copy()
  _attributes = atom.AtomBase._attributes.copy()

  _attributes['primary'] = 'primary'
  _attributes['rel'] = 'rel'

  def __init__(self, primary=None, rel=None, text=None, 
      extension_elements=None, extension_attributes=None):
    self.primary = primary
    self.rel = rel or REL_OTHER
    self.text = text
    self.extension_elements = extension_elements or []
    self.extension_attributes = extension_attributes or {}


class IM(atom.AtomBase):
  _tag = 'im'
  _namespace = gdata.GDATA_NAMESPACE
  _children = atom.AtomBase._children.copy()
  _attributes = atom.AtomBase._attributes.copy()

  _attributes['address'] = 'address'
  _attributes['primary'] = 'primary'
  _attributes['protocol'] = 'protocol'
  _attributes['label'] = 'label'
  _attributes['rel'] = 'rel'

  def __init__(self, primary=None, rel=None, address=None, protocol=None,
      label=None, text=None, extension_elements=None, 
      extension_attributes=None):
    self.protocol = protocol
    self.address = address
    self.primary = primary
    self.rel = rel or REL_OTHER
    self.label = label
    self.text = text
    self.extension_elements = extension_elements or []
    self.extension_attributes = extension_attributes or {}


class Email(atom.AtomBase):
  _tag = 'email'
  _namespace = gdata.GDATA_NAMESPACE
  _children = atom.AtomBase._children.copy()
  _attributes = atom.AtomBase._attributes.copy()

  _attributes['address'] = 'address'
  _attributes['primary'] = 'primary'
  _attributes['rel'] = 'rel'
  _attributes['label'] = 'label'

  def __init__(self, primary=None, rel=None, address=None, text=None, 
      label=None, extension_elements=None, extension_attributes=None):
    self.address = address
    self.primary = primary
    self.rel = rel or REL_OTHER
    self.label = label
    self.text = text
    self.extension_elements = extension_elements or []
    self.extension_attributes = extension_attributes or {}


class PhoneNumber(atom.AtomBase):
  _tag = 'phoneNumber'
  _namespace = gdata.GDATA_NAMESPACE
  _children = atom.AtomBase._children.copy()
  _attributes = atom.AtomBase._attributes.copy()

  _attributes['primary'] = 'primary'
  _attributes['rel'] = 'rel'

  def __init__(self, primary=None, rel=None, text=None, 
      extension_elements=None, extension_attributes=None):
    self.primary = primary
    self.rel = rel or REL_OTHER
    self.text = text
    self.extension_elements = extension_elements or []
    self.extension_attributes = extension_attributes or {}


class Deleted(atom.AtomBase):
  _tag = 'deleted'
  _namespace = gdata.GDATA_NAMESPACE

  def __init__(self, text=None, 
      extension_elements=None, extension_attributes=None):
    self.text = text
    self.extension_elements = extension_elements or []
    self.extension_attributes = extension_attributes or {}


class GroupMembershipInfo(atom.AtomBase):
  _tag = 'groupMembershipInfo'
  _namespace = CONTACTS_NAMESPACE
  _attributes = atom.AtomBase._attributes.copy()

  _attributes['deleted'] = 'deleted'
  _attributes['href'] = 'href'

  def __init__(self, deleted=None, href=None, text=None,
      extension_elements=None, extension_attributes=None):
    self.deleted = deleted
    self.href = href
    self.text = text
    self.extension_elements = extension_elements or []
    self.extension_attributes = extension_attributes or {}


class Birthday(atom.AtomBase):
  _tag = 'birthday'
  _namespace = CONTACTS_NAMESPACE
  _attributes = atom.AtomBase._attributes.copy()

  _attributes['when'] = 'when'

  def __init__(self, when=None, text=None, extension_elements=None, 
      extension_attributes=None):
    self.when = when
    self.text = text
    self.extension_elements = extension_elements or []
    self.extension_attributes = extension_attributes or {}


class ContactEntry(gdata.BatchEntry):
  """A Google Contact flavor of an Atom Entry """

  _children = gdata.BatchEntry._children.copy()

  _children['{%s}postalAddress' % gdata.GDATA_NAMESPACE] = ('postal_address',
      [PostalAddress])
  _children['{%s}phoneNumber' % gdata.GDATA_NAMESPACE] = ('phone_number',
      [PhoneNumber])
  _children['{%s}organization' % gdata.GDATA_NAMESPACE] = ('organization',
      Organization)
  _children['{%s}email' % gdata.GDATA_NAMESPACE] = ('email', [Email])
  _children['{%s}im' % gdata.GDATA_NAMESPACE] = ('im', [IM])
  _children['{%s}deleted' % gdata.GDATA_NAMESPACE] = ('deleted', Deleted)
  _children['{%s}groupMembershipInfo' % CONTACTS_NAMESPACE] = (
      'group_membership_info', [GroupMembershipInfo])
  _children['{%s}extendedProperty' % gdata.GDATA_NAMESPACE] = (
      'extended_property', [gdata.ExtendedProperty])
  _children['{%s}birthday' % CONTACTS_NAMESPACE] = ('birthday', Birthday)
  
  def __init__(self, author=None, category=None, content=None,
      atom_id=None, link=None, published=None, 
      title=None, updated=None, email=None, postal_address=None, 
      deleted=None, organization=None, phone_number=None, im=None,
      extended_property=None, group_membership_info=None, birthday=None,
      batch_operation=None, batch_id=None, batch_status=None,
      extension_elements=None, extension_attributes=None, text=None):
    gdata.BatchEntry.__init__(self, author=author, category=category, 
        content=content, atom_id=atom_id, link=link, published=published,
        batch_operation=batch_operation, batch_id=batch_id, 
        batch_status=batch_status, title=title, updated=updated)
    self.organization = organization
    self.deleted = deleted
    self.phone_number = phone_number or []
    self.postal_address = postal_address or []
    self.im = im or []  
    self.extended_property = extended_property or []
    self.email = email or []
    self.group_membership_info = group_membership_info or []
    self.birthday = birthday
    self.text = text
    self.extension_elements = extension_elements or []
    self.extension_attributes = extension_attributes or {}

  def GetPhotoLink(self):
    for a_link in self.link:
      if a_link.rel == PHOTO_LINK_REL:
        return a_link
    return None

  def GetPhotoEditLink(self):
    for a_link in self.link:
      if a_link.rel == PHOTO_EDIT_LINK_REL:
        return a_link
    return None


def ContactEntryFromString(xml_string):
  return atom.CreateClassFromXMLString(ContactEntry, xml_string)


class ContactsFeed(gdata.BatchFeed, gdata.LinkFinder):
  """A Google Contacts feed flavor of an Atom Feed"""

  _children = gdata.BatchFeed._children.copy()

  _children['{%s}entry' % atom.ATOM_NAMESPACE] = ('entry', [ContactEntry])

  def __init__(self, author=None, category=None, contributor=None,
               generator=None, icon=None, atom_id=None, link=None, logo=None, 
               rights=None, subtitle=None, title=None, updated=None,
               entry=None, total_results=None, start_index=None,
               items_per_page=None, extension_elements=None,
               extension_attributes=None, text=None):
    gdata.BatchFeed.__init__(self, author=author, category=category,
                             contributor=contributor, generator=generator,
                             icon=icon,  atom_id=atom_id, link=link,
                             logo=logo, rights=rights, subtitle=subtitle,
                             title=title, updated=updated, entry=entry,
                             total_results=total_results,
                             start_index=start_index,
                             items_per_page=items_per_page,
                             extension_elements=extension_elements,
                             extension_attributes=extension_attributes,
                             text=text)
                             

def ContactsFeedFromString(xml_string):
  return atom.CreateClassFromXMLString(ContactsFeed, xml_string)


class GroupEntry(gdata.BatchEntry):
  """Represents a contact group."""
  _children = gdata.BatchEntry._children.copy()
  _children['{%s}extendedProperty' % gdata.GDATA_NAMESPACE] = (
      'extended_property', [gdata.ExtendedProperty])

  def __init__(self, author=None, category=None, content=None,
      contributor=None, atom_id=None, link=None, published=None, rights=None,
      source=None, summary=None, control=None, title=None, updated=None,
      extended_property=None, batch_operation=None, batch_id=None, 
      batch_status=None, 
      extension_elements=None, extension_attributes=None, text=None):
    gdata.BatchEntry.__init__(self, author=author, category=category, 
                        content=content,
                        atom_id=atom_id, link=link, published=published,
                        batch_operation=batch_operation, batch_id=batch_id, 
                        batch_status=batch_status,
                        title=title, updated=updated)
    self.extended_property = extended_property or []


def GroupEntryFromString(xml_string):
  return atom.CreateClassFromXMLString(GroupEntry, xml_string)


class GroupsFeed(gdata.BatchFeed):
  """A Google contact groups feed flavor of an Atom Feed"""
  _children = gdata.BatchFeed._children.copy()
  _children['{%s}entry' % atom.ATOM_NAMESPACE] = ('entry', [GroupEntry])


def GroupsFeedFromString(xml_string):
  return atom.CreateClassFromXMLString(GroupsFeed, xml_string)

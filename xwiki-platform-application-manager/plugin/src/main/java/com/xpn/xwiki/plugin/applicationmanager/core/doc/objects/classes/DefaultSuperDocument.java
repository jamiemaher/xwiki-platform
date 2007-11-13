/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes;

import java.util.List;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Default implementation of SuperDocument. This class manage an XWiki document containing provided
 * XWiki class. It add some specifics methods, getters and setters for this type of object and
 * fields. It also override {@link Document} (and then {@link XWikiDocument}) isNew concept
 * considering as new a document that does not contains an XWiki object of the provided XWiki class.
 * 
 * @version $Id: $
 * @see SuperDocument
 * @see SuperClass
 */
public class DefaultSuperDocument extends Document implements SuperDocument
{
    /**
     * Value in int for {@link Boolean#TRUE}.
     */
    private static final int BOOLEANFIELD_TRUE = 1;

    /**
     * Value in int for {@link Boolean#FALSE}.
     */
    private static final int BOOLEANFIELD_FALSE = 0;

    /**
     * Value in int for {@link Boolean} = null.
     */
    private static final int BOOLEANFIELD_MAYBE = 2;

    /**
     * The class manager for this document.
     */
    protected SuperClass sclass;

    /**
     * The id of the XWiki object included in the document to manage.
     */
    protected int objectId;

    /**
     * true if this is a new document of this class (this document can exist but does not contains
     * object of this class).
     */
    protected boolean isNew;

    /**
     * Create instance of DefaultSuperDocument from provided XWikiDocument.
     * 
     * @param sclass the class manager for this document.
     * @param xdoc the XWikiDocument to manage.
     * @param objectId the id of the XWiki object included in the document to manage.
     * @param context the XWiki context.
     * @throws XWikiException error when calling {@link #reload(XWikiContext)}.
     */
    public DefaultSuperDocument(SuperClass sclass, XWikiDocument xdoc, int objectId,
        XWikiContext context) throws XWikiException
    {
        super(xdoc, context);

        this.sclass = sclass;
        this.objectId = objectId;

        reload(context);
    }

    /**
     * @param docFullName modify the full name the the managed XWikiDocument.
     */
    public void setFullName(String docFullName)
    {
        getDoc().setFullName(docFullName, context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see SuperDocument#reload(com.xpn.xwiki.XWikiContext)
     */
    public void reload(XWikiContext context) throws XWikiException
    {
        if (this.getObjectNumbers(this.sclass.getClassFullName()) == 0) {
            if (this.objectId > 0) {
                throw new XWikiException(XWikiException.MODULE_XWIKI_DOC,
                    XWikiException.ERROR_XWIKI_DOES_NOT_EXIST,
                    "Object od id " + this.objectId + "does not exist");
            }

            BaseObject object = getDoc().newObject(this.sclass.getClassFullName(), context);

            XWikiDocument docTemplate =
                context.getWiki().getDocument(this.sclass.getClassTemplateFullName(), context);
            BaseObject templateObject = docTemplate.getObject(this.sclass.getClassFullName());

            if (templateObject != null) {
                object.merge(templateObject);
            }

            if (super.isNew()) {
                setParent(docTemplate.getParent());
                setContent(docTemplate.getContent());
            }

            this.isNew = true;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.SuperDocument#getDocumentApi()
     */
    public Document getDocumentApi()
    {
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.SuperDocument#getObjectId()
     */
    public int getObjectId()
    {
        return this.objectId;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.SuperDocument#getObjectApi()
     */
    public Object getObjectApi()
    {
        BaseObject obj = getBaseObject(false);

        return obj == null ? null : obj.newObjectApi(obj, context);
    }

    /**
     * Get the managed {@link BaseObject}.
     * 
     * @param toWrite indicate that the {@link BaseObject} will be modified.
     * @return the {@link BaseObject}.
     */
    protected BaseObject getBaseObject(boolean toWrite)
    {
        BaseObject obj;

        if (toWrite) {
            obj = getDoc().getObject(this.sclass.getClassFullName(), this.objectId);
        } else {
            obj = this.doc.getObject(this.sclass.getClassFullName(), this.objectId);
        }

        return obj;
    }

    /**
     * Merge two documents BaseObject.
     * 
     * @param sdoc the document to merge.
     */
    public void mergeObject(DefaultSuperDocument sdoc)
    {
        if (getSuperClass() != sdoc.getSuperClass()) {
            return;
        }

        getBaseObject(true).merge(sdoc.getBaseObject(false));
    }

    /**
     * {@inheritDoc}
     * 
     * @see SuperDocument#getSuperClass()
     */
    public SuperClass getSuperClass()
    {
        return this.sclass;
    }

    /**
     * {@inheritDoc}
     * 
     * @see SuperDocument#isNew()
     */
    public boolean isNew()
    {
        return super.isNew() || this.isNew;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.api.Document#saveDocument(String)
     */
    protected void saveDocument(String comment) throws XWikiException
    {
        super.saveDocument(comment);
        this.isNew = false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.api.Document#deleteDocument()
     */
    protected void deleteDocument() throws XWikiException
    {
        if (getObjectNumbers(sclass.getClassFullName()) == 1) {
            super.deleteDocument();
        } else {
            doc.removeObject(getBaseObject(false));
            save();
        }

        this.isNew = true;
    }

    /**
     * Get the value of the field <code>fieldName</code> of the managed object's class.
     * 
     * @param fieldName the name of the field from the managed object's class where to find the
     *            value.
     * @return the value in {@link String} of the field <code>fieldName</code> of the managed
     *         object's class.
     * @see com.xpn.xwiki.doc.XWikiDocument#getStringValue(java.lang.String)
     */
    public String getStringValue(String fieldName)
    {
        BaseObject obj = getBaseObject(false);

        if (obj == null) {
            return null;
        }

        return obj.getStringValue(fieldName);
    }

    /**
     * Modify the value of the field <code>fieldName</code> of the managed object's class.
     * 
     * @param fieldName the name of the field from the managed object's class where to find the
     *            value.
     * @param value the new value of the field <code>fieldName</code> of the managed object's
     *            class.
     * @see com.xpn.xwiki.doc.XWikiDocument#setStringValue(java.lang.String,java.lang.String,java.lang.String)
     */
    public void setStringValue(String fieldName, String value)
    {
        BaseObject obj = getBaseObject(true);

        if (obj != null) {
            obj.setStringValue(fieldName, value);
        }
    }

    /**
     * Get the value of the field <code>fieldName</code> of the managed object's class.
     * 
     * @param fieldName the name of the field from the managed object's class where to find the
     *            value.
     * @return the value in {@link String} of the field <code>fieldName</code> of the managed
     *         object's class.
     * @see com.xpn.xwiki.doc.XWikiDocument#getStringValue(java.lang.String)
     */
    public String getLargeStringValue(String fieldName)
    {
        BaseObject obj = getBaseObject(false);

        if (obj == null) {
            return null;
        }

        return obj.getLargeStringValue(fieldName);
    }

    /**
     * Modify the value of the field <code>fieldName</code> of the managed object's class.
     * 
     * @param fieldName the name of the field from the managed object's class where to find the
     *            value.
     * @param value the new value of the field <code>fieldName</code> of the managed object's
     *            class.
     * @see com.xpn.xwiki.doc.XWikiDocument#setLargeStringValue(java.lang.String,java.lang.String,java.lang.String)
     */
    public void setLargeStringValue(String fieldName, String value)
    {
        BaseObject obj = getBaseObject(true);

        if (obj != null) {
            obj.setLargeStringValue(fieldName, value);
        }
    }

    /**
     * Get the value of the field <code>fieldName</code> of the managed object's class.
     * 
     * @param fieldName the name of the field from the managed object's class where to find the
     *            value.
     * @return the value in {@link List} of the field <code>fieldName</code> of the managed
     *         object's class.
     * @see com.xpn.xwiki.doc.XWikiDocument#getListValue(java.lang.String)
     */
    public List getListValue(String fieldName)
    {
        BaseObject obj = getBaseObject(false);

        if (obj == null) {
            return null;
        }

        return obj.getListValue(fieldName);
    }

    /**
     * Modify the value of the field <code>fieldName</code> of the managed object's class.
     * 
     * @param fieldName the name of the field from the managed object's class where to find the
     *            value.
     * @param value the new value of the field <code>fieldName</code> of the managed object's
     *            class.
     * @see com.xpn.xwiki.doc.XWikiDocument#setStringListValue(java.lang.String,java.lang.String,java.util.List)
     */
    public void setListValue(String fieldName, List value)
    {
        BaseObject obj = getBaseObject(true);

        if (obj != null) {
            obj.setStringListValue(fieldName, value);
        }
    }

    /**
     * Get the value of the field <code>fieldName</code> of the managed object's class.
     * 
     * @param fieldName the name of the field from the managed object's class where to find the
     *            value.
     * @return the value in int of the field <code>fieldName</code> of the managed object's class.
     * @see com.xpn.xwiki.doc.XWikiDocument#getListValue(java.lang.String)
     */
    public int getIntValue(String fieldName)
    {
        BaseObject obj = getBaseObject(false);

        if (obj == null) {
            return 0;
        }

        return obj.getIntValue(fieldName);
    }

    /**
     * Modify the value of the field <code>fieldName</code> of the managed object's class.
     * 
     * @param fieldName the name of the field from the managed object's class where to find the
     *            value.
     * @param value the new value of the field <code>fieldName</code> of the managed object's
     *            class.
     * @see com.xpn.xwiki.doc.XWikiDocument#setIntValue(String, String, int)
     */
    public void setIntValue(String fieldName, int value)
    {
        BaseObject obj = getBaseObject(true);

        if (obj != null) {
            obj.setIntValue(fieldName, value);
        }
    }

    /**
     * Get the value of the field <code>fieldName</code> of the managed object's class.
     * 
     * @param fieldName the name of the field from the managed object's class where to find the
     *            value.
     * @return the value in {@link Boolean} of the field <code>fieldName</code> of the managed
     *         object's class.
     * @see com.xpn.xwiki.doc.XWikiDocument#getListValue(java.lang.String)
     */
    public Boolean getBooleanValue(String fieldName)
    {
        int intValue = getIntValue(fieldName);

        return intValue == BOOLEANFIELD_TRUE ? Boolean.TRUE : (intValue == BOOLEANFIELD_FALSE
            ? Boolean.FALSE : null);
    }

    /**
     * Modify the value of the field <code>fieldName</code> of the managed object's class.
     * 
     * @param fieldName the name of the field from the managed object's class where to find the
     *            value.
     * @param value the new value of the field <code>fieldName</code> of the managed object's
     *            class.
     * @see com.xpn.xwiki.doc.XWikiDocument#setIntValue(String, String, int)
     */
    public void setBooleanValue(String fieldName, Boolean value)
    {
        setIntValue(fieldName, value == null ? BOOLEANFIELD_MAYBE : (value.booleanValue()
            ? BOOLEANFIELD_TRUE : BOOLEANFIELD_FALSE));
    }
}

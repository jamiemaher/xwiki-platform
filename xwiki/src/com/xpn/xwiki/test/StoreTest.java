/**
 * ===================================================================
 *
 * Copyright (c) 2003 Ludovic Dubost, All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details, published at
 * http://www.gnu.org/copyleft/lesser.html or in lesser.txt in the
 * root folder of this distribution.
 *
 * Created by
 * User: Ludovic Dubost
 * Date: 24 nov. 2003
 * Time: 10:55:50
 */
package com.xpn.xwiki.test;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocInterface;
import com.xpn.xwiki.doc.XWikiSimpleDoc;
import com.xpn.xwiki.store.XWikiStoreInterface;
import junit.framework.TestCase;
import org.apache.commons.jrcs.rcs.Version;

import java.io.File;
import java.io.IOException;
import java.util.List;


public abstract class StoreTest extends TestCase {

    public abstract XWikiStoreInterface getStore();

    public void testStandardReadWrite(XWikiStoreInterface store, String web, String name) throws XWikiException {
        XWikiSimpleDoc doc1 = new XWikiSimpleDoc(web, name);
        doc1.setContent(Utils.content1);
        doc1.setAuthor(Utils.author);
        doc1.setParent(Utils.parent);
        store.saveXWikiDoc(doc1);
        XWikiSimpleDoc doc2 = new XWikiSimpleDoc(web, name);
        doc2 = (XWikiSimpleDoc) store.loadXWikiDoc(doc2);
        String content2 = doc2.getContent();
        assertEquals(Utils.content1,content2);
        assertEquals(doc2.getVersion(), Utils.version);
        assertEquals(doc2.getParent(), Utils.parent);
        assertEquals(doc2.getAuthor(), Utils.author);
        doc2.setContent(Utils.content3);
        doc2.setAuthor(Utils.author2);
        store.saveXWikiDoc(doc2);
        XWikiSimpleDoc doc3 = new XWikiSimpleDoc(web, name);
        doc3 = (XWikiSimpleDoc) store.loadXWikiDoc(doc3);
        String content3b = doc3.getContent();
        assertEquals(Utils.content3,content3b);
        assertEquals(doc3.getAuthor(), Utils.author2);
        assertEquals(doc3.getVersion(), Utils.version2);
    }



    public void testVersionedReadWrite(XWikiStoreInterface store,String web, String name) throws XWikiException {
        XWikiSimpleDoc doc3 = new XWikiSimpleDoc(web, name);
        doc3 = (XWikiSimpleDoc) store.loadXWikiDoc(doc3);
        XWikiDocInterface doc4 = store.loadXWikiDoc(doc3,Utils.version);
        String content4 = doc4.getContent();
        assertEquals(Utils.content1,content4);
        assertEquals(doc4.getVersion(),Utils.version);
        assertEquals(doc4.getAuthor(), Utils.author);
        Version[] versions = store.getXWikiDocVersions(doc4);
        assertTrue(versions.length==2);
    }



    public void testStandardReadWrite() throws XWikiException {
        Utils.setStandardData();
        XWikiStoreInterface store = getStore();
        testStandardReadWrite(store, Utils.web, Utils.name);
    }

    public void testVersionedReadWrite() throws XWikiException {
        Utils.setStandardData();
        XWikiStoreInterface store = getStore();
        testStandardReadWrite(store, Utils.web, Utils.name);
        testVersionedReadWrite(store, Utils.web, Utils.name);
    }

    public void testMediumReadWrite() throws XWikiException {
        Utils.setMediumData();
        XWikiStoreInterface store = getStore();
        testStandardReadWrite(store, Utils.web, Utils.name);
        testVersionedReadWrite(store, Utils.web, Utils.name);
    }

    public void testBigVersionedReadWrite() throws XWikiException, IOException {
        Utils.setBigData();
        XWikiStoreInterface store = getStore();
        testStandardReadWrite(store, Utils.web, Utils.name2);
        testVersionedReadWrite(store, Utils.web, Utils.name2);
    }


    public void testAttachmentReadWrite(XWikiStoreInterface store, String web, String name) throws XWikiException, IOException {
        XWikiSimpleDoc doc1 = new XWikiSimpleDoc(web, name);
        doc1.setContent(Utils.content1);
        doc1.setAuthor(Utils.author);
        doc1.setParent(Utils.parent);
        store.saveXWikiDoc(doc1);
        XWikiAttachment attachment1 = new XWikiAttachment(doc1, Utils.filename);
        byte[] attachcontent1 = Utils.getDataAsBytes(new File(Utils.filename));
        attachment1.setContent(attachcontent1);
        doc1.saveAttachmentContent(attachment1);
        doc1.getAttachmentList().add(attachment1);
        store.saveXWikiDoc(doc1);

        XWikiSimpleDoc doc2 = new XWikiSimpleDoc(web, name);
        doc2 = (XWikiSimpleDoc) store.loadXWikiDoc(doc2);
        List attachlist = doc2.getAttachmentList();
        assertEquals("Attachment is not listed", 1, attachlist.size());
        XWikiAttachment attachment2 = (XWikiAttachment) attachlist.get(0);
        assertEquals("Attachment version is not correct", "1.1", attachment2.getVersion());
        byte[] attachcontent2 = attachment2.getContent();
        assertEquals("Attachment content size is not correct", attachcontent1.length, attachcontent2.length);
        for (int i=0;i<attachcontent1.length;i++) {
            assertEquals("Attachment content byte " + i + " is not correct", attachcontent1[i], attachcontent2[i]);
        }
    }


    public void testAttachmentReadWrite() throws XWikiException, IOException {
            Utils.setStandardData();
            XWikiStoreInterface store = getStore();
            testAttachmentReadWrite(store, Utils.web, Utils.name);
        }

    public void testSecondAttachmentReadWrite(XWikiStoreInterface store, String web, String name) throws XWikiException, IOException {

        XWikiSimpleDoc doc3 = new XWikiSimpleDoc(web, name);
        doc3 = (XWikiSimpleDoc) store.loadXWikiDoc(doc3);
        XWikiAttachment attachment2 = new XWikiAttachment(doc3, Utils.filename2);
        byte[] attachcontent2 = Utils.getDataAsBytes(new File(Utils.filename2));
        attachment2.setContent(attachcontent2);
        doc3.saveAttachmentContent(attachment2);
        doc3.getAttachmentList().add(attachment2);
        store.saveXWikiDoc(doc3);

        XWikiSimpleDoc doc4 = new XWikiSimpleDoc(web, name);
        doc4 = (XWikiSimpleDoc) store.loadXWikiDoc(doc4);
        List attachlist = doc4.getAttachmentList();
        assertEquals("Attachment is not listed", 2, attachlist.size());
        XWikiAttachment attachment4 = (XWikiAttachment) attachlist.get(1);
        assertEquals("Attachment version is not correct", "1.1", attachment4.getVersion());
        byte[] attachcontent4 = attachment4.getContent();
        assertEquals("Attachment content size is not correct", attachcontent2.length, attachcontent4.length);
        for (int i=0;i<attachcontent2.length;i++) {
            assertEquals("Attachment content byte " + i + " is not correct", attachcontent2[i], attachcontent4[i]);
        }
    }

        public void testSecondAttachmentReadWrite() throws XWikiException, IOException {
            Utils.setStandardData();
            XWikiStoreInterface store = getStore();
            testAttachmentReadWrite(store, Utils.web, Utils.name);
            testSecondAttachmentReadWrite(store, Utils.web, Utils.name);
        }


    public void testUpdateAttachmentReadWrite(XWikiStoreInterface store, String web, String name) throws XWikiException, IOException {

        XWikiSimpleDoc doc3 = new XWikiSimpleDoc(web, name);
        doc3 = (XWikiSimpleDoc) store.loadXWikiDoc(doc3);
        List attachlist = doc3.getAttachmentList();
        XWikiAttachment attachment3 = (XWikiAttachment) attachlist.get(0);
        byte[] attachcontent3 = Utils.getDataAsBytes(new File(Utils.filename2));
        attachment3.setContent(attachcontent3);
        doc3.saveAttachmentContent(attachment3);
        store.saveXWikiDoc(doc3);

        XWikiSimpleDoc doc4 = new XWikiSimpleDoc(web, name);
        doc4 = (XWikiSimpleDoc) store.loadXWikiDoc(doc4);
        attachlist = doc4.getAttachmentList();
        assertEquals("Attachment is not listed", 1, attachlist.size());
        XWikiAttachment attachment4 = (XWikiAttachment) attachlist.get(0);
        assertEquals("Attachment version is not correct", "1.2", attachment4.getVersion());
        byte[] attachcontent4 = attachment4.getContent();
        assertEquals("Attachment content size is not correct", attachcontent3.length, attachcontent4.length);
        for (int i=0;i<attachcontent3.length;i++) {
            assertEquals("Attachment content byte " + i + " is not correct", attachcontent3[i], attachcontent4[i]);
        }
    }

        public void testUpdateAttachmentReadWrite() throws XWikiException, IOException {
            Utils.setStandardData();
            XWikiStoreInterface store = getStore();
            testAttachmentReadWrite(store, Utils.web, Utils.name);
            testUpdateAttachmentReadWrite(store, Utils.web, Utils.name);
        }


}

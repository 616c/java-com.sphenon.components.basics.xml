package com.sphenon.basics.xml;                    

/****************************************************************************
  Copyright 2001-2024 Sphenon GmbH

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations
  under the License.
*****************************************************************************/

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DOMParserLineInfo extends DOMParser  {

    private XMLLocator locator; 

    public DOMParserLineInfo() {
        try {                        
            this.setFeature( "http://apache.org/xml/features/dom/defer-node-expansion", false ); 
        } catch (org.xml.sax.SAXException se) {
        }
    }
    
    public void startElement(QName elementQName, XMLAttributes attrList, Augmentations augs) throws XNIException {
        super.startElement(elementQName, attrList, augs);
        Node node = null;
        try {
            node = (Node) this.getProperty( "http://apache.org/xml/properties/dom/current-element-node" );
        } catch (org.xml.sax.SAXException se) {
        }
        if (node != null) {
            node.setUserData( "line", String.valueOf(locator.getLineNumber()), null);
        }
    }
    
    public void startDocument(XMLLocator locator, String encoding, NamespaceContext namespaceContext, Augmentations augs) throws XNIException {
        super.startDocument(locator, encoding, namespaceContext, augs);
        this.locator = locator;
        Node node = null ;
        try {
            node = (Node) this.getProperty( "http://apache.org/xml/properties/dom/current-element-node" );
        } catch(org.xml.sax.SAXException se) {
        }
        if (node != null) {
            node.setUserData( "line", String.valueOf(locator.getLineNumber()), null);
        }
    }
}

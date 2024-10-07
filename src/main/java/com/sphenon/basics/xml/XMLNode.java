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

import com.sphenon.basics.context.*;
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.configuration.*;
import com.sphenon.basics.message.*;
import com.sphenon.basics.exception.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.customary.*;
import com.sphenon.basics.expression.*;
import com.sphenon.basics.many.*;
import com.sphenon.basics.data.*;
import com.sphenon.basics.graph.*;

import com.sphenon.basics.xml.returncodes.*;

import org.apache.xerces.xni.parser.XMLInputSource;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import javax.xml.XMLConstants;

import org.w3c.dom.ls.LSOutput;

import org.w3c.dom.xpath.XPathResult;
import org.w3c.dom.xpath.XPathEvaluator;
// import org.apache.xpath.domapi.XPathEvaluatorImpl;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

public class XMLNode implements GenericIterable {
    static protected long notification_level;
    static public    long adjustNotificationLevel(long new_level) { long old_level = notification_level; notification_level = new_level; return old_level; }
    static public    long getNotificationLevel() { return notification_level; }
    static { notification_level = NotificationLocationContext.getLevel(RootContext.getInitialisationContext(), "com.sphenon.basics.xml.XMLNode"); };

    protected CallContext creation_context;
    protected List<Node> nodes;
    protected List<XMLNode> xml_nodes;

    public XMLNode (CallContext context) {
        this.creation_context = context;
        nodes = null;
    }

    public XMLNode (CallContext context, Node node) {
        this.creation_context = context;
        nodes = new ArrayList<Node>();
        nodes.add(node);
    }

    public XMLNode (CallContext context, List<Node> nodes) {
        this.creation_context = context;
        this.nodes = nodes;
    }

    static public XMLNode createXMLNode(CallContext context, String uri) throws InvalidXML {
        return new XMLNode(context, XMLUtil.parse(context, new XMLInputSource(null, uri, null), notification_level, uri));
    }

    static public XMLNode createXMLNode(CallContext context, TreeLeaf tree_leaf) throws InvalidXML {
        Data_MediaObject data = ((Data_MediaObject)(((NodeContent_Data)(tree_leaf.getContent(context))).getData(context)));
        return createXMLNode(context, data instanceof Data_MediaObject_File ? new XMLInputSource(null, ((Data_MediaObject_File)(data)).getCurrentFile(context).getPath(), null) : new XMLInputSource(null, null, null, data.getStream(context), null), data.getDispositionFilename(context));
    }

    static public XMLNode createXMLNode(CallContext context, Data_MediaObject data) throws InvalidXML {
        return createXMLNode(context, data instanceof Data_MediaObject_File ? new XMLInputSource(null, ((Data_MediaObject_File)(data)).getCurrentFile(context).getPath(), null) : new XMLInputSource(null, null, null, data.getStream(context), null), data.getDispositionFilename(context));
    }

    static public XMLNode createXMLNode(CallContext context, XMLInputSource xml_input_source, String resource_id) throws InvalidXML {
        return new XMLNode(context, XMLUtil.parse(context, xml_input_source, notification_level, resource_id));
    }

    static public XMLNode createXMLNode(CallContext context, String xml_string, String resource_id) throws InvalidXML {
        java.io.ByteArrayInputStream xml_bais = null;
        try {
            xml_bais = new java.io.ByteArrayInputStream(xml_string.getBytes("UTF8"));
            return createXMLNode(context, xml_bais, resource_id);
        } catch (java.io.UnsupportedEncodingException uee) {
            InvalidXML.createAndThrow(context, uee, "Cannot get UTF8 bytes '%(xml_string)'", "xml_string", xml_string);
            throw (InvalidXML) null; // compiler insists
        }
    }

    static public XMLNode createXMLNode(CallContext context, InputStream input_stream, String resource_id) throws InvalidXML {
        XMLNode xml_node = new XMLNode(context, XMLUtil.parse(context, new XMLInputSource(null, null, null, input_stream, null), notification_level, resource_id));
        try {
            input_stream.close();
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentFailure(context, ioe, "Could not close stream after parsing");
            throw (ExceptionEnvironmentFailure) null; // compiler insists
        }
        return xml_node;
    }

    static public XMLNode createXMLNode(CallContext context, File file) throws InvalidXML {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException fnfe) {
            CustomaryContext.create(Context.create(context)).throwPreConditionViolation(context, fnfe, "File '%(file)' does not exist (while creating XML node)", "file", file.getPath());
            throw (ExceptionPreConditionViolation) null; // compiler insists
        }
        XMLNode xml_node = new XMLNode(context, XMLUtil.parse(context, new XMLInputSource(null, null, null, new BufferedInputStream(fis), null), notification_level, file.getPath()));
        try {
            fis.close();
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentFailure(context, ioe, "Could not close stream after parsing");
            throw (ExceptionEnvironmentFailure) null; // compiler insists
        }
        return xml_node;
    }

    public List<XMLNode> getNodes(CallContext context) {
        if (this.xml_nodes == null) {
            this.xml_nodes = new ArrayList<XMLNode>();
            for (Node node : this.nodes) {
                this.xml_nodes.add(new XMLNode(context, node));
            }
        }
        return this.xml_nodes;
    }

    public List<Node> getDOMNodes(CallContext context) {
        return this.nodes;
    }

    public String getName(CallContext context) {
        return (nodes != null && nodes.size() == 1 ? nodes.get(0).getNodeName() : null);
    }

    public String getAttribute(CallContext context, String name) {
        return (nodes != null && nodes.size() == 1 && nodes.get(0) instanceof Element ? ((Element)(nodes.get(0))).getAttribute(name) : null);
    }

    public boolean exists(CallContext context) {
        return (nodes != null && nodes.size() != 0);
    }

    public String toString (CallContext context) {
        return XMLUtil.serialiseContent(context, this.nodes);
    }

    public String toString () {
        return toString(this.creation_context);
    }

    public String serialise(CallContext context) {
        return XMLUtil.serialise(context, this.nodes);
    }

    public String serialise(CallContext context, Writer writer) {
        return XMLUtil.serialise(context, this.nodes, writer);
    }

    public String serialise(CallContext context, TreeLeaf tree_leaf) {
        return XMLUtil.serialise(context, this.nodes, tree_leaf);
    }

    public String serialise(CallContext context, LSOutput ls_output) {
        return XMLUtil.serialise(context, this.nodes, ls_output);
    }

    public String serialise(CallContext context, boolean serialise_as_fragment) {
        return XMLUtil.serialise(context, this.nodes, serialise_as_fragment);
    }

    public String serialise(CallContext context, Writer writer, boolean serialise_as_fragment) {
        return XMLUtil.serialise(context, this.nodes, writer, serialise_as_fragment);
    }

    public String serialise(CallContext context, TreeLeaf tree_leaf, boolean serialise_as_fragment) {
        return XMLUtil.serialise(context, this.nodes, tree_leaf, serialise_as_fragment);
    }

    public String serialise(CallContext context, LSOutput ls_output, boolean serialise_as_fragment) {
        return XMLUtil.serialise(context, this.nodes, ls_output, serialise_as_fragment);
    }

    public String serialiseContent(CallContext context) {
        return XMLUtil.serialiseContent(context, this.nodes);
    }

    public String serialiseContent(CallContext context, Writer writer) {
        return XMLUtil.serialiseContent(context, this.nodes, writer);
    }

    public String serialiseContent(CallContext context, TreeLeaf tree_leaf) {
        return XMLUtil.serialiseContent(context, this.nodes, tree_leaf);
    }

    public String serialiseContent(CallContext context, LSOutput ls_output) {
        return XMLUtil.serialiseContent(context, this.nodes, ls_output);
    }

    protected void processTextNode(CallContext context, Node node, StringBuilder sb) {
        if (    node.getNodeType() == Node.TEXT_NODE
             || node.getNodeType() == Node.CDATA_SECTION_NODE
           ) {
            sb.append(((Text) node).getData());
            return;
        }
        if (    node.getNodeType() == Node.ATTRIBUTE_NODE
           ) {
            sb.append(((Attr) node).getNodeValue());
            return;
        }
        CustomaryContext.create((Context)context).throwConfigurationError(context, "XMLNode to render to text does contain DOM node not of type 'Text' (node '%(content)')", "content", this.toString(context));
        throw (ExceptionConfigurationError) null; // compiler insists
    }

    public String toText (CallContext context) {
        if (this.nodes == null) { return ""; }
        StringBuilder sb = new StringBuilder();
        for (Node node : nodes) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Node child = node.getFirstChild();
                while (child != null) {
                    processTextNode(context, child, sb);
                    child = child.getNextSibling();
                }
            } else {
                processTextNode(context, node, sb);
            }
        }
        return sb.toString();
    }

    protected Document owner_document;

    protected Document getOwnerDocument(CallContext context) {
        return owner_document != null ? owner_document : (owner_document = (nodes == null ? null : nodes.size() == 0 ? null : (nodes.get(0) instanceof Document ? ((Document) nodes.get(0)) : nodes.get(0).getOwnerDocument())));
    }

    public XMLNode getChilds(CallContext context) {
        List<Node> result_nodes = new ArrayList<Node>();
        
        for (Node node : this.nodes) {

            NodeList result = node.getChildNodes();

            for (int n=0; n<result.getLength(); n++) {
                result_nodes.add(result.item(n));
            }
        }

        return new XMLNode(context, result_nodes);
    }

    /**
      @param filter they match attributes if name is just name; xml:nsuri and xml:name as names
                    match the namespace uri and name
    */
    public XMLNode getChildElementsByRegExp(CallContext context, NamedRegularExpressionFilter... filters) {
        List<Node> result_nodes = new ArrayList<Node>();
        
        for (Node node : this.nodes) {

            NodeList result = node.getChildNodes();

            NODES: for (int n=0; n<result.getLength(); n++) {
                Node child = result.item(n);
                if (child.getNodeType() == Node.ELEMENT_NODE) {                    
                    if (filters != null && filters.length != 0) {
                        for (NamedRegularExpressionFilter nref : filters) {
                            String name = nref.getName(context);
                            if (nref.matches(context, 
                                             name.equals("xml:nsuri") ? child.getNamespaceURI() :
                                             name.equals("xml:name") ? child.getNodeName() :
                                             ((Element)child).getAttribute(name)
                                            ) == false) {
                                continue NODES;
                            }
                        }
                    }
                    result_nodes.add(child);
                }
            }
        }

        return new XMLNode(context, result_nodes);
    }

    public XMLNode getChildElementsByFilters(CallContext context, NamedRegularExpressionFilter[]... filters) {
        XMLNode current = this;
        if (filters != null) {
            for (NamedRegularExpressionFilter[] filter : filters) {
                current = current.getChildElementsByRegExp(context, filter);
            }
        }
        return current;
    }

    public boolean isText(CallContext context) {
        if (nodes != null && nodes.size() == 1) {
            short nt = nodes.get(0).getNodeType();
            return (    nt == Node.CDATA_SECTION_NODE
                     || nt == Node.TEXT_NODE
                   );
        }
        return false;
    }

    public boolean isElement(CallContext context) {
        if (nodes != null && nodes.size() == 1) {
            short nt = nodes.get(0).getNodeType();
            return (    nt == Node.ELEMENT_NODE
                   );
        }
        return false;
    }

    public boolean isComment(CallContext context) {
        if (nodes != null && nodes.size() == 1) {
            short nt = nodes.get(0).getNodeType();
            return (    nt == Node.COMMENT_NODE
                   );
        }
        return false;
    }

    public boolean isDocument(CallContext context) {
        if (nodes != null && nodes.size() == 1) {
            short nt = nodes.get(0).getNodeType();
            return (    nt == Node.DOCUMENT_NODE
                   );
        }
        return false;
    }

    public boolean isDocumentType(CallContext context) {
        if (nodes != null && nodes.size() == 1) {
            short nt = nodes.get(0).getNodeType();
            return (    nt == Node.DOCUMENT_TYPE_NODE
                   );
        }
        return false;
    }

    public boolean isProcessingInstruction(CallContext context) {
        if (nodes != null && nodes.size() == 1) {
            short nt = nodes.get(0).getNodeType();
            return (    nt == Node.PROCESSING_INSTRUCTION_NODE
                   );
        }
        return false;
    }

    public String getNodeType(CallContext context) {
        String result = "";
        if (nodes != null) {
            for (Node node : nodes) {
                if (result != null && result.length() != 0) {
                    result += ",";
                }
                switch (node.getNodeType()) {
                    case Node.CDATA_SECTION_NODE :
                        result += "CDATA_SECTION_NODE"; break;
                    case Node.TEXT_NODE :
                        result += "TEXT_NODE"; break;
                    case Node.ELEMENT_NODE :
                        result += "ELEMENT_NODE"; break;
                    case Node.COMMENT_NODE :
                        result += "COMMENT_NODE"; break;
                    case Node.ATTRIBUTE_NODE  :
                        result += "ATTRIBUTE_NODE"; break;
                    case Node.DOCUMENT_FRAGMENT_NODE  :
                        result += "DOCUMENT_FRAGMENT_NODE"; break;
                    case Node.DOCUMENT_NODE  :
                        result += "DOCUMENT_NODE"; break;
                    case Node.DOCUMENT_TYPE_NODE  :
                        result += "DOCUMENT_TYPE_NODE"; break;
                    case Node.ENTITY_NODE  :
                        result += "ENTITY_NODE"; break;
                    case Node.ENTITY_REFERENCE_NODE  :
                        result += "ENTITY_REFERENCE_NODE"; break;
                    case Node.NOTATION_NODE  :
                        result += "NOTATION_NODE"; break;
                    case Node.PROCESSING_INSTRUCTION_NODE  :
                        result += "PROCESSING_INSTRUCTION_NODE"; break;
                    default :
                        result += "???"; break;
                }
            }
        }
        return result;
    }

    public String getNamespace(CallContext context) {
        return (nodes != null && nodes.size() == 1 ? nodes.get(0).getNamespaceURI() : null);
    }

    protected class MyNamespaceContext implements javax.xml.namespace.NamespaceContext {
        protected Map<String,String> namespaces;
        public MyNamespaceContext(CallContext context, Map<String,String> namespaces) {
            this.namespaces = namespaces;
        }
        public String getNamespaceURI(String prefix) {
            if (prefix == null) throw new NullPointerException("Null prefix");
            else if ("pre".equals(prefix)) return "http://www.example.org/books";
            else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
            String nsuri = namespaces == null ? null : namespaces.get(prefix);
            if (nsuri != null) { return nsuri; }
            return XMLConstants.NULL_NS_URI;
        }

        // This method isn't necessary for XPath processing.
        public String getPrefix(String uri) {
            throw new UnsupportedOperationException();
        }

        // This method isn't necessary for XPath processing either.
        public Iterator getPrefixes(String uri) {
            throw new UnsupportedOperationException();
        }
    }
    
    static protected XPathFactory xpath_factory;

    public XMLNode resolveXPath(CallContext context, String xpath) {
        return resolveXPath(context, xpath, null);
    }

    public XMLNode resolveXPath(CallContext context, String xpath, Map<String,String> namespaces) {
        if (xpath == null || xpath.length() == 0) { return this; }

        if (xpath_factory == null) {
            xpath_factory = XPathFactory.newInstance();
        }
        XPath xp = xpath_factory.newXPath();
        xp.setNamespaceContext(new MyNamespaceContext(context, namespaces));
        XPathExpression xpe = null;
        try {
            xpe = xp.compile(xpath);
        } catch (XPathExpressionException xpee) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, xpee, "Could not compile XPath '%(xpath)'", "xpath", xpath);
            throw (ExceptionConfigurationError) null; // compiler insists
        }

        List<Node> result_nodes = new ArrayList<Node>();
        
        for (Node node : this.nodes) {
            NodeList result = null;
            try {
                result = (org.w3c.dom.NodeList) xpe.evaluate(node, XPathConstants.NODESET);
            } catch (XPathExpressionException xpee) {
                CustomaryContext.create((Context)context).throwConfigurationError(context, xpee, "Could not evaluate XPath '%(xpath)'", "xpath", xpath);
                throw (ExceptionConfigurationError) null; // compiler insists
            }

            for (int n=0; n<result.getLength(); n++) {
                result_nodes.add(result.item(n));
            }
        }

        return new XMLNode(context, result_nodes);
    }

    public XMLNode transform(CallContext context, SourceWithTimestamp transformer_source, Object... parameters) throws TransformationFailure {

        Transformer transformer = XMLUtil.getTransformer(context, transformer_source, this.getOwnerDocument(context).getDocumentURI(), parameters);

        List<Node> result_nodes = new ArrayList<Node>();
        for (Node node : this.nodes) {
            DOMSource source = new DOMSource(node);
            DOMResult result = new DOMResult();
            XMLUtil.transform(context, transformer, source, result);
            result_nodes.add(result.getNode());
        }

        return new XMLNode(context, result_nodes);
    }

    public java.util.Iterator<XMLNode> getIterator (CallContext context) {
        return this.getNodes(context).iterator();
    }

    public java.lang.Iterable<XMLNode> getIterable (CallContext context) {
        return this.getNodes(context);
    }

    protected Element getSingleElement(CallContext context) {
        if (nodes == null || nodes.size() != 1) {
            CustomaryContext.create((Context)context).throwPreConditionViolation(context, "Cannot manipulate XML nodes that do not contain exactly one DOM node");
            throw (ExceptionPreConditionViolation) null; // compiler insists
        }

        Node node = nodes.get(0);

        if (node.getNodeType() != Node.ELEMENT_NODE) {
            CustomaryContext.create((Context)context).throwPreConditionViolation(context, "Cannot append to XML nodes that do not contain a DOM Element");
            throw (ExceptionPreConditionViolation) null; // compiler insists
        }

        return (Element) node;
    }

    public void appendElement(CallContext context, String element_name, String... attributes) {
        Element element = getSingleElement(context);

        Element new_element = getOwnerDocument(context).createElement(element_name);
        if (attributes != null) {
            for (int a=0; a<attributes.length; a+=2) {
                new_element.setAttribute(attributes[a], attributes[a+1]);
            }
        }

        element.appendChild(new_element);
    }

    public void setText(CallContext context, String text) {
        Element element = getSingleElement(context);
        Node child;
        while ((child = element.getFirstChild()) != null) {
            element.removeChild(child);
        }
        Text new_text = getOwnerDocument(context).createTextNode(text);        

        element.appendChild(new_text);
    }

    /**
       Checks the leading and trailing internal nodes of this node and creates
       a new node with the same nodes but without all leading and trailing
       empty text nodes

       @return a new node, if all consecutive leading and trailing text nodes
               could be removed and they all contained only pure whitespace,
               otherwise null, if there were non-whitespace leading or trailing
               text nodes
     */
    public XMLNode trim(CallContext context) {
        int first = -1;
        int last  = -1;
        boolean leading_non_ws = false;
        boolean trailing_non_ws = false;

        int index = 0;
        if (this.nodes != null) {
            for (Node node : this.nodes) {
                short nt = node.getNodeType();
                if (    nt == Node.CDATA_SECTION_NODE
                     || nt == Node.TEXT_NODE
                   ) {
                    String data = ((Text) node).getData();
                    if (data.matches("\\s*") == false) {
                        if (first == -1) { leading_non_ws = true; }
                        trailing_non_ws = true;
                    }
                } else {
                    if (first == -1) { first = index; }
                    last = index;
                    trailing_non_ws = false;
                }
                index++;
            }
        }

        if (leading_non_ws || trailing_non_ws) {
            return null;
        }

        List<Node> result_nodes = (first == -1 ? new ArrayList<Node>() : this.nodes.subList(first, last + 1));

        return new XMLNode(context, result_nodes);
    }
}

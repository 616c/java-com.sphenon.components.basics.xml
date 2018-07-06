package com.sphenon.basics.xml;

/****************************************************************************
  Copyright 2001-2018 Sphenon GmbH

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
import com.sphenon.basics.data.*;
import com.sphenon.basics.graph.*;

import com.sphenon.basics.xml.returncodes.*;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

//[DoesNotWork]
// import org.w3c.dom.DOMImplementationRegistry;
// import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSOutput;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;

import java.util.HashMap;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

import java.io.Writer;
import java.io.StringWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class XMLUtil {
    static protected long notification_level;
    static public    long adjustNotificationLevel(long new_level) { long old_level = notification_level; notification_level = new_level; return old_level; }
    static public    long getNotificationLevel() { return notification_level; }
    static { notification_level = NotificationLocationContext.getLevel(RootContext.getInitialisationContext(), "com.sphenon.basics.xml.XMLUtil"); };

    static protected class ParserSlot {
        public boolean   locked;
        public DOMParser dom_parser;
        public DOMParser dom_parser_line_info;
        public DOMParser getParser(CallContext context, long notification_level) {
            if ((notification_level & Notifier.DIAGNOSTICS) != 0) {
                if (dom_parser_line_info == null) {
                    dom_parser_line_info = new DOMParserLineInfo();
                    setFeatures(context, dom_parser_line_info); 
                }
                return dom_parser_line_info;
            } else {
                if (dom_parser == null) {
                    dom_parser = new DOMParser();
                    setFeatures(context, dom_parser); 
                }
                return dom_parser;
            }
        }
        public void setFeatures(CallContext context, DOMParser parser) {
            try {
                // "//XSLT" does not work with this setting: (Generator Test 0080)
                parser.setFeature("http://xml.org/sax/features/namespaces", true);
                // parser.setFeature("http://xml.org/sax/features/namespace-declarations", true);

                // Files Backend mit Xalan does not work with this setting:
                // parser.setFeature("http://xml.org/sax/features/namespaces", false);

                // These *@!$-settings do work at all:
                // javax.xml.parsers.SAXParserFactory.newInstance().setFeature("http://xml.org/sax/features/namespace-prefixes", true);
                // parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
                // either: use DOMParser, then no namespace-prefixes can be set
                // or    : use javax...DocumentBuilder, then no derivation possible for the purpose of LineInfo
                //
                // import javax.xml.parsers.DocumentBuilderFactory;
                // import javax.xml.parsers.DocumentBuilder;
                // DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                // dbf.setNamespaceAware(true);
                // try {
                //     dbf.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
                // } catch (ParserConfigurationException e) {
                // }
                parser.setFeature("http://xml.org/sax/features/validation", false);
                parser.setFeature("http://apache.org/xml/features/validation/schema", false);
                parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            } catch (SAXException saxe) {
                CustomaryContext.create((Context)context).throwConfigurationError(context, saxe, "SAX-Parser does not support all expected features");
                throw (ExceptionConfigurationError) null;
            }
        }
    }

    static protected Vector<ParserSlot> parser_slots = new Vector<ParserSlot>();

    static protected ParserSlot getParserSlot(CallContext context) {
        for (ParserSlot slot : parser_slots) {
            if (slot.locked == false) {
                slot.locked = true;
                return slot;
            }
        }
        ParserSlot slot = new ParserSlot();
        parser_slots.add(slot);
        slot.locked = true;
        return slot;
    }

    static public Document parse(CallContext context, XMLInputSource input_source, long notification_level, String resource_id) throws InvalidXML {
        ParserSlot slot;
        synchronized (parser_slots) {
            slot = getParserSlot(context);
        }
        try {
            DOMParser parser = slot.getParser(context, notification_level);
            parser.setErrorHandler(new XMLErrorHandler(context, resource_id, notification_level, false));
            try {
                parser.parse(input_source);
            } catch (java.io.IOException ioe) {
                CustomaryContext.create((Context)context).throwEnvironmentError(context, ioe, "Could not parse '%(resourceid)'", "resourceid", resource_id);
                throw (ExceptionEnvironmentError) null; // compiler insists
            } catch (org.apache.xerces.xni.parser.XMLParseException xpe) {
                InvalidXML.createAndThrow(context, xpe, "Could not parse '%(resourceid)'", "resourceid", resource_id);
                throw (InvalidXML) null; // compiler insists
            }
            Document document = parser.getDocument();
            parser.setErrorHandler(null);
            if ((notification_level & Notifier.DIAGNOSTICS) != 0) {
                document.setUserData("file", resource_id, null);
            }
            return document;
        } finally {
            synchronized (parser_slots) {
                slot.locked = false;
            }
        }
    }

    /* ----------------------------------------------------------------------------- */

    static public String serialise(CallContext context, Node node) {
        return serialise(context, null, node, false, null, null, null);
    }

    static public String serialise(CallContext context, Node node, Writer writer) {
        return serialise(context, null, node, false, null, writer, null);
    }

    static public String serialise(CallContext context, Node node, TreeLeaf tree_leaf) {
        return serialise(context, null, node, false, null, getWriter(context, tree_leaf), null);
    }

    static public String serialise(CallContext context, Node node, LSOutput ls_output) {
        return serialise(context, null, node, false, ls_output, null, null);
    }

    static public String serialise(CallContext context, List<Node> nodes) {
        return serialise(context, nodes, null, false, null, null, null);
    }

    static public String serialise(CallContext context, List<Node> nodes, Writer writer) {
        return serialise(context, nodes, null, false, null, writer, null);
    }

    static public String serialise(CallContext context, List<Node> nodes, TreeLeaf tree_leaf) {
        return serialise(context, nodes, null, false, null, getWriter(context, tree_leaf), null);
    }

    static public String serialise(CallContext context, List<Node> nodes, LSOutput ls_output) {
        return serialise(context, nodes, null, false, ls_output, null, null);
    }

    static public String serialise(CallContext context, List<Node> nodes, boolean serialise_as_fragment) {
        return serialise(context, nodes, null, false, null, null, serialise_as_fragment);
    }

    static public String serialise(CallContext context, List<Node> nodes, Writer writer, boolean serialise_as_fragment) {
        return serialise(context, nodes, null, false, null, writer, serialise_as_fragment);
    }

    static public String serialise(CallContext context, List<Node> nodes, TreeLeaf tree_leaf, boolean serialise_as_fragment) {
        return serialise(context, nodes, null, false, null, getWriter(context, tree_leaf), serialise_as_fragment);
    }

    static public String serialise(CallContext context, List<Node> nodes, LSOutput ls_output, boolean serialise_as_fragment) {
        return serialise(context, nodes, null, false, ls_output, null, serialise_as_fragment);
    }

    static public String serialiseContent(CallContext context, Node node) {
        return serialise(context, null, node, true, null, null, null);
    }

    static public String serialiseContent(CallContext context, Node node, Writer writer) {
        return serialise(context, null, node, true, null, writer, null);
    }

    static public String serialiseContent(CallContext context, Node node, TreeLeaf tree_leaf) {
        return serialise(context, null, node, true, null, getWriter(context, tree_leaf), null);
    }

    static public String serialiseContent(CallContext context, Node node, LSOutput ls_output) {
        return serialise(context, null, node, true, ls_output, null, null);
    }

    static public String serialiseContent(CallContext context, List<Node> nodes) {
        return serialise(context, nodes, null, true, null, null, null);
    }

    static public String serialiseContent(CallContext context, List<Node> nodes, Writer writer) {
        return serialise(context, nodes, null, true, null, writer, null);
    }

    static public String serialiseContent(CallContext context, List<Node> nodes, TreeLeaf tree_leaf) {
        return serialise(context, nodes, null, true, null, getWriter(context, tree_leaf), null);
    }

    static public String serialiseContent(CallContext context, List<Node> nodes, LSOutput ls_output) {
        return serialise(context, nodes, null, true, ls_output, null, null);
    }

    /**
       Serializes either the Nodes or their content, depending on the
       only_content argument, to either the LSOutput, the writer or to the
       returned String.
       @param nodes             to be serialized
       @param node              to be serialized if nodes is null
       @param only_content      if true, the top level elements are not serialized (their surrounding tags are omitted)
       @param ls_output         target of serialization if non null, if null see writer
       @param writer            target of serialization if non null and ls_output is null, if also null a String is returned containing the result
       @return                  the serialized result if ls_output and writer are both null, otherwise returns null
     */
    static protected String serialise(CallContext context, List<Node> nodes, Node node, boolean only_content, LSOutput ls_output, Writer writer, Boolean serialise_as_fragment) {
        if (node == null && (nodes == null || nodes.size() == 0)) { return ""; }

        Node n0 = (nodes == null ? node : nodes.get(0));

        boolean is_document = (n0 instanceof Document);
        Document document = (is_document ? ((Document) n0) : n0.getOwnerDocument());

        if (ls_output == null && writer != null) {
            ls_output = createLSOutput(context, document, writer);
        }

        if (only_content || (nodes != null && nodes.size() > 1)) {
            return serialise(context, createDocumentFragment(context, document, nodes, node, only_content), null, document, ls_output, serialise_as_fragment);
        } else {
            return serialise(context, null, is_document ? null : n0, document, ls_output, serialise_as_fragment);
        }
    }

    /**
       Serializes either the DocumentFragment, or the Node, or the Document,
       depending which on in this order is the first non null one. The
       document needs to be non null in eiher case.

       @param document_fragment to be serialized if non null
       @param node              to be serialized if non null and document_fragment is null
       @param document          to be serialized if both document_fragment and node are null
       @param ls_output         target of serialization if non null, if null a String is returned containing the result
       @param serialise_as_fragment if true, no xml and namespace declaration will be created, otherwise
                                    if false, both will
       @return                  the serialized result if ls_output is null, otherwise returns null
    */
    static synchronized protected String serialise(CallContext context, DocumentFragment document_fragment, Node node, Document document, LSOutput ls_output, Boolean serialise_as_fragment) {
        if (serialise_as_fragment == null) {
            serialise_as_fragment = (document_fragment != null || node != null ? true : false);
        }
        Writer string_writer = null;
        if (ls_output == null) {
            string_writer = new StringWriter();
            ls_output = createLSOutput(context, document, string_writer);
        }

        if (document_fragment != null) {
            if (getLSSerializer(context, document, serialise_as_fragment).write(document_fragment, ls_output) == false) {
                System.err.println("something went wrong, but what?");
            }
        } else if (node != null) {
            if (getLSSerializer(context, document, serialise_as_fragment).write(node, ls_output) == false) {
                System.err.println("something went wrong, but what?");
            }
        } else {
            if (getLSSerializer(context, document, serialise_as_fragment).write(document, ls_output) == false) {
                System.err.println("something went wrong, but what?");
            }
        }
    
        return string_writer == null ? null : string_writer.toString();
    }

    static protected void appendNode(CallContext context, DocumentFragment document_fragment, Node node, boolean only_content) {
        if (only_content) {
            Node child = node.getFirstChild();
            while (child != null) {
                if (child.getNodeType() != Node.DOCUMENT_TYPE_NODE
                    // Node.CDATA_SECTION_NODE
                    // Node.TEXT_NODE
                    // Node.ELEMENT_NODE
                    // Node.COMMENT_NODE
                    // Node.ATTRIBUTE_NODE 
                    // Node.DOCUMENT_FRAGMENT_NODE 
                    // Node.DOCUMENT_NODE 
                    // Node.ENTITY_NODE 
                    // Node.ENTITY_REFERENCE_NODE 
                    // Node.NOTATION_NODE 
                    // Node.PROCESSING_INSTRUCTION_NODE 
                   ) {
                    document_fragment.appendChild(child.cloneNode(true));
                }
                child = child.getNextSibling();
            }
        } else {
            document_fragment.appendChild(node.cloneNode(true));
        }
    }

    static protected DocumentFragment createDocumentFragment(CallContext context, Document document, List<Node> nodes, Node node, boolean only_content) {
        DocumentFragment document_fragment = document.createDocumentFragment();

        if (nodes != null) {
            for (Node n : nodes) {
                appendNode(context, document_fragment, n, only_content);
            }
        } else {
            appendNode(context, document_fragment, node, only_content);
        }
        
        return document_fragment;
    }

    static protected LSOutput createLSOutput(CallContext context, Document document, Writer writer) {
        DOMImplementationLS domils = ((DOMImplementationLS)(document.getImplementation()));
        LSOutput ls_output = domils.createLSOutput();
        ls_output.setCharacterStream(writer);
        ls_output.setEncoding("UTF-8");
        return ls_output;
    }

    // does not work
    //     static DOMImplementationRegistry registry;
    //     static DOMImplementationLS impl;
    //     static {
    //         System.setProperty(DOMImplementationRegistry.PROPERTY, "org.apache.xerces.dom.DOMImplementationSourceImpl");
    //         registry = DOMImplementationRegistry.newInstance();
    //         impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
    //     }

    static protected HashMap<DOMImplementationLS,LSSerializer> fragment_serializers;
    static protected HashMap<DOMImplementationLS,LSSerializer> document_serializers;

    static protected LSSerializer getLSSerializer(CallContext context, Document document, boolean fragment) {
        LSSerializer serializer = null;
        DOMImplementationLS domils = ((DOMImplementationLS)(document.getImplementation()));
        if (fragment) {
            if (fragment_serializers == null) {
                fragment_serializers = new HashMap<DOMImplementationLS,LSSerializer>();
            } else {
                serializer = fragment_serializers.get(domils);
            }
            if (serializer == null) {
                serializer = domils.createLSSerializer();
                serializer.getDomConfig().setParameter("xml-declaration", new Boolean(false));
                serializer.getDomConfig().setParameter("cdata-sections", new Boolean(true));
                serializer.getDomConfig().setParameter("comments", new Boolean(true));
                serializer.getDomConfig().setParameter("element-content-whitespace", new Boolean(true));
                serializer.getDomConfig().setParameter("entities", new Boolean(true));
                serializer.getDomConfig().setParameter("namespace-declarations", new Boolean(false));
                fragment_serializers.put(domils, serializer);
            }
        } else {
            if (document_serializers == null) {
                document_serializers = new HashMap<DOMImplementationLS,LSSerializer>();
            } else {
                serializer = document_serializers.get(domils);
            }
            if (serializer == null) {
                serializer = domils.createLSSerializer();
                serializer.getDomConfig().setParameter("xml-declaration", new Boolean(true));
                serializer.getDomConfig().setParameter("cdata-sections", new Boolean(true));
                serializer.getDomConfig().setParameter("comments", new Boolean(true));
                serializer.getDomConfig().setParameter("element-content-whitespace", new Boolean(true));
                serializer.getDomConfig().setParameter("entities", new Boolean(true));
                serializer.getDomConfig().setParameter("namespace-declarations", new Boolean(true));
                document_serializers.put(domils, serializer);
            }
        }
        return serializer;
    }

    static protected Writer getWriter(CallContext context, TreeLeaf tree_leaf) {
        Data_MediaObject data = ((Data_MediaObject)(((NodeContent_Data)(tree_leaf.getContent(context))).getData(context)));
        try {
            return (data instanceof Data_MediaObject_File ? new OutputStreamWriter(new FileOutputStream(((Data_MediaObject_File)(data)).getCurrentFile(context)), "UTF-8") : new OutputStreamWriter(data.getOutputStream(context), "UTF-8"));
        } catch (FileNotFoundException fnfe) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, fnfe, "Cannot write to TreeNode '%(node)'", "node", tree_leaf.getId(context));
            throw (ExceptionConfigurationError) null; // compiler insists
        } catch (UnsupportedEncodingException uee) {
            CustomaryContext.create((Context)context).throwEnvironmentFailure(context, "UTF-8 not supported");
            throw (ExceptionEnvironmentFailure) null; // compiler insists
        }
    }

    /* ----------------------------------------------------------------------------- */

    static protected class Entry {
        public Entry (Templates templates, long creation_time) {
            this.templates = templates;
            this.creation_time = creation_time;
        }
        public Templates templates;
        public long      creation_time;
    }
    static protected HashMap<String,Entry> templates_map;
    static protected TransformerFactory transformer_factory;

    static protected synchronized Templates getTransformerTemplates(CallContext context, SourceWithTimestamp transformer_source) {
        if (transformer_factory == null) {
            transformer_factory = TransformerFactory.newInstance();
            transformer_factory.setErrorListener(new XMLTransformErrorListener(context, "factory", "initialisation", notification_level));
        }

        if (templates_map == null) {
            templates_map = new HashMap<String,Entry>();
        }

        String sysid = transformer_source.getSource(context).getSystemId();
        if (sysid == null || sysid.length() == 0) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, "XML transformer source with no system id");
            throw (ExceptionConfigurationError) null; // compiler insists
        }
        Entry entry = templates_map.get(sysid);
        long last_modification_of_source = transformer_source.getLastModification(context);
        if (entry == null || entry.creation_time < last_modification_of_source) {
            Templates transformer_templates;
            try {
                transformer_templates = transformer_factory.newTemplates(transformer_source.getSource(context));
            } catch (TransformerConfigurationException tce) {
                CustomaryContext.create((Context)context).throwConfigurationError(context, tce, "Invalid transformer");
                throw (ExceptionConfigurationError) null; // compiler insists
            }
            entry = new Entry(transformer_templates, last_modification_of_source);
            templates_map.put(sysid, entry);
        }
        return entry.templates;
    }

    static public void transform(CallContext context, Source source, Result result, SourceWithTimestamp transformer_source, Object... parameters) throws TransformationFailure {
        Transformer transformer = getTransformer(context, transformer_source, source.getSystemId(), parameters);
        transform(context, transformer, source, result);
    }

    static public Transformer getTransformer(CallContext context, SourceWithTimestamp transformer_source, String source_id, Object... parameters) throws TransformationFailure {
        Templates transformer_templates = getTransformerTemplates(context, transformer_source);
        Transformer transformer;
        try {
            transformer = transformer_templates.newTransformer();
        } catch (TransformerConfigurationException tce) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, tce, "Invalid transformer");
            throw (ExceptionConfigurationError) null; // compiler insists
        }

        transformer.setErrorListener(new XMLTransformErrorListener(context, source_id, transformer_source.getSource(context).getSystemId(), notification_level));

        // transformer.setOutputProperty("{http://xml.apache.org/xalan}content-handler", "org.apache.xalan.serialize.SerializerToXML");
        transformer.setOutputProperty("encoding", "UTF-8");

        if (parameters.length % 2 != 0) {
            CustomaryContext.create((Context)context).throwPreConditionViolation(context, "XML transformation called with odd number of parameters (must be an even name/value pair list)");
            throw (ExceptionPreConditionViolation) null; // compiler insists
        }
        for (int p=0; p<parameters.length; p+=2) {
            if ((parameters[p] instanceof String) == false) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, "XML transformation called with invalid parameters, one name is not a String (must be an even name/value pair list)");
                throw (ExceptionPreConditionViolation) null; // compiler insists
            }
            String name  = (String) parameters[p];
            Object value = parameters[p+1];
            transformer.setParameter(name, value);
        }

        return transformer;
    }

    static public void transform(CallContext context, Transformer transformer, Source source, Result result) throws TransformationFailure {
        try {
            transformer.transform(source, result);
        } catch (TransformerException te) {
            TransformationFailure.createAndThrow(context, te, "XML transformation failed");
            throw (TransformationFailure) null; // compiler insists
        }
    }
}

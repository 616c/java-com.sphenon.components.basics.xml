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

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.Element;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLErrorHandler implements ErrorHandler {
    protected CallContext context;
    protected CustomaryContext cc;
    protected String uri;
    protected long notification_level;
    protected boolean throw_error;

    public XMLErrorHandler(CallContext context, String uri, long notification_level, boolean throw_error) {
        this.context = context;
        this.uri = uri;
        this.notification_level = notification_level;
        this.throw_error = throw_error;
        cc = CustomaryContext.create((Context) context);
    }

    public void warning (SAXParseException exception) throws SAXException {
        if ((notification_level & Notifier.OBSERVATION) != 0) { cc.sendNotice(context, "DOM parser warning in '%(uri)': %(exception)", "uri", uri, "exception", exception); }
    }

    public void error (SAXParseException exception) throws SAXException {
        if ((notification_level & Notifier.MONITORING) != 0) { cc.sendCaution(context, "DOM parser error in '%(uri)': %(exception)", "uri", uri, "exception", exception); }
    }

    public void fatalError (SAXParseException exception) throws SAXException {
        if (this.throw_error) {
            cc.throwConfigurationError(context, exception, "DOM parser fatal error in '%(uri)'", "uri", uri);
            throw (ExceptionConfigurationError) null; // compiler insists
        } else {
            throw exception;
        }
    }
}

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

import javax.xml.transform.*;

public class XMLTransformErrorListener implements ErrorListener {
    protected CallContext context;
    protected CustomaryContext cc;
    protected String uri;
    protected String transformer;
    protected long notification_level;

    public XMLTransformErrorListener(CallContext context, String uri, String transformer, long notification_level) {
        this.context = context;
        this.uri = uri;
        this.transformer = transformer;
        this.notification_level = notification_level;
        cc = CustomaryContext.create((Context) context);
    }

    public void warning (TransformerException exception) throws TransformerException {
        if ((notification_level & Notifier.OBSERVATION) != 0) { cc.sendNotice(context, "XML transformer '%(transformer)' warning in '%(uri)': %(exception)", "transformer", transformer, "uri", uri, "exception", exception); }
    }

    public void error (TransformerException exception) throws TransformerException {
        if ((notification_level & Notifier.MONITORING) != 0) { cc.sendCaution(context, "XML transformer '%(transformer)' error in '%(uri)': %(exception)", "transformer", transformer, "uri", uri, "exception", exception); }
    }

    public void fatalError (TransformerException exception) throws TransformerException {
        cc.throwConfigurationError(context, exception, "XML transformer '%(transformer)' fatal error in '%(uri)'", "transformer", transformer, "uri", uri);
        throw (ExceptionConfigurationError) null; // compiler insists
    }
}

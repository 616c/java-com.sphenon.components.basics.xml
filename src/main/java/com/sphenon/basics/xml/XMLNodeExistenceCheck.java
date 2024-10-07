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

import java.util.Iterator;
import java.lang.Iterable;

public class XMLNodeExistenceCheck {

    protected CallContext context;
    protected XMLNode xml_node;

    public XMLNodeExistenceCheck (CallContext context, Object object) {
        this.context = context;
        this.xml_node = (XMLNode) object;
    }

    public boolean exists(CallContext context) {
        return (this.xml_node != null && this.xml_node.exists(context));
    }

    public boolean notexists(CallContext context) {
        return ! exists(context);
    }

    public XMLNode getValue(CallContext context) {
        return this.xml_node;
    }

    public boolean notempty(CallContext context) {
        return this.exists(context);
    }

    public boolean empty(CallContext context) {
        return ! notempty(context);
    }
}

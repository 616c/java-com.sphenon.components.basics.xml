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

import javax.xml.transform.*;
import javax.xml.transform.dom.*;

public class SourceWithTimestamp {

    public SourceWithTimestamp(CallContext context, Source source, long last_modification) {
        this.source = source;
        this.last_modification = last_modification;
    }

    protected Source source;

    public Source getSource (CallContext context) {
        return this.source;
    }

    public void setSource (CallContext context, Source source) {
        this.source = source;
    }

    protected long last_modification;

    public long getLastModification (CallContext context) {
        return this.last_modification;
    }

    public void setLastModification (CallContext context, long last_modification) {
        this.last_modification = last_modification;
    }
}

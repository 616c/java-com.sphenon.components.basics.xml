package com.sphenon.basics.xml.test;

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
import com.sphenon.basics.graph.*;
import com.sphenon.basics.graph.factories.*;
import com.sphenon.basics.testing.*;

import com.sphenon.basics.xml.*;

import java.io.File;
import java.nio.CharBuffer;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Test_Basics extends com.sphenon.basics.testing.classes.TestBase {
    static protected long notification_level;
    static public    long adjustNotificationLevel(long new_level) { long old_level = notification_level; notification_level = new_level; return old_level; }
    static public    long getNotificationLevel() { return notification_level; }
    static { notification_level = NotificationLocationContext.getLevel(RootContext.getInitialisationContext(), "com.sphenon.basics.xml.test.Test_Basics"); };

    public Test_Basics (CallContext context) {
    }

    public String getId (CallContext context) {
        if (this.id == null) {
            this.id = "XMLBasics";
        }
        return this.id;
    }

    public TestResult perform (CallContext context, TestRun test_run) {

        try {
            TreeLeaf tl = (TreeLeaf) Factory_TreeNode.tryConstruct(context, "oorl://File/${WORKSPACE}/sphenon/projects/components/basics/xml/v0001/origin/tests/manual/test.xml", NodeType.LEAF);
            TreeLeaf tl1 = (TreeLeaf) Factory_TreeNode.tryConstruct(context, "oorl://File/${WORKSPACE}/sphenon/projects/components/basics/xml/v0001/origin/tests/manual/test.out.1.xml", NodeType.LEAF, true);
            TreeLeaf tl2 = (TreeLeaf) Factory_TreeNode.tryConstruct(context, "oorl://File/${WORKSPACE}/sphenon/projects/components/basics/xml/v0001/origin/tests/manual/test.out.2.xml", NodeType.LEAF, true);
            XMLNode xn = XMLNode.createXMLNode(context, tl);

            XMLNode xnr = null;

            xnr = xn.resolveXPath(context, "/A/B");
            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "/A/B: " + (xnr.exists(context) ? xnr : "not found") + " (should be found)"); }
            if (xnr.exists(context) == false) {
                return new TestResult_Failure(context, "/A/B not found in test.xml");
            }

            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "serialised /A/B: " + xnr.serialise(context)); }
            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "serialisedContent /A/B: " + xnr.serialiseContent(context)); }

            xnr = xn.resolveXPath(context, "/A/C");
            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "/A/C: " + (xnr.exists(context) ? xnr : "not found") + " (should not be found)"); }
            if (xnr.exists(context) == true) {
                return new TestResult_Failure(context, "/A/C found in test.xml");
            }

            xnr = xn.resolveXPath(context, "/A/D");
            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "/A/D: " + (xnr.exists(context) ? xnr : "not found") + " (should be found three times)"); }
            if (xnr.exists(context) == false) {
                return new TestResult_Failure(context, "/A/D not found in test.xml");
            }

            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "serialised /A/D: " + xnr.serialise(context)); }
            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "serialisedContent /A/D: " + xnr.serialiseContent(context)); }


            xnr = xn.resolveXPath(context, "/A");
            if (xnr.exists(context) == false) {
                return new TestResult_Failure(context, "/A not found in test.xml");
            }

            xnr.appendElement(context, "Neu", "Farbe", "Rot", "Form", "Dreieck");
            
            xnr = xn.resolveXPath(context, "/A/D/X");
            if (xnr.exists(context) == false) {
                return new TestResult_Failure(context, "/A/D/X not found in test.xml");
            }

            xnr.setText(context, "DREI!!!");

            xn.serialise(context, tl1);
            xn.serialiseContent(context, tl2);
        } catch (Throwable t) {
            return new TestResult_ExceptionRaised(context, t);
        }
        
        return TestResult.OK;
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.admingui.console.component.dnd;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.el.ValueExpression;
import java.util.List;
import java.util.ArrayList;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;

@FacesComponent(Draggable.COMPONENT_TYPE)
@ResourceDependencies({
    @ResourceDependency(library="glassfish/js", name="jquery-1.6.2.min.js", target="head"),
    @ResourceDependency(library="glassfish/js", name="jquery-ui-1.8.15.min.js", target="head"),
    @ResourceDependency(library="glassfish/js", name="jquery-ui-base.css", target="head")
})
public class Draggable extends UIComponentBase {

    public static final String COMPONENT_TYPE = "org.glassfish.admingui.console.component.Draggable";
    public static final String COMPONENT_FAMILY = COMPONENT_TYPE;
    private static final String DEFAULT_RENDERER = COMPONENT_TYPE;
    private static final String OPTIMIZED_PACKAGE = "org.glassfish.admingui.console.component.";

    protected enum PropertyKeys {

        proxy, 
        dragOnly, 
        forValue("for"), 
        disabled, 
        axis, 
        containment, 
        helper, 
        revert, 
        snap, 
        snapMode, 
        snapTolerance, 
        zindex, 
        handle, 
        opacity, 
        stack, 
        grid, 
        scope, 
        cursor,
        revertDuration;
        
        String toString;

        PropertyKeys(String toString) {
            this.toString = toString;
        }

        PropertyKeys() {
        }

        public String toString() {
            return ((this.toString != null) ? this.toString : super.toString());
        }
    }

    public Draggable() {
        setRendererType(DEFAULT_RENDERER);
    }

    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public boolean isProxy() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.proxy, false);
    }

    public void setProxy(boolean _proxy) {
        getStateHelper().put(PropertyKeys.proxy, _proxy);
        handleAttribute("proxy", _proxy);
    }

    public boolean isDragOnly() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.dragOnly, false);
    }

    public void setDragOnly(boolean _dragOnly) {
        getStateHelper().put(PropertyKeys.dragOnly, _dragOnly);
        handleAttribute("dragOnly", _dragOnly);
    }

    public java.lang.String getFor() {
        final String id = (java.lang.String) getStateHelper().eval(PropertyKeys.forValue, null);
        return (id != null) ? id : getParent().getId();
    }

    public void setFor(java.lang.String _for) {
        getStateHelper().put(PropertyKeys.forValue, _for);
        handleAttribute("forValue", _for);
    }

    public boolean isDisabled() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.disabled, false);
    }

    public void setDisabled(boolean _disabled) {
        getStateHelper().put(PropertyKeys.disabled, _disabled);
        handleAttribute("disabled", _disabled);
    }

    public java.lang.String getAxis() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.axis, null);
    }

    public void setAxis(java.lang.String _axis) {
        getStateHelper().put(PropertyKeys.axis, _axis);
        handleAttribute("axis", _axis);
    }

    public java.lang.String getContainment() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.containment, null);
    }

    public void setContainment(java.lang.String _containment) {
        getStateHelper().put(PropertyKeys.containment, _containment);
        handleAttribute("containment", _containment);
    }

    public java.lang.String getHelper() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.helper, "clone");
    }

    public void setHelper(java.lang.String _helper) {
        getStateHelper().put(PropertyKeys.helper, _helper);
        handleAttribute("helper", _helper);
    }

    public String getRevert() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.revert, "false");
    }

    public void setRevert(String _revert) {
        getStateHelper().put(PropertyKeys.revert, _revert);
        handleAttribute("revert", _revert);
    }

    public boolean isSnap() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.snap, false);
    }

    public void setSnap(boolean _snap) {
        getStateHelper().put(PropertyKeys.snap, _snap);
        handleAttribute("snap", _snap);
    }

    public java.lang.String getSnapMode() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.snapMode, null);
    }

    public void setSnapMode(java.lang.String _snapMode) {
        getStateHelper().put(PropertyKeys.snapMode, _snapMode);
        handleAttribute("snapMode", _snapMode);
    }

    public int getSnapTolerance() {
        return (java.lang.Integer) getStateHelper().eval(PropertyKeys.snapTolerance, 20);
    }

    public void setSnapTolerance(int _snapTolerance) {
        getStateHelper().put(PropertyKeys.snapTolerance, _snapTolerance);
        handleAttribute("snapTolerance", _snapTolerance);
    }

    public Integer getRevertDuration() {
        return (java.lang.Integer) getStateHelper().eval(PropertyKeys.revertDuration, null);
    }

    public void setRevertDuration(Integer _revertDuration) {
        getStateHelper().put(PropertyKeys.revertDuration, _revertDuration);
        handleAttribute("revertDuration", _revertDuration);
    }

    public int getZindex() {
        return (java.lang.Integer) getStateHelper().eval(PropertyKeys.zindex, -1);
    }

    public void setZindex(int _zindex) {
        getStateHelper().put(PropertyKeys.zindex, _zindex);
        handleAttribute("zindex", _zindex);
    }

    public java.lang.String getHandle() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.handle, null);
    }

    public void setHandle(java.lang.String _handle) {
        getStateHelper().put(PropertyKeys.handle, _handle);
        handleAttribute("handle", _handle);
    }

    public double getOpacity() {
        return (java.lang.Double) getStateHelper().eval(PropertyKeys.opacity, 1.0);
    }

    public void setOpacity(double _opacity) {
        getStateHelper().put(PropertyKeys.opacity, _opacity);
        handleAttribute("opacity", _opacity);
    }

    public java.lang.String getStack() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.stack, null);
    }

    public void setStack(java.lang.String _stack) {
        getStateHelper().put(PropertyKeys.stack, _stack);
        handleAttribute("stack", _stack);
    }

    public java.lang.String getGrid() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.grid, null);
    }

    public void setGrid(java.lang.String _grid) {
        getStateHelper().put(PropertyKeys.grid, _grid);
        handleAttribute("grid", _grid);
    }

    public java.lang.String getScope() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.scope, null);
    }

    public void setScope(java.lang.String _scope) {
        getStateHelper().put(PropertyKeys.scope, _scope);
        handleAttribute("scope", _scope);
    }

    public java.lang.String getCursor() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.cursor, "crosshair");
    }

    public void setCursor(java.lang.String _cursor) {
        getStateHelper().put(PropertyKeys.cursor, _cursor);
        handleAttribute("cursor", _cursor);
    }

    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    public void handleAttribute(String name, Object value) {
        List<String> setAttributes = (List<String>) this.getAttributes().get("javax.faces.component.UIComponentBase.attributesThatAreSet");
        if (setAttributes == null) {
            String cname = this.getClass().getName();
            if (cname != null && cname.startsWith(OPTIMIZED_PACKAGE)) {
                setAttributes = new ArrayList<String>(6);
                this.getAttributes().put("javax.faces.component.UIComponentBase.attributesThatAreSet", setAttributes);
            }
        }
        if (setAttributes != null) {
            if (value == null) {
                ValueExpression ve = getValueExpression(name);
                if (ve == null) {
                    setAttributes.remove(name);
                } else if (!setAttributes.contains(name)) {
                    setAttributes.add(name);
                }
            }
        }
    }
}
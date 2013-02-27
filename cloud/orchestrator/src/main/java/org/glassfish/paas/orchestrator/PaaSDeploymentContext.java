/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.paas.orchestrator;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;

import java.util.HashMap;
import java.util.Map;

/**
 * Deployment Context of a PaaS enabled application</br>
 *
 * @author Jagadish Ramu
 */
public class PaaSDeploymentContext {
    private DeploymentContext dc;
    private String appName;
    private ReadableArchive archive;
    private Map<String, Object> transientAppMetaData = new HashMap<String, Object>();

    public PaaSDeploymentContext(String appName, DeploymentContext dc){
        this.appName = appName;
        this.dc = dc;
        if(dc != null){
            this.archive = dc.getSource();
        }
    }

    public PaaSDeploymentContext(String appName, ReadableArchive archive){
        this.appName = appName;
        this.archive = archive;
    }

    /**
     * provides the application-name. </br>
     * application-name will be null in case of shared or external-service related operations/invocations.
     * @return String appName
     */
    public String getAppName(){
        return appName;
    }

    /**
     * provides the deployment-context associated with the deployment</br>
     * deployment-context will be null in case of non-deployment related activity
     * eg: scaling, monitoring actions etc.,
     * @return DeploymentContext
     */
    public DeploymentContext getDeploymentContext(){
        return dc;
    }

    public void addTransientAppMetaData(String metaDataKey, Object metaData) {
        if (metaData!=null) {
            transientAppMetaData.put(metaDataKey, metaData);
        }
    }

    public <T> T getTransientAppMetaData(String key, Class<T> metadataType) {
        Object metaData = transientAppMetaData.get(key);
        if (metaData != null) {
            return metadataType.cast(metaData);
        }
        return null;
    }

    public ReadableArchive getArchive(){
        return archive;
    }
}

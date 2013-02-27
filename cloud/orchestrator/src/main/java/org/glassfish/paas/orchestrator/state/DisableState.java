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

package org.glassfish.paas.orchestrator.state;

import org.glassfish.paas.orchestrator.PaaSDeploymentContext;
import org.glassfish.paas.orchestrator.PaaSDeploymentException;
import org.glassfish.paas.orchestrator.provisioning.ServiceInfo;
import org.glassfish.paas.orchestrator.provisioning.ServiceScope;
import org.glassfish.paas.orchestrator.service.metadata.ServiceDescription;
import org.glassfish.paas.orchestrator.service.metadata.ServiceMetadata;
import org.glassfish.paas.orchestrator.service.spi.ProvisionedService;
import org.glassfish.paas.orchestrator.service.spi.ServicePlugin;
import org.jvnet.hk2.annotations.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Jagadish Ramu
 */
@Service
public class DisableState extends AbstractPaaSDeploymentState {

    public void handle(PaaSDeploymentContext context) throws PaaSDeploymentException {
        stopServices(context);
    }

    private void stopServices(PaaSDeploymentContext context) throws PaaSDeploymentException {
        String appName = context.getAppName();
        final ServiceMetadata appServiceMetadata = appInfoRegistry.getServiceMetadata(appName);

        List<ServiceDescription> stoppedServiceSDs = new ArrayList<ServiceDescription>();
        for (ServiceDescription sd : appServiceMetadata.getServiceDescriptions()) {
            if(ServiceScope.APPLICATION.equals(sd.getServiceScope())){
                try {
                    stopService(context, appName, sd);
                    stoppedServiceSDs.add(sd);
                } catch (Exception e) {
                    Object args[]=new Object[]{sd.getName(),appName};
                    logger.log(Level.WARNING, localStrings.getString("exception.stop.service",args),e);

                    EnableState enableState = habitat.getComponent(EnableState.class);
                    for (ServiceDescription stoppedSD : stoppedServiceSDs) {
                        try {
                            enableState.startService(context, appName, stoppedSD);
                        } catch (Exception stopException) {
                            args=new Object[]{stoppedSD.getName(),appName};
                            logger.log(Level.WARNING, localStrings.getString("exception.start.service",args),stopException);

                        }
                    }
                    throw new PaaSDeploymentException(e);
                }
            }
        }
    }

    public boolean stopService(PaaSDeploymentContext context, String appName, ServiceDescription sd) {
        ServicePlugin chosenPlugin = sd.getPlugin();
        ServiceInfo serviceInfo = serviceUtil.getServiceInfo(sd.getName(), appName);
        if(serviceInfo != null){
            ProvisionedService ps = orchestrator.getProvisionedService(sd, appName);
            chosenPlugin.stopService(ps, serviceInfo);
            serviceUtil.updateState(ps, appName);
            return true;
        }else{
            Object[] args=new Object[]{sd.getName(),appName};
            logger.log(Level.WARNING,"unable.retrieve.serviceinfo",args);
            return false;
        }
    }

    public Class getRollbackState() {
        return EnableState.class;
    }
}

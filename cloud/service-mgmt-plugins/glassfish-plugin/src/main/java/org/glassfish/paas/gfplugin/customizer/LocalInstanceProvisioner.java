/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.paas.gfplugin.customizer;

import org.glassfish.embeddable.CommandRunner;
import org.glassfish.hk2.scopes.Singleton;
import org.glassfish.paas.gfplugin.GlassFishPluginConstants;
import org.glassfish.paas.gfplugin.GlassFishProvisionedService;
import org.glassfish.paas.orchestrator.service.spi.ProvisionedService;
import org.glassfish.virtualization.runtime.VirtualClusters;
import org.glassfish.virtualization.spi.VirtualCluster;
import org.glassfish.virtualization.spi.VirtualMachine;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Bhavanishankar S
 */
@Service
@Scoped(Singleton.class)
public class LocalInstanceProvisioner implements GlassFishPluginConstants {

    @Inject(optional = true)
    private VirtualClusters virtualClusters;

    public void provision(GlassFishProvisionedService das, ProvisionedService... instances) {

        CommandRunner cr = null;

        try {
            cr = das.getProvisionedGlassFish().getCommandRunner();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        String clusterName = das.getServiceDescription().getVirtualClusterName();

        VirtualCluster virtualCluster = null;

        try {
            virtualCluster = virtualClusters.byName(clusterName);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        for (ProvisionedService instance : instances) {

            String vmId = instance.getServiceProperties().getProperty("vm-id");

            VirtualMachine vm = virtualCluster.vmByName(vmId);

            String instanceName = vm.getName();

            cr.run(CREATE_LOCAL_INSTANCE, "--cluster=" + clusterName, instanceName);

            cr.run(START_LOCAL_INSTANCE, instanceName);

        }

    }

}

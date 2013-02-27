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

package org.glassfish.paas.spe.common.commands;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.paas.orchestrator.provisioning.cli.ServiceUtil;
import org.glassfish.virtualization.runtime.VirtualClusters;
import org.glassfish.virtualization.runtime.VirtualMachineLifecycle;
import org.glassfish.virtualization.spi.VirtualCluster;
import org.glassfish.virtualization.spi.VirtualMachine;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 * This is an additional command in order to be able to purge a service manually.
 * <p/>
 * This should not be used unless a service needs to be forcefully purged.
 *
 * @author bhavanishankar@java.net
 */

@Service(name = "_delete-service")
@Scoped(PerLookup.class)
@CommandLock(CommandLock.LockType.NONE)

public class DeleteService implements AdminCommand {

    @Param(name = "servicename", primary = true)
    private String serviceName;

    @Param(name = "appname", optional = true)
    private String appName;

    @Param(name = "virtualcluster", optional = true)
    private String virtualClusterName;

    @Inject
    private ServiceUtil serviceUtil;

    @Inject
    VirtualMachineLifecycle vmLifecycle;

    @Inject
    private VirtualClusters virtualClusters;

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        try {
            String vmId = serviceUtil.getInstanceID(serviceName, appName);
            VirtualCluster virtualCluster = virtualClusters.byName(virtualClusterName);
            VirtualMachine vm = virtualCluster.vmByName(vmId);
            vmLifecycle.delete(vm);
            serviceUtil.unregisterServiceInfo(serviceName, appName);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            report.setMessage("Successfully deleted service [" + serviceName + "]");
        } catch (Exception exception) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage("Unable to delete service [" + serviceName + "]");
            report.setFailureCause(exception);
        }
    }

}

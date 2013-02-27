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

package org.glassfish.virtualization.local;

import com.sun.enterprise.config.serverbeans.Cluster;
import org.glassfish.virtualization.config.ServerPoolConfig;
import org.glassfish.virtualization.config.VirtualMachineConfig;
import org.glassfish.virtualization.spi.VirtualCluster;
import org.glassfish.virtualization.spi.*;
import org.glassfish.virtualization.spi.EventSource;
import org.glassfish.virtualization.util.ListenableFutureImpl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Server Pool based on non virtualized environments like the local machine
 * on which this process is running.
 *
 * @author Jerome Dochez
 */
class LocalServerPool implements ServerPool {

    final Map<String, VirtualMachine> vms = new HashMap<String, VirtualMachine>();
    final ServerPoolConfig config;
    final LocalServerPoolFactory serverPoolFactory;
    final AtomicLong vmId;

    public LocalServerPool(ServerPoolConfig config, LocalServerPoolFactory serverPoolFactory) {
        this.config = config;
        this.serverPoolFactory = serverPoolFactory;
        if (serverPoolFactory.getDomain().getClusters()!=null) {
            for (Cluster cluster : serverPoolFactory.getDomain().getClusters().getCluster()) {
                for (VirtualMachineConfig vmc : cluster.getExtensionsByType(VirtualMachineConfig.class)) {
                    if (vmc.getServerPool().getName().equals(config.getName())) {
                        vms.put(vmc.getName(), new LocalVirtualMachine(vmc, vmc.getTemplate().getUser(), this, null, vmc.getName()));
                    }
                }
            }
        }
        vmId = new AtomicLong(vms.size());
    }

    @Override
    public ServerPoolConfig getConfig() {
        return config;
    }

    @Override
    public String getName() {
        return getConfig().getName();
    }

    @Override
    public Collection<VirtualMachine> getVMs() throws VirtException {
        return vms.values();
    }

    @Override
    public VirtualMachine vmByName(String name) throws VirtException {
        return vms.get(name);
    }

    @Override
    public PhasedFuture<AllocationPhase, VirtualMachine> allocate(
            TemplateInstance template, VirtualCluster cluster, EventSource<AllocationPhase> source)
            throws VirtException {

        String vmName = getName() + "-" + vmId.incrementAndGet();
        VirtualMachineConfig config = VirtualMachineConfig.Utils.create(
                vmName,
                template.getConfig(),
                getConfig(),
                cluster.getConfig());

        LocalVirtualMachine vm = new LocalVirtualMachine(config, template.getConfig().getUser(), this, null, vmName);
        // this needs to be improved.
        vm.setProperty(VirtualMachine.PropertyName.INSTALL_DIR,
                serverPoolFactory.getServerContext().getInstallRoot().getParentFile().getAbsolutePath());
        cluster.add(vm);
        vms.put(vmName, vm);
        CountDownLatch latch = new CountDownLatch(1);

        PhasedFuture<AllocationPhase, VirtualMachine> future =
                new ListenableFutureImpl<AllocationPhase, VirtualMachine>(latch, vm, source);
        template.getCustomizer().customize(cluster, vm);
        vm.start();
        latch.countDown();
        return future;
    }

    @Override
    public void install(TemplateInstance template) throws IOException {
        // templates are already locally installed...
    }
}

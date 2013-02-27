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

package org.glassfish.virtualization.local;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.util.ExecException;
import com.sun.enterprise.util.ProcessExecutor;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.virtualization.config.VirtUser;
import org.glassfish.virtualization.config.VirtualMachineConfig;
import org.glassfish.virtualization.spi.*;
import org.glassfish.virtualization.util.AbstractVirtualMachine;
import org.glassfish.virtualization.util.RuntimeContext;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * abstraction of a non virtualization bare metal PC.
 *
 * @author Jerome Dochez
 */
class LocalVirtualMachine extends AbstractVirtualMachine {

    final String vmName;
    final Machine machine;
    final LocalServerPool pool;
    private static final Logger logger = RuntimeContext.logger;

    LocalVirtualMachine(VirtualMachineConfig config, VirtUser user, LocalServerPool pool, Machine machine, String vmName) {
        super(config, user);
        this.vmName = vmName;
        this.pool = pool;
        this.machine = machine;
    }


    @Override
    public String getName() {
        return vmName;
    }

    @Override
    public InetAddress getAddress() {
        // we need to return our ip address.
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            RuntimeContext.logger.log(Level.SEVERE, "Cannot get list of network interfaces",e);
            try {
                return InetAddress.getByAddress(new byte[] {127, 0, 0, 1});
            } catch (UnknownHostException e1) {
                RuntimeContext.logger.log(Level.SEVERE, "Cannot create loop back address");
                return null;
            }
        }
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            // we need a way to customize this through configuration
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress inetAddress = addresses.nextElement();
                String address = inetAddress.getHostAddress();
                if (address.contains(".")) {
                    return inetAddress;
                }
            }
        }
        try {
            return InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        } catch (UnknownHostException e1) {
            RuntimeContext.logger.log(Level.SEVERE, "Cannot create loop back address");
            return null;
        }
    }

    @Override
    public void setAddress(InetAddress address) {
        // ignore/
    }

    @Override
    public void start() throws VirtException {
        for (Cluster cluster : pool.serverPoolFactory.getDomain().getClusters().getCluster()) {
            for (VirtualMachineConfig vmc : cluster.getExtensionsByType(VirtualMachineConfig.class)) {
                if (vmc.getName().equals(getName())) {
                    TemplateInstance ti = pool.serverPoolFactory.getTemplateRepository() .byName(vmc.getTemplate().getName());
                    if (ti!=null) {
                        ti.getCustomizer().start(this, false);
                    }
                    return;
                }
            }
        }
        throw new RuntimeException("Cannot find registered virtual machine " + getName());
    }

    @Override
    public void suspend() throws VirtException {
        throw new RuntimeException("Local processes cannot be suspended or resumed");
    }

    @Override
    public void resume() throws VirtException {
        throw new RuntimeException("Local processes cannot be suspended or resumed");
    }

    @Override
    public void stop() throws VirtException {
        // nothing to do ?
    }

    @Override
    public void delete() throws VirtException {
        // nothing to do, the delete-instance took care of it.
    }

    @Override
    public VirtualMachineInfo getInfo() {
        return new VirtualMachineInfo() {
            @Override
            public long maxMemory() throws VirtException {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public long memory() throws VirtException {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public long cpuTime() throws VirtException {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Machine.State getState() throws VirtException {
                return Machine.State.READY;
            }

            @Override
            public void registerMemoryListener(MemoryListener ml, long delay, TimeUnit unit) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void unregisterMemoryListener(MemoryListener ml) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int nbVirtCpu() throws VirtException {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    @Override
    public ServerPool getServerPool() {
        return pool;
    }

    @Override
    public Machine getMachine() {
        return machine;
    }

    @Override
    public String executeOn(String[] args) throws IOException, InterruptedException {
        ProcessExecutor processExecutor = new ProcessExecutor(args);
        try {
            String[] returnLines = processExecutor.execute(true);
            StringBuffer stringBuffer = new StringBuffer();
            if (returnLines != null) {
                for (String returnLine : returnLines) {
                    stringBuffer.append(returnLine);
                }
            }
            return stringBuffer.toString();
        } catch (ExecException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean upload(File localFile, File remoteTargetDirectory) {
        try {
            File toFile = new File(remoteTargetDirectory, localFile.getName());
            if(!toFile.equals(localFile)) {
                FileUtils.copy(localFile, toFile);
                logger.log(Level.INFO, "Successfully copied file {0} to directory {1}",
                        new Object[]{localFile.getAbsolutePath(), remoteTargetDirectory.getAbsolutePath()});
            }
            return true;
        } catch (Exception e) {
            RuntimeContext.logger.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
    }

    public boolean download(File remoteFile, File localTargetDirectory) {
        try {
            FileUtils.copy(remoteFile, new File(localTargetDirectory, remoteFile.getName()));
            logger.log(Level.INFO, "Successfully copied file {0} to directory {2}",
                    new Object[]{remoteFile.getAbsolutePath(), localTargetDirectory.getAbsolutePath()});
            return true;
        } catch (Exception e) {
            RuntimeContext.logger.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
    }
}

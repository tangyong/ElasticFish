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

package org.glassfish.paas.spe.common;

import org.glassfish.internal.api.Globals;
import org.glassfish.paas.orchestrator.service.ServiceStatus;
import org.glassfish.paas.orchestrator.service.ServiceType;
import org.glassfish.paas.orchestrator.service.metadata.ServiceDescription;
import org.glassfish.paas.orchestrator.service.spi.ProvisionedService;
import org.glassfish.paas.orchestrator.service.spi.Service;
import org.glassfish.paas.orchestrator.service.spi.ServiceLogRecord;
import org.glassfish.paas.orchestrator.service.spi.ServiceLogType;
import org.glassfish.virtualization.runtime.VirtualClusters;
import org.glassfish.virtualization.spi.VirtualCluster;
import org.glassfish.virtualization.spi.VirtualMachine;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

/**
 * Represents a node (or instance) belonging to a service.
 *
 * @author Bhavanishankar S
 */

public class BasicProvisionedService implements ProvisionedService {

    private ServiceDescription serviceDescription;
    private Properties serviceProperties;
    private ServiceStatus status;
    private Set<Service> childServices;

    /*
    private ServiceInfo serviceInfo;

    public AbstractProvisionedService(ServiceDescription serviceDescription,
                                      ServiceInfo serviceInfo) {
    }
    */
    
    public BasicProvisionedService(ServiceDescription serviceDescription,
                                   Properties serviceProperties, ServiceStatus status) {
        this.serviceDescription = serviceDescription;
        this.status = status;
	    this.serviceProperties = serviceProperties;
        childServices = new LinkedHashSet<Service>();
    }

    public ServiceType getServiceType() {
        String sdServiceType = serviceDescription.getServiceType();
        // TODO :: FIXME >> make serviceType extensible

        Collection<ServiceType> serviceTypes = Globals.getDefaultHabitat().getAllByContract(ServiceType.class);
        for(ServiceType serviceType : serviceTypes){
            if(serviceType.getName().equals(sdServiceType)){
                return serviceType;
            }
        }
        return null;
    }

    public ServiceDescription getServiceDescription() {
        return serviceDescription;
    }

    public Properties getServiceProperties() {
        return serviceProperties;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status){
        this.status = status;
    }

    public String getName() {
        return serviceDescription.getName();
    }

    public Set<Service> getChildServices() {
        return childServices;
    }

    public void addChildService(ProvisionedService provisionedService){
        childServices.add(provisionedService);
    }

    public Properties getProperties() {
        return serviceProperties;
    }

    /**
     * Get the virtual machine on which this servicenode is running.
     *
     * @return underlying virtual machine on which this servicenode is running.
     */
    public VirtualMachine getVM() {
        VirtualClusters virtualClusters = Globals.getDefaultHabitat().
                getByType(VirtualClusters.class);
        String clusterName = getServiceDescription().getVirtualClusterName();
        VirtualCluster virtualCluster = null;
        try {
            virtualCluster = virtualClusters.byName(clusterName);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        String vmId = getServiceProperties().getProperty("vm-id");
        VirtualMachine vm = virtualCluster.vmByName(vmId);
        return vm;
    }

    public Map<Service, List<ServiceLogRecord>> collectLogs(ServiceLogType type, Level level, Date since) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Map<Service, List<ServiceLogRecord>> collectLogs(ServiceLogType type, Level level, long count) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Set<ServiceLogType> getLogTypes() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public ServiceLogType getDefaultLogType() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}

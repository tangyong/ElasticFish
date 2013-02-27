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
package org.glassfish.paas.orchestrator.provisioning.cli;


import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.logging.LogDomains;
import org.glassfish.hk2.scopes.Singleton;
import org.glassfish.paas.orchestrator.ServiceOrchestratorImpl;
import org.glassfish.paas.orchestrator.config.*;
import org.glassfish.paas.orchestrator.config.Service;
import org.glassfish.paas.orchestrator.provisioning.ServiceInfo;
import org.glassfish.paas.orchestrator.provisioning.ServiceScope;
import org.glassfish.paas.orchestrator.service.ConfiguredServiceImpl;
import org.glassfish.paas.orchestrator.service.ServiceStatus;
import org.glassfish.paas.orchestrator.service.metadata.ServiceCharacteristics;
import org.glassfish.paas.orchestrator.service.metadata.ServiceDescription;
import org.glassfish.paas.orchestrator.service.metadata.TemplateIdentifier;
import org.glassfish.paas.orchestrator.service.spi.*;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import java.beans.PropertyVetoException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


@org.jvnet.hk2.annotations.Service
@Scoped(Singleton.class)
public class ServiceUtil {

    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    @Inject
    private Domain domain;

    @Inject
    private Habitat habitat;

    private static Logger logger = LogDomains.getLogger(ServiceOrchestratorImpl.class,LogDomains.PAAS_LOGGER);

    protected final static StringManager localStrings = StringManager.getManager(ServiceOrchestratorImpl.class);

    public static ExecutorService getThreadPool() {
        return threadPool;
    }

    public boolean isValidService(String serviceName, String appName) {
        ServiceInfo entry = getServiceInfo(serviceName, appName);
        return entry != null;
    }

    public void setProperty(String serviceName, String appName,
                            final String propName, final String propValue) {
        Service matchingService = getService(serviceName, appName);
        if (matchingService != null) {
            try {
                if (ConfigSupport.apply(new SingleConfigCode<Service>() {
                    public Object run(Service serviceConfig) throws PropertyVetoException, TransactionFailure {
                        Property property = serviceConfig.getProperty(propName);
                        if (property != null) {
                            Transaction t = Transaction.getTransaction(serviceConfig);
                            Property p_w = t.enroll(property);
                            p_w.setValue(propValue);

                        } else {
                            Property prop = serviceConfig.createChild(Property.class);
                            prop.setName(propName);
                            prop.setValue(propValue);
                            serviceConfig.getProperty().add(prop);
                        }
                        return serviceConfig;
                    }
                }, matchingService) == null) {
                    String msg = "Unable to update property [" + propName + "] of service [" + serviceName + "]";
                    System.out.println(msg);
                    throw new RuntimeException(msg);
                }
            } catch (TransactionFailure transactionFailure) {
                transactionFailure.printStackTrace();
                throw new RuntimeException(transactionFailure.getMessage(), transactionFailure);
            }
        } else {
            throw new RuntimeException("Invalid service, no such service [" + serviceName + "] found");
        }
    }

/*
    public void removeProperty(String serviceName, String appName,
                               final String propName) {
        Service matchingService = getService(serviceName, appName);
        if (matchingService != null) {
            try {
                if (ConfigSupport.apply(new SingleConfigCode<Service>() {
                    public Object run(Service serviceConfig) throws PropertyVetoException, TransactionFailure {
                        Property property = serviceConfig.getProperty(propName);
                        if (property != null) {
                            serviceConfig.getProperty().remove(property);
                        }
                        return serviceConfig;
                    }
                }, matchingService) == null) {
                    String msg = "Unable to remove property [" + propName + "] of service [" + serviceName + "]";
                    System.out.println(msg);
                    throw new RuntimeException(msg);
                }
            } catch (TransactionFailure transactionFailure) {
                transactionFailure.printStackTrace();
                throw new RuntimeException(transactionFailure.getMessage(), transactionFailure);
            }
        } else {
            throw new RuntimeException("Invalid service, no such service [" + serviceName + "] found");
        }
    }

*/

    public void updateState(ProvisionedService service, String appName){

        if (service.getChildServices() != null) {
            for (org.glassfish.paas.orchestrator.service.spi.Service childService : service.getChildServices()) {
                updateState(childService.getName(), appName, service.getStatus().toString());
            }
        }
        updateState(service.getName(), appName, service.getStatus().toString());
        fireServiceChangeEvent(getServiceChangeEventType(service.getStatus()), service);
    }

    private ServiceChangeEvent.Type getServiceChangeEventType(ServiceStatus status){
        if(ServiceStatus.RUNNING.equals(status) || ServiceStatus.STARTED.equals(status)){
            return ServiceChangeEvent.Type.STARTED;
        }else if(ServiceStatus.STOPPED.equals(status) || ServiceStatus.NOT_RUNNING.equals(status)){
            return ServiceChangeEvent.Type.STOPPED;
        }else{
            throw new RuntimeException("Unknown Service Status : " + status);
        }
    }

    public void updateState(String serviceName, String appName, final String state) {
        Service matchingService = getService(serviceName, appName);
        if (matchingService != null) {
            if (matchingService instanceof ApplicationScopedService) {
                ApplicationScopedService appScopedService = (ApplicationScopedService) matchingService;
                try {
                    if (ConfigSupport.apply(new SingleConfigCode<ApplicationScopedService>() {
                        public Object run(ApplicationScopedService serviceConfig) throws PropertyVetoException, TransactionFailure {
                            serviceConfig.setState(state);
                            return serviceConfig;
                        }
                    }, appScopedService) == null) {
                        String msg = "Unable to update state [" + state + "] of service [" + serviceName + "]";
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    }
                } catch (TransactionFailure transactionFailure) {
                    transactionFailure.printStackTrace();
                    throw new RuntimeException(transactionFailure.getMessage(), transactionFailure);
                }
            }

            if (matchingService instanceof SharedService) {
                SharedService sharedService = (SharedService) matchingService;
                try {
                    if (ConfigSupport.apply(new SingleConfigCode<SharedService>() {
                        public Object run(SharedService serviceConfig) throws PropertyVetoException, TransactionFailure {
                            serviceConfig.setState(state);
                            return serviceConfig;
                        }
                    }, sharedService) == null) {
                        String msg = "Unable to update state [" + state + "] of service [" + serviceName + "]";
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    }
                } catch (TransactionFailure transactionFailure) {
                    transactionFailure.printStackTrace();
                    throw new RuntimeException(transactionFailure.getMessage(), transactionFailure);
                }
            }

        } else {
            throw new RuntimeException("Invalid service, no such service [" + serviceName + "] found");
        }
    }

    /**
     * Utility to get the list of applications that refer a shared or external service.
     * @param service shared-service-name or external-service-name
     * @return Collection list of applications that use the service
     */
    public List<String> getApplicationsUsingService(String service){
        List<String> applications = new ArrayList<String>();
        ServiceInfo serviceInfo = getServiceInfo(service, null);
        if(serviceInfo != null){
            for(ServiceRef serviceRef : getServices().getServiceRefs()){
                if(serviceRef.getServiceName().equals(service)){
                    applications.add(serviceRef.getApplicationName());
                }
            }
        }
        return applications;
    }


/*
    public boolean isServiceAlreadyConfigured(String serviceName, String appName, ServiceType type) {
        //TODO hack, ignoring the shared-service/external-service related call for now.
        if(appName==null){
            return false;
        }
        Service matchingService = getService(serviceName, appName);
        return matchingService != null;
    }
*/

    public String getServiceType(String serviceName, String appName) {
        ServiceInfo entry = getServiceInfo(serviceName, appName);
        if (entry != null) {
            return entry.getServerType();
        } else {
            return null;
        }
    }


/*
    public String getServiceState(String serviceName, String appName, ServiceType type) {
        ServiceInfo entry = getServiceInfo(serviceName, appName, type);
        if (entry != null) {
            return entry.getState();
        } else {
            return null;
        }
    }
*/

    public String getIPAddress(String serviceName, String appName) {
        ServiceInfo entry = getServiceInfo(serviceName, appName);
        if (entry != null) {
            return entry.getIpAddress();
        } else {
            return null;
        }
    }

    public String getInstanceID(String serviceName, String appName) {
        ServiceInfo entry = getServiceInfo(serviceName, appName);
        if (entry != null) {
            return entry.getInstanceId();
        } else {
            return null;
        }
    }

    public String getProperty(String serviceName, String appName, String propertyName) {
        ServiceInfo entry = getServiceInfo(serviceName, appName);
        if (entry != null) {
            return entry.getProperties().get(propertyName);
        } else {
            return null;
        }
    }


    public ServiceInfo getServiceInfo(String serviceName, String appName) {
        Service matchingService = null;
        ServiceInfo serviceInfo = null;
        matchingService = getService(serviceName, appName);

        if (matchingService != null) {
            serviceInfo = new ServiceInfo();
            serviceInfo.setAppName(appName);
            serviceInfo.setServiceName(matchingService.getServiceName());
            if (matchingService.getProperty() != null) {
                if (matchingService.getProperty("ip-address") != null) {
                    serviceInfo.setIpAddress(matchingService.getProperty("ip-address").getValue());
                }

                if (matchingService.getProperty("vm-id") != null) {
                    serviceInfo.setInstanceId(matchingService.getProperty("vm-id").getValue());
                }

                List<Property> properties = matchingService.getProperty();
                for (Property property : properties) {
                    serviceInfo.getProperties().put(property.getName(), property.getValue());
                }
            }

            //TODO need a "Stateful" service type ?
            if (matchingService instanceof ApplicationScopedService) {
                serviceInfo.setState(((ApplicationScopedService) matchingService).getState());
            } else if (matchingService instanceof SharedService) {
                serviceInfo.setState(((SharedService) matchingService).getState());
            }
            serviceInfo.setServerType(matchingService.getType());

            for(Service service : getServices().getServices()){
                if(service.getParentService() != null && service.getParentService().equals(serviceName)){
                    if(appName != null && matchingService instanceof ApplicationScopedService){
                        if(service instanceof ApplicationScopedService){
                            ApplicationScopedService childService = (ApplicationScopedService)service;
                            if(!appName.equals(childService.getApplicationName())){
                                continue;
                            }
                        }else{
                            continue;
                        }
                    }
                    ServiceInfo childServiceInfo = getServiceInfo(service.getServiceName(), appName);
                    serviceInfo.addChildService(childServiceInfo);
                }
            }
        }
        return serviceInfo;
    }

    public ConfiguredService getExternalService(String serviceName){
        Services services = getServices();
        ExternalService externalService = null;
        for(Service service : services.getServices()){
            if(service.getServiceName().equals(serviceName) && service instanceof ExternalService){
                externalService = (ExternalService)service;
                break;
            }
        }
        if(externalService == null){
            throw new RuntimeException("No external service by name "+serviceName+" found");
        }
        ServiceDescription sd = new ServiceDescription();
        sd.setServiceScope(ServiceScope.EXTERNAL);
        sd.setName(externalService.getServiceName());
        sd.setServiceType(externalService.getType());

        Properties properties = new Properties();
        if (externalService.getConfigurations() != null && externalService.getConfigurations().getConfiguration() != null) {
            List<org.glassfish.paas.orchestrator.service.metadata.Property> configurationList = new ArrayList<org.glassfish.paas.orchestrator.service.metadata.Property>();
            for (Configuration config : externalService.getConfigurations().getConfiguration()) {
                org.glassfish.paas.orchestrator.service.metadata.Property property
                        = new org.glassfish.paas.orchestrator.service.metadata.Property();
                property.setName(config.getName());
                property.setValue(config.getValue());
                configurationList.add(property);
                properties.put(property.getName(), property.getValue());
            }
            sd.setConfigurations(configurationList);
        }
        return new ConfiguredServiceImpl(externalService.getServiceName(),
                getServiceType(externalService.getType()), sd, properties);
    }
    public Service getService(String serviceName, String appName) {
        Service matchingService = null;
        Services services = getServices();
        for (Service service : services.getServices()) {
            if (service.getServiceName().equals(serviceName)) {
                if (appName != null) {
                    if (service instanceof ApplicationScopedService) {
                        String applicationName = ((ApplicationScopedService) service).getApplicationName();
                        if (appName.equals(applicationName)) {
                            matchingService = service;
                        }
                    } else {
                        for (ServiceRef serviceRef : services.getServiceRefs()) {
                            if (appName.equals(serviceRef.getApplicationName()) &&
                                    serviceRef.getServiceName().equals(serviceName)) {
                                matchingService = service;
                                break;
                            }
                        }
                    }
                } else {
                    matchingService = service;
                }
                break;
            }
        }
        return matchingService;
    }

    public Services getServices() {
        Services services = domain.getExtensionByType(Services.class);
        if (services == null) {
            try {
                if (ConfigSupport.apply(new SingleConfigCode<Domain>() {
                    public Object run(Domain param) throws PropertyVetoException, TransactionFailure {
                        Services services = param.createChild(Services.class);
                        param.getExtensions().add(services);
                        return services;
                    }
                }, domain) == null) {
                    System.out.println("Unable to create 'services' config");
                }
            } catch (TransactionFailure transactionFailure) {
                System.out.println("Unable to create 'services' config due to : " + transactionFailure.getMessage());
                throw new RuntimeException(transactionFailure.getMessage(), transactionFailure);
            }
        }

        services = domain.getExtensionByType(Services.class);
        return services;
    }


    public void unregisterServiceInfo(final String serviceName, final String appName) {
        Services services = getServices();
        try {
            if (ConfigSupport.apply(new SingleConfigCode<Services>() {
                public Object run(Services servicesConfig) throws PropertyVetoException, TransactionFailure {
                    Service deletedService = null;
                    for (Service service : servicesConfig.getServices()) {
                        if (serviceName.equals(service.getServiceName())) {
                            if (service instanceof ApplicationScopedService) {
                                ApplicationScopedService appScopedService = (ApplicationScopedService) service;
                                if (appScopedService.getApplicationName().equals(appName)) {
                                    servicesConfig.getServices().remove(appScopedService);
                                    deletedService = appScopedService;
                                    break;
                                }
                            }else{
                                //shared or external service
                                servicesConfig.getServices().remove(service);
                                deletedService = service;
                                break;
                            }
                        }
                    }
                    return deletedService;
                }
            }, services) == null) {
                String msg = "Unable to remove service [" + serviceName + "]";
                System.out.println(msg);
                throw new RuntimeException(msg);
            }
        } catch (TransactionFailure transactionFailure) {
            transactionFailure.printStackTrace();
            throw new RuntimeException(transactionFailure.getMessage(), transactionFailure);
        }
    }


    public void registerServiceReference(final String serviceName, final String appName){
        Services services = getServices();
        boolean serviceFound = false;
        for (Service service : services.getServices()) {
            if (service.getServiceName().equals(serviceName) && (service instanceof SharedService) ||
                    (service instanceof ExternalService)) {
                serviceFound = true;
                break;
            }
        }
        if(serviceFound){
            try {
                ConfigSupport.apply(new SingleConfigCode<Services>() {
                    public Object run(Services param) throws PropertyVetoException, TransactionFailure {
                        ServiceRef serviceRef = param.createChild(ServiceRef.class);
                        serviceRef.setApplicationName(appName);
                        serviceRef.setServiceName(serviceName);
                        param.getServiceRefs().add(serviceRef);
                        return param;
                    }
                }, services);
            } catch (TransactionFailure exception) {
                throw new RuntimeException(exception.getMessage(), exception);
            }
        }else{
            throw new RuntimeException("no shared or external service by name ["+serviceName+"] found.");
        }
    }

    public void unregisterServiceReference(final String serviceName, final String appName){
        Services services = getServices();
            try {
                ConfigSupport.apply(new SingleConfigCode<Services>() {
                    public Object run(Services param) throws PropertyVetoException, TransactionFailure {
                        ServiceRef serviceRefToUnregister = null;

                        for (ServiceRef serviceRef : param.getServiceRefs()) {
                            if (serviceRef.getServiceName().equals(serviceName) && (serviceRef.getApplicationName().equals(appName))) {
                                serviceRefToUnregister = serviceRef;
                                break;
                            }
                        }
                        if(serviceRefToUnregister != null){
                            param.getServiceRefs().remove(serviceRefToUnregister);
                        }else{
                            throw new RuntimeException("no shared or external service by name ["+serviceName+"] is referred" +
                                    "by application ["+appName+"]");
                        }
                        return param;
                    }
                }, services);
            } catch (TransactionFailure exception) {
                throw new RuntimeException(exception.getMessage(), exception);
            }
    }

    public void registerService(final ServiceInfo entry) {
        Services services = getServices();
        try {
            if (entry.getAppName() == null) {
                ConfigSupport.apply(new SingleConfigCode<Services>() {
                    public Object run(Services servicesConfig) throws PropertyVetoException, TransactionFailure {
                        SharedService service = servicesConfig.createChild(SharedService.class);
                        service.setServiceName(entry.getServiceName());
                        service.setType(entry.getServerType());

                        if (entry.getParentService() != null) {
                            service.setParentService(entry.getParentService().getServiceName());
                        }
                        service.setState(entry.getState());

                        Map<String, String> properties = entry.getProperties();
                        if (properties != null) {
                            for (Map.Entry<String, String> entry : properties.entrySet()) {
                                Property prop = service.createChild(Property.class);
                                prop.setName(entry.getKey());
                                prop.setValue(entry.getValue());
                                service.getProperty().add(prop);
                            }
                        }

                        servicesConfig.getServices().add(service);
                        return service;
                    }
                }, services);
            } else if (ConfigSupport.apply(new SingleConfigCode<Services>() {
                public Object run(Services servicesConfig) throws PropertyVetoException, TransactionFailure {
                    ApplicationScopedService service = servicesConfig.createChild(ApplicationScopedService.class);

                    service.setServiceName(entry.getServiceName());
                    service.setType(entry.getServerType());

                    if (entry.getAppName() != null) {
                        service.setApplicationName(entry.getAppName());
                    }
                    if (entry.getParentService() != null) {
                        service.setParentService(entry.getParentService().getServiceName());
                    }
                    service.setState(entry.getState());

                    Map<String, String> properties = entry.getProperties();
                    if (properties != null) {
                        for (Map.Entry<String, String> entry : properties.entrySet()) {
                            Property prop = service.createChild(Property.class);
                            prop.setName(entry.getKey());
                            prop.setValue(entry.getValue());
                            service.getProperty().add(prop);
                        }
                    }

                    servicesConfig.getServices().add(service);
                    return service;
                }
            }, services) == null) {
                String msg = "Unable to register service [" + entry.getServiceName() + "]";
                logger.log(Level.WARNING, msg);
                throw new RuntimeException(msg);
            }
        } catch (TransactionFailure exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    public ServiceStatus getServiceStatus(ServiceInfo entry) {
        if (entry.getState() != null && entry.getState().equalsIgnoreCase(ServiceStatus.RUNNING.toString())) {
            return ServiceStatus.RUNNING;
        }
        if (entry.getState() != null && entry.getState().equalsIgnoreCase(ServiceStatus.STARTING.toString())) {
            return ServiceStatus.STARTING;
        }
        if (entry.getState() != null && entry.getState().equalsIgnoreCase(ServiceStatus.STOPPED.toString())) {
            return ServiceStatus.STOPPED;
        }
        if (entry.getState() != null && entry.getState().equalsIgnoreCase(ServiceStatus.STOPPED.toString())) {
            return ServiceStatus.STOPPED;
        }
        //TODO handle delete in progress/create in progress later.

        return ServiceStatus.UNKNOWN;
    }

    public ServiceProvisioningEngines getServiceProvisioningEngines() {
        ServiceProvisioningEngines spes = domain.getExtensionByType(ServiceProvisioningEngines.class);
        if (spes == null) {
            try {
                if (ConfigSupport.apply(new SingleConfigCode<Domain>() {
                    public Object run(Domain param) throws PropertyVetoException, TransactionFailure {
                        ServiceProvisioningEngines spes = param.createChild(ServiceProvisioningEngines.class);
                        param.getExtensions().add(spes);
                        return spes;
                    }
                }, domain) == null) {
                    logger.log(Level.SEVERE, "unable.tocreate.spe");
                }
            } catch (TransactionFailure transactionFailure) {
                logger.log(Level.SEVERE, "unable.tocreate.spe",transactionFailure);
                throw new RuntimeException(transactionFailure.getMessage(), transactionFailure);
            }
        }

        spes = domain.getExtensionByType(ServiceProvisioningEngines.class);
        return spes;
    }

/*    public ServiceDescription getExternalServiceDescription(ServiceInfo serviceInfo){
        ServiceDescription sd = null;
        Service service = getService(serviceInfo.getServiceName(), serviceInfo.getAppName());
        if(service != null){
            if(service instanceof ExternalService){
                ExternalService externalService = (ExternalService)service;

                Object characteristicsOrTemplate = null;

                List<org.glassfish.paas.orchestrator.service.metadata.Property> configuration = null;
                if(externalService.getConfigurations() != null){
                    if(externalService.getConfigurations().getConfiguration() != null){
                        configuration = new ArrayList<org.glassfish.paas.orchestrator.service.metadata.Property>();
                        for(Configuration config : externalService.getConfigurations().getConfiguration()){
                            org.glassfish.paas.orchestrator.service.metadata.Property property =
                                    new org.glassfish.paas.orchestrator.service.metadata.Property(config.getName(), config.getValue());
                            configuration.add(property);
                        }
                    }
                }
                sd = new ServiceDescription(externalService.getServiceName(), null,
                        null, characteristicsOrTemplate, configuration);
            }
        }else{
            throw new RuntimeException("No such service ["+serviceInfo.getServiceName()+"] is available");
        }
        return sd;
    }*/

    //TODO implementation for external and app-scoped-service also.
    public ServiceDescription getSharedServiceDescription(ServiceInfo serviceInfo){
        ServiceDescription sd = null;
        Service service = getService(serviceInfo.getServiceName(), serviceInfo.getAppName());
        if(service != null){
            if(service instanceof SharedService){
                SharedService sharedService = (SharedService)service;

                Object characteristicsOrTemplate = null;
                Characteristics characteristics = sharedService.getCharacteristics();
                if(characteristics != null){
                    if(characteristics.getCharacteristic() != null){
                        List<org.glassfish.paas.orchestrator.service.metadata.Property> serviceCharacteristicsList
                                = new ArrayList<org.glassfish.paas.orchestrator.service.metadata.Property>();
                        for(Characteristic characteristic : characteristics.getCharacteristic()){
                            org.glassfish.paas.orchestrator.service.metadata.Property property =
                                    new org.glassfish.paas.orchestrator.service.metadata.Property(characteristic.getName(), characteristic.getValue());
                            serviceCharacteristicsList.add(property);
                        }
                        ServiceCharacteristics serviceCharacteristics = new ServiceCharacteristics();
                        serviceCharacteristics.setServiceCharacteristics(serviceCharacteristicsList);
                        characteristicsOrTemplate = serviceCharacteristics;
                    }
                }else if(sharedService.getTemplate() != null){
                    TemplateIdentifier tid = new TemplateIdentifier();
                    tid.setId(sharedService.getTemplate());
                    characteristicsOrTemplate = tid;
                }

                List<org.glassfish.paas.orchestrator.service.metadata.Property> configuration = null;
                if(sharedService.getConfigurations() != null){
                    if(sharedService.getConfigurations().getConfiguration() != null){
                        configuration = new ArrayList<org.glassfish.paas.orchestrator.service.metadata.Property>();
                        for(Configuration config : sharedService.getConfigurations().getConfiguration()){
                            org.glassfish.paas.orchestrator.service.metadata.Property property =
                                    new org.glassfish.paas.orchestrator.service.metadata.Property(config.getName(), config.getValue());
                            configuration.add(property);
                        }
                    }
                }

                sd = new ServiceDescription(sharedService.getServiceName(), null,
                        null, characteristicsOrTemplate, configuration);
                sd.setVirtualClusterName(serviceInfo.getServiceName());
            }
        }else{
            throw new RuntimeException("No such service ["+serviceInfo.getServiceName()+"] is available");
        }
        return sd;
    }

    public org.glassfish.paas.orchestrator.service.ServiceType getServiceType(String serviceTypeString){
        Collection<org.glassfish.paas.orchestrator.service.ServiceType> serviceTypes =
                habitat.getAllByContract(org.glassfish.paas.orchestrator.service.ServiceType.class);
        for(org.glassfish.paas.orchestrator.service.ServiceType serviceType : serviceTypes){
            if(serviceType.toString().equals(serviceTypeString)){
                return serviceType;
            }
        }
        throw new RuntimeException("Unable to determine the type");
    }

    public void unregisterService(String appName, org.glassfish.paas.orchestrator.service.spi.Service service) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServiceName(service.getName());
        serviceInfo.setAppName(appName);
        serviceInfo.setInstanceId(service.getServiceProperties().getProperty("vm-id"));
        serviceInfo.setIpAddress(service.getServiceProperties().getProperty("ip-address"));
        serviceInfo.setServerType(service.getServiceType().toString());
        if(service instanceof ProvisionedService){
            serviceInfo.setState(((ProvisionedService)service).getStatus().toString());
        }
        unregisterServiceInfo(service.getName(), appName);
        if(service.getChildServices() != null){
            for(org.glassfish.paas.orchestrator.service.spi.Service serviceNode : service.getChildServices()){
                unregisterServiceInfo(serviceNode.getName(), appName);
            }
        }
    }

    public void unregisterService(ServiceInfo serviceInfo) {
        unregisterServiceInfo(serviceInfo.getServiceName(), serviceInfo.getAppName());
        if(serviceInfo.getChildServices() != null){
            for(ServiceInfo serviceNode : serviceInfo.getChildServices()){
                unregisterServiceInfo(serviceNode.getServiceName(), serviceNode.getAppName());
            }
        }
    }

    public void registerService(String appName, org.glassfish.paas.orchestrator.service.spi.Service service,
                                org.glassfish.paas.orchestrator.service.spi.Service parentService) {
        ServiceInfo parentServiceInfo = null;
        if(parentService != null){
            parentServiceInfo = createServiceInfo(appName, parentService, null);
        }
        ServiceInfo serviceInfo = createServiceInfo(appName, service, parentServiceInfo);
        registerService(serviceInfo);
        if(service.getChildServices() != null){
            for(org.glassfish.paas.orchestrator.service.spi.Service serviceNode : service.getChildServices()){
                ServiceInfo serviceNodeInfo = createServiceInfo(appName, serviceNode, serviceInfo);
                serviceInfo.addChildService(serviceNodeInfo);
                registerService(serviceNodeInfo);
            }
        }
    }

    public void fireServiceChangeEvent(ServiceChangeEvent.Type type,
                                          org.glassfish.paas.orchestrator.service.spi.Service ps) {
        for(ServiceChangeListener listener : getServiceChangeListeners()) {
            try{
            listener.onEvent(new ServiceChangeEvent(type, this, null, ps));
            }catch(Exception e){
                if(logger.isLoggable(Level.FINEST)){
                 logger.log(Level.FINEST,localStrings.getString("failure.listener.event"), e);
                }
            }
        }
    }

    private Collection<ServiceChangeListener> getServiceChangeListeners() {
        return habitat.getAllByContract(ServiceChangeListener.class);
    }

    private ServiceInfo createServiceInfo(String appName, org.glassfish.paas.orchestrator.service.spi.Service serviceNode,
                                          ServiceInfo parentServiceInfo) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServiceName(serviceNode.getName());
        serviceInfo.setAppName(appName);
        if(serviceNode.getServiceProperties().getProperty("vm-id") != null){
            serviceInfo.setInstanceId(serviceNode.getServiceProperties().getProperty("vm-id"));
        }
        if(serviceNode.getServiceProperties().getProperty("ip-address") != null){
            serviceInfo.setIpAddress(serviceNode.getServiceProperties().getProperty("ip-address"));
        }

        serviceInfo.setServerType(serviceNode.getServiceType().toString());
        if(serviceNode instanceof ProvisionedService){
            serviceInfo.setState(((ProvisionedService) serviceNode).getStatus().toString());
        }
        Properties serviceProperties = serviceNode.getServiceProperties();
        if(serviceProperties != null){
            for (String key : serviceProperties.stringPropertyNames()) {
                serviceInfo.setProperty(key, (String)serviceProperties.get(key));
            }
        }
        serviceInfo.setParentService(parentServiceInfo);
        return serviceInfo;
    }

    public PaasApplications getPaasApplications() {
        PaasApplications paasApplications = domain.getExtensionByType(PaasApplications.class);
        if (paasApplications == null) {
            try {
                if (ConfigSupport.apply(new SingleConfigCode<Domain>() {
                    public Object run(Domain param) throws PropertyVetoException, TransactionFailure {
                        PaasApplications  paasApplications = param.createChild(PaasApplications.class);
                        param.getExtensions().add(paasApplications);
                        return paasApplications;
                    }
                }, domain) == null) {
                    logger.log(Level.SEVERE, "unable.tocreate.paasapplications");
                }
            } catch (TransactionFailure transactionFailure) {
                logger.log(Level.SEVERE, "unable.tocreate.paasapplications",transactionFailure);
                throw new RuntimeException(transactionFailure.getMessage(), transactionFailure);
            }
        }

        paasApplications = domain.getExtensionByType(PaasApplications.class);
        return paasApplications;
    }
}

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

package org.glassfish.elasticity.metrics.commands;

import com.sun.enterprise.config.serverbeans.Domain;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.elasticity.config.serverbeans.*;

import javax.inject.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.*;

import java.beans.PropertyVetoException;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: cmott
 * Date: 10/19/11
 */
@Service(name="create-jvm-memory-metric-gatherer")
@I18n("create.jvm-memory.metric.gatherer")
@Scoped(PerLookup.class)
public class CreateJvmMemoryMetricGathererCommand implements AdminCommand{

   @Inject
   Domain domain;

   @Inject
   ElasticServices elasticServices;

  @Param(name="name", primary = true)
   String name;

  @Param(name="service")
  String servicename;

    @Param(name="collection-rate", optional = true)
    int collectionRate;

    @Param(name="retain-data", optional = true)
    int retainData;

    @Param(name="auto-start", defaultValue = "false", optional = true)
    boolean autoStart;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        Logger logger= context.logger;

        if (elasticServices == null)   {
            //service doesn't exist
            String msg = Strings.get("elasticity.not.found", servicename);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        ElasticServiceConfig elasticService= elasticServices.getElasticService(servicename);
        if (elasticService == null) {
            //service doesn't exist
            String msg = Strings.get("noSuchService", servicename);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }
        if (elasticService.getMetricGatherers().getMetricGatherer(name) == null) {
             try {
                 createMetricGathererElement(name);
            } catch(TransactionFailure e) {
                logger.warning("failed.to.create.metric.gatherer " + name);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(e.getMessage());
             }
         } //if already exists then it's an error
    }

        public void createMetricGathererElement(final String alertName) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode() {
            @Override
            public Object run(ConfigBeanProxy param) throws PropertyVetoException, TransactionFailure {
                // get the transaction
                Transaction t = Transaction.getTransaction(param);
                if (t != null) {
                    ElasticServiceConfig elasticService = elasticServices.getElasticService(servicename);
                    if (elasticService != null) {
                        ElasticServiceConfig writeableService = t.enroll(elasticService);
                        MetricGatherers writeableMGs = elasticService.getMetricGatherers();
                        if (writeableMGs == null)
                            writeableMGs = writeableService.createChild(MetricGatherers.class);
                        else
                            writeableMGs = t.enroll(writeableMGs);

                        MetricGathererConfig writeableGatherer = writeableMGs.createChild(MetricGathererConfig.class);
                        if (name != null)
                            writeableGatherer.setName(name);
                        if (autoStart)
                            writeableGatherer.setAutoStart(autoStart);
                        if (collectionRate > 0)
                            writeableGatherer.setCollectionRate(collectionRate);

                        writeableMGs.getMetricGatherer().add(writeableGatherer);
                        writeableService.setMetricGatherers(writeableMGs);
                    }
                }
                return Boolean.TRUE;
            }

        }, domain);
    }

    @Service(name = "create-memory-alert")
    @I18n("create.memory.alert")
    @Scoped(PerLookup.class)
    @RestEndpoints({ @RestEndpoint(configBean = ElasticServices.class, opType = RestEndpoint.OpType.POST, path = "create-memory-alert", description = "Create memory alert") })
    public static class CreateMemoryAlert implements AdminCommand{

        @org.jvnet.hk2.annotations.Inject(optional = true)
        ElasticServices elasticServices;

        @org.jvnet.hk2.annotations.Inject
        private CommandRunner cr;

        @Param (name ="alertname", primary = true)
        String alertname;

        @Param(name="servicename")
        String servicename;

        @Param(name="threshold")
        String threshold;

        @Param(name="enabled", defaultValue="true",optional=true)
        boolean enabled;

        @Param(name="sample-interval", optional = true)
        String sampleInterval;

        @Override
        public void execute(AdminCommandContext context) {
            ActionReport report = context.getActionReport();
            Logger logger= context.logger;

            if (elasticServices == null)   {
                //service doesn't exist
                String msg = Strings.get("elasticity.not.enabled");
                logger.warning(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
            ElasticServiceConfig elasticService= elasticServices.getElasticService(servicename);
            if (elasticService == null) {
                //service doesn't exist
                String msg = Strings.get("noSuchService", servicename);
                logger.warning(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }

            AlertConfig  alert = elasticService.getAlerts().getAlert(alertname);
            if (elasticService == null) {
                //alert doesn't exist
                String msg = Strings.get("noSuchAlert", alertname);
                logger.warning(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
            //delete the existing alert

            CommandRunner.CommandInvocation ci = cr.getCommandInvocation("delete-alert", report);
            ParameterMap map = new ParameterMap();
            map.add("service", servicename);
            map.add("DEFAULT", alertname);
            ci.parameters(map);
            ci.execute();

            //create a new alert with new values
            String expression = "any[avg(jvm_memory.heap.used)*100/jvm_memory.maxMemory]  > " +threshold ;

            ci = cr.getCommandInvocation("create-alert", report);
            map = new ParameterMap();
            map.add("service", servicename);
            if (sampleInterval != null)
                map.add("sampleinterval",sampleInterval);
            map.add("expression", expression);
            if(!enabled)
                map.add("enabled", "false");
            map.add("DEFAULT", alertname);
            ci.parameters(map);
            ci.execute();

            //add alarm to the alert , only add alarm state
            ci = cr.getCommandInvocation("add-alert-action",report);
            map = new ParameterMap();
            map.add("service", servicename);
            map.add("actionref","scale-up-action");
            map.add("state","alarm-state");
            map.add("DEFAULT", alertname);
            ci.parameters(map);
            ci.execute();
        }
    }
}

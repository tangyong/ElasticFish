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

package org.glassfish.elasticity.engine.commands;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.elasticity.config.serverbeans.*;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import javax.inject.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.*;
import com.sun.enterprise.config.serverbeans.Domain;

import java.beans.PropertyVetoException;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: cmott
 * Date: 10/24/11
 */
@Service(name="create-log-action")
@I18n("creaet.log.action")
@Scoped(PerLookup.class)
public class CreateLogActionCommand implements AdminCommand {
    @Inject
    ElasticServices elasticServices;

    @Inject
    Domain domain;

    @Param(name="service")
    String servicename;

    @Param(name="name", primary = true)
    String name;

    @Param(name="level", optional = true)
    String level;


    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        Logger logger= context.logger;

        if (elasticServices == null)   {
            // elastic service doesn't exist
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
    // check if the log action already exists
        if (elasticService.getActions().getLogAction(name) != null) {
           logger.warning("delete.action.exists " + name);
           report.setActionExitCode(ActionReport.ExitCode.FAILURE);
           report.setMessage("delete.action.exists "+ name);
        }  else {
            try {
                createLogActionElement(name);
            } catch(TransactionFailure e) {
                 logger.warning("failed.to.create.log.action " + name);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                 report.setMessage(e.getMessage());
             }
        }
    }
        public void createLogActionElement(final String alertName) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode() {
            @Override
            public Object run(ConfigBeanProxy param) throws PropertyVetoException, TransactionFailure {
                // get the transaction
                Transaction t = Transaction.getTransaction(param);
                if (t != null) {
                    ElasticServiceConfig elasticService = elasticServices.getElasticService(servicename);
                    if (elasticService != null) {
                        ElasticServiceConfig writeableService = t.enroll(elasticService);
                        Actions writeableAction = elasticService.getActions();
                        if (writeableAction == null)
                            writeableAction = writeableService.createChild(Actions.class);
                        else
                            writeableAction = t.enroll(writeableAction);

                        LogAction writeableLog = writeableAction.createChild(LogAction.class);
                        if (name != null)
                            writeableLog.setName(name);
                        if (level != null)
                            writeableLog.setLogLevel(level);
                        writeableLog.setType("log");

                        writeableAction.getLogAction().add(writeableLog);
                    }
                }
                return Boolean.TRUE;
            }

        }, domain);
    }
}

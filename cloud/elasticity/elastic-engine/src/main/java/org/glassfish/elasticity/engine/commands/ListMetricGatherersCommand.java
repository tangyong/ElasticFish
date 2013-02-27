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
import org.glassfish.elasticity.api.AbstractMetricGatherer;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import javax.inject.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.Param;
import org.jvnet.hk2.component.PerLookup;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: cmott
 * Date: 9/27/11
 */
@Service(name="list-metric-gatherers")
@I18n("list.metric.gatherers")
@Scoped(PerLookup.class)
public class ListMetricGatherersCommand  implements AdminCommand {

    @Inject
    AbstractMetricGatherer[] metricGatherers;

    @Param(name="service")
    String servicename;

    private static final String EOL = "\n";

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        Logger logger= context.logger;

        // Look for the Metric Gatherer services and list them
        // Eventually want to list if they are running, for now they are
        StringBuilder sb = new StringBuilder();
        boolean firstName =true;

        for (AbstractMetricGatherer mg : metricGatherers) {
            String metricName = mg.getClass().getAnnotation(Service.class).toString();
            if ( firstName)
                firstName = false;
             else
                sb.append(EOL);
             int nameIndex = metricName.indexOf("name=") + 5;
            int endNameIndex = metricName.indexOf(",");
            sb.append(metricName.substring(nameIndex, endNameIndex));

        }

        report.setMessage(sb.toString());
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        }
}

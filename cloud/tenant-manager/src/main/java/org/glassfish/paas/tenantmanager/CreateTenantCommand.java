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
package org.glassfish.paas.tenantmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.paas.tenantmanager.impl.SecurityStore;
import org.glassfish.paas.tenantmanager.impl.TenantManagerEx;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

import javax.inject.Inject;

/**
 * Create Tenant Command using zero config initialization.
 * 
 * @author Bhakti Mehta
 * @author Andriy Zhdanov
 *
 */
@Service(name = "create-tenant")
@Scoped(PerLookup.class)
@I18n("create.tenant")
@TargetType(value={CommandTarget.STANDALONE_INSTANCE})
@org.glassfish.api.admin.ExecuteOn({RuntimeType.DAS})
public final class CreateTenantCommand implements AdminCommand {
    @Inject
    private TenantManagerEx tm;

    @Param (primary=true)
    String tenantId;

    //TODO: this is still a String, need to convert to char[]
    @Param(name="password", password=true)
    private String password;

    private ActionReport report;

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(CreateTenantCommand.class);

    final private static Logger logger = LogDomains.getLogger(CreateTenantCommand.class, LogDomains.PAAS_LOGGER);

    @Inject
    SecurityStore securityStore;

    @Override
    public void execute(AdminCommandContext context) {
        report = context.getActionReport();

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        if (tenantId == null) {
            String msg = localStrings.getLocalString("NullTenantId", "TenantId cannot be null");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        logger.log(Level.INFO, "create.tenant.admin", tenantId + ".admin");
        securityStore.create(tenantId + ".admin", password.toCharArray());

        // see TenantConfigService.getTenantManagerConfig for zero-config initialization

        logger.log(Level.INFO, "create.tenant", tenantId);
        tm.create(tenantId, "admin");

        // note, both creates fail independently if already exists
    }

}
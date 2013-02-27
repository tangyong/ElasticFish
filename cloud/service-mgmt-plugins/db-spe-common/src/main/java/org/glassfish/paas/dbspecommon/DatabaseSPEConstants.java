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
package org.glassfish.paas.dbspecommon;


/**
 * Common Constants used by the Database Service Provisioning Engines
 *
 * @author Shalini M
 */
public interface DatabaseSPEConstants {

    /**
     * Service Configuration related properties
     */
    static final String INIT_SQL_SVC_CONFIG = "database.init.sql";

    static final String DATABASE_NAME_SVC_CONFIG = "database.name";

    static final String INITSQL_SVC_CONFIG = "init.sql";

    static final String SERVICE_PROPERTIES = "service.properties";
    /**
     * Service Characteristics related properties
     */
    static final String SERVICE_TYPE = "service-type";

    static final String RDBMS_ServiceType = "Database";

    static final String RESOURCE_TYPE = "resourcetype";

    static final String SERVICE_INIT_TYPE_LAZY = "lazy";

    /**
     * Template index related constants
     */
    static final String SERVICE_TYPE_INDEX = "ServiceType";

    static final String VIRTUALIZATION_TYPE = "VirtualizationType";

    /**
     * Provisioned service related constants
     */
    static final String VIRTUAL_MACHINE_ID = "vm-id";

    static final String VIRTUAL_MACHINE_IP_ADDRESS = "ip-address";

    /**
     * Database connectivity related constants
     */
    static final String USER = "user";

    static final String PASSWORD = "password";

    static final String DATABASENAME = "databasename";

    static final String PORT = "PortNumber";

    static final String HOST = "host";

    static final String URL = "URL";

    static final String CLASSNAME = "classname";

    static final String DATASOURCE = "javax.sql.DataSource";

    /**
     * Temporary constants
     */
    public String DB_INITIALIZED = "_db_initialized";

    public String DB_UNINITIALIZED = "_db_uninitialized";
}

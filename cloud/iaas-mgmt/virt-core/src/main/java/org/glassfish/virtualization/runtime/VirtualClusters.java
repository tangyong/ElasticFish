/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.virtualization.runtime;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import org.glassfish.virtualization.spi.IAAS;
import org.glassfish.virtualization.spi.VirtException;
import org.glassfish.virtualization.spi.VirtualCluster;
import org.jvnet.hk2.annotations.Service;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Manages instances of virtual clusters
 */
@Service
public class VirtualClusters {

	@Inject
    Domain domain;
	
	@Inject
    IAAS iaas;
	
    final Map<String, VirtualCluster> clusterMap = new HashMap<String, VirtualCluster>();

    public VirtualClusters() {
    }

    public void add(VirtualCluster vc) {
        clusterMap.put(vc.getConfig().getName(), vc);
    }

    public void remove(VirtualCluster vc) {
        vc.delete();
        clusterMap.remove(vc.getConfig().getName());
    }

    public VirtualCluster byName(String clusterName) throws VirtException {
        if (!clusterMap.containsKey(clusterName)) {
            Cluster cluster = domain.getClusterNamed(clusterName);
            if (cluster==null) {
                throw new InvalidParameterException(clusterName + " cluster not found");
            }
            synchronized (this) {
                if (!clusterMap.containsKey(clusterName)) {
                    clusterMap.put(clusterName, new VirtualCluster(iaas, cluster));
                }
            }
        }
        return clusterMap.get(clusterName);
    }

    public Iterable<VirtualCluster> getClusters() {
        return clusterMap.values();
    }

}

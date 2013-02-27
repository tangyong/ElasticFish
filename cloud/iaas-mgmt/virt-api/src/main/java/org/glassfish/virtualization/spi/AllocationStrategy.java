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
package org.glassfish.virtualization.spi;

import java.util.Collection;
import java.util.List;

/**
 * An allocation strategy is responsible for allocating virtual machines given
 * a set of server pools and machines within these server pools.
 *
 * @author Jerome Dochez
 */
public interface AllocationStrategy {

    /**
     * allocate a virtual machine within the provided server pools, notifying of progress all listeners
     *
     * @param serverPools the server pools elligible to run the virtual machine
     * @param constraints the virtual machine requested characteristics
     * @param listeners listeners to register before any work is performed
     * @return a phased future implementation that can be used to monitor progress and get the
     * result of the allocation as {@link VirtualMachine} instance.
     * @throws VirtException if the allocation failed.
     */
    PhasedFuture<AllocationPhase, VirtualMachine> allocate(Collection<ServerPool> serverPools,
                                                               AllocationConstraints constraints,
                                                               List<Listener<AllocationPhase>> listeners)
        throws VirtException;

    /**
     * Returns the server pool allocation strategy for a particular server pool instance.
     * @param serverPool the server pool instance
     * @return the server pool allocation strategy
     */
    ServerPoolAllocationStrategy getServerPoolStrategy(ServerPool serverPool);
}

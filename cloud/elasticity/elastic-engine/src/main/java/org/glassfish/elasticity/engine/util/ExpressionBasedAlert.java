/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.elasticity.engine.util;

import org.glassfish.elasticity.api.AbstractAlert;
import org.glassfish.elasticity.api.AlertContext;
import org.glassfish.elasticity.config.serverbeans.AlertConfig;
import org.glassfish.elasticity.engine.container.AlertContextImpl;
import org.glassfish.elasticity.expression.*;
import org.glassfish.elasticity.util.NotEnoughMetricDataException;
import org.glassfish.hk2.Services;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple Alert that uses an expression
 *
 * @param <C>
 * @author Mahesh Kannan
 */
public class ExpressionBasedAlert<C extends AlertConfig>
        extends AbstractAlert<C> {

    private static final Logger _logger = EngineUtil.getLogger();

    private C config;

    private Services services;

    public void initialize(Services services, C config) {
        this.services = services;
        this.config = config;
    }

    public AlertState execute(AlertContext ctx) {

        _logger.log(Level.FINE, "About to execute alert: " + config.getName() + "; service = "
                + ctx.getElasticService().getName());

        AlertContextImpl ctxImpl = (AlertContextImpl) ctx;
        ElasticExpressionEvaluator evaluator = new ElasticExpressionEvaluator(services, ctxImpl);
        AlertState result = AlertState.NO_DATA;
        try {
            ExpressionParser parser = new ExpressionParser(config.getExpression());
            ExpressionNode parsedNode = parser.parse();

            result = ((Boolean) evaluator.evaluate(parsedNode)).booleanValue()
                ? AlertState.ALARM : AlertState.OK;
        } catch (NotEnoughMetricDataException nemdEx) {
            _logger.log(Level.FINE, "Not enough data to execute alert", nemdEx);
        } catch (ExpressionEvaluationException ex) {
            _logger.log(Level.WARNING, "Exception during alert execution", ex);
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "Exception during alert execution", ex);
        }

        _logger.log(Level.FINE, "Alert executed. AlertState = " + result);
        return result;
    }


    private void printExpression() {
            ExpressionParser parser = new ExpressionParser(config.getExpression());
            ExpressionNode parsedNode = parser.parse();

            StringBuilder sb = new StringBuilder("ExpressionBasedAlert[" + config.getName() + "] Expression: " + parsedNode);
            System.out.println(sb.toString());
    }

}

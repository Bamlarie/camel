/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.reifier;

import org.apache.camel.AsyncProcessor;
import org.apache.camel.ErrorHandlerFactory;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RoutingSlipDefinition;
import org.apache.camel.processor.RoutingSlip;
import org.apache.camel.reifier.errorhandler.ErrorHandlerReifier;
import org.apache.camel.spi.RouteContext;

import static org.apache.camel.model.RoutingSlipDefinition.DEFAULT_DELIMITER;

public class RoutingSlipReifier extends ExpressionReifier<RoutingSlipDefinition<?>> {

    public RoutingSlipReifier(RouteContext routeContext, ProcessorDefinition<?> definition) {
        super(routeContext, (RoutingSlipDefinition) definition);
    }

    @Override
    public Processor createProcessor() throws Exception {
        Expression expression = createExpression(definition.getExpression());
        String delimiter = parseString(definition.getUriDelimiter());
        if (delimiter == null) {
            delimiter = DEFAULT_DELIMITER;
        }

        RoutingSlip routingSlip = new RoutingSlip(camelContext, expression, delimiter);
        if (definition.getIgnoreInvalidEndpoints() != null) {
            routingSlip.setIgnoreInvalidEndpoints(parseBoolean(definition.getIgnoreInvalidEndpoints(), false));
        }
        if (definition.getCacheSize() != null) {
            routingSlip.setCacheSize(parseInt(definition.getCacheSize()));
        }

        // and wrap this in an error handler
        ErrorHandlerFactory builder = routeContext.getErrorHandlerFactory();
        // create error handler (create error handler directly to keep it light
        // weight,
        // instead of using ProcessorDefinition.wrapInErrorHandler)
        AsyncProcessor errorHandler = (AsyncProcessor)ErrorHandlerReifier.reifier(routeContext, builder).createErrorHandler(routingSlip.newRoutingSlipProcessorForErrorHandler());
        routingSlip.setErrorHandler(errorHandler);

        return routingSlip;
    }

}

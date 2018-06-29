/**
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
package org.apache.camel.component.teiid;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.sql.SqlComponent;
import org.apache.camel.util.CamelContextHelper;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.teiid.runtime.EmbeddedConfiguration;
import org.teiid.runtime.EmbeddedServer;
import org.teiid.translator.ExecutionFactory;
import org.teiid.translator.TranslatorException;

/**
 * The <a href="http://camel.apache.org/teiid-component.html">Teiid Component</a> is for working with data sources over Teiid abstraction using JDBC queries.
 * 
 */
public class TeiidComponent extends SqlComponent {
	
	EmbeddedServer server = new EmbeddedServer();
	List<URL> vdbs = new ArrayList<>();

    public TeiidComponent() {
    }
    
    public TeiidComponent(CamelContext camelContext) {
    	super(camelContext);
    }
    
    @Override
    protected void doStart() throws Exception {
    	super.doStart();
    	server.start(new EmbeddedConfiguration());
    	
    	for(URL vdb: vdbs) {
    		try (InputStream in = vdb.openStream()) {
	    		server.deployVDB(in);
			} catch (Exception e) {
				throw new RuntimeCamelException(e);
			}
    	}
    }
    
    @Override
    protected void doStop() throws Exception {
    	super.doStop();
    	server.stop();
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
    	BasicDataSource teiidDataSource = new BasicDataSource();
    	teiidDataSource.setDriver(server.getDriver());
    	teiidDataSource.setUrl("jdbc:teiid:Projects");
    	
    	DataSource target = teiidDataSource;

        String parameterPlaceholderSubstitute = getAndRemoveParameter(parameters, "placeholder", String.class, "#");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(target);
        Map<String, Object> templateOptions = IntrospectionSupport.extractProperties(parameters, "template.");
        IntrospectionSupport.setProperties(jdbcTemplate, templateOptions);

        String query = remaining;
        if (isUsePlaceholder()) {
            query = query.replaceAll(parameterPlaceholderSubstitute, "?");
        }

        String onConsume = getAndRemoveParameter(parameters, "consumer.onConsume", String.class);
        if (onConsume == null) {
            onConsume = getAndRemoveParameter(parameters, "onConsume", String.class);
        }
        if (onConsume != null && isUsePlaceholder()) {
            onConsume = onConsume.replaceAll(parameterPlaceholderSubstitute, "?");
        }
        String onConsumeFailed = getAndRemoveParameter(parameters, "consumer.onConsumeFailed", String.class);
        if (onConsumeFailed == null) {
            onConsumeFailed = getAndRemoveParameter(parameters, "onConsumeFailed", String.class);
        }
        if (onConsumeFailed != null && isUsePlaceholder()) {
            onConsumeFailed = onConsumeFailed.replaceAll(parameterPlaceholderSubstitute, "?");
        }
        String onConsumeBatchComplete = getAndRemoveParameter(parameters, "consumer.onConsumeBatchComplete", String.class);
        if (onConsumeBatchComplete == null) {
            onConsumeBatchComplete = getAndRemoveParameter(parameters, "onConsumeBatchComplete", String.class);
        }
        if (onConsumeBatchComplete != null && isUsePlaceholder()) {
            onConsumeBatchComplete = onConsumeBatchComplete.replaceAll(parameterPlaceholderSubstitute, "?");
        }

        TeiidEndpoint endpoint = new TeiidEndpoint(uri, this, jdbcTemplate, query);
        endpoint.setPlaceholder(parameterPlaceholderSubstitute);
        endpoint.setUsePlaceholder(isUsePlaceholder());
        endpoint.setOnConsume(onConsume);
        endpoint.setOnConsumeFailed(onConsumeFailed);
        endpoint.setOnConsumeBatchComplete(onConsumeBatchComplete);
        endpoint.setDataSource(target);
        endpoint.setDataSourceRef(null);
        endpoint.setTemplateOptions(templateOptions);
        return endpoint;
    }

	public void deployVDB(URL url) {
		vdbs.add(url);	
	}

	public void addDataSource(String dataSourceRef) {
		DataSource ds = CamelContextHelper.mandatoryLookup(getCamelContext(), dataSourceRef, DataSource.class);
		server.addConnectionFactory("java:/" + dataSourceRef, ds);
	}
	
	public void addTranslator(String translatorClass) {
		try {
			@SuppressWarnings("unchecked")
			Class<? extends ExecutionFactory<?, ?>> clazz = (Class<? extends ExecutionFactory<?, ?>>) getClass().getClassLoader().loadClass(translatorClass);
			server.addTranslator(clazz);
		} catch (ClassNotFoundException | TranslatorException e) {
			throw new RuntimeCamelException(e);
		}
	}
}

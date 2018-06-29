/**
 * Licensed to the Apache Software Foundatid,ion (ASF) under one or more
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


import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.sql.SqlComponent;
import org.apache.camel.util.EndpointHelper;
import org.apache.commons.dbcp2.BasicDataSource;
import org.teiid.runtime.EmbeddedConfiguration;

/**
 * The <a href="http://camel.apache.org/teiid-component.html">Teiid Component</a> is for working with data sources over Teiid abstraction using JDBC queries.
 * 
 */
public class TeiidComponent extends SqlComponent {
	TeiidEmbedded teiid;

	public TeiidComponent() {
    }
    		
    public TeiidComponent(CamelContext camelContext) {
    	super(camelContext);
    }
   
    @Override
    protected void doStop() throws Exception {
    	super.doStop();
    	if (teiid != null) {
    		teiid.stop();
    		teiid = null;
    	}
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
    	String dataSourcesParameter = "dataSources";
    	String dataSources = getAndRemoveParameter(parameters, dataSourcesParameter, String.class);
    	if (dataSources == null) {
    		dataSourcesParameter = "dataSourceRefs";
    		dataSources = getAndRemoveParameter(parameters, dataSourcesParameter, String.class);
    	}
    	
    	if (dataSources != null) {
    		String invalidDataSourceParameter = "dataSource";
    		String invalidDataSource = getAndRemoveParameter(parameters, invalidDataSourceParameter, String.class);
    		if (invalidDataSource == null) {
    			invalidDataSourceParameter = "dataSourceRef";
    			invalidDataSource = getAndRemoveParameter(parameters, invalidDataSourceParameter, String.class);
        	}
    		if (invalidDataSource != null) {
        		throw new RuntimeCamelException("Must not specify " + invalidDataSourceParameter + " together with " + dataSourcesParameter);
        	}
    		
    		if (teiid == null) {
	    		teiid = new TeiidEmbedded();
	    		teiid.addTranslatorsFromClassPath();
	    		teiid.start(new EmbeddedConfiguration());
    		}
    		
	    	for (String dataSource : dataSources.split(",")) {
	    		Object ds = EndpointHelper.resolveReferenceParameter(getCamelContext(), dataSource, Object.class);
	    		teiid.addConnectionFactory(dataSource, ds);
			}
	    	
    		teiid.deployDefaultVDB();
	    	
	    	BasicDataSource teiidDataSource = new BasicDataSource();
	    	teiidDataSource.setDriver(teiid.getDriver());
	    	teiidDataSource.setUrl(teiid.getURLForDefaultVDB());
	    	setDataSource(teiidDataSource);
    	}
    	
        return super.createEndpoint(uri, remaining, parameters);
    }

}

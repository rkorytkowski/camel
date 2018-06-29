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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;

import org.apache.camel.RuntimeCamelException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.teiid.adminapi.Model;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.SourceMappingMetadata;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.adminapi.impl.VDBMetadataParser;
import org.teiid.deployers.VirtualDatabaseException;
import org.teiid.dqp.internal.datamgr.ConnectorManagerRepository.ConnectorManagerException;
import org.teiid.runtime.EmbeddedServer;
import org.teiid.translator.ExecutionFactory;
import org.teiid.translator.Translator;
import org.teiid.translator.TranslatorException;

public class TeiidEmbedded extends EmbeddedServer {
	
	VDBMetaData teiidDefaultVDB;
	
	public TeiidEmbedded() {
		teiidDefaultVDB = new VDBMetaData();
		teiidDefaultVDB.addProperty("implicit", "true");
		teiidDefaultVDB.setName(TeiidConstants.VDB_NAME);
		teiidDefaultVDB.setVersion(TeiidConstants.VDB_VERSION);
	}
	
	@SuppressWarnings("unchecked")
	public void addTranslatorsFromClassPath() {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
    	provider.addIncludeFilter(new AnnotationTypeFilter(Translator.class));
    	Set<BeanDefinition> translatorDefs = provider.findCandidateComponents(TeiidConstants.FILTER_PACKAGE_TRANSLATOR);
    	
    	for(BeanDefinition translatorDef: translatorDefs) {
    		Class<?> translatorClass;
			try {
				translatorClass = Class.forName(translatorDef.getBeanClassName());
				addTranslator((Class<? extends ExecutionFactory<?, ?>>) translatorClass);
			} catch (ClassNotFoundException | TranslatorException e) {
				throw new RuntimeCamelException(e);
			}
    	}
	}
	
	@Override
	public void addConnectionFactory(String name, Object connectionFactory) {
		super.addConnectionFactory(name, connectionFactory);
		
		if (connectionFactory instanceof DataSource && teiidDefaultVDB.getModel(name) == null) {
			ModelMetaData model = new ModelMetaData();
			model.setName(name);
			model.setModelType(Model.Type.PHYSICAL);
			
			model.addProperty("importer.useQualifiedName", "false");
			model.addProperty("importer.tableTypes", "TABLE,VIEW");

			SourceMappingMetadata source = new SourceMappingMetadata();
			source.setName(name);
			source.setConnectionJndiName(name);
			
			String translatorName = null;
			if (translatorName == null) {
				DataSource ds = (DataSource) connectionFactory;
				String driverClass;
				try {
					driverClass = DriverManager.getDriver(ds.getConnection().getMetaData().getURL()).getClass().getName();
				} catch (SQLException e) {
					throw new RuntimeCamelException(e);
				}
				
				translatorName = TeiidConstants.TRANSLATOR_FOR_DRIVER.get(driverClass);
				if (translatorName == null) {
					throw new RuntimeException("Could not find translator for " + driverClass);
				}
			}
			source.setTranslatorName(translatorName);
			model.addSourceMapping(source);
			
			teiidDefaultVDB.addModel(model);
		}
	}
	
	public void deployDefaultVDB() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			VDBMetadataParser.marshell(teiidDefaultVDB, out);
			undeployVDB(TeiidConstants.VDB_NAME, TeiidConstants.VDB_VERSION);
			deployVDB(new ByteArrayInputStream(out.toByteArray()));
		} catch (XMLStreamException | IOException | VirtualDatabaseException | ConnectorManagerException | TranslatorException e) {
			throw new RuntimeCamelException(e);
		}
	}
	
	public String getURLForDefaultVDB() {
		return "jdbc:teiid:" + TeiidConstants.VDB_NAME;
	}

}

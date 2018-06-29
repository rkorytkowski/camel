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
package org.apache.camel.component.sql;

import java.util.List;
import java.util.Map;


import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.After;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class TeiidDataSourcesTest extends CamelTestSupport {

    private EmbeddedDatabase dbProjects;
    private EmbeddedDatabase dbLicenses;

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();

        dbProjects = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.DERBY).addScript("sql/createAndPopulateProjectsDatabase.sql").build();

        jndi.bind("dbProjects", dbProjects);
        
        dbLicenses = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL).addScript("sql/createAndPopulateLicensesDatabase.sql").build();

        jndi.bind("dbLicenses", dbLicenses);

        return jndi;
    }

    @Test
    public void testSimpleBody() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        template.sendBody("direct:simple", "Camel");

        mock.assertIsSatisfied();

        // the result is a List
        List<?> received = assertIsInstanceOf(List.class, mock.getReceivedExchanges().get(0).getIn().getBody());

        // and each row in the list is a Map
        Map<?, ?> row = assertIsInstanceOf(Map.class, received.get(0));

        // and we should be able the get the project from the map that should be Linux
        assertEquals("http://www.apache.org/licenses/LICENSE-2.0", row.get("url"));
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();

        dbProjects.shutdown();
        dbLicenses.shutdown();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
            	
                from("direct:simple")
                    .to("teiid:select url from projects inner join licenses on projects.license = licenses.license"
                    		+ " where project = #?dataSources=dbProjects,dbLicenses")
                    .to("mock:result");
                
               
            }
        };
    }
}
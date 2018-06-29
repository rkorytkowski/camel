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

import java.util.HashMap;
import java.util.Map;

public class TeiidConstants {

	public static final String VDB_NAME = "default";
	public static final String VDB_VERSION = "1.0.0";

	public static final String FILTER_PACKAGE_TRANSLATOR = "org.teiid.translator";

	public static final Map<String, String> TRANSLATOR_FOR_DRIVER;

	static {
		TRANSLATOR_FOR_DRIVER = new HashMap<>();
		TRANSLATOR_FOR_DRIVER.put("com.ingres.jdbc.IngresDriver", "actian-vector");
		TRANSLATOR_FOR_DRIVER.put("com.ibm.db2.jcc.DB2Driver", "db2");
		TRANSLATOR_FOR_DRIVER.put("org.apache.derby.jdbc.AutoloadedDriver", "derby");
		TRANSLATOR_FOR_DRIVER.put("org.apache.derby.jdbc.ClientDriver", "derby");
		TRANSLATOR_FOR_DRIVER.put("org.h2.Driver", "h2");
		TRANSLATOR_FOR_DRIVER.put("com.sap.db.jdbc.Driver", "hana");
		TRANSLATOR_FOR_DRIVER.put("org.apache.hive.jdbc.HiveDriver", "hive");
		TRANSLATOR_FOR_DRIVER.put("org.apache.hadoop.hive.jdbc.HiveDriver", "impala");
		TRANSLATOR_FOR_DRIVER.put("org.hsqldb.jdbc.JDBCDriver", "hsql");
		TRANSLATOR_FOR_DRIVER.put("com.informix.jdbc.IfxDriver", "informix");
		TRANSLATOR_FOR_DRIVER.put("com.ingres.jdbc.IngresDriver", "ingres");
		TRANSLATOR_FOR_DRIVER.put("com.intersys.jdbc.CacheDriver", "intersystems-cache");
		TRANSLATOR_FOR_DRIVER.put("com.mysql.jdbc.Driver", "mysql5");
		TRANSLATOR_FOR_DRIVER.put("org.olap4j.driver.xmla.XmlaOlap4jDriver", "olap");
		TRANSLATOR_FOR_DRIVER.put("mondrian.olap4j.MondrianOlap4jDriver", "olap");
		TRANSLATOR_FOR_DRIVER.put("oracle.jdbc.OracleDriver", "oracle");
		TRANSLATOR_FOR_DRIVER.put("com.osisoft.jdbc.Driver", "osisoft-pi");
		TRANSLATOR_FOR_DRIVER.put("org.apache.phoenix.jdbc.PhoenixDriver", "phoenix");
		TRANSLATOR_FOR_DRIVER.put("org.postgresql.Driver", "postgresql");
		TRANSLATOR_FOR_DRIVER.put("com.facebook.presto.jdbc.PrestoDriver", "prestodb");
		TRANSLATOR_FOR_DRIVER.put("com.microsoft.sqlserver.jdbc.SQLServerDriver", "sqlserver");
		TRANSLATOR_FOR_DRIVER.put("net.sourceforge.jtds.jdbc.Driver", "sqlserver");
		TRANSLATOR_FOR_DRIVER.put("com.sybase.jdbc2.jdbc.SybDriver", "sybase");
		TRANSLATOR_FOR_DRIVER.put("com.sybase.jdbc4.jdbc.SybDriver", "sybase");
		TRANSLATOR_FOR_DRIVER.put("net.ucanaccess.jdbc.UcanaccessDriver", "ucanaccess");
		TRANSLATOR_FOR_DRIVER.put("com.vertica.jdbc.Driver", "vertica");
	}
}
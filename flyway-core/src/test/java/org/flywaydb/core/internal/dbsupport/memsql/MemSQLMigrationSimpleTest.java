/**
 * Copyright 2010-2016 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.dbsupport.memsql;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using MemSQL.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.MemSQL.class)
public class MemSQLMigrationSimpleTest {

    @Test
    public void migrateWithExistingSchemaSetInPropertyButNotInUrl() throws Exception {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:mysql://127.0.0.1:3306/", "flyway_user", "flyway");
        flyway.setSchemas("WATCHTOWER");
        flyway.setTable("FLYWAYSCHEMAVERSION");
        flyway.setValidateOnMigrate(false);
        //flyway.setCleanDisabled(true);
        flyway.setOutOfOrder(true);
        flyway.setBaselineOnMigrate(true);
        flyway.setSqlMigrationPrefix("Schema");
        flyway.setLocations("migration/dbsupport/memsql/sql/others");
        //flyway.clean();
        assertEquals(1, flyway.migrate());
    }
}

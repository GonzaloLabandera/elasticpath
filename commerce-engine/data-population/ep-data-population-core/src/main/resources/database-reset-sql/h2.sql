-- SQL to drop the current EP database.
-- SQL to create the EP database
-- FIXME: Customized schema is not used actually during populating data to local h2 db by liquibase.
-- DROP SCHEMA ${data.population.schemaname} CASCADE;
-- clean up data in public schema
DROP ALL OBJECTS;
SET WRITE_DELAY 0;


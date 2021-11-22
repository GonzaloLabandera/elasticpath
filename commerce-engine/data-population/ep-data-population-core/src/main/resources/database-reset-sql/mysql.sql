-- SQL to drop the current EP database if exists.
DROP DATABASE IF EXISTS ${data.population.schemaname};

-- SQL to create the EP database
CREATE DATABASE ${data.population.schemaname} CHARACTER SET utf8;

-- SQL to create user
CREATE USER IF NOT EXISTS '${data.population.username}'@'localhost' IDENTIFIED BY '${data.population.password}';
CREATE USER IF NOT EXISTS '${data.population.username}'@'%' IDENTIFIED BY '${data.population.password}';

-- SQL to grant the necessary database permissions on a new EP database
GRANT ALL PRIVILEGES ON ${data.population.schemaname}.* TO '${data.population.username}'@'localhost';
GRANT ALL PRIVILEGES ON ${data.population.schemaname}.* TO '${data.population.username}'@'%';

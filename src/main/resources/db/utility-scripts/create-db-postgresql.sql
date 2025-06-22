-- Create Database Manually first.Creating databases in Postgres is a 
--      little bit “manual” there’s no fully pure SQL transactional way to 
--      do “create-if-not-exists” for databases.
-- 
-- CREATE DATABASE watguessr;

-- Create user if not exists
-- Can run on any database. The pg_catalog schema is shared across all databases in a PostgreSQL instance.
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT FROM pg_catalog.pg_roles
            WHERE rolname = 'watuser'
        ) THEN
            CREATE USER watuser WITH PASSWORD 'goon';
        END IF;
    END
$$;

-- Connect to watguessr database (in psql client)
-- \connect watguessr

CREATE SCHEMA IF NOT EXISTS watguessr;

GRANT ALL PRIVILEGES ON DATABASE watguessr TO watuser;

-- Connect to watguessr database again (in psql client)
-- \connect watguessr

GRANT ALL ON SCHEMA watguessr TO watuser;
GRANT ALL ON ALL TABLES IN SCHEMA watguessr TO watuser;
GRANT ALL ON ALL SEQUENCES IN SCHEMA watguessr TO watuser;
GRANT ALL ON ALL FUNCTIONS IN SCHEMA watguessr TO watuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA watguessr GRANT ALL ON TABLES TO watuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA watguessr GRANT ALL ON SEQUENCES TO watuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA watguessr GRANT ALL ON FUNCTIONS TO watuser;
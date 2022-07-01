CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- There is no 'if not exists' for type
-- But use a 'Drop' will cause exception if the type is applied in table schema.
-- DROP TYPE post_status;

-- The value only accept single quotes.
-- CREATE TYPE post_status AS ENUM( 'DRAFT', 'PENDING_MODERATION', 'PUBLISHED');

-- A simple way to skip exception when creating type if it is existed.
DO $$ BEGIN
    CREATE TYPE post_status AS ENUM( 'DRAFT', 'PENDING_MODERATION', 'PUBLISHED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Use EnumCodec to handle enum between Java and pg. 
-- see: https://github.com/pgjdbc/r2dbc-postgresql#postgres-enum-types
-- CREATE CAST (varchar AS post_status) WITH INOUT AS IMPLICIT;
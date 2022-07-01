CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS posts (
    id UUID NOT NULL /* [jooq ignore start] */ DEFAULT uuid_generate_v4()/* [jooq ignore stop] */,
    title VARCHAR(255),
    content VARCHAR(255),
    status VARCHAR(255) default 'DRAFT',
    created_at TIMESTAMP ,
    updated_at TIMESTAMP, --NOT NULL DEFAULT LOCALTIMESTAMP,
    created_by VARCHAR(255),
    version BIGINT,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS comments(
    id UUID NOT NULL /* [jooq ignore start] */ DEFAULT uuid_generate_v4()/* [jooq ignore stop] */,
    post_id UUID NOT NULL REFERENCES posts(id),
    content VARCHAR(255),
    created_at TIMESTAMP ,
    updated_at TIMESTAMP, --NOT NULL DEFAULT LOCALTIMESTAMP,
    created_by VARCHAR(255),
    version BIGINT,
    PRIMARY KEY (id)
);

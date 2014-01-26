
# --- !Ups

CREATE TABLE IF NOT EXISTS blobs (
  id SERIAL PRIMARY KEY,
  name varchar (30)
);

INSERT INTO blobs (name) VALUES ('blobA');
INSERT INTO blobs (name) VALUES ('blobB');

# --- !Downs

DROP TABLE IF EXISTS blobs;


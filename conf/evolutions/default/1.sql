
# --- !Ups

CREATE TABLE IF NOT EXISTS blobs (
  id SERIAL PRIMARY KEY,
  name varchar (30)
);

CREATE TABLE IF NOT EXISTS tags (
  id SERIAL PRIMARY KEY,
  name varchar (30)
);

CREATE TABLE IF NOT EXISTS blobs_tags (
  blob_id INTEGER,
  tag_id INTEGER,
  FOREIGN KEY (blob_id) REFERENCES blobs(id),
  FOREIGN KEY (blob_id) REFERENCES tags(id) ON DELETE CASCADE,
  PRIMARY KEY (blob_id, tag_id)
);

INSERT INTO blobs (name) VALUES ('blobA');
INSERT INTO blobs (name) VALUES ('blobB');

INSERT INTO tags (name) VALUES ('tagA');
INSERT INTO tags (name) VALUES ('tagB');
INSERT INTO tags (name) VALUES ('tagC');

# --- !Downs

DROP TABLE IF EXISTS blobs;
DROP TABLE IF EXISTS tags CASCADE;
DROP TABLE IF EXISTS blobs_tags;


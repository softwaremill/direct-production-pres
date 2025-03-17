CREATE TABLE "cities"
(
  "name"            TEXT        NOT NULL
);

INSERT INTO "cities" ("name")
VALUES
  ('Berlin'),
  ('London'),
  ('Paris'),
  ('Rome'),
  ('Madrid'),
  ('Lisbon'),
  ('Amsterdam'),
  ('Prague'),
  ('Vienna'),
  ('Budapest'),
  ('Warsaw'),
  ('Stockholm'),
  ('Oslo'),
  ('Helsinki'),
  ('Copenhagen'),
  ('Athens'),
  ('Dublin'),
  ('Brussels'),
  ('Luxembourg'),
  ('Zurich'),
  ('Monaco');

CREATE TABLE "trips"
(
  "id"              TEXT        NOT NULL,
  "start"           TEXT        NOT NULL,
  "finish"          TEXT        NOT NULL,
  "mode"            TEXT        NOT NULL,
  "created"         TIMESTAMPTZ NOT NULL
);
ALTER TABLE "trips" ADD CONSTRAINT "trips_id" PRIMARY KEY ("id");

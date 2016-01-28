CREATE TYPE activity_type AS ENUM ('SCREENING', 'RANDOMIZATION', 'WASH_OUT', 'FOLLOW_UP', 'TREATMENT', 'OTHER');
CREATE TYPE allocation_type AS ENUM ('UNKNOWN', 'RANDOMIZED', 'NONRANDOMIZED');
CREATE TYPE blinding_type AS ENUM ('OPEN', 'SINGLE_BLIND', 'DOUBLE_BLIND', 'TRIPLE_BLIND', 'UNKNOWN');
CREATE TYPE study_source AS ENUM ('MANUAL', 'CLINICALTRIALS');
CREATE TYPE study_status AS ENUM ('NOT_YET_RECRUITING', 'RECRUITING', 'ENROLLING', 'ACTIVE', 'COMPLETED', 'SUSPENDED', 'TERMINATED', 'WITHDRAWN', 'UNKNOWN');
CREATE TYPE measurement_type as ENUM ('CONTINUOUS', 'RATE', 'CATEGORICAL');
CREATE TYPE variable_type as ENUM ('POPULATION_CHARACTERISTIC', 'ENDPOINT', 'ADVERSE_EVENT');
CREATE TYPE epoch_offset as ENUM ('FROM_EPOCH_START', 'BEFORE_EPOCH_END');

CREATE TABLE "studies" (
  "metadata" hstore,
  "id" bigserial,
  "name" varchar NOT NULL,
  "title" text,
  "objective" text,
  "allocation" allocation_type,
  "blinding" blinding_type,
  "number_of_centers" int4,
  "created_at" timestamp,
  "source" study_source DEFAULT 'MANUAL',
  "exclusion" text,
  "inclusion" text,
  "status" study_status,
  "start_date" date,
  "end_date" date,
  "notes" text[],
  "blinding_notes" text[],
  "allocation_notes" text[],
  "title_notes" text[],
  PRIMARY KEY ("id")
);
CREATE INDEX ON "studies" ("name");

CREATE TABLE "units" (
  "id" bigserial,
  "study" bigint REFERENCES studies ("id"),
  "name" varchar NOT NULL,
  "description" text,
  PRIMARY KEY ("id")
);

CREATE TABLE "indications" (
  "id" bigserial,
  "study" bigint REFERENCES studies ("id"),
  "name" varchar NOT NULL,
  "description" text,
  PRIMARY KEY ("id")
);

CREATE TABLE "drugs" (
  "id" bigserial,
  "study" bigint REFERENCES studies ("id"),
  "name" varchar NOT NULL,
  "description" text,
  PRIMARY KEY ("id")
);

CREATE TABLE "namespaces" (
  "id" bigserial,
  "name" varchar NOT NULL,
  "description" text,
  PRIMARY KEY ("id")
);

CREATE TABLE "namespace_studies" (
  "namespace" bigint REFERENCES namespaces (id),
  "study" bigint REFERENCES studies (id),
  PRIMARY KEY ("namespace", "study")
);

CREATE TABLE "namespace_concepts" (
  "namespace" bigint REFERENCES namespaces (id),
  "concept_path" varchar NOT NULL,
  "metadata" hstore,
  PRIMARY KEY ("namespace", "concept_path")
);

CREATE TABLE "references" (
  "study" bigint REFERENCES studies (id),
  "id" varchar,
  "repository" text DEFAULT 'PubMed',
  PRIMARY KEY ("study", "id")
);

CREATE TABLE "activities" (
  "id" bigserial,
  "study" bigint,
  "name" varchar,
  "type" activity_type,
  PRIMARY KEY ("id"),
  UNIQUE ("study", "name")
);

CREATE TABLE "treatments" (
  "id" bigserial,
  "activity" bigint REFERENCES activities ("id"),
  "drug" bigint REFERENCES drugs ("id"),
  "periodicity" interval DEFAULT 'P0D',
  PRIMARY KEY ("id"),
  UNIQUE("activity", "drug")
);

CREATE TABLE "treatment_dosings" (
  "treatment" bigint REFERENCES treatments (id),
  "planned_time" interval,
  "min_dose" float,
  "max_dose" float,
  "scale_modifier" varchar,
  "unit" bigint REFERENCES units (id),
  PRIMARY KEY ("treatment", "planned_time")
);
CREATE INDEX ON "treatment_dosings" ("treatment") WHERE "planned_time" IS NULL;



CREATE TABLE "epochs" (
  "id" bigserial,
  "study" bigint REFERENCES studies (id),
  "name" varchar,
  "duration" interval DEFAULT 'P0D',
  "notes" text[],
  PRIMARY KEY ("id"),
  UNIQUE ("study", "name")
);
CREATE INDEX ON "epochs" ("study");

CREATE TABLE "arms" (
  "id" bigserial,
  "study" bigint REFERENCES studies (id),
  "name" varchar,
  "arm_size" int4,
  "notes" text[],
  PRIMARY KEY ("id"),
  UNIQUE ("study", "name")
);
CREATE INDEX ON "arms" ("study");
COMMENT ON COLUMN "arms"."name" IS 'Empty string indicates "total population"';

CREATE TABLE "designs" (
  "arm" bigint REFERENCES arms ("id"),
  "epoch" bigint REFERENCES epochs ("id"),
  "activity" bigint REFERENCES activities ("id"),
  PRIMARY KEY ("arm", "epoch")
);

CREATE TABLE "variables" (
  "id" bigserial,
  "study" bigint REFERENCES studies ("id"),
  "name" varchar,
  "description" text,
  "unit_description" text,
  "is_primary" bool,
  "measurement_type" measurement_type,
  "variable_type" variable_type,
  "notes" text[],
  PRIMARY KEY ("id"),
  UNIQUE ("study", "name")
);
CREATE INDEX ON "variables" ("study");

CREATE TABLE "variable_categories" (
  "variable" bigint REFERENCES variables (id),
  "category_name" varchar,
  PRIMARY KEY ("variable", "category_name")
);

CREATE TABLE "measurement_moments" (
  "id" bigserial,
  "study" bigint REFERENCES studies ("id"),
  "name" varchar,
  "epoch" bigint REFERENCES epochs ("id"),
  "is_primary" bool,
  "offset_from_epoch" interval,
  "relative_to" epoch_offset,
  "notes" text[],
  PRIMARY KEY ("id"),
  UNIQUE ("epoch", "offset_from_epoch", "relative_to"),
  UNIQUE ("study", "name")
);

CREATE TABLE "measurements" (
  "study" bigint REFERENCES studies (id),
  "variable" bigint REFERENCES variables (id),
  "measurement_moment" bigint REFERENCES measurement_moments (id),
  "arm" bigint REFERENCES arms (id),
  "attribute" varchar,
  "integer_value" bigint,
  "real_value" float,
  PRIMARY KEY ("variable", "measurement_moment", "arm", "attribute")
);

CREATE VIEW namespace_uris AS
SELECT
  namespace,
  concept_path,
  'http://trials.drugis.org/namespace/' || namespace || '/' || concept_path AS uri
FROM namespace_concepts;

CREATE TYPE activity_type AS ENUM ('SCREENING', 'RANDOMIZATION', 'WASH_OUT', 'FOLLOW_UP', 'TREATMENT', 'OTHER');
CREATE TYPE allocation_type AS ENUM ('UNKNOWN', 'RANDOMIZED', 'NONRANDOMIZED');
CREATE TYPE blinding_type AS ENUM ('OPEN', 'SINGLE_BLIND', 'DOUBLE_BLIND', 'TRIPLE_BLIND', 'UNKNOWN');
CREATE TYPE source AS ENUM ('MANUAL', 'CLINICALTRIALS');
CREATE TYPE status AS ENUM ('NOT_YET_RECRUITING', 'RECRUITING', 'ENROLLING', 'ACTIVE', 'COMPLETED', 'SUSPENDED', 'TERMINATED', 'WITHDRAWN', 'UNKNOWN');
CREATE TYPE measurement_type as ENUM ('CONTINUOUS', 'RATE', 'CATEGORICAL');
CREATE TYPE variable_type as ENUM ('PopulationCharacteristic', 'Endpoint', 'AdverseEvent');
CREATE TYPE epoch_offset as ENUM ('FROM_EPOCH_START', 'BEFORE_EPOCH_END');

CREATE TABLE "concepts" (
  "id" uuid,
  "label" varchar,
  "description" text,
  "code" varchar,
  "namespace" varchar NOT NULL,
  PRIMARY KEY ("id")
);

CREATE TABLE "concept_map" (
  "sub" uuid REFERENCES concepts (id),
  "super" uuid REFERENCES concepts (id),
  PRIMARY KEY ("sub", "super")
);

CREATE TABLE "drugs" (
  "id" bigserial,
  "name" varchar NOT NULL,
  "description" text,
  "concept" uuid REFERENCES concepts (id),
  PRIMARY KEY ("id")
);
CREATE INDEX ON "drugs" ("concept");

CREATE TABLE "indications" (
  "id" bigserial,
  "name" varchar NOT NULL,
  "description" text,
  "concept" uuid REFERENCES concepts (id),
  PRIMARY KEY ("id")
);
CREATE INDEX ON "indications" ("concept");

CREATE TABLE "units" (
  "id" bigserial,
  "name" varchar NOT NULL,
  "description" text,
  "concept" uuid REFERENCES concepts (id),
  PRIMARY KEY ("id")
);
CREATE INDEX ON "units" ("concept");

CREATE TABLE "treatments" (
  "id" bigserial,
  "study_id" bigint,
  "activity_name" varchar NOT NULL,
  "drug" bigint REFERENCES drugs (id),
  "periodicity" interval DEFAULT 'P0D',
  PRIMARY KEY ("id"),
  UNIQUE("study_id", "activity_name", "drug")
);

CREATE TABLE "treatment_dosings" (
  "treatment_id" bigint REFERENCES treatments (id),
  "planned_time" interval,
  "min_dose" float,
  "max_dose" float,
  "scale_modifier" varchar,
  "unit" bigint REFERENCES units (id),
  PRIMARY KEY ("treatment_id", "planned_time")
);
CREATE INDEX ON "treatment_dosings" ("treatment_id") WHERE "planned_time" IS NULL;

CREATE TABLE "activities" (
  "study_id" bigint,
  "name" varchar,
  "type" activity_type,
  PRIMARY KEY ("study_id", "name")
);
ALTER TABLE "treatments" ADD CONSTRAINT "treatment_activity_fkey" FOREIGN KEY ("study_id", "activity_name") REFERENCES "activities" ("study_id", "name");

CREATE TABLE "studies" (
  "metadata" hstore,
  "id" bigserial,
  "name" varchar NOT NULL,
  "title" text,
  "indication" bigint REFERENCES indications (id),
  "objective" text,
  "allocation_type" allocation_type,
  "blinding_type" blinding_type,
  "number_of_centers" int2,
  "created_at" date,
  "source" source DEFAULT 'MANUAL',
  "exclusion" text,
  "inclusion" text,
  "status" status,
  "start_date" date,
  "end_date" date,
  "notes" text[],
  "blinding_type_notes" text[],
  "allocation_type_notes" text[],
  "title_notess" text[],
  PRIMARY KEY ("id")
);
CREATE INDEX ON "studies" ("name");
CREATE INDEX ON "studies" ("indication");

CREATE TABLE "namespaces" (
  "id" bigserial,
  "name" varchar NOT NULL,
  "description" text,
  PRIMARY KEY ("id")
);

CREATE TABLE "namespace_studies" (
  "namespace_id" bigint REFERENCES namespaces (id),
  "study_id" bigint REFERENCES studies (id),
  PRIMARY KEY ("namespace_id", "study_id")
);

CREATE TABLE "references" (
  "study_id" bigint REFERENCES studies (id),
  "id" varchar,
  "repository" text DEFAULT 'PubMed',
  PRIMARY KEY ("study_id", "id")
);

CREATE TABLE "epochs" (
  "study_id" bigint REFERENCES studies (id),
  "name" varchar,
  "duration" interval DEFAULT 'P0D',
  "notes" text[],
  PRIMARY KEY ("study_id", "name")
);
CREATE INDEX ON "epochs" ("study_id");

CREATE TABLE "arms" (
  "study_id" bigint REFERENCES studies (id),
  "name" varchar,
  "arm_size" varchar,
  "notes" text[],
  PRIMARY KEY ("study_id", "name")
);
CREATE INDEX ON "arms" ("study_id");
COMMENT ON COLUMN "arms"."name" IS 'Empty string indicates "total population"';

CREATE TABLE "designs" (
  "study_id" bigint,
  "arm_name" varchar,
  "epoch_name" varchar,
  "activity_name" varchar,
  PRIMARY KEY ("study_id", "arm_name", "epoch_name")
);
ALTER TABLE "designs" ADD CONSTRAINT "design_arm_fkey" FOREIGN KEY ("study_id", "arm_name") REFERENCES "arms" ("study_id", "name");
ALTER TABLE "designs" ADD CONSTRAINT "design_epoch_fkey" FOREIGN KEY ("study_id", "epoch_name") REFERENCES "epochs" ("study_id", "name");
ALTER TABLE "designs" ADD CONSTRAINT "design_activity_fkey" FOREIGN KEY ("study_id", "activity_name") REFERENCES "activities" ("study_id", "name");

CREATE TABLE "variables" (
  "id" bigserial,
  "study_id" bigint,
  "variable_concept" uuid REFERENCES concepts (id),
  "name" varchar,
  "description" text,
  "is_primary" bool,
  "measurement_type" measurement_type,
  "unit" bigint REFERENCES units (id),
  "variable_type" variable_type,
  "notes" text[],
  PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX ON "variables" ("study_id", "variable_concept");

CREATE TABLE "variable_categories" (
  "variable" bigint REFERENCES variables (id),
  "category_name" varchar,
  PRIMARY KEY ("variable", "category_name")
);

CREATE TABLE "measurements" (
  "study_id" bigint,
  "variable_concept" uuid REFERENCES concepts (id),
  "measurement_moment_name" varchar,
  "arm_name" varchar,
  "attribute" varchar,
  "integer_value" bigint,
  "real_value" float,
  PRIMARY KEY ("variable_concept", "measurement_moment_name", "arm_name", "attribute")
);
COMMENT ON COLUMN "measurements"."variable_concept" IS 'Uniquely identifies the study';
ALTER TABLE "measurements" ADD CONSTRAINT "variable_measurement_fkey" FOREIGN KEY ("study_id", "variable_concept") REFERENCES "variables" ("study_id", "variable_concept");
ALTER TABLE "measurements" ADD CONSTRAINT "arm_measurement_fkey" FOREIGN KEY ("study_id", "arm_name") REFERENCES "arms" ("study_id", "name");

CREATE TABLE "measurement_moments" (
  "study_id" bigint,
  "name" varchar,
  "epoch_name" varchar,
  "is_primary" bool,
  "offset_from_epoch" interval,
  "before_epoch" epoch_offset,
  "notes" text[],
  PRIMARY KEY ("study_id", "name"),
  UNIQUE ("study_id", "epoch_name", "offset_from_epoch", "before_epoch")
);
ALTER TABLE "measurement_moments" ADD CONSTRAINT "epoch_study_measurement_fkey" FOREIGN KEY ("study_id", "epoch_name") REFERENCES "epochs" ("study_id", "name");
ALTER TABLE "measurements" ADD CONSTRAINT "measurement_moments_fkey" FOREIGN KEY ("study_id", "measurement_moment_name") REFERENCES "measurement_moments" ("study_id", "name");

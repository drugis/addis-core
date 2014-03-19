SET DATABASE SQL SYNTAX PGS TRUE;
SET DATABASE SQL SIZE FALSE;

CREATE TABLE namespaces (id serial NOT NULL,
    name VARCHAR NOT NULL,
    description VARCHAR NOT NULL,
    PRIMARY KEY (id));

CREATE TABLE studies (
    id bigint NOT NULL,
    name character varying NOT NULL,
    title text,
    PRIMARY KEY (id));

 --
 -- Name: namespace_studies; Type: TABLE; Schema: public; Owner: trialverse; Tablespace:
 --
 CREATE TABLE namespace_studies (
     namespace bigint NOT NULL,
     study bigint NOT NULL,
       PRIMARY KEY(namespace, study),
       FOREIGN KEY(namespace) REFERENCES namespaces(id),
       FOREIGN KEY(study) REFERENCES studies(id));




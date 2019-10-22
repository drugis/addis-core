SET DATABASE SQL SYNTAX PGS TRUE;
SET DATABASE SQL SIZE FALSE;

-- changeset reidd:1

CREATE TABLE UserConnection (userId varchar(255) NOT NULL,
  providerId VARCHAR(255) NOT NULL,
  providerUserId VARCHAR(255),
  rank INT NOT NULL,
  displayName VARCHAR(255),
  profileUrl VARCHAR(512),
  imageUrl VARCHAR(512),
  accessToken VARCHAR(255) NOT NULL,
  secret VARCHAR(255),
  refreshToken VARCHAR(255),
  expireTime bigint,
  PRIMARY KEY (userId, providerId, providerUserId));
CREATE UNIQUE index UserConnectionRank ON UserConnection(userId, providerId, rank);

CREATE TABLE Account (id SERIAL NOT NULL,
            username VARCHAR UNIQUE,
            firstName VARCHAR NOT NULL,
            lastName VARCHAR NOT NULL,
            password VARCHAR DEFAULT '',
            PRIMARY KEY (id));

CREATE TABLE AccountRoles (
    accountId INT,
    role VARCHAR NOT NULL,
    FOREIGN KEY (accountId) REFERENCES Account(id)
);

CREATE TABLE Project (id SERIAL NOT NULL,
            owner INT,
            name VARCHAR NOT NULL,
            description TEXT NOT NULL,
            trialverseId INT,
            PRIMARY KEY (id),
            FOREIGN KEY(owner) REFERENCES Account(id));

CREATE TABLE Outcome (id SERIAL NOT NULL,
                      project INT,
                      name VARCHAR NOT NULL,
                      motivation TEXT NOT NULL,
                      semanticOutcomeLabel VARCHAR NOT NULL,
                      semanticOutcomeUri VARCHAR NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(project) REFERENCES Project(id));

CREATE TABLE Intervention (id SERIAL NOT NULL,
                           project INT,
                           name VARCHAR NOT NULL,
                           motivation TEXT NOT NULL,
                           semanticInterventionLabel VARCHAR NOT NULL,
                           semanticInterventionUri VARCHAR NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(project) REFERENCES Project(id));

CREATE TABLE Analysis (
  id SERIAL NOT NULL,
        projectId INT,
        name VARCHAR NOT NULL,
        analysisType VARCHAR NOT NULL,
        studyId INT,
  PRIMARY KEY (id),
  FOREIGN KEY(projectId) REFERENCES Project(id));

CREATE TABLE Analysis_Outcomes (
  AnalysisId INT,
  OutcomeId INT,
  PRIMARY KEY(AnalysisId, OutcomeId),
  FOREIGN KEY(AnalysisId) REFERENCES Analysis(id),
  FOREIGN KEY(OutcomeId) REFERENCES Outcome(id)
);

CREATE TABLE Analysis_Interventions (
  AnalysisId INT,
  InterventionId INT,
  PRIMARY KEY(AnalysisId, InterventionId),
  FOREIGN KEY(AnalysisId) REFERENCES Analysis(id),
  FOREIGN KEY(InterventionId) REFERENCES Intervention(id)
);

-- changeset reidd:2

CREATE TABLE Scenario (id SERIAL NOT NULL,
    						workspace INT NOT NULL,
    						title VARCHAR NOT NULL,
    						state VARCHAR NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY(workspace) REFERENCES Analysis(id));

-- changeset reidd:3

ALTER TABLE Analysis ADD problem VARCHAR NULL;

-- changeset stroombergc:4
CREATE SEQUENCE shared_analysis_id_seq;

CREATE TABLE SingleStudyBenefitRiskAnalysis (
id INT DEFAULT nextval('shared_analysis_id_seq') NOT NULL,
        projectId INT,
        name VARCHAR NOT NULL,
        studyId INT,
        problem VARCHAR NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(projectId) REFERENCES Project(id));

CREATE TABLE NetworkMetaAnalysis (
  id INT DEFAULT nextval('shared_analysis_id_seq') NOT NULL,
  projectId INT,
  name VARCHAR NOT NULL,
  studyId INT,
  outcomeId INT,
  problem VARCHAR NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(projectId) REFERENCES Project(id),
  FOREIGN KEY(outcomeId) REFERENCES Outcome(id)
);


DROP TABLE Analysis CASCADE;

ALTER TABLE Analysis_Outcomes RENAME TO SingleStudyBenefitRiskAnalysis_Outcome;
ALTER TABLE Analysis_Interventions RENAME TO SingleStudyBenefitRiskAnalysis_Intervention;

ALTER TABLE SingleStudyBenefitRiskAnalysis_Intervention ADD CONSTRAINT ssbr_analysis_interventions_analysisid_fkey FOREIGN KEY (analysisId) REFERENCES SingleStudyBenefitRiskAnalysis(id);
ALTER TABLE SingleStudyBenefitRiskAnalysis_Outcome ADD CONSTRAINT ssbr_analysis_outcomes_analysisid_fkey FOREIGN KEY (analysisId) REFERENCES SingleStudyBenefitRiskAnalysis(id);
ALTER TABLE scenario ADD CONSTRAINT ssbr_scenario_workspace_fkey FOREIGN KEY (workspace) REFERENCES SingleStudyBenefitRiskAnalysis(id);

-- changeset stroombergc:5
CREATE TABLE Model (
  id SERIAL NOT NULL,
  analysisId INT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(analysisId) REFERENCES NetworkMetaAnalysis(id));

-- changeset reidd:6
CREATE TABLE ArmExclusion (
  id SERIAL NOT NULL,
  trialverseId BIGINT NOT NULL,
  analysisId INT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(analysisId) REFERENCES NetworkMetaAnalysis(id)
);

-- changeset reidd:7
CREATE TABLE InterventionExclusion (
  id SERIAL NOT NULL,
  interventionId INT NOT NULL,
  analysisId INT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(analysisId) REFERENCES NetworkMetaAnalysis(id),
  FOREIGN KEY(interventionId) REFERENCES Intervention(id)
);

-- changeset gertvv:8
ALTER TABLE NetworkMetaAnalysis DROP COLUMN studyId;
ALTER TABLE NetworkMetaAnalysis DROP COLUMN problem;

CREATE TABLE PataviTask (
  id SERIAL NOT NULL,
  modelId INT NOT NULL,
  method varchar,
  problem TEXT,
  result TEXT,
  PRIMARY KEY(id),
  FOREIGN KEY(modelId) REFERENCES Model(id)
);

-- changeset reidd:9
DROP TABLE InterventionExclusion CASCADE;

--changeset reidd:10
ALTER TABLE Project DROP COLUMN trialverseId;
ALTER TABLE Project ADD COLUMN namespaceUid VARCHAR;

--changeset stroombergc:11
ALTER TABLE SingleStudyBenefitRiskAnalysis DROP COLUMN studyId;
ALTER TABLE SingleStudyBenefitRiskAnalysis ADD COLUMN studyUid VARCHAR;

--changeset stroombergc:12
ALTER TABLE ArmExclusion DROP COLUMN trialverseId;
ALTER TABLE ArmExclusion ADD COLUMN trialverseUid VARCHAR;

--changeset reidd:13
CREATE TABLE remarks (
  analysisId INT NOT NULL,
  remarks TEXT,
  PRIMARY KEY(analysisId),
  FOREIGN KEY(analysisId) REFERENCES SingleStudyBenefitRiskAnalysis(id)
);

--changeset stroombergc:14
ALTER TABLE project ADD COLUMN datasetVersion VARCHAR;
--rollback ALTER TABLE project drop column datasetVersion

--changeset stroombergc:15
-- this can not be done in hsql, this is a problem
--ALTER TABLE project ALTER column description DROP NOT NULL;
--rollback ALTER TABLE project ALTER COLUMN description SET NOT NULL;

--changeset reidd:16
ALTER TABLE SingleStudyBenefitRiskAnalysis DROP COLUMN studyUId;
ALTER TABLE SingleStudyBenefitRiskAnalysis ADD COLUMN studyGraphUid VARCHAR;
--rollback ALTER TABLE SingleStudyBenefitRiskAnalysis DROP COLUMN studyGraphUid;
--rollback ALTER TABLE SingleStudyBenefitRiskAnalysis ADD COLUMN studyUid VARCHAR;

--changeset stroombergc:17
DROP TABLE IF EXISTS PataviTask;
ALTER TABLE model ADD COLUMN taskId INT;

--changeset stroombergc:18
ALTER TABLE model ADD COLUMN title VARCHAR NOT NULL DEFAULT 'model 1 (generated by conversion)';

--changeset reidd:19
ALTER TABLE model ADD COLUMN linearModel VARCHAR NOT NULL DEFAULT 'fixed';
--rollback ALTER TABLE model DROP COLUMN linearModel ;

--changeset stroombergc:20
ALTER TABLE model ADD COLUMN modelType VARCHAR NOT NULL DEFAULT '{"type": "network"}';
--rollback ALTER TABLE model DROP COLUMN modelType ;

--changeset stroombergc:21
ALTER TABLE model ALTER COLUMN linearModel SET DEFAULT 'random';
--rollback ALTER TABLE model ALTER COLUMN linearModel SET DEFAULT 'fixed';

--changeset reidd:22
ALTER TABLE model ADD COLUMN burnInIterations INT NOT NULL DEFAULT 5000;
ALTER TABLE model ADD COLUMN inferenceIterations INT NOT NULL DEFAULT 20000;
ALTER TABLE model ADD COLUMN thinningFactor INT NOT NULL DEFAULT 10;
--rollback ALTER TABLE model DROP COLUMN burnInIterations;
--rollback ALTER TABLE model DROP COLUMN inferenceIterations;
--rollback ALTER TABLE model DROP COLUMN thinningFactor;

--changeset stroombergc:23
ALTER TABLE model ADD COLUMN likelihood VARCHAR(255);
ALTER TABLE model ADD COLUMN link VARCHAR(255);
ALTER TABLE model ALTER likelihood SET NOT NULL;
ALTER TABLE model ALTER link SET NOT NULL;
--rollback ALTER TABLE model DROP COLUMN likelihood;
--rollback ALTER TABLE model DROP COLUMN link;

--changeset stroombergc:24
ALTER TABLE model ADD COLUMN outcomeScale DOUBLE PRECISION;
--rollback ALTER TABLE model DROP COLUMN outcomeScale;

--changeset stroombergc:25
ALTER TABLE SingleStudyBenefitRiskAnalysis ALTER COLUMN name RENAME TO title;
--rollback ALTER TABLE SingleStudyBenefitRiskAnalysis ALTER COLUMN title RENAME TO name;
ALTER TABLE NetworkMetaAnalysis ALTER COLUMN name RENAME TO title;
--rollback ALTER TABLE NetworkMetaAnalysis ALTER COLUMN title RENAME TO name;

--changeset stroomberg 26:
ALTER TABLE Account ADD COLUMN email VARCHAR(255);
--rollback ALTER TABLE Account DROP COLUMN email;

--changeset reidd:27
ALTER TABLE model ADD COLUMN heterogeneityPrior VARCHAR;
--rollback ALTER TABLE model DROP COLUMN heterogeneityPrior;

--changeset stroombergc:28
CREATE TABLE covariate (
  id SERIAL NOT NULL,
  project int,
  name varchar NOT NULL,
  motivation TEXT,
  definitionkey varchar NOT NULL,
  type varchar not null,
  PRIMARY KEY (id),
  FOREIGN KEY(project) REFERENCES Project(id)
);
--rollback DROP TABLE covariate;

--changeset stroombergc:29
CREATE TABLE CovariateInclusion (
  id SERIAL NOT NULL,
  covariateId INT NOT NULL,
  analysisId INT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(analysisId) REFERENCES NetworkMetaAnalysis(id),
  FOREIGN KEY(covariateId) REFERENCES covariate(id)
);
--rollback DROP TABLE CovariateInclusion;

--changeset stroombergc:30
ALTER TABLE model ADD COLUMN regressor VARCHAR;
--rollback ALTER TABLE model DROP COLUMN regressor;

--changeset stroombergc:31
ALTER TABLE model ADD COLUMN sensitivity VARCHAR;
--rollback ALTER TABLE model DROP COLUMN sensitivity;

--changeset reidd:32
ALTER TABLE NetworkMetaAnalysis ADD COLUMN primaryModel INT;
ALTER TABLE NetworkMetaAnalysis ADD FOREIGN KEY(primaryModel) REFERENCES model(id);
--rollback ALTER TABLE analysis DROP CONSTRAINT "analysis_primarymodel_fkey";
--rollback ALTER TABLE analysis DROP COLUMN primaryModel;

--changeset gertvv:33

CREATE TABLE VersionMapping (id SERIAL NOT NULL,
    versionedDatasetUrl VARCHAR NOT NULL,
    ownerUuid VARCHAR NOT NULL,
    trialverseDatasetUrl VARCHAR NOT NULL,
    PRIMARY KEY (id));

CREATE TABLE ApplicationKey (id SERIAL NOT NULL,
            secretKey VARCHAR UNIQUE,
            accountId INT NOT NULL,
            applicationName VARCHAR NOT NULL,
            creationDate DATE NOT NULL,
            revocationDate DATE NOT NULL,
            PRIMARY KEY (id),
            FOREIGN KEY (accountId) REFERENCES Account(id));

--changeset reidd:34
ALTER TABLE account ADD CONSTRAINT unique_email UNIQUE (email);
--rollback ALTER TABLE account DROP CONSTRAINT unique_email;
ALTER TABLE account ALTER email SET NOT NULL;
--rollback ALTER TABLE account ALTER email DROP NOT NULL;

--changeset reidd:36
UPDATE model SET heterogeneityprior = NULL WHERE heterogeneityprior = '{''type'': ''automatic'' }';

--changeset reidd:37
-- nb changeset is about constraints, hsql has different syntax and we're never going to test cascading deletes in it anyway

--changeset reidd:38
CREATE TABLE MetaBenefitRiskAnalysis (
  id SERIAL NOT NULL,
  title VARCHAR NOT NULL,
  projectId INT NOT NULL,
  finalized BOOLEAN NOT NULL,
  problem VARCHAR,
  PRIMARY KEY (id),
  CONSTRAINT metaBenefitRiskAnalysis_project_fkey FOREIGN KEY(projectId) REFERENCES Project(id)
);

CREATE TABLE MetaBenefitRiskAnalysis_Alternative (
   analysisId INT,
   alternativeId INT,
   PRIMARY KEY(analysisId, alternativeId),
   FOREIGN KEY(analysisId) REFERENCES MetaBenefitRiskAnalysis(id),
   FOREIGN KEY(alternativeId) REFERENCES Intervention(id)
);

CREATE TABLE MbrOutcomeInclusion (
    metaBenefitRiskAnalysisId INT,
    outcomeId INT,
    networkMetaAnalysisId INT,
    modelId INT,
    baseline VARCHAR,
    PRIMARY KEY (metaBenefitRiskAnalysisId, outcomeId, networkMetaAnalysisId, modelId),
    FOREIGN KEY(metaBenefitRiskAnalysisId) REFERENCES MetaBenefitRiskAnalysis(id),
    FOREIGN KEY(outcomeId) REFERENCES Outcome(id),
    FOREIGN KEY(networkMetaAnalysisId) REFERENCES NetworkMetaAnalysis(id),
    FOREIGN KEY(modelId) REFERENCES Model(id)
);

ALTER TABLE scenario DROP CONSTRAINT ssbr_scenario_workspace_fkey;

--rollback DROP TABLE MbrOutcomeInclusion;
--rollback DROP TABLE MetaBenefitRiskAnalysis;
--rollback ALTER TABLE scenario ADD CONSTRAINT ssbr_scenario_workspace_fkey FOREIGN KEY (workspace) REFERENCES SingleStudyBenefitRiskAnalysis(id);

--changeset stroombergc:39
CREATE TABLE FeaturedDataset (
   datasetUrl VARCHAR NOT NULL,
   PRIMARY KEY(dataseturl)
);
--rollback DROP TABLE FeaturedDataset;

--changeset reidd:40
ALTER TABLE covariate ADD COLUMN populationCharacteristicId INT;
ALTER TABLE covariate ADD FOREIGN KEY (populationCharacteristicId) REFERENCES outcome(id);
--rollback ALTER TABLE covariate DROP CONSTRAINT "covariate_populationcharacteristicid_fkey";

--changeset reidd:41
-- constraint changeset, ignore

--changeset reidd:42
-- constraint changeset, ignore

--changeset reidd:43
-- constraint changeset, ignore

--changeset reidd:44
-- constraint changeset, ignore

--changeset reidd:45
-- constraint changeset, ignore

--changeset reidd:46
ALTER TABLE intervention RENAME TO AbstractIntervention;

CREATE TABLE SimpleIntervention(
  simpleInterventionId int NOT NULL,
  PRIMARY KEY(simpleInterventionId),
  FOREIGN KEY(simpleInterventionId) REFERENCES AbstractIntervention(id)
);

CREATE TABLE FixedDoseIntervention (
  fixedInterventionId int NOT NULL,
  lowerBoundType varchar,
  lowerBoundValue DOUBLE PRECISION,
  lowerBoundUnitName varchar,
  lowerBoundUnitPeriod varchar,
  upperBoundType varchar,
  upperBoundValue DOUBLE PRECISION,
  upperBoundUnitName varchar,
  upperBoundUnitPeriod varchar,
  PRIMARY KEY (fixedInterventionId),
  FOREIGN KEY(fixedInterventionId) REFERENCES AbstractIntervention(id)
 );

CREATE TABLE TitratedDoseIntervention (
  titratedInterventionId int NOT NULL,
  minLowerBoundType varchar,
  minLowerBoundUnitName varchar,
  minLowerBoundUnitPeriod varchar,
  minLowerBoundValue DOUBLE PRECISION,
  minUpperBoundType varchar,
  minUpperBoundUnitName varchar,
  minUpperBoundUnitPeriod varchar,
  minUpperBoundValue DOUBLE PRECISION,
  maxLowerBoundType varchar,
  maxLowerBoundUnitName varchar,
  maxLowerBoundUnitPeriod varchar,
  maxLowerBoundValue DOUBLE PRECISION,
  maxUpperBoundType varchar,
  maxUpperBoundUnitName varchar,
  maxUpperBoundUnitPeriod varchar,
  maxUpperBoundValue DOUBLE PRECISION,
  PRIMARY KEY (titratedInterventionId),
  FOREIGN KEY(titratedInterventionId) REFERENCES AbstractIntervention(id)
);

CREATE TABLE BothDoseTypesIntervention (
  bothTypesInterventionId int NOT NULL,
  minLowerBoundType varchar,
  minLowerBoundUnitName varchar,
  minLowerBoundUnitPeriod varchar,
  minLowerBoundValue DOUBLE PRECISION,
  minUpperBoundType varchar,
  minUpperBoundUnitName varchar,
  minUpperBoundUnitPeriod varchar,
  minUpperBoundValue DOUBLE PRECISION,
  maxLowerBoundType varchar,
  maxLowerBoundUnitName varchar,
  maxLowerBoundUnitPeriod varchar,
  maxLowerBoundValue DOUBLE PRECISION,
  maxUpperBoundType varchar,
  maxUpperBoundUnitName varchar,
  maxUpperBoundUnitPeriod varchar,
  maxUpperBoundValue DOUBLE PRECISION,
  PRIMARY KEY (bothTypesInterventionId),
  FOREIGN KEY(bothTypesInterventionId) REFERENCES AbstractIntervention(id)
);

CREATE SEQUENCE shared_intervention_id_seq;

--rollback DROP TABLE FixedDoseIntervention;
--rollback DROP TABLE TitratedDoseIntervention;
--rollback DROP TABLE BothDoseTypesIntervention;

--changeset reidd:47
CREATE TABLE AbstractAnalysis(
  id INT NOT NULL,
  projectId INT NOT NULL,
  title VARCHAR NOT NULL,
  PRIMARY KEY(id),
  FOREIGN KEY (projectId) REFERENCES project(id)
);
INSERT INTO AbstractAnalysis (id, projectId, title)
SELECT id, projectId, title FROM SingleStudyBenefitRiskAnalysis;
INSERT INTO AbstractAnalysis (id, projectId, title)
SELECT id, projectId, title FROM NetworkMetaAnalysis;
INSERT INTO AbstractAnalysis (id, projectId, title)
SELECT id, projectId, title FROM MetaBenefitRiskAnalysis;
ALTER TABLE SingleStudyBenefitRiskAnalysis DROP CONSTRAINT PUBLIC.SYS_FK_10205;
ALTER TABLE SingleStudyBenefitRiskAnalysis DROP projectId;
ALTER TABLE SingleStudyBenefitRiskAnalysis DROP title;
ALTER TABLE NetworkMetaAnalysis DROP CONSTRAINT PUBLIC.SYS_FK_10214;
ALTER TABLE NetworkMetaAnalysis DROP COLUMN projectId;
ALTER TABLE NetworkMetaAnalysis DROP COLUMN title;
ALTER TABLE MetaBenefitRiskAnalysis DROP CONSTRAINT metaBenefitRiskAnalysis_project_fkey;
ALTER TABLE MetaBenefitRiskAnalysis DROP COLUMN projectId;
ALTER TABLE MetaBenefitRiskAnalysis DROP COLUMN title;

CREATE TABLE InterventionInclusion (
  interventionId INT,
  analysisId INT
 );
DROP TABLE MetaBenefitRiskAnalysis_Alternative;

--changeset stroombergc:48
ALTER TABLE FixedDoseIntervention ADD COLUMN lowerBoundUnitConcept varchar;
ALTER TABLE FixedDoseIntervention ADD COLUMN upperBoundUnitConcept varchar;
ALTER TABLE TitratedDoseIntervention ADD COLUMN minLowerBoundUnitConcept varchar;
ALTER TABLE TitratedDoseIntervention ADD COLUMN minUpperBoundUnitConcept varchar;
ALTER TABLE TitratedDoseIntervention ADD COLUMN maxLowerBoundUnitConcept varchar;
ALTER TABLE TitratedDoseIntervention ADD COLUMN maxUpperBoundUnitConcept varchar;
ALTER TABLE BothDoseTypesIntervention ADD COLUMN minLowerBoundUnitConcept varchar;
ALTER TABLE BothDoseTypesIntervention ADD COLUMN minUpperBoundUnitConcept varchar;
ALTER TABLE BothDoseTypesIntervention ADD COLUMN maxLowerBoundUnitConcept varchar;
ALTER TABLE BothDoseTypesIntervention ADD COLUMN maxUpperBoundUnitConcept varchar;

--changeset stroombergc:49
UPDATE abstractintervention SET semanticinterventionuri = CONCAT('http://trials.drugis.org/', semanticinterventionuri) WHERE LEFT(semanticinterventionuri, 4) <> 'http';
--rollback UPDATE abstractintervention SET semanticinterventionuri = RIGHT(semanticinterventionuri, 36) WHERE LEFT(semanticinterventionuri, 4) = 'http';

--changeset stroombergc:50
 -- constraints please ignore

--changeset stroombergc:51
DROP TABLE SingleStudyBenefitRiskAnalysis_Intervention;
--rollback CREATE TABLE SingleStudyBenefitRiskAnalysis_Intervention (AnalysisId INT,InterventionId INT,PRIMARY KEY(AnalysisId, InterventionId),FOREIGN KEY(AnalysisId) REFERENCES SingleStudyBenefitRiskAnalysis(id),FOREIGN KEY(InterventionId) REFERENCES Intervention(id));

--changeset reidd:52
ALTER TABLE SingleStudyBenefitRiskAnalysis ALTER COLUMN studyGraphUid RENAME TO studyGraphUri;
--rollback ALTER TABLE SingleStudyBenefitRiskAnalysis ALTER COLUMN studyGraphUri RENAME TO studyGraphUid;

--changeset reidd:53
-- updates data, irrelevant for test

--changeset reidd:54
-- updates data, irrelevant for test

--changeset reidd:55
-- updates data, irrelevant for test

--changeset reidd:56
ALTER TABLE model DROP COLUMN taskId;
ALTER TABLE model ADD COLUMN taskUrl VARCHAR;
--rollback ALTER TABLE model ADD COLUMN taskId int;
--rollback ALTER TABLE model DROP COLUMN taskUrl;

--changeset stroombergc:57
CREATE TABLE SingleIntervention (
   singleInterventionId INT NOT NULL,
   semanticInterventionLabel VARCHAR NOT NULL,
   semanticInterventionUri VARCHAR NOT NULL,
   PRIMARY KEY(singleInterventionId),
   FOREIGN KEY(singleInterventionId) REFERENCES AbstractIntervention(id)
);

INSERT INTO SingleIntervention (singleInterventionId, semanticInterventionLabel, semanticInterventionUri) SELECT id, semanticInterventionLabel, semanticInterventionUri FROM AbstractIntervention;

ALTER TABLE AbstractIntervention DROP COLUMN semanticInterventionLabel;
ALTER TABLE AbstractIntervention DROP COLUMN semanticInterventionUri;

CREATE TABLE CombinationIntervention (
   combinationInterventionId INT NOT NULL,
   PRIMARY KEY(combinationInterventionId),
   FOREIGN KEY(combinationInterventionId) REFERENCES AbstractIntervention(id)
);

CREATE TABLE InterventionCombination (
   combinationInterventionId INT NOT NULL,
   singleInterventionId INT NOT NULL,
   PRIMARY KEY(combinationInterventionId, singleInterventionId),
   FOREIGN KEY(combinationInterventionId) REFERENCES CombinationIntervention(combinationInterventionId),
   FOREIGN KEY(singleInterventionId) REFERENCES SingleIntervention(singleInterventionId)
);
--rollback DROP TABLE InterventionCombination;
--rollback DROP TABLE CombinationIntervention;
--rollback ALTER TABLE AbstractIntervention ADD COLUMN semanticInterventionLabel;
--rollback ALTER TABLE AbstractIntervention ADD COLUMN semanticInterventionUri;
--rollback INSERT INTO AbstractIntervention (semanticInterventionLabel, semanticInterventionUri) SELECT semanticInterventionLabel, semanticInterventionUri FROM SingleIntervention;
--rollback DROP TABLE SingleIntervention;

--changeset stroombergc:58
ALTER TABLE model ADD COLUMN archived boolean NOT NULL DEFAULT FALSE ;
ALTER TABLE model ADD COLUMN archived_on date;
--rollback ALTER TABLE model DROP COLUMN archived_on;
--rollback ALTER TABLE model DROP COLUMN archived;

--changeset reidd:59
CREATE TABLE funnelPlot (
  id SERIAL NOT NULL,
  modelId INT NOT NULL REFERENCES model(id),
  PRIMARY KEY(id)
);

CREATE TABLE funnelPlotComparison(
  plotId INT NOT NULL REFERENCES funnelplot(id),
  t1 INT NOT NULL,
  t2 INT NOT NULL,
  biasDirection INT NOT NULL,
  PRIMARY KEY(plotId, t1, t2)
);
--rollback DROP TABLE funnelPlotComparison CASCADE;
--rollback DROP TABLE funnelPlot CASCA

--changeset stroombergc:60
ALTER TABLE Outcome ADD COLUMN direction INT NOT NULL DEFAULT 1;
--rollback ALTER TABLE Outcome DROP COLUMN direction;

--changeset reidd:62
CREATE TABLE InterventionSet (
   interventionSetId INT NOT NULL,
   PRIMARY KEY(interventionSetId),
   FOREIGN KEY(interventionSetId) REFERENCES AbstractIntervention(id)
);

CREATE TABLE InterventionSetItem (
   interventionSetId INT NOT NULL,
   interventionId INT NOT NULL,
   PRIMARY KEY(interventionSetId, interventionId),
   FOREIGN KEY(interventionSetId) REFERENCES InterventionSet(interventionSetId),
   FOREIGN KEY(interventionId) REFERENCES AbstractIntervention(id)
);
--rollback DROP TABLE interventionSet;
--rollback DROP TABLE InterventionSetItem

--changeset reidd:63
CREATE TABLE MeasurementMomentInclusion (
  id INT NOT NULL,
  analysisId INT NOT NULL,
  study VARBINARY(255),
  measurementMoment VARBINARY(255)
);
--rollback DROP TABLE MeasurementMomentInclusion

--changeset reidd:64
CREATE TABLE customReport (
  id SERIAL NOT NULL,
  projectId INT NOT NULL,
  text TEXT NOT NULL,
  PRIMARY KEY(id),
  FOREIGN KEY(projectId) REFERENCES Project(id)
);
--rollback DROP TABLE customReport

--changeset reidd:65
ALTER TABLE project ADD COLUMN isArchived boolean NOT NULL DEFAULT FALSE ;
ALTER TABLE project ADD COLUMN archived_on date;
--rollback ALTER TABLE project DROP COLUMN archived_on;
--rollback ALTER TABLE project DROP COLUMN isArchived;

--changeset keijserj:66
CREATE TABLE MultipleIntervention(
   multipleInterventionId INT NOT NULL,
   PRIMARY KEY(multipleInterventionId),
   FOREIGN KEY(multipleInterventionId) REFERENCES AbstractIntervention(id)
);
CREATE TABLE MultipleInterventionItem (
   multipleInterventionId INT NOT NULL,
   interventionId INT NOT NULL,
   PRIMARY KEY(multipleInterventionId, interventionId),
   FOREIGN KEY(multipleInterventionId) REFERENCES MultipleIntervention(multipleInterventionId),
   FOREIGN KEY(interventionId) REFERENCES AbstractIntervention(id)
);
INSERT INTO MultipleIntervention(multipleInterventionId) SELECT interventionSetId from InterventionSet;
INSERT INTO MultipleInterventionItem(multipleInterventionId, interventionId) SELECT interventionSetId, interventionId from InterventionSetItem;
INSERT INTO MultipleIntervention(multipleInterventionId) SELECT combinationInterventionId from CombinationIntervention;
INSERT INTO MultipleInterventionItem(multipleInterventionId, interventionId) SELECT combinationInterventionId, singleInterventionId from InterventionCombination;
DROP TABLE InterventionSetItem;
DROP TABLE InterventionCombination;
--rollback CREATE TABLE InterventionSetItem (interventionSetId INT NOT NULL, interventionId INT NOT NULL, PRIMARY KEY(interventionSetId, interventionId), FOREIGN KEY(interventionSetId) REFERENCES InterventionSet(interventionSetId), FOREIGN KEY(interventionId) REFERENCES AbstractIntervention(id));
--rollback INSERT INTO InterventionSetItem(interventionSetId, interventionId) SELECT multipleInterventionId, interventionId FROM multipleInterventionItem WHERE multipleInterventionId IN (SELECT interventionSetId FROM interventionSet) ;
--rollback CREATE TABLE InterventionCombination (combinationInterventionId INT NOT NULL, singleInterventionId INT NOT NULL, PRIMARY KEY(combinationInterventionId, singleInterventionId), FOREIGN KEY(combinationInterventionId) REFERENCES CombinationIntervention(combinationInterventionId), FOREIGN KEY(singleInterventionId) REFERENCES SingleIntervention(singleInterventionId));
--rollback INSERT INTO InterventionCombination(combinationInterventionId, singleInterventionId) SELECT multipleInterventionId, interventionId FROM multipleInterventionItem WHERE multipleInterventionId IN (SELECT combinationInterventionId FROM CombinationIntervention) ;

--changeset keijserj:67
ALTER TABLE AbstractAnalysis ADD COLUMN isArchived boolean NOT NULL DEFAULT FALSE ;
ALTER TABLE AbstractAnalysis ADD COLUMN archived_on date;
--rollback ALTER TABLE AbstractAnalysis DROP COLUMN archived_on;
--rollback ALTER TABLE AbstractAnalysis DROP COLUMN isArchived;

--changeset keijserj:68
CREATE TABLE modelBaseline (
  modelId INT NOT NULL,
  baseline VARCHAR NOT NULL,
  PRIMARY KEY (modelId),
  FOREIGN KEY (modelId) REFERENCES Model(id)
);
--rollback DROP TABLE modelBaseline;

--changeset keijserj:69
CREATE TABLE effectsTableExclusion (
    analysisId INT NOT NULL,
    alternativeId INT NOT NULL,
    PRIMARY KEY (analysisId, alternativeId),
    FOREIGN KEY (analysisId) REFERENCES AbstractAnalysis(id)
);
--rollback DROP TABLE effectsTableExclusion;

--changeset keijserj:70
ALTER TABLE FixedDoseIntervention ADD COLUMN lowerBoundConversionMultiplier DOUBLE PRECISION;
ALTER TABLE FixedDoseIntervention ADD COLUMN upperBoundConversionMultiplier DOUBLE PRECISION;
ALTER TABLE TitratedDoseIntervention ADD COLUMN minLowerBoundConversionMultiplier DOUBLE PRECISION;
ALTER TABLE TitratedDoseIntervention ADD COLUMN minUpperBoundConversionMultiplier DOUBLE PRECISION;
ALTER TABLE TitratedDoseIntervention ADD COLUMN maxLowerBoundConversionMultiplier DOUBLE PRECISION;
ALTER TABLE TitratedDoseIntervention ADD COLUMN maxUpperBoundConversionMultiplier DOUBLE PRECISION;
ALTER TABLE BothDoseTypesIntervention ADD COLUMN minLowerBoundConversionMultiplier DOUBLE PRECISION;
ALTER TABLE BothDoseTypesIntervention ADD COLUMN minUpperBoundConversionMultiplier DOUBLE PRECISION;
ALTER TABLE BothDoseTypesIntervention ADD COLUMN maxLowerBoundConversionMultiplier DOUBLE PRECISION;
ALTER TABLE BothDoseTypesIntervention ADD COLUMN maxUpperBoundConversionMultiplier DOUBLE PRECISION;

--changeset reidd:71
CREATE TABLE scaledUnit (
  id SERIAL NOT NULL,
  projectId INT NOT NULL,
  conceptUri VARCHAR NOT NULL,
  multiplier DOUBLE PRECISION NOT NULL,
  name VARCHAR NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(projectId) REFERENCES Project(id)
);

--changeset keijserj:72
CREATE TABLE subProblem(
  id SERIAL NOT NULL,
  workspaceId INT NOT NULL,
  definition VARCHAR NOT NULL,
  title VARCHAR NOT NULL,
  PRIMARY KEY(id),
  FOREIGN KEY(workspaceId) REFERENCES AbstractAnalysis(id) ON DELETE CASCADE
);
ALTER TABLE scenario ADD COLUMN subProblemId INT;

--changeset keijserj:73
ALTER TABLE effectsTableExclusion ALTER COLUMN alternativeId VARCHAR;

--changeset keijserj:74
ALTER TABLE effectsTableExclusion RENAME TO effectsTableAlternativeInclusion;

--changeset reidd:75
DROP TABLE remarks;
ALTER TABLE metabenefitriskanalysis RENAME TO benefitriskanalysis;
INSERT INTO benefitriskanalysis(id, problem, finalized) SELECT id, problem, CASE WHEN problem IS NULL THEN false ELSE true END FROM SingleStudyBenefitRiskAnalysis;

ALTER TABLE mbroutcomeinclusion RENAME TO BenefitRiskNMAOutcomeInclusion;
CREATE TABLE BenefitRiskStudyOutcomeInclusion(
    analysisId INT NOT NULL,
    outcomeId INT NOT NULL,
    studyGraphUri VARCHAR,
    PRIMARY KEY(analysisId, outcomeId),
    FOREIGN KEY(analysisId) REFERENCES BenefitRiskAnalysis(id),
    FOREIGN KEY(outcomeId) REFERENCES outcome(id)
);
ALTER TABLE BenefitRiskNMAOutcomeInclusion ALTER COLUMN  metabenefitriskanalysisid RENAME TO analysisId;
INSERT INTO BenefitRiskStudyOutcomeInclusion(analysisId, studyGraphUri, outcomeId)
    SELECT id, studyGraphUri, outcomeId
    FROM SingleStudyBenefitRiskAnalysis INNER JOIN SingleStudyBenefitRiskAnalysis_Outcome ON SingleStudyBenefitRiskAnalysis.id = SingleStudyBenefitRiskAnalysis_Outcome.analysisId;
DROP TABLE SingleStudyBenefitRiskAnalysis_Outcome;
DROP TABLE SingleStudyBenefitRiskAnalysis;

--changeset keijserj:76
--DROP TABLE effectsTableAlternativeInclusion;

--changeset keijserj:77
CREATE TABLE ordering(
    analysisId INT NOT NULL,
    ordering VARCHAR NOT NULL,
    PRIMARY KEY(analysisId),
    FOREIGN KEY(analysisId) REFERENCES BenefitRiskAnalysis(id) ON DELETE CASCADE
);

--changeset keijserj:78
CREATE TABLE toggledColumns(
    analysisId INT NOT NULL,
    toggledColumns VARCHAR NOT NULL,
    PRIMARY KEY(analysisId),
    FOREIGN KEY(analysisId) REFERENCES BenefitRiskAnalysis(id) ON DELETE CASCADE
);
--changeset keijserj:79
CREATE TABLE workspaceSettings(
  analysisId INT NOT NULL,
  settings VARCHAR NOT NULL,
  PRIMARY KEY (analysisId),
  FOREIGN KEY (analysisId) REFERENCES BenefitRiskAnalysis(id) ON DELETE CASCADE
);
DROP TABLE toggledColumns;

--changeset keijserj:80
ALTER TABLE BenefitRiskStudyOutcomeInclusion ADD COLUMN baseline VARCHAR;

--changeset keijserj:81
ALTER TABLE VersionMapping ADD COLUMN archived BOOLEAN DEFAULT false;
ALTER TABLE VersionMapping ADD COLUMN archivedon VARCHAR;
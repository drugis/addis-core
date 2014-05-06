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

CREATE TABLE Analysis (id SERIAL NOT NULL,
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
CREATE TABLE SingleStudyBenefitRiskAnalysis (id SERIAL NOT NULL,
        projectId INT,
        name VARCHAR NOT NULL,
        studyId INT,
        problem VARCHAR NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(projectId) REFERENCES Project(id));

CREATE TABLE NetworkMetaAnalysis (id SERIAL NOT NULL,
          projectId INT,
          name VARCHAR NOT NULL,
          studyId INT,
          outcomeId INT,
          problem VARCHAR NULL,
    PRIMARY KEY (id),
    FOREIGN KEY(projectId) REFERENCES Project(id),
    FOREIGN KEY(outcomeId) REFERENCES Outcome(id));


DROP TABLE Analysis CASCADE;

ALTER TABLE Analysis_Outcomes RENAME TO SingleStudyBenefitRiskAnalysis_Outcomes;
ALTER TABLE Analysis_Interventions RENAME TO SingleStudyBenefitRiskAnalysis_Interventions;

ALTER TABLE SingleStudyBenefitRiskAnalysis_Interventions ADD CONSTRAINT ssbr_analysis_interventions_analysisid_fkey FOREIGN KEY (analysisId) REFERENCES SingleStudyBenefitRiskAnalysis(id);
ALTER TABLE SingleStudyBenefitRiskAnalysis_Outcomes ADD CONSTRAINT ssbr_analysis_outcomes_analysisid_fkey FOREIGN KEY (analysisId) REFERENCES SingleStudyBenefitRiskAnalysis(id);
ALTER TABLE scenario ADD CONSTRAINT ssbr_scenario_workspace_fkey FOREIGN KEY (workspace) REFERENCES SingleStudyBenefitRiskAnalysis(id);


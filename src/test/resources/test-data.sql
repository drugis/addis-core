INSERT INTO public.account (id, username, firstname, lastname, email) VALUES (1, '1000123', 'Connor', 'Bonnor', 'connor@test.com');
INSERT INTO public.account (id, username, firstname, lastname, email) VALUES (2, '2000123', 'Daan', 'Baan', 'foo@bar.com');

INSERT INTO public.ApplicationKey (id, secretKey, accountId, applicationName, creationDate, revocationDate) VALUES (1, 'supersecretkey', 1, 'Test Application', '2015-09-01', '2080-12-31');

INSERT INTO public.project (id, owner, name, description, namespaceUid, datasetversion) VALUES (1, 1, 'testname 1', 'testdescription 1', 'namespaceUid-1', 'version-1');
INSERT INTO public.project (id, owner, name, description, namespaceUid, datasetversion) VALUES (2, 2, 'testname 2', 'testdescription 2', 'namespaceUid-1', 'version-1');
INSERT INTO public.project (id, owner, name, description, namespaceUid, datasetversion) VALUES (3, 1, 'testname 3', 'testdescription 3', 'namespaceUid-2', 'version-1');

INSERT INTO public.outcome (id, project, name, motivation, semanticOutcomeLabel, semanticOutcomeUri) VALUES (1, 1, 'outcome 1', 'outcome description 1', 'outcome1', 'org.drugis.addis.outcome://outcome1');
INSERT INTO public.outcome (id, project, name, motivation, semanticOutcomeLabel, semanticOutcomeUri) VALUES (2, 1, 'outcome 2', 'outcome description 2', 'outcome2', 'org.drugis.addis.outcome://outcome2');
INSERT INTO public.outcome (id, project, name, motivation, semanticOutcomeLabel, semanticOutcomeUri) VALUES (3, 2, 'outcome 3', 'outcome description 3', 'outcome3', 'org.drugis.addis.outcome://outcome3');

INSERT INTO public.intervention (id, project, name, motivation, semanticInterventionLabel, semanticInterventionUri) VALUES (1, 1, 'intervention 1', 'intervention description 1', 'intervention1', 'http://trials.drugis.org/namespaces/1/interventions/1');
INSERT INTO public.intervention (id, project, name, motivation, semanticInterventionLabel, semanticInterventionUri) VALUES (2, 1, 'intervention 2', 'intervention description 2', 'intervention2', 'http://trials.drugis.org/namespaces/1/interventions/2');
INSERT INTO public.intervention (id, project, name, motivation, semanticInterventionLabel, semanticInterventionUri) VALUES (3, 2, 'intervention 3', 'intervention description 3', 'intervention3', 'http://trials.drugis.org/namespaces/1/interventions/3');

INSERT INTO public.SingleStudyBenefitRiskAnalysis (id, projectId, title) VALUES (-1, 1, 'analysis 1');
INSERT INTO public.SingleStudyBenefitRiskAnalysis (id, projectId, title) VALUES (-2, 1, 'analysis 2');
INSERT INTO public.SingleStudyBenefitRiskAnalysis (id, projectId, title) VALUES (-3 ,2, 'analysis 3');
INSERT INTO public.SingleStudyBenefitRiskAnalysis (id, projectId, title, problem) VALUES (-4, 1, 'analysis 3', 'singlestudy problem');

INSERT INTO public.NetworkMetaAnalysis(id, projectId, title) VALUES (-5, 1, 'nma');
INSERT INTO public.NetworkMetaAnalysis(id, projectId, title) VALUES (-6, 1, 'nma 2');
INSERT INTO public.NetworkMetaAnalysis(id, projectId, title) VALUES (-7, 2, 'nma task test');

INSERT INTO public.scenario (id, workspace, title, state) VALUES (1, -1, 'Default', 'problem state');
INSERT INTO public.scenario (id, workspace, title, state) VALUES (2, -1, 'Scenario title', 'problem state modified');
INSERT INTO public.scenario (id, workspace, title, state) VALUES (3, -2, 'Default for different analysis', 'problem state modified');

INSERT INTO public.model(id, analysisId, title, linearModel, modelType, heterogeneityPrior, burnInIterations, inferenceIterations, thinningFactor, likelihood, link) VALUES (1, -5, 'model title', 'fixed', '{"type": "network"}', '{"type": "automatic"}', 5000, 20000, 10, 'binom', 'logit');
INSERT INTO public.model(id, analysisId, title, linearModel, modelType, heterogeneityPrior, burnInIterations, inferenceIterations, thinningFactor, likelihood, link, outcomeScale) VALUES (2, -5, 'model title', 'fixed', '{"type": "pairwise", "details": {"to": {id: -1, "name" : "study1"}, "from": {"id": -2, "name": "study2"}}}', '{"type": "variance", "values": {"mean": 2.3, "stdDev": 0.3} }', 5000, 20000, 10,  'binom', 'logit', 2.2);

INSERT INTO public.model(id, analysisId, taskId, title, linearModel, modelType, burnInIterations, inferenceIterations, thinningFactor, likelihood, link) VALUES (3, -7, 1, 'model title', 'fixed', '{"type": "network"}', 50, 20, 1, 'binom', 'logit');
INSERT INTO public.model(id, analysisId, taskId, title, linearModel, modelType, burnInIterations, inferenceIterations, thinningFactor, likelihood, link) VALUES (4, -7, 2, 'model title', 'fixed', '{"type": "network"}', 50, 20, 1, 'binom', 'logit');

INSERT INTO public.armExclusion (id, trialverseUid, analysisId) VALUES (-1, '-101', -6);
INSERT INTO public.armExclusion (id, trialverseUid, analysisId) VALUES (-2, '-102', -6);

INSERT INTO public.interventionInclusion (id, interventionId, analysisId) VALUES (-1, 2, -6);

INSERT INTO public.remarks(analysisId, remarks) VALUES(-1, 'yo yo yo !');

INSERT INTO public.covariate(id, project, name, motivation, definitionkey) VALUES (1, 1, 'covariate 1 name', 'my motivation', 'ALLOCATION_RANDOMIZED');
INSERT INTO public.covariate(id, project, name, motivation, definitionkey) VALUES (2, 1, 'covariate 2 name', 'my motivation', 'BLINDING_AT_LEAST_SINGLE_BLIND');
INSERT INTO public.covariate(id, project, name, motivation, definitionkey) VALUES (3, 2, 'covariate 3 name', 'my motivation', 'ALLOCATION_RANDOMIZED');

UPDATE public.NetworkMetaAnalysis SET primaryModel = 1 WHERE id = -7;
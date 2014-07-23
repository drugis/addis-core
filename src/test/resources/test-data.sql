INSERT INTO public.account (id, username, firstname, lastname, password) VALUES (1, 'foo@bar.com', 'Connor', 'Bonnor', null);
INSERT INTO public.account (id, username, firstname, lastname, password) VALUES (2, 'test@bla.com', 'Daan', 'Baan', null);

INSERT INTO public.project (id, owner, name, description, namespaceUid) VALUES (1, 1, 'testname 1', 'testdescription 1', 'namespaceUid-1');
INSERT INTO public.project (id, owner, name, description, namespaceUid) VALUES (2, 2, 'testname 2', 'testdescription 2', 'namespaceUid-1');
INSERT INTO public.project (id, owner, name, description, namespaceUid) VALUES (3, 1, 'testname 3', 'testdescription 3', 'namespaceUid-2');

INSERT INTO public.outcome (id, project, name, motivation, semanticOutcomeLabel, semanticOutcomeUri) VALUES (1, 1, 'outcome 1', 'outcome description 1', 'outcome1', 'org.drugis.addis.outcome://outcome1');
INSERT INTO public.outcome (id, project, name, motivation, semanticOutcomeLabel, semanticOutcomeUri) VALUES (2, 1, 'outcome 2', 'outcome description 2', 'outcome2', 'org.drugis.addis.outcome://outcome2');
INSERT INTO public.outcome (id, project, name, motivation, semanticOutcomeLabel, semanticOutcomeUri) VALUES (3, 2, 'outcome 3', 'outcome description 3', 'outcome3', 'org.drugis.addis.outcome://outcome3');

INSERT INTO public.intervention (id, project, name, motivation, semanticInterventionLabel, semanticInterventionUri) VALUES (1, 1, 'intervention 1', 'intervention description 1', 'intervention1', 'http://trials.drugis.org/namespaces/1/interventions/1');
INSERT INTO public.intervention (id, project, name, motivation, semanticInterventionLabel, semanticInterventionUri) VALUES (2, 1, 'intervention 2', 'intervention description 2', 'intervention2', 'http://trials.drugis.org/namespaces/1/interventions/2');
INSERT INTO public.intervention (id, project, name, motivation, semanticInterventionLabel, semanticInterventionUri) VALUES (3, 2, 'intervention 3', 'intervention description 3', 'intervention3', 'http://trials.drugis.org/namespaces/1/interventions/3');

INSERT INTO public.SingleStudyBenefitRiskAnalysis (id, projectId, name) VALUES (-1, 1, 'analysis 1');
INSERT INTO public.SingleStudyBenefitRiskAnalysis (id, projectId, name) VALUES (-2, 1, 'analysis 2');
INSERT INTO public.SingleStudyBenefitRiskAnalysis (id, projectId, name) VALUES (-3 ,2, 'analysis 3');
INSERT INTO public.SingleStudyBenefitRiskAnalysis (id, projectId, name, problem) VALUES (-4, 1, 'analysis 3', 'singlestudy problem');

INSERT INTO public.NetworkMetaAnalysis(id, projectId, name) VALUES (-5, 1, 'nma');
INSERT INTO public.NetworkMetaAnalysis(id, projectId, name) VALUES (-6, 1, 'nma 2');

INSERT INTO public.scenario (id, workspace, title, state) VALUES (1, -1, 'Default', 'problem state');
INSERT INTO public.scenario (id, workspace, title, state) VALUES (2, -1, 'Scenario title', 'problem state modified');
INSERT INTO public.scenario (id, workspace, title, state) VALUES (3, -2, 'Default for different analysis', 'problem state modified');

INSERT INTO public.model(id, analysisId) VALUES (1, -5);

INSERT INTO public.armExclusion (id, trialverseId, analysisId) VALUES (-1, -101, -6);
INSERT INTO public.armExclusion (id, trialverseId, analysisId) VALUES (-2, -102, -6);

INSERT INTO public.interventionInclusion (id, interventionId, analysisId) VALUES (-1, 2, -6);

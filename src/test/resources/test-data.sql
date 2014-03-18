INSERT INTO public.account (id, username, firstname, lastname, password) VALUES (1, 'foo@bar.com', 'Connor', 'Bonnor', null);
INSERT INTO public.account (id, username, firstname, lastname, password) VALUES (2, 'test@bla.com', 'Daan', 'Baan', null);

INSERT INTO public.project (id, owner, name, description, trialverseId) VALUES (1, 1, 'testname 1', 'testdescription 1', 1);
INSERT INTO public.project (id, owner, name, description, trialverseId) VALUES (2, 2, 'testname 2', 'testdescription 2', 1);
INSERT INTO public.project (id, owner, name, description, trialverseId) VALUES (3, 1, 'testname 3', 'testdescription 3', 2);

INSERT INTO public.outcome (id, version, project, name, motivation, semanticOutcomeLabel, semanticOutcomeUri) VALUES (1, 1, 1, 'outcome 1', 'outcome description 1', 'outcome1', 'org.drugis.addis.outcome://outcome1');
INSERT INTO public.outcome (id, version, project, name, motivation, semanticOutcomeLabel, semanticOutcomeUri) VALUES (2, 1, 1, 'outcome 2', 'outcome description 2', 'outcome2', 'org.drugis.addis.outcome://outcome2');
INSERT INTO public.outcome (id, version, project, name, motivation, semanticOutcomeLabel, semanticOutcomeUri) VALUES (3, 1, 2, 'outcome 3', 'outcome description 3', 'outcome3', 'org.drugis.addis.outcome://outcome3');

INSERT INTO public.intervention (id, project, name, motivation, semanticInterventionLabel, semanticInterventionUri) VALUES (1, 1, 'intervention 1', 'intervention description 1', 'intervention1', 'http://trials.drugis.org/namespaces/1/interventions/1');
INSERT INTO public.intervention (id, project, name, motivation, semanticInterventionLabel, semanticInterventionUri) VALUES (2, 1, 'intervention 2', 'intervention description 2', 'intervention2', 'http://trials.drugis.org/namespaces/1/interventions/2');
INSERT INTO public.intervention (id, project, name, motivation, semanticInterventionLabel, semanticInterventionUri) VALUES (3, 2, 'intervention 3', 'intervention description 3', 'intervention3', 'http://trials.drugis.org/namespaces/1/interventions/3');

INSERT INTO public.analysis (id, version, projectId, name, analysisType) VALUES (1, 1,1, 'analysis 1', 'Single-study Benefit-Risk');
INSERT INTO public.analysis (id, version, projectId, name, analysisType) VALUES (2, 1,1, 'analysis 2', 'Single-study Benefit-Risk');
INSERT INTO public.analysis (id, version, projectId, name, analysisType) VALUES (3, 1,2, 'analysis 3', 'Single-study Benefit-Risk');
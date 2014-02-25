INSERT INTO public.account (id, username, firstname, lastname, password) VALUES (1, 'foo@bar.com', 'Connor', 'Bonnor', null);
INSERT INTO public.account (id, username, firstname, lastname, password) VALUES (2, 'test@bla.com', 'Daan', 'Baan', null);

INSERT INTO public.project (id, owner, name, description, trialverse) VALUES (1, 1, 'testname 1', 'testdescription 1', 'org.drugis.addis.trialverse://testtrialverse1');
INSERT INTO public.project (id, owner, name, description, trialverse) VALUES (2, 2, 'testname 2', 'testdescription 2', 'org.drugis.addis.trialverse://testtrialverse2');
INSERT INTO public.project (id, owner, name, description, trialverse) VALUES (3, 1, 'testname 3', 'testdescription 3', 'org.drugis.addis.trialverse://testtrialverse1');

INSERT INTO public.outcome (id, project, name, motivation, semanticOutcome) VALUES (1, 1, 'outcome 1', 'outcome description 1', 'org.drugis.addis.outcome://outcome1');
INSERT INTO public.outcome (id, project, name, motivation, semanticOutcome) VALUES (2, 1, 'outcome 2', 'outcome description 2', 'org.drugis.addis.outcome://outcome2');
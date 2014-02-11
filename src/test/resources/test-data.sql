INSERT INTO public.account (id, username, firstname, lastname, password) VALUES (1, 'foo@bar.com', 'Connor', 'Bonnor', null);
INSERT INTO public.account (id, username, firstname, lastname, password) VALUES (2, 'test@bla.com', 'Daan', 'Baan', null);

INSERT INTO public.project (id, owner, name, description, namespace) VALUES (1, 1, 'testname 1', 'testdescription 1', 'org.drugis.addis.trialverse://testnamespace1');
INSERT INTO public.project (id, owner, name, description, namespace) VALUES (2, 2, 'testname 2', 'testdescription 2', 'org.drugis.addis.trialverse://testnamespace2');
INSERT INTO public.project (id, owner, name, description, namespace) VALUES (3, 1, 'testname 3', 'testdescription 3', 'org.drugis.addis.trialverse://testnamespace1');

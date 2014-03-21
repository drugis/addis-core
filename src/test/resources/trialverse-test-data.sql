INSERT INTO public.namespaces (id, name, description) VALUES (1, 'name1', 'description1');
INSERT INTO public.namespaces (id, name, description) VALUES (2, 'name2', 'description2');
INSERT INTO public.namespaces (id, name, description) VALUES (3, 'name3', 'description3');

--
-- Data for Name: studies; Type: TABLE DATA; Schema: public; Owner: trialverse
--
INSERT INTO studies VALUES (1, 'Feighner et al, 1991', 'Double-blind comparison of bupropion and fluoxetine in depressed outpatients');
INSERT INTO studies VALUES (2, 'Nemeroff et al, 1995', 'Double-blind multicenter comparison of fluvoxamine versus sertraline in the treatment of depressed outpatients. Depression 1995; 3:163-169');
INSERT INTO studies VALUES (3, 'Montgomery et al, 2004', 'A randomised study comparing escitalopram with venlafaxine XR in primary care patients with major depressive disorder.');
INSERT INTO studies VALUES (4, 'Sechter et al, 1999', 'A double-blind comparison of sertraline and fluoxetine in the treatment of major depressive episode in outpatients.');
INSERT INTO studies VALUES (5, 'Coleman et al, 2001', 'A placebo-controlled comparison of the effects on sexual functioning of bupropion sustained release and fluoxetine');
INSERT INTO studies VALUES (6, 'De Nayer et al, 2002', 'Venlafaxine compared with fluoxetine in outpatients with depression and concomitant anxiety.');

INSERT INTO namespace_studies VALUES(1, 1);
INSERT INTO namespace_studies VALUES(1, 2);
INSERT INTO namespace_studies VALUES(1, 3);
INSERT INTO namespace_studies VALUES(2, 4);
INSERT INTO namespace_studies VALUES(2, 5);
INSERT INTO namespace_studies VALUES(2, 6);

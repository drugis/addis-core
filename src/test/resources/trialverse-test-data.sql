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

INSERT INTO  arms (id, study, name) VALUES(1, 1, 'study 1 arm 1');
INSERT INTO  arms (id, study, name) VALUES(2, 1, 'study 1 arm 2');

INSERT INTO  activities (id, study, name) VALUES(1, 1, 'study 1 activity 1');
INSERT INTO  activities (id, study, name) VALUES(2, 1, 'study 1 activity 2');

INSERT INTO  treatments (id, activity, drug) VALUES(1, 1, 1);
INSERT INTO  treatments (id, activity, drug) VALUES(2, 2, 3);

INSERT INTO  measurement_moments (id, study, name, epoch, is_primary) VALUES(1, 1, 'P0D BEFORE_EPOCH_END Main phase', 1, true);

INSERT INTO  designs (arm, activity, epoch) VALUES(1, 1, 1);
INSERT INTO  designs (arm, activity, epoch) VALUES(2, 2, 1);

INSERT INTO variables (id, study, name, description, unit_description, is_primary, measurement_type, variable_type)
               VALUES (1,   1,    'Nausea', 'descr1', 'Millipuke', true, 'RATE', 'TEST');
INSERT INTO variables (id, study, name, description, unit_description, is_primary, measurement_type, variable_type)
               VALUES (2,   1,    'HAM', 'Pig', 'descr2', false, 'RATE', 'TEST');

INSERT INTO measurements(study, variable, measurement_moment, arm, attribute, integer_value, real_value)
                  VALUES(1,     1,        1,                  1,  'sample size', 60, NULL);
INSERT INTO measurements(study, variable, measurement_moment, arm, attribute, integer_value, real_value)
                  VALUES(1,     1,        1,                  1,  'rate'       , 15, NULL);
INSERT INTO measurements(study, variable, measurement_moment, arm, attribute, integer_value, real_value)
                  VALUES(1,     1,        1,                  2,  'sample size', 50, NULL);
INSERT INTO measurements(study, variable, measurement_moment, arm, attribute, integer_value, real_value)
                  VALUES(1,     1,        1,                  2,  'rate'       ,  5, NULL);

INSERT INTO measurements(study, variable, measurement_moment, arm, attribute, integer_value, real_value)
                  VALUES(1,     2,        1,                  1,  'sample size', 40, NULL);
INSERT INTO measurements(study, variable, measurement_moment, arm, attribute, integer_value, real_value)
                  VALUES(1,     2,        1,                  1,  'rate'       , 12, NULL);
INSERT INTO measurements(study, variable, measurement_moment, arm, attribute, integer_value, real_value)
                  VALUES(1,     2,        1,                  2,  'sample size', 30, NULL);
INSERT INTO measurements(study, variable, measurement_moment, arm, attribute, integer_value, real_value)
                  VALUES(1,     2,        1,                  2,  'rate'       ,  2, NULL);
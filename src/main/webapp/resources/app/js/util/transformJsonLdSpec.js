'use strict';
define(['angular', 'lodash', './transformJsonLd', './context'], function(angular, _, transformJsonLd, context) {
  describe('transformJsonLd', function() {

    var emptyStudy = {
      '@id': 'http://trials.drugis.org/studies/695855bd-5782-4c67-a270-eb4459c3a4f6',
      '@type': 'http://trials.drugis.org/ontology#Study',
      'label': 'study 1',
      'comment': 'my study',
      '@context': context
    };

    var emptyTransformed = {
      '@graph': [{
        '@id': 'http://trials.drugis.org/studies/695855bd-5782-4c67-a270-eb4459c3a4f6',
        '@type': 'ontology:Study',
        'label': 'study 1',
        'comment': 'my study',
        'has_outcome': [],
        'has_arm': [],
        'has_epochs': {
          '@id': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'
        },
        'has_group': [],
        'has_included_population': [],
        'has_activity': [],
        'has_indication': [],
        'has_objective': [],
        'has_publication': [],
        'has_eligibility_criteria': []
      }],
      '@context': context
    };

    var studyWithList = {
      '@graph': [{
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000000',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'Rhinitis',
        'sameAs': 'http://trials.drugis.org/concepts/f1c8825c-be60-41ba-aa36-8040c8ac1438'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000001',
        'dosingPeriodicity': 'P1D',
        'unit': 'http://trials.drugis.org/instances/a82113a8-9353-4155-9758-57f03b467320',
        'value': '2.000000e+01'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000002',
        'applied_in_epoch': 'http://trials.drugis.org/instances/62bc14d7-4fbc-4973-8119-ffaca0bb18e4',
        'applied_to_arm': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000003',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#continuous',
        'comment': '',
        'label': 'CGI-S bas',
        'sameAs': 'http://trials.drugis.org/concepts/fe3787d5-92f2-4bf0-b0d1-9c56458738c9'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000004',
        'category': 'Male',
        'http://trials.drugis.org/ontology#count': '14'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000005',
        'applied_in_epoch': 'http://trials.drugis.org/instances/a02fdb05-4986-4f58-b20f-973572813c8d',
        'applied_to_arm': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000006',
        'applied_in_epoch': 'http://trials.drugis.org/instances/a02fdb05-4986-4f58-b20f-973572813c8d',
        'applied_to_arm': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000007',
        'has_id': 'http://pubmed.com/11926722'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000008',
        'category': 'Male',
        'http://trials.drugis.org/ontology#count': '22'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000009',
        'first': {
          '@id': 'http://trials.drugis.org/instances/62bc14d7-4fbc-4973-8119-ffaca0bb18e4'
        },
        'rest': {
          '@list': ['http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c']
        }
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac1100590000000a',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'Dizziness',
        'sameAs': 'http://trials.drugis.org/concepts/e2d2ea4e-53b0-42d3-bcbb-a74041e66d2b'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000000',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'Dropouts',
        'sameAs': 'http://trials.drugis.org/concepts/14b15b36-f46a-4488-9d58-b31b210094d2'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000001',
        'dosingPeriodicity': 'P1D',
        'unit': 'http://trials.drugis.org/instances/a82113a8-9353-4155-9758-57f03b467320',
        'value': '4.000000e+01'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000002',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'Sweating',
        'sameAs': 'http://trials.drugis.org/concepts/e5030f87-435f-4293-bb90-c6b48bf2f1fc'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000003',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'Dry Mouth',
        'sameAs': 'http://trials.drugis.org/concepts/4d1ce568-6edb-42fe-8ac8-6f45b8351a56'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000004',
        'dosingPeriodicity': 'P1D',
        'unit': 'http://trials.drugis.org/instances/a82113a8-9353-4155-9758-57f03b467320',
        'value': '0.000000e+00'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000005',
        'applied_in_epoch': 'http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c',
        'applied_to_arm': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000006',
        'applied_in_epoch': 'http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c',
        'applied_to_arm': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000007',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'Headache',
        'sameAs': 'http://trials.drugis.org/concepts/8c6c7ad4-5db0-414a-b7f9-2e10d90ed624'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000008',
        'category': 'Female',
        'http://trials.drugis.org/ontology#count': '44'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000009',
        'dosingPeriodicity': 'P1D',
        'unit': 'http://trials.drugis.org/instances/a82113a8-9353-4155-9758-57f03b467320',
        'value': '1.200000e+02'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000000a',
        'comment': 'Duloxetine hydrochloride, a dual reuptake inhibitor of serotonin and norepinephrine, was evaluated for therapeutic efficacy and safety/tolerability in the treatment of major depression.'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000000b',
        'first': {
          '@id': 'http://trials.drugis.org/instances/a02fdb05-4986-4f58-b20f-973572813c8d'
        },
        'rest': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000009'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000000d',
        'category': 'Male',
        'http://trials.drugis.org/ontology#count': '26'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000000e',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#continuous',
        'comment': ['years', ''],
        'label': 'Age',
        'sameAs': 'http://trials.drugis.org/concepts/c6682d87-425f-4814-b2b5-7c62fa714ec8'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000000f',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'Asthenia',
        'sameAs': 'http://trials.drugis.org/concepts/01002898-3bcb-4e97-b598-ebb8d66b646f'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000010',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'Constipation',
        'sameAs': 'http://trials.drugis.org/concepts/3319acba-e246-45fe-ad4b-fdd31a73c2db'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000011',
        'applied_in_epoch': 'http://trials.drugis.org/instances/62bc14d7-4fbc-4973-8119-ffaca0bb18e4',
        'applied_to_arm': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000012',
        'category': 'Female',
        'http://trials.drugis.org/ontology#count': '48'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000013',
        'applied_in_epoch': 'http://trials.drugis.org/instances/a02fdb05-4986-4f58-b20f-973572813c8d',
        'applied_to_arm': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000014',
        '@type': 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment',
        'treatment_dose': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000001',
        'treatment_has_drug': 'http://trials.drugis.org/instances/4586c415-aa62-4995-bb47-1f92f7f95cb9'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000015',
        'first': 'Male',
        'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest': {
          '@list': ['Female']
        }
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000016',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#continuous',
        'comment': ['kg', ''],
        'label': 'Weight',
        'sameAs': 'http://trials.drugis.org/concepts/22ea6c4c-54a5-4f40-aa1a-d8272794226f'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000017',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'HAM-D Responders',
        'sameAs': 'http://trials.drugis.org/concepts/143d0285-65ba-4d22-9ba0-8e622cfc1046'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000018',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'Somnolence',
        'sameAs': 'http://trials.drugis.org/concepts/744c5efe-db79-47bb-872a-5fcc12642e05'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000019',
        'category': 'Female',
        'http://trials.drugis.org/ontology#count': '19'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001a',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'Diarrhea',
        'sameAs': 'http://trials.drugis.org/concepts/d091accc-f65d-4e9d-8ad3-3657deb73f46'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001b',
        'applied_in_epoch': 'http://trials.drugis.org/instances/62bc14d7-4fbc-4973-8119-ffaca0bb18e4',
        'applied_to_arm': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001c',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'Insomnia',
        'sameAs': 'http://trials.drugis.org/concepts/11e8f65e-44ae-4f59-9dee-eaae4fad7dce'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001d',
        '@type': 'http://trials.drugis.org/ontology#TitratedDoseDrugTreatment',
        'treatment_has_drug': 'http://trials.drugis.org/instances/d887928f-923b-49a9-8056-4cea570f9317',
        'treatment_max_dose': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000009',
        'treatment_min_dose': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000001'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001e',
        '@type': 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment',
        'treatment_dose': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000004',
        'treatment_has_drug': 'http://trials.drugis.org/instances/131649ff-418f-40a7-8744-04a849811025'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001f',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#continuous',
        'comment': ['', 'HAMD score at the baseline'],
        'label': 'HAMD bas',
        'sameAs': 'http://trials.drugis.org/concepts/8fc0ac78-25fc-46d1-9a21-44f447f54b20'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000020',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'Anorexia',
        'sameAs': 'http://trials.drugis.org/concepts/b5ea99ad-4459-4f3c-ab92-c22896360dd1'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4bac11005900000001',
        'applied_in_epoch': 'http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c',
        'applied_to_arm': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4bac11005900000002',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'categoryList': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000015',
        'measurementType': 'http://trials.drugis.org/ontology#categorical',
        'comment': '',
        'label': 'Sex',
        'sameAs': 'http://trials.drugis.org/concepts/1f5e8f47-a9c6-403d-aacc-a8e31bac91b1'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4bac11005900000003',
        'comment': 'Inclusion criteria:\n\nParticipants were male and female outpatients, aged 18 to 65 years, who met criteria for nonpsychotic major depressive disorder as defined in the Diagnostic and Statistical Manual of Mental Disorders, Fourth Edition (DSM-IV). The diagnosis of major depressive disorder was confirmed by the Mini-International Neuropsychiatric Interview. In addition, patients were required to have a Clinical Global Impressions-Severity of Illness (CGI-S) rating of at least 4 (moderate) at visit 1 and a clinicianrated HAM-D-17 total score of at least 15 at visits 1 and 2.\n\nExclusion criteria:\n\nPatients were excluded if they had any primary DSM-IV Axis I diagnosis other than major depressive disorder or any anxiety disorder as a primary diagnosis within the past year, with the exception of specific phobias. Patients were also excluded if they had a history of substance abuse or dependence within the past year or had a positive urine drug screen at study entry. Patients could not have failed 2 or more adequate courses of antidepressant therapy during the current episode.'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4bac11005900000004',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
        'comment': '',
        'label': 'Nausea',
        'sameAs': 'http://trials.drugis.org/concepts/3d3be4f9-9141-4987-abb7-0d8aab2a46b3'
      }, {
        '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4bac11005900000005',
        '@type': 'http://trials.drugis.org/ontology#Variable',
        'measurementType': 'http://trials.drugis.org/ontology#continuous',
        'comment': '',
        'label': 'MADRS bas',
        'sameAs': 'http://trials.drugis.org/concepts/2e74195e-d823-4535-b424-6fdbc962d9cc'
      }, {
        '@id': 'http://trials.drugis.org/instances/008d92d7-2e09-418e-a7f1-d80d9c0936fd',
        'http://trials.drugis.org/ontology#count': 24,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/79d73187-8172-4a2c-8544-d832ffecf944',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/046896b7-eb0e-4987-b8d0-c90267b7546b',
        'http://trials.drugis.org/ontology#count': 2,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/9c87d2ed-ec07-450a-b890-e751833c6010',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/070dbb5b-1e1a-480c-8292-d13422809b67',
        'http://trials.drugis.org/ontology#count': 3,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/fa2c27e5-c37e-41b3-8b2c-79ee14d32d79',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/095c733f-3acc-4cc4-b99c-290bae766506',
        'http://trials.drugis.org/ontology#count': 10,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/3a2b5ea2-23df-40eb-a466-ee6b63bb4e7f',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/0b8448ae-7534-4be8-bb1e-8f1dd72e3e71',
        'http://trials.drugis.org/ontology#count': 4,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/9a005b39-a256-404c-aefb-5c15b34d316c',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/0e77cf6e-3bc2-48e9-a922-3ab930391988',
        'mean': '2.490000e+01',
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/b0b2396e-4e04-4939-a85e-0db6c100853d',
        'http://trials.drugis.org/ontology#sample_size': 70,
        'standard_deviation': '6.700000e+00'
      }, {
        '@id': 'http://trials.drugis.org/instances/0ebddd55-1d7b-4d8a-aaad-61097639c722',
        'category_count': ['http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000004', 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000019'],
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/2df2bf6f-8ad9-417c-9f9f-aeb91cecdc0a'
      }, {
        '@id': 'http://trials.drugis.org/instances/12846ed8-4e41-4c22-9070-b865e9b8be75',
        'http://trials.drugis.org/ontology#count': 24,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/79d73187-8172-4a2c-8544-d832ffecf944',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/131649ff-418f-40a7-8744-04a849811025',
        '@type': 'http://trials.drugis.org/ontology#Drug',
        'label': 'Placebo',
        'sameAs': 'http://trials.drugis.org/concepts/920c27a7-18bf-4996-980b-afb40019f9e9'
      }, {
        '@id': 'http://trials.drugis.org/instances/13d5e7c6-8c65-4169-a821-8b3584c5ff4f',
        '@type': 'http://trials.drugis.org/ontology#AdverseEvent',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000020',
        'comment': '',
        'label': 'Anorexia'
      }, {
        '@id': 'http://trials.drugis.org/instances/144cdead-c55b-4630-b7b3-bf8184054896',
        'http://trials.drugis.org/ontology#count': 6,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/8ae4494d-480c-4b16-a372-9bace3990f61',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/1afd0f6f-4975-40d4-a557-73ef4aa39046',
        '@type': 'http://trials.drugis.org/ontology#TreatmentActivity',
        'has_activity_application': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4bac11005900000001',
        'has_drug_treatment': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001d',
        'label': 'Duloxetine'
      }, {
        '@id': 'http://trials.drugis.org/instances/1ccfd850-8f46-436c-a09b-6b467251b28e',
        '@type': 'http://trials.drugis.org/ontology#ParticipantFlow',
        'in_epoch': 'http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c',
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'http://trials.drugis.org/ontology#participants_starting': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/1e4c7b30-130c-4866-99e1-89694aebf4fb',
        '@type': 'http://trials.drugis.org/ontology#AdverseEvent',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000003',
        'comment': '',
        'label': 'Dry Mouth'
      }, {
        '@id': 'http://trials.drugis.org/instances/22f7cbf2-b194-4da8-8ea6-038de9d21ec8',
        'http://trials.drugis.org/ontology#count': 13,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/6e584670-2a38-4cb6-ba83-4925ca89d5e6',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/248685b7-22e1-4a19-a6b5-fdf4377289bc',
        'http://trials.drugis.org/ontology#count': 3,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/b4066649-66e5-4d64-aaf5-92906b56d6df',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/2a4878b5-60c4-406f-a33c-2c3a29d61307',
        'mean': '4.100000e+00',
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/e16bd57c-55bc-41ff-992d-16b95e7e5249',
        'http://trials.drugis.org/ontology#sample_size': 33,
        'standard_deviation': '6.000000e-01'
      }, {
        '@id': 'http://trials.drugis.org/instances/2df2bf6f-8ad9-417c-9f9f-aeb91cecdc0a',
        '@type': 'http://trials.drugis.org/ontology#PopulationCharacteristic',
        'is_measured_at': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4bac11005900000002',
        'comment': '',
        'label': 'Sex'
      }, {
        '@id': 'http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c',
        '@type': 'http://trials.drugis.org/ontology#Epoch',
        'duration': 'P56D',
        'label': 'Main phase'
      }, {
        '@id': 'http://trials.drugis.org/instances/31586125-87b9-4883-ac89-9e1ebbe6d2d0',
        'mean': '1.790000e+01',
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/843ac064-faee-4670-a806-9125046a8c70',
        'http://trials.drugis.org/ontology#sample_size': 33,
        'standard_deviation': '4.300000e+00'
      }, {
        '@id': 'http://trials.drugis.org/instances/36dd9189-7322-4811-ad35-0c546c226855',
        'mean': '4.200000e+00',
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/e16bd57c-55bc-41ff-992d-16b95e7e5249',
        'http://trials.drugis.org/ontology#sample_size': 70,
        'standard_deviation': '6.000000e-01'
      }, {
        '@id': 'http://trials.drugis.org/instances/374aa576-1c5f-43db-9b3f-9905c1a6ca87',
        'mean': '4.140000e+01',
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/c7ccd428-9ce2-4405-9aa7-1d71827853e4',
        'http://trials.drugis.org/ontology#sample_size': 70,
        'standard_deviation': '1.330000e+01'
      }, {
        '@id': 'http://trials.drugis.org/instances/3a2b5ea2-23df-40eb-a466-ee6b63bb4e7f',
        '@type': 'http://trials.drugis.org/ontology#AdverseEvent',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001a',
        'comment': '',
        'label': 'Diarrhea'
      }, {
        '@id': 'http://trials.drugis.org/instances/3d4f52c6-771a-4f1b-b5e5-a6cfa69dd8c0',
        'http://trials.drugis.org/ontology#count': 5,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/9a005b39-a256-404c-aefb-5c15b34d316c',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/422bb177-eeee-4cad-bfef-43dc4348ee8f',
        'http://trials.drugis.org/ontology#count': 9,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/8ae4494d-480c-4b16-a372-9bace3990f61',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/4586c415-aa62-4995-bb47-1f92f7f95cb9',
        '@type': 'http://trials.drugis.org/ontology#Drug',
        'label': 'Fluoxetine',
        'sameAs': 'http://trials.drugis.org/concepts/b7f316d3-9e7b-482b-87e5-709ae47a5f94'
      }, {
        '@id': 'http://trials.drugis.org/instances/4769054b-ee4a-4d7e-9cff-8ae3d87f7fc2',
        'http://trials.drugis.org/ontology#count': 5,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/5eb936c7-63d1-43ea-85ae-81a4e4fc6305',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        '@type': 'http://trials.drugis.org/ontology#MeasurementMoment',
        'relative_to_anchor': 'http://trials.drugis.org/ontology#anchorEpochStart',
        'relative_to_epoch': 'http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c',
        'time_offset': 'PT0S',
        'label': 'P0D FROM_EPOCH_START Main phase'
      }, {
        '@id': 'http://trials.drugis.org/instances/56718b59-e7ff-4546-8d13-f89df2cffbe2',
        'http://trials.drugis.org/ontology#count': 5,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/9c87d2ed-ec07-450a-b890-e751833c6010',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/59690fd3-4cfc-42eb-a699-57d98eee4a9a',
        '@type': 'http://trials.drugis.org/ontology#TreatmentActivity',
        'has_activity_application': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000005',
        'has_drug_treatment': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000014',
        'label': 'Fluoxetine'
      }, {
        '@id': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        '@type': 'http://trials.drugis.org/ontology#Arm',
        'label': 'Fluoxetine'
      }, {
        '@id': 'http://trials.drugis.org/instances/5eb936c7-63d1-43ea-85ae-81a4e4fc6305',
        '@type': 'http://trials.drugis.org/ontology#AdverseEvent',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000000',
        'comment': '',
        'label': 'Rhinitis'
      }, {
        '@id': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        '@type': 'http://trials.drugis.org/ontology#Arm',
        'label': 'Duloxetine'
      }, {
        '@id': 'http://trials.drugis.org/instances/62bc14d7-4fbc-4973-8119-ffaca0bb18e4',
        '@type': 'http://trials.drugis.org/ontology#Epoch',
        'label': 'Randomization'
      }, {
        '@id': 'http://trials.drugis.org/instances/662e4baa-07bd-4773-98fd-cede0ee70db8',
        'http://trials.drugis.org/ontology#count': 11,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/5eb936c7-63d1-43ea-85ae-81a4e4fc6305',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/6e584670-2a38-4cb6-ba83-4925ca89d5e6',
        '@type': 'http://trials.drugis.org/ontology#AdverseEvent',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000018',
        'comment': '',
        'label': 'Somnolence'
      }, {
        '@id': 'http://trials.drugis.org/instances/704d29ab-4e82-49d4-bb71-0344489d6ef5',
        '@type': 'http://trials.drugis.org/ontology#AdverseEvent',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000007',
        'comment': '',
        'label': 'Headache'
      }, {
        '@id': 'http://trials.drugis.org/instances/715bfa4a-9194-4aef-b941-c71f816bc14c',
        'http://trials.drugis.org/ontology#count': 11,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/9c87d2ed-ec07-450a-b890-e751833c6010',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/720d3933-3f26-4874-a20c-94cf127f35bf',
        'http://trials.drugis.org/ontology#count': 5,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/b4066649-66e5-4d64-aaf5-92906b56d6df',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/739e09e4-091d-4fa9-bfde-e6334e29f414',
        'http://trials.drugis.org/ontology#count': 7,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/3a2b5ea2-23df-40eb-a466-ee6b63bb4e7f',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/754bce41-3aa8-4edc-b0c7-fe7d728534ed',
        'mean': '7.850000e+01',
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/7d75b6a9-533e-4f01-87f2-756f35dd06e3',
        'http://trials.drugis.org/ontology#sample_size': 33,
        'standard_deviation': '1.780000e+01'
      }, {
        '@id': 'http://trials.drugis.org/instances/765a1fb9-6c05-4634-879d-04ef49496d6d',
        'http://trials.drugis.org/ontology#count': 13,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/fa2c27e5-c37e-41b3-8b2c-79ee14d32d79',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/79d73187-8172-4a2c-8544-d832ffecf944',
        '@type': 'http://trials.drugis.org/ontology#Endpoint',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000000',
        'comment': '',
        'label': 'Dropouts'
      }, {
        '@id': 'http://trials.drugis.org/instances/7d75b6a9-533e-4f01-87f2-756f35dd06e3',
        '@type': 'http://trials.drugis.org/ontology#PopulationCharacteristic',
        'has_result_property': ['http://trials.drugis.org/ontology#sample_size', 'http://trials.drugis.org/ontology#mean', 'http://trials.drugis.org/ontology#standard_deviation'],
        'is_measured_at': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000016',
        'comment': '',
        'label': 'Weight'
      }, {
        '@id': 'http://trials.drugis.org/instances/7e390f4c-5b78-466d-bc37-7f48423e22ed',
        'http://trials.drugis.org/ontology#count': 15,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/96239d8e-0894-4868-93d7-ea19eae8f808',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/804414b9-fedd-44db-871a-659f424e1068',
        'http://trials.drugis.org/ontology#count': 22,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/704d29ab-4e82-49d4-bb71-0344489d6ef5',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/82589f57-4002-494f-ba71-e548a88d67ec',
        'http://trials.drugis.org/ontology#count': 14,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/b4066649-66e5-4d64-aaf5-92906b56d6df',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/832c6ed1-464f-417a-b6e1-778cd37549f8',
        'http://trials.drugis.org/ontology#count': 12,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/1e4c7b30-130c-4866-99e1-89694aebf4fb',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/843ac064-faee-4670-a806-9125046a8c70',
        '@type': 'http://trials.drugis.org/ontology#PopulationCharacteristic',
        'has_result_property': ['http://trials.drugis.org/ontology#sample_size', 'http://trials.drugis.org/ontology#mean', 'http://trials.drugis.org/ontology#standard_deviation'],
        'is_measured_at': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001f',
        'comment': 'HAMD score at the baseline',
        'label': 'HAMD bas'
      }, {
        '@id': 'http://trials.drugis.org/instances/8ae4494d-480c-4b16-a372-9bace3990f61',
        '@type': 'http://trials.drugis.org/ontology#AdverseEvent',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4bac11005900000004',
        'comment': '',
        'label': 'Nausea'
      }, {
        '@id': 'http://trials.drugis.org/instances/8ca7aa4c-1391-4222-88a6-819434efcb84',
        '@type': 'http://trials.drugis.org/ontology#WashOutActivity',
        'has_activity_application': ['http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000005', 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000013', 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000006'],
        'label': 'Wash out'
      }, {
        '@id': 'http://trials.drugis.org/instances/917910a4-4ab3-4404-ba9f-9571d465baee',
        'mean': '7.590000e+01',
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/7d75b6a9-533e-4f01-87f2-756f35dd06e3',
        'http://trials.drugis.org/ontology#sample_size': 70,
        'standard_deviation': '1.620000e+01'
      }, {
        '@id': 'http://trials.drugis.org/instances/9557605d-e793-4977-8ada-9fb899fafb72',
        '@type': 'http://trials.drugis.org/ontology#TreatmentActivity',
        'has_activity_application': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000006',
        'has_drug_treatment': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001e',
        'label': 'Placebo'
      }, {
        '@id': 'http://trials.drugis.org/instances/96239d8e-0894-4868-93d7-ea19eae8f808',
        '@type': 'http://trials.drugis.org/ontology#Endpoint',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000017',
        'comment': '',
        'label': 'HAM-D Responders'
      }, {
        '@id': 'http://trials.drugis.org/instances/96d983a3-e187-4fbd-9390-ad528f5fd8d5',
        'mean': '1.920000e+01',
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/843ac064-faee-4670-a806-9125046a8c70',
        'http://trials.drugis.org/ontology#sample_size': 70,
        'standard_deviation': '5.000000e+00'
      }, {
        '@id': 'http://trials.drugis.org/instances/9a005b39-a256-404c-aefb-5c15b34d316c',
        '@type': 'http://trials.drugis.org/ontology#AdverseEvent',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000010',
        'comment': '',
        'label': 'Constipation'
      }, {
        '@id': 'http://trials.drugis.org/instances/9c87d2ed-ec07-450a-b890-e751833c6010',
        '@type': 'http://trials.drugis.org/ontology#AdverseEvent',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac1100590000000a',
        'comment': '',
        'label': 'Dizziness'
      }, {
        '@id': 'http://trials.drugis.org/instances/9cb1fc73-c39c-40f6-a3ab-c892994bd7d7',
        'mean': '8.360000e+01',
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/7d75b6a9-533e-4f01-87f2-756f35dd06e3',
        'http://trials.drugis.org/ontology#sample_size': 70,
        'standard_deviation': '2.000000e+01'
      }, {
        '@id': 'http://trials.drugis.org/instances/9ebdc105-84e3-401d-9cf8-2f9231c349ef',
        'http://trials.drugis.org/ontology#count': 3,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/e3d4db4e-bd06-4359-bd0b-c103f5d52fdc',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/a02fdb05-4986-4f58-b20f-973572813c8d',
        '@type': 'http://trials.drugis.org/ontology#Epoch',
        'duration': 'P7D',
        'label': 'Placebo run-in'
      }, {
        '@id': 'http://trials.drugis.org/instances/a2bd0841-c6ce-4d5a-bd43-5943f1f5f4ea',
        'mean': '1.840000e+01',
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/843ac064-faee-4670-a806-9125046a8c70',
        'http://trials.drugis.org/ontology#sample_size': 70,
        'standard_deviation': '4.000000e+00'
      }, {
        '@id': 'http://trials.drugis.org/instances/a31279a2-4785-4b57-968a-e00d9692911d',
        'mean': '3.970000e+01',
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/c7ccd428-9ce2-4405-9aa7-1d71827853e4',
        'http://trials.drugis.org/ontology#sample_size': 33,
        'standard_deviation': '1.050000e+01'
      }, {
        '@id': 'http://trials.drugis.org/instances/a7df1bfd-809a-41cc-99dd-892c84ad2744',
        'http://trials.drugis.org/ontology#count': 5,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/e3d4db4e-bd06-4359-bd0b-c103f5d52fdc',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/a82113a8-9353-4155-9758-57f03b467320',
        '@type': 'http://trials.drugis.org/concepts/e7ac99cd-d16d-449f-ad2f-29e35dbffb1d',
        'conversionMultiplier': '1.000000e-03',
        'label': 'milligram'
      }, {
        '@id': 'http://trials.drugis.org/instances/aa54abb9-e81e-4492-b8bf-e46a68571971',
        '@type': 'http://trials.drugis.org/ontology#RandomizationActivity',
        'has_activity_application': ['http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000002', 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001b', 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000011'],
        'label': 'Randomization'
      }, {
        '@id': 'http://trials.drugis.org/instances/b0b2396e-4e04-4939-a85e-0db6c100853d',
        '@type': 'http://trials.drugis.org/ontology#PopulationCharacteristic',
        'has_result_property': ['http://trials.drugis.org/ontology#sample_size', 'http://trials.drugis.org/ontology#mean', 'http://trials.drugis.org/ontology#standard_deviation'],
        'is_measured_at': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4bac11005900000005',
        'comment': '',
        'label': 'MADRS bas'
      }, {
        '@id': 'http://trials.drugis.org/instances/b0d8bfca-fbcf-4962-a313-6baa726ca9ff',
        '@type': 'http://trials.drugis.org/ontology#ParticipantFlow',
        'in_epoch': 'http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c',
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'http://trials.drugis.org/ontology#participants_starting': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/b2dbe62a-9da2-4768-913f-8d4a890cdba8',
        'mean': '2.260000e+01',
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/b0b2396e-4e04-4939-a85e-0db6c100853d',
        'http://trials.drugis.org/ontology#sample_size': 33,
        'standard_deviation': '6.900000e+00'
      }, {
        '@id': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        '@type': 'http://trials.drugis.org/ontology#MeasurementMoment',
        'relative_to_anchor': 'http://trials.drugis.org/ontology#anchorEpochEnd',
        'relative_to_epoch': 'http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c',
        'time_offset': 'PT0S',
        'label': 'P0D BEFORE_EPOCH_END Main phase'
      }, {
        '@id': 'http://trials.drugis.org/instances/b4066649-66e5-4d64-aaf5-92906b56d6df',
        '@type': 'http://trials.drugis.org/ontology#AdverseEvent',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001c',
        'comment': '',
        'label': 'Insomnia'
      }, {
        '@id': 'http://trials.drugis.org/instances/b6037d10-1431-45c2-8c07-4e00e72f6cd1',
        'category_count': ['http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000000d', 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000008'],
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/2df2bf6f-8ad9-417c-9f9f-aeb91cecdc0a'
      }, {
        '@id': 'http://trials.drugis.org/instances/b635b9a1-937f-41f5-941c-28475abd7a01',
        'http://trials.drugis.org/ontology#count': 11,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/704d29ab-4e82-49d4-bb71-0344489d6ef5',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/b6fb6d24-d090-4ec1-871c-bdea63625bcb',
        'http://trials.drugis.org/ontology#count': 12,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/e3d4db4e-bd06-4359-bd0b-c103f5d52fdc',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/b8ec4a7b-e209-46d4-9c01-ec04a1246ce0',
        '@type': 'http://trials.drugis.org/ontology#Indication',
        'label': 'Severe depression',
        'sameAs': 'http://trials.drugis.org/concepts/4e7accec-b021-4f72-b9f8-dd7868dd1f87'
      }, {
        '@id': 'http://trials.drugis.org/instances/bb9dcc86-e25b-450a-8995-69fc5dd56809',
        'mean': '4.300000e+00',
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/e16bd57c-55bc-41ff-992d-16b95e7e5249',
        'http://trials.drugis.org/ontology#sample_size': 70,
        'standard_deviation': '6.000000e-01'
      }, {
        '@id': 'http://trials.drugis.org/instances/be03b3b5-a315-41b7-8081-ec4ea390ea68',
        'http://trials.drugis.org/ontology#count': 12,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/79d73187-8172-4a2c-8544-d832ffecf944',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/c0cbbe94-fbee-4e4e-8361-3511594a6c02',
        'http://trials.drugis.org/ontology#count': 6,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/fa2c27e5-c37e-41b3-8b2c-79ee14d32d79',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/c498d9c3-0bd8-4d50-b48b-3e7f4eda6c7d',
        'http://trials.drugis.org/ontology#count': 21,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/1e4c7b30-130c-4866-99e1-89694aebf4fb',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/c70e228e-4014-43d7-a441-7e5a679605fe',
        'http://trials.drugis.org/ontology#count': 3,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/13d5e7c6-8c65-4169-a821-8b3584c5ff4f',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/c760daa9-8641-4c0e-b626-fac9a0442fe2',
        'category_count': ['http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000012', 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000008'],
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/2df2bf6f-8ad9-417c-9f9f-aeb91cecdc0a'
      }, {
        '@id': 'http://trials.drugis.org/instances/c7ccd428-9ce2-4405-9aa7-1d71827853e4',
        '@type': 'http://trials.drugis.org/ontology#PopulationCharacteristic',
        'has_result_property': ['http://trials.drugis.org/ontology#sample_size', 'http://trials.drugis.org/ontology#mean', 'http://trials.drugis.org/ontology#standard_deviation'],
        'is_measured_at': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000000e',
        'comment': '',
        'label': 'Age'
      }, {
        '@id': 'http://trials.drugis.org/instances/ce8c7c2c-abd2-4eb5-b388-512963c78cb1',
        'http://trials.drugis.org/ontology#count': 7,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/1e4c7b30-130c-4866-99e1-89694aebf4fb',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/ceb1597d-eb35-40ec-8ca3-4465ab19ed86',
        'http://trials.drugis.org/ontology#count': 12,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/5eb936c7-63d1-43ea-85ae-81a4e4fc6305',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/cf41bf6c-f5b1-482f-95ff-b045d81c252b',
        'http://trials.drugis.org/ontology#count': 25,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/96239d8e-0894-4868-93d7-ea19eae8f808',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/d710c649-7385-4499-afd9-6ffada3ad397',
        'http://trials.drugis.org/ontology#count': 34,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/96239d8e-0894-4868-93d7-ea19eae8f808',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/d72f9eb7-1ca6-448e-8375-61e01bb9c8c1',
        'http://trials.drugis.org/ontology#count': 10,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/3a2b5ea2-23df-40eb-a466-ee6b63bb4e7f',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/d887928f-923b-49a9-8056-4cea570f9317',
        '@type': 'http://trials.drugis.org/ontology#Drug',
        'label': 'Duloxetine',
        'sameAs': 'http://trials.drugis.org/concepts/3c294b63-0591-4940-8875-ece3b3729273'
      }, {
        '@id': 'http://trials.drugis.org/instances/d9b4642e-bfde-4930-8efc-3736c8db405e',
        'http://trials.drugis.org/ontology#count': 7,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/6e584670-2a38-4cb6-ba83-4925ca89d5e6',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/da511d34-fc54-44ad-b3c6-61193a35c643',
        'http://trials.drugis.org/ontology#count': 2,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/13d5e7c6-8c65-4169-a821-8b3584c5ff4f',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/da84668c-1239-4c27-b59d-ccd821caed1c',
        'http://trials.drugis.org/ontology#count': 7,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/13d5e7c6-8c65-4169-a821-8b3584c5ff4f',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/dfe19932-876d-4a82-8246-11ab5ca33d08',
        'http://trials.drugis.org/ontology#count': 8,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/9a005b39-a256-404c-aefb-5c15b34d316c',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/e16bd57c-55bc-41ff-992d-16b95e7e5249',
        '@type': 'http://trials.drugis.org/ontology#PopulationCharacteristic',
        'has_result_property': ['http://trials.drugis.org/ontology#sample_size', 'http://trials.drugis.org/ontology#mean', 'http://trials.drugis.org/ontology#standard_deviation'],
        'is_measured_at': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000003',
        'comment': '',
        'label': 'CGI-S bas'
      }, {
        '@id': 'http://trials.drugis.org/instances/e2417ba9-a34b-4f59-a49b-3d47e71139bd',
        'mean': '2.290000e+01',
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/b0b2396e-4e04-4939-a85e-0db6c100853d',
        'http://trials.drugis.org/ontology#sample_size': 70,
        'standard_deviation': '6.100000e+00'
      }, {
        '@id': 'http://trials.drugis.org/instances/e3d4db4e-bd06-4359-bd0b-c103f5d52fdc',
        '@type': 'http://trials.drugis.org/ontology#AdverseEvent',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000000f',
        'comment': '',
        'label': 'Asthenia'
      }, {
        '@id': 'http://trials.drugis.org/instances/e7cf1b9c-d2c6-4324-9b14-36ed7a84db93',
        'mean': '4.230000e+01',
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
        'of_outcome': 'http://trials.drugis.org/instances/c7ccd428-9ce2-4405-9aa7-1d71827853e4',
        'http://trials.drugis.org/ontology#sample_size': 70,
        'standard_deviation': '1.080000e+01'
      }, {
        '@id': 'http://trials.drugis.org/instances/ed49cf5c-6cef-4a30-8403-19899ba16e16',
        '@type': 'http://trials.drugis.org/ontology#ParticipantFlow',
        'in_epoch': 'http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c',
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'http://trials.drugis.org/ontology#participants_starting': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/f099ee82-0780-4cae-973f-f5bb38dd2b61',
        'http://trials.drugis.org/ontology#count': 14,
        'of_group': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/704d29ab-4e82-49d4-bb71-0344489d6ef5',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/f12f8497-699e-47cc-9a95-e1596d26c0f0',
        'http://trials.drugis.org/ontology#count': 9,
        'of_group': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/8ae4494d-480c-4b16-a372-9bace3990f61',
        'http://trials.drugis.org/ontology#sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/fa2c27e5-c37e-41b3-8b2c-79ee14d32d79',
        '@type': 'http://trials.drugis.org/ontology#AdverseEvent',
        'has_result_property': ['http://trials.drugis.org/ontology#count', 'http://trials.drugis.org/ontology#sample_size'],
        'is_measured_at': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_variable': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000002',
        'comment': '',
        'label': 'Sweating'
      }, {
        '@id': 'http://trials.drugis.org/instances/fc58835e-089f-49ef-88f9-e95c208d9914',
        'http://trials.drugis.org/ontology#count': 7,
        'of_group': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
        'of_moment': 'http://trials.drugis.org/instances/b37171ac-7b9c-4093-a76c-75a9548ebb34',
        'of_outcome': 'http://trials.drugis.org/instances/6e584670-2a38-4cb6-ba83-4925ca89d5e6',
        'http://trials.drugis.org/ontology#sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb',
        '@type': 'http://trials.drugis.org/ontology#Arm',
        'label': 'Placebo'
      }, {
        '@id': 'http://trials.drugis.org/studies/68043679-24a6-4c44-a464-7d1a83f85605',
        '@type': 'http://trials.drugis.org/ontology#Study',
        'has_activity': ['http://trials.drugis.org/instances/1afd0f6f-4975-40d4-a557-73ef4aa39046', 'http://trials.drugis.org/instances/aa54abb9-e81e-4492-b8bf-e46a68571971', 'http://trials.drugis.org/instances/59690fd3-4cfc-42eb-a699-57d98eee4a9a', 'http://trials.drugis.org/instances/8ca7aa4c-1391-4222-88a6-819434efcb84', 'http://trials.drugis.org/instances/9557605d-e793-4977-8ada-9fb899fafb72'],
        'has_allocation': 'http://trials.drugis.org/ontology#AllocationRandomized',
        'has_arm': ['http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb', 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8', 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b'],
        'has_blinding': 'http://trials.drugis.org/ontology#DoubleBlind',
        'has_eligibility_criteria': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4bac11005900000003',
        'has_epochs': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000000b',
        'has_indication': 'http://trials.drugis.org/instances/b8ec4a7b-e209-46d4-9c01-ec04a1246ce0',
        'http://trials.drugis.org/ontology#has_number_of_centers': 8,
        'has_objective': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000000a',
        'has_outcome': ['http://trials.drugis.org/instances/704d29ab-4e82-49d4-bb71-0344489d6ef5', 'http://trials.drugis.org/instances/3a2b5ea2-23df-40eb-a466-ee6b63bb4e7f', 'http://trials.drugis.org/instances/13d5e7c6-8c65-4169-a821-8b3584c5ff4f', 'http://trials.drugis.org/instances/9c87d2ed-ec07-450a-b890-e751833c6010', 'http://trials.drugis.org/instances/1e4c7b30-130c-4866-99e1-89694aebf4fb', 'http://trials.drugis.org/instances/843ac064-faee-4670-a806-9125046a8c70', 'http://trials.drugis.org/instances/b0b2396e-4e04-4939-a85e-0db6c100853d', 'http://trials.drugis.org/instances/fa2c27e5-c37e-41b3-8b2c-79ee14d32d79', 'http://trials.drugis.org/instances/e16bd57c-55bc-41ff-992d-16b95e7e5249', 'http://trials.drugis.org/instances/c7ccd428-9ce2-4405-9aa7-1d71827853e4', 'http://trials.drugis.org/instances/5eb936c7-63d1-43ea-85ae-81a4e4fc6305', 'http://trials.drugis.org/instances/6e584670-2a38-4cb6-ba83-4925ca89d5e6', 'http://trials.drugis.org/instances/79d73187-8172-4a2c-8544-d832ffecf944', 'http://trials.drugis.org/instances/96239d8e-0894-4868-93d7-ea19eae8f808', 'http://trials.drugis.org/instances/e3d4db4e-bd06-4359-bd0b-c103f5d52fdc', 'http://trials.drugis.org/instances/9a005b39-a256-404c-aefb-5c15b34d316c', 'http://trials.drugis.org/instances/2df2bf6f-8ad9-417c-9f9f-aeb91cecdc0a', 'http://trials.drugis.org/instances/b4066649-66e5-4d64-aaf5-92906b56d6df', 'http://trials.drugis.org/instances/7d75b6a9-533e-4f01-87f2-756f35dd06e3', 'http://trials.drugis.org/instances/8ae4494d-480c-4b16-a372-9bace3990f61'],
        'has_primary_epoch': 'http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c',
        'has_publication': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000007',
        'status': 'http://trials.drugis.org/ontology#StatusCompleted',
        'comment': 'Duloxetine in the treatment of major depressive disorder: a double-blind clinical trial',
        'label': 'Goldstein et al, 2002'
      }],
      '@context': context
    };

    function findStudy(graph) {
      return _.find(graph, function(node) {
        return node['@type'] === 'ontology:Study' ||
          node['@type'] === 'http://trials.drugis.org/ontology#Study';
      });
    }

    it('should transform an empty study', function() {
      var transformed = transformJsonLd(emptyStudy);
      expect(transformed['@graph']).toEqual(emptyTransformed['@graph']);
      expect(transformed['@context']).toEqual(emptyTransformed['@context']);
    });

    it('should transform a study with a list', function() {
      var toTransform = angular.copy(studyWithList);
      var result = transformJsonLd(toTransform);
      var study = findStudy(result['@graph']);
      expect(study.has_epochs).not.toBeNull();
    });

    it('should work if the list is just an @list with nil', function() {
      var toTransform = angular.copy(studyWithList);
      var study = findStudy(toTransform['@graph']);
      toTransform['@graph'] = _.reject(toTransform['@graph'], function(node) {
        return node['@id'] === study.has_epochs;
      });
      study.has_epochs = {
        '@list': []
      };
      var result = transformJsonLd(toTransform);
      study = findStudy(result['@graph']);
      expect(study.has_epochs).toEqual({
        '@id': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'
      });
    });

  });
});

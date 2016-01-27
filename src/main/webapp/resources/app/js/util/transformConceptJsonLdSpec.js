'use strict';
define(['util/transformConceptJsonLd'], function(transformConceptJsonLd) {
  fdescribe('transformConceptJsonLd', function() {
    it('should work for an empty concepts graph', function() {
      var emptyData = {
        '@context': {},
        '@graph': []
      };
      expect(transformConceptJsonLd(emptyData)).toEqual(emptyData);
    });
    it('should work for concepts including categoricals', function() {

      var data = {
        '@graph': [{
          '@id': 'http://localhost:8080/.well-known/genid/00000152697798d7e849192900000001',
          'first': 'Male',
          'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest': {
            '@list': ['Female']
          }
        }, {
          '@id': 'http://trials.drugis.org/concepts/00fe9527-6ae7-41d3-9bf3-bfd802c18244',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Concentration Difficulties'
        }, {
          '@id': 'http://trials.drugis.org/concepts/02c0e50f-c856-4919-90f6-fc31f9b51b4a',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Agitation'
        }, {
          '@id': 'http://trials.drugis.org/concepts/0653ec84-d917-45ed-a937-fe4202df853e',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Depersonalization'
        }, {
          '@id': 'http://trials.drugis.org/concepts/06649e4e-9d2a-45e1-bf74-a9db8d78f5dd',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Anxiety'
        }, {
          '@id': 'http://trials.drugis.org/concepts/08289108-ed4d-4a26-97d1-230671b71c45',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Bronchitis'
        }, {
          '@id': 'http://trials.drugis.org/concepts/1020b392-7e7b-42eb-bac1-2fbaebcf2ce6',
          '@type': 'http://trials.drugis.org/ontology#Drug',
          'label': 'Fluoxetine',
          'sameAs': 'http://www.whocc.no/ATC2011/N06AB03'
        }, {
          '@id': 'http://trials.drugis.org/concepts/107b35da-78bd-4d1d-941e-affde8f7b749',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Diminished Sexual Desire'
        }, {
          '@id': 'http://trials.drugis.org/concepts/11d6e892-49d5-46c5-9439-d549f73cb047',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Chest Pain'
        }, {
          '@id': 'http://trials.drugis.org/concepts/16a99c68-8e05-4625-ad59-1ce2acc5e574',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#continuous',
          'comment': ['', 'Deviation from the baseline of CGI Severity of Illness score'],
          'label': 'CGI Severity Change'
        }, {
          '@id': 'http://trials.drugis.org/concepts/24f07683-3866-4e1b-ac3b-312cd41dea2d',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Orgiastic Dysfunction'
        }, {
          '@id': 'http://trials.drugis.org/concepts/26181b58-8df3-43fc-a5db-4607b89335d4',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Blurred Vision'
        }, {
          '@id': 'http://trials.drugis.org/concepts/2c2c8ec5-915f-4334-b189-c8aab035b290',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Weight Loss'
        }, {
          '@id': 'http://trials.drugis.org/concepts/30fe7dbc-dfce-4e7c-a9a6-c6eed5aee401',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Abnormal Ejaculation'
        }, {
          '@id': 'http://trials.drugis.org/concepts/3284feaf-cba9-4381-a73a-92d1de833d44',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Nausea'
        }, {
          '@id': 'http://trials.drugis.org/concepts/4165e26f-7a09-4d2b-9274-9c718fe9e03b',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Tinnitis'
        }, {
          '@id': 'http://trials.drugis.org/concepts/44defb89-c566-463c-ac30-847bc740ac74',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Rhinitis'
        }, {
          '@id': 'http://trials.drugis.org/concepts/4b0d9ed7-7b8d-4904-946d-c8b8e6c02a7e',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Dream Abnormality'
        }, {
          '@id': 'http://trials.drugis.org/concepts/4d51a9f8-b925-4cd6-9687-653f2d55ee93',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Impotence'
        }, {
          '@id': 'http://trials.drugis.org/concepts/501073d5-38e8-4637-832f-1f6ef181a34b',
          '@type': 'http://trials.drugis.org/ontology#Drug',
          'label': 'Fluvoxamine',
          'sameAs': 'http://www.whocc.no/ATC2011/N06AB08'
        }, {
          '@id': 'http://trials.drugis.org/concepts/546a68e2-b98f-41fb-ba3a-3ee3e4c93b7a',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Reduced Sleep'
        }, {
          '@id': 'http://trials.drugis.org/concepts/54a55d22-8d1f-4db1-9084-ec5e4074cb99',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#continuous',
          'comment': '',
          'label': 'CGI-S bas'
        }, {
          '@id': 'http://trials.drugis.org/concepts/5820ca93-19e2-4539-9095-3b4f7c414619',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Flu Syndrome'
        }, {
          '@id': 'http://trials.drugis.org/concepts/590b27b4-cfed-4cec-aaa3-d8a4c8133051',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Reduced Salivation'
        }, {
          '@id': 'http://trials.drugis.org/concepts/5c0687e1-20c1-41d8-ba1a-3e19eb63d204',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Orgasmic Dysfunction'
        }, {
          '@id': 'http://trials.drugis.org/concepts/6511967e-41db-478d-992f-767b29234ea6',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Dyspepsia'
        }, {
          '@id': 'http://trials.drugis.org/concepts/6ed4cd55-3ebc-497c-b720-388456cca75e',
          '@type': 'http://trials.drugis.org/ontology#Drug',
          'label': 'Bupropion',
          'sameAs': 'http://www.whocc.no/ATC2011/N06AX12'
        }, {
          '@id': 'http://trials.drugis.org/concepts/6f24496b-54f8-467b-97e5-6d452ef98f99',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Increased Sexual Desire'
        }, {
          '@id': 'http://trials.drugis.org/concepts/7032635a-47ea-45d3-a0e7-e453296345f1',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Increased Duration Of Sleep'
        }, {
          '@id': 'http://trials.drugis.org/concepts/71e7cf23-2ac7-4564-8af6-c338546dd133',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Palpitation'
        }, {
          '@id': 'http://trials.drugis.org/concepts/733df255-9e03-47b1-83eb-947aeff12e91',
          '@type': 'http://trials.drugis.org/ontology#Drug',
          'label': 'Mirtazapine',
          'sameAs': 'http://www.whocc.no/ATC2011/N06AX11'
        }, {
          '@id': 'http://trials.drugis.org/concepts/741d2c68-2592-47db-902b-e51ecafb4915',
          '@type': 'http://trials.drugis.org/ontology#Drug',
          'label': 'Citalopram',
          'sameAs': 'http://www.whocc.no/ATC2011/N06AB04'
        }, {
          '@id': 'http://trials.drugis.org/concepts/76c1fa34-78ee-49ef-b9b2-e10f89890c9f',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Abdominal Pain'
        }, {
          '@id': 'http://trials.drugis.org/concepts/77f38e13-1e34-4e8e-a5cd-37656795cc68',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Pain'
        }, {
          '@id': 'http://trials.drugis.org/concepts/7a7bcd9a-633c-4e97-a068-7c527b1e3a2a',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Infection'
        }, {
          '@id': 'http://trials.drugis.org/concepts/7af6e330-0a60-4d01-bfe8-63905965fafa',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'categoryList': 'http://localhost:8080/.well-known/genid/00000152697798d7e849192900000001',
          'measurementType': 'http://trials.drugis.org/ontology#categorical',
          'comment': '',
          'label': 'Sex'
        }, {
          '@id': 'http://trials.drugis.org/concepts/7cc8f4b6-da3a-4b17-affc-1457ec9f7ba3',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Nervousness'
        }, {
          '@id': 'http://trials.drugis.org/concepts/7f4aa02d-2181-4fb8-b619-f6400ec491bb',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Flatulence'
        }, {
          '@id': 'http://trials.drugis.org/concepts/7ff310f5-df47-4e32-ab59-5359ec5a9f27',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Dropouts'
        }, {
          '@id': 'http://trials.drugis.org/concepts/8041ab66-8854-4f34-aa3e-655eac3a2c05',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#continuous',
          'comment': ['', 'kg/m2'],
          'label': 'BMI'
        }, {
          '@id': 'http://trials.drugis.org/concepts/82840b66-1dfe-4a33-ac0d-952b410f343a',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Tremor'
        }, {
          '@id': 'http://trials.drugis.org/concepts/8557fd7c-c989-4990-9c15-6672d61bc234',
          '@type': 'http://www.w3.org/2002/07/owl#Class',
          'symbol': 'l',
          'label': 'liter',
          'sameAs': 'http://qudt.org/schema/qudt#Liter'
        }, {
          '@id': 'http://trials.drugis.org/concepts/85de1ca6-75e0-45fb-8c8b-78b0fbf05be2',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Insomnia'
        }, {
          '@id': 'http://trials.drugis.org/concepts/86fb5e60-a635-41b3-af8b-e507462bae70',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Failing Memory'
        }, {
          '@id': 'http://trials.drugis.org/concepts/8f234a83-ad3e-4b35-aa57-9e7b5237bf49',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Rash'
        }, {
          '@id': 'http://trials.drugis.org/concepts/923bb7f2-0b90-4e2f-92d0-8d3e3a487cc0',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'MADRS Responders'
        }, {
          '@id': 'http://trials.drugis.org/concepts/9d7bd022-2253-4e8b-b949-69c6317845e0',
          '@type': 'http://trials.drugis.org/ontology#Drug',
          'label': 'Venlafaxine',
          'sameAs': 'http://www.whocc.no/ATC2011/N06AX16'
        }, {
          '@id': 'http://trials.drugis.org/concepts/9e2bfb5f-4260-490b-b150-eb19e1532a05',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Asthenia'
        }, {
          '@id': 'http://trials.drugis.org/concepts/9f86b466-27a1-4132-aff7-28fd57cca1e5',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Sweating'
        }, {
          '@id': 'http://trials.drugis.org/concepts/a18c540c-5c53-4671-ab38-032d7c3c72c2',
          '@type': 'http://trials.drugis.org/ontology#Drug',
          'label': 'Placebo',
          'sameAs': 'http://www.whocc.no/ATC2011/Placebo'
        }, {
          '@id': 'http://trials.drugis.org/concepts/a3f518be-f5bc-49d7-b353-5fae439e7f0b',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#continuous',
          'comment': ['', 'kg'],
          'label': 'Weight'
        }, {
          '@id': 'http://trials.drugis.org/concepts/a6c702b7-e54a-46c5-855f-caebabe22dc1',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Pharyngitis'
        }, {
          '@id': 'http://trials.drugis.org/concepts/a723cf1f-5954-4f4b-a5a0-e5e4b8aa532a',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Constipation'
        }, {
          '@id': 'http://trials.drugis.org/concepts/a8ed8198-616e-42b0-809b-acf7fa3d3cf7',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Dizziness'
        }, {
          '@id': 'http://trials.drugis.org/concepts/aac22655-e642-4692-bae3-8af535eaedee',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Vertigo'
        }, {
          '@id': 'http://trials.drugis.org/concepts/b283da0a-1fe7-4a56-8e8c-c00f32101df1',
          '@type': 'http://trials.drugis.org/ontology#Drug',
          'label': 'Sertraline',
          'sameAs': 'http://www.whocc.no/ATC2011/N06AB06'
        }, {
          '@id': 'http://trials.drugis.org/concepts/b2e6b8e1-101e-4dbb-9b33-b6af5ce481de',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Dry Mouth'
        }, {
          '@id': 'http://trials.drugis.org/concepts/b8474503-6a0e-4f5a-a16d-8bbb5bf3ae53',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Micturation Disturbances'
        }, {
          '@id': 'http://trials.drugis.org/concepts/b8e6ec1b-2365-4569-ac87-6abb7769c47c',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Fatigue'
        }, {
          '@id': 'http://trials.drugis.org/concepts/bc455a06-e9b7-493d-ad4e-30083e0b3cbd',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Respiratory Disorder'
        }, {
          '@id': 'http://trials.drugis.org/concepts/bf767184-2fb4-4fc7-9217-6f1dfd407135',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Erectile Dysfunction'
        }, {
          '@id': 'http://trials.drugis.org/concepts/c017057f-bfc9-4e1a-8ece-3fb5671f3746',
          '@type': 'http://trials.drugis.org/ontology#Drug',
          'label': 'Paroxetine',
          'sameAs': 'http://www.whocc.no/ATC2011/N06AB05'
        }, {
          '@id': 'http://trials.drugis.org/concepts/c44eb9f0-750b-4461-90e0-f432f11e5930',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Sexual Dysfunction'
        }, {
          '@id': 'http://trials.drugis.org/concepts/c5d43717-47d7-40f0-aa22-5208f4417218',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Increased Salivation'
        }, {
          '@id': 'http://trials.drugis.org/concepts/c6fd150f-a3dc-4ebd-80c9-6180f156c2f4',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Emotional Indifference'
        }, {
          '@id': 'http://trials.drugis.org/concepts/d0428715-a112-4dc0-96e9-8fbb05a011c6',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#continuous',
          'comment': '',
          'label': 'MADRS bas'
        }, {
          '@id': 'http://trials.drugis.org/concepts/d1ecec65-7684-4fe3-8390-444a739aa754',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'HAM-D Responders'
        }, {
          '@id': 'http://trials.drugis.org/concepts/d329d342-fb92-45e2-ac73-d9c1924b7626',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Anorexia'
        }, {
          '@id': 'http://trials.drugis.org/concepts/db1089d6-c1db-4a6f-bd99-25597da64d02',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Back Pain'
        }, {
          '@id': 'http://trials.drugis.org/concepts/dd6c3dc5-5113-445d-b600-444da11b3f07',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#continuous',
          'comment': ['', 'HAMD score at the baseline'],
          'label': 'HAMD bas'
        }, {
          '@id': 'http://trials.drugis.org/concepts/ddb060e2-1ea6-4ad3-b2f7-9fd62bec13a0',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Diarrhea'
        }, {
          '@id': 'http://trials.drugis.org/concepts/e25f01d3-04b0-41ae-9085-91af6d2202dd',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Vomiting'
        }, {
          '@id': 'http://trials.drugis.org/concepts/e71ac335-d8ba-4431-be6b-f652e64f38b9',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Appetite Increased'
        }, {
          '@id': 'http://trials.drugis.org/concepts/ead4aaab-51d0-455a-a1a3-856ee5fc8dbb',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Accommodation Disturbances'
        }, {
          '@id': 'http://trials.drugis.org/concepts/ece2bcd3-dfd7-4c93-9e27-a98e5ecd3355',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Weight Gain'
        }, {
          '@id': 'http://trials.drugis.org/concepts/ee6531f5-39c0-44be-aa0b-712291397690',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Somnolence'
        }, {
          '@id': 'http://trials.drugis.org/concepts/efdec39b-8e43-42dc-a927-4259ff51cdaf',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#continuous',
          'comment': ['', 'years'],
          'label': 'Age'
        }, {
          '@id': 'http://trials.drugis.org/concepts/f1118591-1e11-4664-b94a-0bb465d6169b',
          '@type': 'http://trials.drugis.org/ontology#Indication',
          'label': 'Severe depression',
          'sameAs': 'http://www.ihtsdo.org/SCT_310497006'
        }, {
          '@id': 'http://trials.drugis.org/concepts/f557efa3-1e99-4a33-8bd5-93b1b2954c4f',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Headache'
        }, {
          '@id': 'http://trials.drugis.org/concepts/f9e6899d-a4a8-49bf-970b-0d401fbac296',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Gastralgia'
        }, {
          '@id': 'http://trials.drugis.org/concepts/fae9a4c4-a048-4b4c-a15a-c3bd8d2ba716',
          '@type': 'http://trials.drugis.org/ontology#Drug',
          'label': 'Duloxetine',
          'sameAs': 'http://www.whocc.no/ATC2011/N06AX21'
        }, {
          '@id': 'http://trials.drugis.org/concepts/fcb5fb34-a2cb-4766-8ff7-d14fe455786d',
          '@type': 'http://www.w3.org/2002/07/owl#Class',
          'symbol': 'g',
          'label': 'gram',
          'sameAs': 'http://qudt.org/schema/qudt#Gram'
        }, {
          '@id': 'http://trials.drugis.org/concepts/fe0824d3-f7a8-495f-ac9e-d7df6a9c9d73',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Dysmenorrhea'
        }, {
          '@id': 'http://trials.drugis.org/concepts/fed7fb7c-fe8c-4f1e-ad36-39211cfe7c60',
          '@type': 'http://trials.drugis.org/ontology#Drug',
          'label': 'Escitalopram',
          'sameAs': 'http://www.whocc.no/ATC2011/N06AB10'
        }],
        '@context': {
          'sameAs': {
            '@id': 'http://www.w3.org/2002/07/owl#sameAs',
            '@type': '@id'
          },
          'label': 'http://www.w3.org/2000/01/rdf-schema#label',
          'measurementType': {
            '@id': 'http://trials.drugis.org/ontology#measurementType',
            '@type': '@id'
          },
          'comment': 'http://www.w3.org/2000/01/rdf-schema#comment',
          'symbol': 'http://qudt.org/schema/qudt#symbol',
          'categoryList': {
            '@id': 'http://trials.drugis.org/ontology#categoryList',
            '@type': '@id'
          },
          'rest': {
            '@id': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest',
            '@type': '@id'
          },
          'first': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#first'
        }
      };

      expect(transformConceptJsonLd(data)).toBe('awesome');

    });
  });
});

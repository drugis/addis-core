'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the activity service', function() {

    var mockStudyUuid = 'mockStudyUuid',
      rootScope, q,
      commentServiceStub,
      studyDefer,
      jsonStudy,
      studyServiceMock = jasmine.createSpyObj('StudyService', ['getStudy', 'save']),
      activityService,
      drugServiceMock = jasmine.createSpyObj('DrugService', ['queryItems']),
      unitServiceMock;

    beforeEach(function() {
      module('trialverse.activity', function($provide) {
        commentServiceStub = jasmine.createSpyObj('CommentService', [
          'addComment'
        ]);
        $provide.value('DrugService', drugServiceMock);
        $provide.value('UnitService', unitServiceMock);
        $provide.value('StudyService', studyServiceMock);
        $provide.value('CommentService', commentServiceStub);
      });
    });

    beforeEach(module('trialverse.activity'));

    beforeEach(inject(function($q, $rootScope, $httpBackend, ActivityService) {
      q = $q;
      rootScope = $rootScope;

      studyDefer = q.defer();
      studyServiceMock.getStudy.and.returnValue(studyDefer.promise);

      activityService = ActivityService;
    }));


    fdescribe('query activities', function() {

      beforeEach(function() {
        jsonStudy = {
          'has_activity': [{
            '@id': 'http://trials.drugis.org/instances/6d44e008-450a-4363-aae2-f6a79801283d',
            '@type': 'ontology:WashOutActivity',
            'has_activity_application': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194dac1100590000000f',
              'applied_in_epoch': 'http://trials.drugis.org/instances/a9a9511a-d83e-4b29-931b-c2e0f90bc46c',
              'applied_to_arm': 'http://trials.drugis.org/instances/71ec1cfc-347c-4582-b2ae-5088ece45f85'
            }, {
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac11005900000006',
              'applied_in_epoch': 'http://trials.drugis.org/instances/a9a9511a-d83e-4b29-931b-c2e0f90bc46c',
              'applied_to_arm': 'http://trials.drugis.org/instances/1c3c67ba-4c0c-46e3-846c-5e9d72c5ed80'
            }],
            'label': 'Wash out'
          }, {
            '@id': 'http://trials.drugis.org/instances/13aad31a-7cb8-4e11-a094-ffe815ab75f9',
            '@type': 'ontology:TreatmentActivity',
            'has_activity_application': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac11005900000010',
              'applied_in_epoch': 'http://trials.drugis.org/instances/b5a68049-451a-4ae6-acf5-72a2f1b846e4',
              'applied_to_arm': 'http://trials.drugis.org/instances/1c3c67ba-4c0c-46e3-846c-5e9d72c5ed80'
            }],
            'has_drug_treatment': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac11005900000013',
              '@type': 'ontology:TitratedDoseDrugTreatment',
              'treatment_has_drug': 'http://trials.drugis.org/instances/a331aea9-58cc-4e1f-928d-fb5879bae8c1',
              'treatment_max_dose': [{
                '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac11005900000001',
                'dosingPeriodicity': 'P1D',
                'unit': 'http://trials.drugis.org/instances/8691b100-e5d9-4048-acc3-6ed9731e0896',
                'value': 40
              }],
              'treatment_min_dose': [{
                '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194dac11005900000001',
                'dosingPeriodicity': 'P1D',
                'unit': 'http://trials.drugis.org/instances/8691b100-e5d9-4048-acc3-6ed9731e0896',
                'value': 20
              }]
            }],
            'label': 'Fluoxetine'
          }, {
            '@id': 'http://trials.drugis.org/instances/b86211ee-7541-4d38-9dc8-35c61f554fd2',
            '@type': 'ontology:RandomizationActivity',
            'has_activity_application': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac11005900000011',
              'applied_in_epoch': 'http://trials.drugis.org/instances/fddaefbe-5f5a-4995-a365-8825d63c014c',
              'applied_to_arm': 'http://trials.drugis.org/instances/1c3c67ba-4c0c-46e3-846c-5e9d72c5ed80'
            }, {
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194dac1100590000000a',
              'applied_in_epoch': 'http://trials.drugis.org/instances/fddaefbe-5f5a-4995-a365-8825d63c014c',
              'applied_to_arm': 'http://trials.drugis.org/instances/71ec1cfc-347c-4582-b2ae-5088ece45f85'
            }],
            'label': 'Randomization'
          }, {
            '@id': 'http://trials.drugis.org/instances/5a6d0fbb-a022-46c9-bd75-19a2cef9abf2',
            '@type': 'ontology:TreatmentActivity',
            'has_activity_application': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac11005900000009',
              'applied_in_epoch': 'http://trials.drugis.org/instances/b5a68049-451a-4ae6-acf5-72a2f1b846e4',
              'applied_to_arm': 'http://trials.drugis.org/instances/71ec1cfc-347c-4582-b2ae-5088ece45f85'
            }],
            'has_drug_treatment': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac11005900000003',
              '@type': 'ontology:TitratedDoseDrugTreatment',
              'treatment_has_drug': 'http://trials.drugis.org/instances/1e7464b5-c5ca-4b08-a735-a3aa361532d6',
              'treatment_max_dose': [{
                '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac11005900000004',
                'dosingPeriodicity': 'P1D',
                'unit': 'http://trials.drugis.org/instances/8691b100-e5d9-4048-acc3-6ed9731e0896',
                'value': 100
              }],
              'treatment_min_dose': [{
                '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194dac11005900000002',
                'dosingPeriodicity': 'P1D',
                'unit': 'http://trials.drugis.org/instances/8691b100-e5d9-4048-acc3-6ed9731e0896',
                'value': 50
              }]
            }],
            'label': 'Sertraline'
          }]
        };
        studyDefer.resolve(jsonStudy);
        rootScope.$apply();
      })


      it('should return the activities contained in the study', function(done) {

        // call function under test
        activityService.queryItems().then(function(result) {
          var activities = result;

          // verify query result
          expect(activities.length).toBe(4);
          expect(activities[0].label).toEqual(jsonStudy.has_activity[0].label);
          expect(activities[1].label).toEqual(jsonStudy.has_activity[1].label);
          expect(activities[2].label).toEqual(jsonStudy.has_activity[2].label);
          expect(activities[3].label).toEqual(jsonStudy.has_activity[3].label);
          expect(activities[0].activityType).toEqual(activityService.ACTIVITY_TYPE_OPTIONS['ontology:WashOutActivity']);
          expect(activities[1].activityType).toEqual(activityService.ACTIVITY_TYPE_OPTIONS['ontology:TreatmentActivity']);
          expect(activities[2].activityType).toEqual(activityService.ACTIVITY_TYPE_OPTIONS['ontology:RandomizationActivity']);
          expect(activities[3].activityType).toEqual(activityService.ACTIVITY_TYPE_OPTIONS['ontology:TreatmentActivity']);
          expect(activities[0].activityDescription).not.toBeDefined();
          expect(activities[1].activityDescription).not.toBeDefined();
          expect(activities[2].activityDescription).not.toBeDefined();
          expect(activities[3].activityDescription).not.toBeDefined();

          expect(activities[0].treatments).not.toBeDefined();
          expect(activities[1].treatments.length).toBe(1);
          expect(activities[1].treatments[0].treatmentDoseType).toEqual('http://trials.drugis.org/ontology#TitratedDoseDrugTreatment');
          expect(activities[1].treatments[0].drug.uri).toEqual('http://trials.drugis.org/instances/drug1Uuid');
          expect(activities[1].treatments[0].drug.label).toEqual('Sertraline');
          expect(activities[1].treatments[0].doseUnit.uri).toEqual('http://trials.drugis.org/instances/unit1Uuid');
          expect(activities[1].treatments[0].doseUnit.label).toEqual('milligram');
          expect(activities[1].treatments[0].dosingPeriodicity).toEqual('P1D');
          expect(activities[1].treatments[0].maxValue).toEqual('1.500000e+02');
          expect(activities[1].treatments[0].minValue).toEqual('5.000000e+01');
          expect(activities[1].treatments[0].treatmentUri).toEqual('http://trials.drugis.org/instances/treatment1Uuid');

          expect(activities[1].treatments[1].treatmentDoseType).toEqual('http://trials.drugis.org/ontology#FixedDoseDrugTreatment');
          expect(activities[1].treatments[1].drug.label).toEqual('Bupropion');
          expect(activities[1].treatments[1].doseUnit.label).toEqual('liter');
          expect(activities[1].treatments[1].dosingPeriodicity).toEqual('P1D');
          expect(activities[1].treatments[1].fixedValue).toEqual('0.000000e+00');

          done();
        });
        rootScope.$digest();
      });
    });

    describe('add non-treatment activity', function() {

      var queryPromise;
      var newActivity = {
        activityUri: 'http://trials.drugis.org/instances/newActivityUuid',
        label: 'newActivityLabel',
        activityType: {
          uri: 'http://mockActivityUri'
        },
        activityDescription: 'some description'
      };

      beforeEach(function(done) {

        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);
        activityService.addItem(newActivity).then(function() {
          queryPromise = activityService.queryItems();
          done();
        });
        rootScope.$digest();
      });

      it('should add the new activity to the graph', function() {
        queryPromise.then(function(result) {
          expect(result.length).toBe(1);
          expect(activities[0].label).toEqual(newActivity.label);
          expect(activities[0].activityType).toEqual(newActivity.activityType);
          expect(activities[0].activityDescription).toEqual(newActivity.activityDescription);
          expect(activities[0].treatments).not.toBeDefined();
        });
      });
    });

    describe('add treatment activity', function() {

      var queryActivityPromise, queryUnitPromise, queryDrugsPromise;
      var fixedTreatment = {
        treatmentDoseType: 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment',
        drug: {
          uri: 'http://drug/newDrugUuid',
          label: 'new drug'
        },
        doseUnit: {
          uri: 'http://unit/oldUnit',
          label: 'old unit label'
        },
        fixedValue: '1500',
        dosingPeriodicity: 'P3W'
      };

      var titRatedTreatment = {
        treatmentDoseType: 'http://trials.drugis.org/ontology#TitratedDoseDrugTreatment',
        drug: {
          uri: 'http://drug/oldDrugUuid',
          label: 'old drug'
        },
        doseUnit: {
          uri: 'http://unit/oldUnit',
          label: 'old unit label'
        },
        minValue: '120',
        maxValue: '1300',
        dosingPeriodicity: 'P2W'
      };

      var newActivity = {
        activityUri: 'http://trials.drugis.org/instances/newActivityUuid',
        label: 'newActivityLabel',
        activityType: {
          uri: 'http://trials.drugis.org/ontology#TreatmentActivity'
        },
        treatments: [fixedTreatment, titRatedTreatment]
      };

      beforeEach(function(done) {

        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        activityService.addItem(newActivity).then(function() {
          queryActivityPromise = activityService.queryItems();
          queryDrugsPromise = drugService.queryItems();
          queryUnitPromise = unitService.queryItems()
          done();
        });

        rootScope.$digest();
      });

      it('should add the new activity to the graph', function(done) {
        queryActivityPromise.then(function(resultActivities) {
          expect(resultActivities.length).toBe(1);
          var activity = resultActivities[0];
          expect(activity.treatments.length).toBe(2);

          expect(activity.treatments[1].treatmentDoseType).toEqual('http://trials.drugis.org/ontology#TitratedDoseDrugTreatment');
          expect(activity.treatments[1].drug.label).toEqual('old drug');
          expect(activity.treatments[1].doseUnit.label).toEqual('old unit label');
          expect(activity.treatments[1].dosingPeriodicity).toEqual('P2W');
          expect(activity.treatments[1].minValue).toEqual('1.2e2');
          expect(activity.treatments[1].maxValue).toEqual('1.3e3');

          expect(activity.treatments[0].treatmentDoseType).toEqual('http://trials.drugis.org/ontology#FixedDoseDrugTreatment');
          expect(activity.treatments[0].drug.label).toEqual('new drug');
          expect(activity.treatments[0].doseUnit.label).toEqual('old unit label');
          expect(activity.treatments[0].dosingPeriodicity).toEqual('P3W');
          expect(activity.treatments[0].fixedValue).toEqual('1.5e3');

        });

        queryDrugsPromise.then(function(drugResults) {
          expect(drugResults.length).toBe(2);
          expect(drugResults[1].label).toEqual('old drug');
          expect(drugResults[0].label).toEqual('new drug');
        });

        queryUnitPromise.then(function(unitResults) {
          expect(unitResults.length).toBe(1);
          expect(unitResults[0].label).toEqual('old unit label');
          done();
        });
      });
    });

    describe('edit activities', function() {

      beforeEach(function(done) {
        // load some mock graph with activities
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/test_graphs/activitiesMockGraph.ttl', false);
        xmlHTTP.send(null);
        var activitiesEditMockGraph = xmlHTTP.responseText;

        xmlHTTP.open('PUT', scratchStudyUri + '/data?graph=' + graphUri, false);
        xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
        xmlHTTP.send(activitiesEditMockGraph);

        // stub remotestoreServiceStub.executeQuery method
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        var oldTreatment = {
          treatmentUri: 'http://trials.drugis.org/instances/treatment2Uuid',
          treatmentDoseType: 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment',
          drug: {
            uri: 'http://trials.drugis.org/instances/drug1Uuid',
            label: 'Sertraline'
          },
          doseUnit: {
            uri: 'http://trials.drugis.org/instances/unit1Uuid',
            label: 'milligram'
          },
          fixedValue: '150',
          dosingPeriodicity: 'P3W'
        };
        var newTreatment = {
          treatmentDoseType: 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment',
          drug: {
            uri: 'http://drug/newDrugUuid2',
            label: 'new drug 2'
          },
          doseUnit: {
            uri: 'http://trials.drugis.org/instances/unit1Uuid',
            label: 'milligram'
          },
          fixedValue: '150',
          dosingPeriodicity: 'P3W'
        };

        var editActivity = {
          activityUri: 'http://trials.drugis.org/instances/activity2Uuid',
          label: 'edit label',
          activityType: {
            uri: 'http://trials.drugis.org/ontology#TreatmentActivity'
          },
          treatments: [oldTreatment, newTreatment],
          activityDescription: undefined
        };

        activityService.editItem(editActivity).then(function() {
          done();
        });

        rootScope.$digest();
      });

      it('should edit the activity', function(done) {

        activityService.queryItems().then(function(activities) {
          // verify query result
          expect(activities.length).toBe(2);
          expect(activities[0].label).toEqual('edit label');
          expect(activities[0].activityType).toEqual(activityService.ACTIVITY_TYPE_OPTIONS['http://trials.drugis.org/ontology#TreatmentActivity']);
          expect(activities[0].activityDescription).not.toBeDefined();
          expect(activities[0].treatments.length).toBe(3);
          expect(activities[0].treatments[0].drug.label).toEqual('new drug 2');
          expect(commentServiceStub.addComment).not.toHaveBeenCalled();
          done();
        });
      });
    });

    describe('delete activity', function() {

      beforeEach(function(done) {
        // load some mock graph with activities
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/test_graphs/activitiesMockGraph.ttl', false);
        xmlHTTP.send(null);
        var activitiesQueryMockGraph = xmlHTTP.responseText;

        xmlHTTP.open('PUT', scratchStudyUri + '/data?graph=' + graphUri, false);
        xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
        xmlHTTP.send(activitiesQueryMockGraph);

        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        var deleteActivity = {
          activityUri: 'http://trials.drugis.org/instances/activity2Uuid',
          activityType: {
            uri: 'some uri'
          }
        };

        activityService.deleteItem(deleteActivity).then(function() {
          done();
        });
        rootScope.$digest();

      });

      it('should remove the activity', function(done) {

        var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s ?p ?o }}';
        var result = testUtils.queryTeststore(query);
        var resultTriples = testUtils.deFusekify(result);

        // verify results
        expect(resultTriples.length).toBe(13); // todo needs to be six the delete does cleanup of treamtmentStuff
        done();
      });
    });


  });
});

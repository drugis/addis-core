'use strict';
define(['angular', 'angular-mocks'], function(angular, angularMocks) {
  describe('the activity service', function() {

    var mockStudyUuid = 'mockStudyUuid',
      rootScope, q,
      commentServiceStub,
      studyDefer,
      jsonStudy,
      graphDefer,
      jsonGraph,
      studyServiceMock = jasmine.createSpyObj('StudyService', ['getStudy', 'getJsonGraph', 'save']),
      activityService,
      drugServiceMock = jasmine.createSpyObj('DrugService', ['queryItems']),
      unitServiceMock;

    beforeEach(function() {
      module('trialverse.activity', function($provide) {
        $provide.value('DrugService', drugServiceMock);
        $provide.value('UnitService', unitServiceMock);
        $provide.value('StudyService', studyServiceMock);
      });
    });

    beforeEach(module('trialverse.activity'));

    beforeEach(inject(function($q, $rootScope, ActivityService) {
      q = $q;
      rootScope = $rootScope;

      studyDefer = q.defer();
      graphDefer = q.defer();
      var saveDefer = q.defer();
      saveDefer.resolve();

      studyServiceMock.getStudy.and.returnValue(studyDefer.promise);
      studyServiceMock.getJsonGraph.and.returnValue(graphDefer.promise);
      studyServiceMock.save.and.returnValue(saveDefer.promise);
      activityService = ActivityService;
    }));


    describe('query activities', function() {

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
              'treatment_has_drug': 'http://trials.drugis.org/instances/drug1Uuid',
              'treatment_max_dose': [{
                '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac11005900000001',
                'dosingPeriodicity': 'P1D',
                'unit': 'http://trials.drugis.org/instances/unit1Uuid',
                'value': 40
              }],
              'treatment_min_dose': [{
                '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194dac11005900000001',
                'dosingPeriodicity': 'P1D',
                'unit': 'http://trials.drugis.org/instances/unit1Uuid',
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
              '@type': 'ontology:FixedDoseDrugTreatment',
              'treatment_has_drug': 'http://trials.drugis.org/instances/1e7464b5-c5ca-4b08-a735-a3aa361532d6',
              'treatment_dose': [{
                '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac11005900000004',
                'dosingPeriodicity': 'P1D',
                'unit': 'http://trials.drugis.org/instances/unit1Uuid',
                'value': 100
              }]
            }],
            'label': 'Sertraline'
          }]
        };
        jsonGraph = {
          '@graph': [{
            '@id': 'http://trials.drugis.org/instances/drug1Uuid',
            '@type': 'ontology:Drug',
            label: 'Sertraline'
          }, {
            '@id': 'http://trials.drugis.org/instances/1e7464b5-c5ca-4b08-a735-a3aa361532d6',
            '@type': 'ontology:Drug',
            label: 'Bupropion'
          }, {
            '@id': 'http://trials.drugis.org/instances/unit1Uuid',
            '@type': 'ontology:Unit',
            label: 'milligram'
          }]
        }
        studyDefer.resolve(jsonStudy);
        graphDefer.resolve(jsonGraph);
        rootScope.$apply();
      });


      it('should return the activities contained in the study', function(done) {

        // call function under test
        activityService.queryItems().then(function(result) {
          var activities = result;

          // verify query result
          expect(activities.length).toBe(4);
          expect(activities[0].activityUri).toEqual(jsonStudy.has_activity[0]['@id']);
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
          expect(activities[1].treatments[0].treatmentDoseType).toEqual('ontology:TitratedDoseDrugTreatment');
          expect(activities[1].treatments[0].drug.uri).toEqual('http://trials.drugis.org/instances/drug1Uuid');
          expect(activities[1].treatments[0].drug.label).toEqual('Sertraline');
          expect(activities[1].treatments[0].doseUnit.uri).toEqual('http://trials.drugis.org/instances/unit1Uuid');
          expect(activities[1].treatments[0].doseUnit.label).toEqual('milligram');
          expect(activities[1].treatments[0].dosingPeriodicity).toEqual('P1D');
          expect(activities[1].treatments[0].maxValue).toEqual(40);
          expect(activities[1].treatments[0].minValue).toEqual(20);

          expect(activities[3].treatments[0].treatmentDoseType).toEqual('ontology:FixedDoseDrugTreatment');
          expect(activities[3].treatments[0].drug.label).toEqual('Bupropion');
          expect(activities[3].treatments[0].doseUnit.label).toEqual('milligram');
          expect(activities[3].treatments[0].dosingPeriodicity).toEqual('P1D');
          expect(activities[3].treatments[0].fixedValue).toEqual(100);

          done();
        });
        rootScope.$digest();
      });
    });

    describe('add non-treatment activity', function() {

      var newActivity = {
        label: 'newActivityLabel',
        activityType: {
          uri: 'http://mockActivityUri'
        },
        activityDescription: 'some description'
      };

      beforeEach(function() {
        jsonStudy = {
          has_activity: []
        };
        studyDefer.resolve(jsonStudy);
        rootScope.$digest();
        activityService.addItem(newActivity);
      });

      it('should add the new activity to the graph', function() {
        activityService.queryItems().then(function(result) {
          expect(result.length).toBe(1);
          expect(result[0].label).toEqual(newActivity.label);
          expect(result[0].activityType).toEqual(newActivity.activityType);
          expect(result[0].activityDescription).toEqual(newActivity.activityDescription);
          expect(result[0].treatments).not.toBeDefined();
        });
      });
    });

    describe('add treatment activity', function() {

      var fixedTreatment = {
        treatmentDoseType: 'ontology:FixedDoseDrugTreatment',
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
        treatmentDoseType: 'ontology:TitratedDoseDrugTreatment',
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
          uri: 'ontology:TreatmentActivity'
        },
        treatments: [fixedTreatment, titRatedTreatment]
      };

      beforeEach(function(done) {
        var jsonStudy = {
          has_activity: []
        };
        var jsonGraph = {
          '@graph': [{
            '@id': 'http://drug/newDrugUuid',
            label: 'new drug'
          }, {
            '@id': 'http://unit/oldUnit',
            label: 'old unit label'
          }, {
            '@id': 'http://drug/oldDrugUuid',
            label: 'old drug'
          }]
        };
        graphDefer.resolve(jsonGraph);
        studyDefer.resolve(jsonStudy);
        activityService.addItem(newActivity).then(done);

        rootScope.$digest();
      });

      it('should add the new activity to the graph', function(done) {
        activityService.queryItems().then(function(resultActivities) {
          expect(resultActivities.length).toBe(1);
          var activity = resultActivities[0];
          expect(activity.treatments.length).toBe(2);


          expect(activity.treatments[0].treatmentDoseType).toEqual('ontology:FixedDoseDrugTreatment');
          expect(activity.treatments[0].drug.label).toEqual('new drug');
          expect(activity.treatments[0].doseUnit.label).toEqual('old unit label');
          expect(activity.treatments[0].dosingPeriodicity).toEqual('P3W');
          expect(activity.treatments[0].fixedValue).toEqual('1500');

          expect(activity.treatments[1].treatmentDoseType).toEqual('ontology:TitratedDoseDrugTreatment');
          expect(activity.treatments[1].drug.label).toEqual('old drug');
          expect(activity.treatments[1].doseUnit.label).toEqual('old unit label');
          expect(activity.treatments[1].dosingPeriodicity).toEqual('P2W');
          expect(activity.treatments[1].minValue).toEqual('120');
          expect(activity.treatments[1].maxValue).toEqual('1300');

          done();
        });
      });
    });

    describe('edit activities', function() {
      beforeEach(function() {
        jsonStudy = {
          'has_activity': [{
            '@id': 'http://trials.drugis.org/instances/6d44e008-450a-4363-aae2-f6a79801283d',
            '@type': 'ontology:WashOutActivity',
            'has_activity_application': [],
            'label': 'Wash out',
          }, {
            '@id': 'http://trials.drugis.org/instances/activity2Uuid',
            '@type': 'ontology:RandomizationActivity',
            'has_activity_application': [],
            label: 'Randomization',
            comment: 'activity 2 comment'
          }]
        };
        studyDefer.resolve(jsonStudy);
        graphDefer.resolve({});
        rootScope.$digest();
      });

      it('should edit a non-treatment activity', function(done) {
        var editActivity = {
          activityUri: 'http://trials.drugis.org/instances/activity2Uuid',
          label: 'edit label',
          activityType: {
            uri: 'ontology:ScreeningActivity'
          }
        };
        activityService.editItem(editActivity).then(function() {
          activityService.queryItems().then(function(activities) {
            expect(activities.length).toBe(2);
            expect(activities[1].label).toEqual('edit label');
            expect(activities[1].activityType).toEqual(activityService.ACTIVITY_TYPE_OPTIONS['ontology:ScreeningActivity']);
            expect(activities[1].activityDescription).not.toBeDefined();
            done();
          });
        });
        rootScope.$digest();
      });
    });

    describe('delete activity', function() {
      beforeEach(function() {
        jsonStudy = {
          'has_activity': [{
            '@id': 'http://trials.drugis.org/instances/6d44e008-450a-4363-aae2-f6a79801283d',
            '@type': 'ontology:WashOutActivity',
            'has_activity_application': [],
            'label': 'Wash out',
          }, {
            '@id': 'http://trials.drugis.org/instances/activity2Uuid',
            '@type': 'ontology:RandomizationActivity',
            'has_activity_application': [],
            label: 'Randomization',
            comment: 'activity 2 comment'
          }]
        };
        studyDefer.resolve(jsonStudy);
        graphDefer.resolve({});
        rootScope.$digest();
      });
      it('should delete a non-treatment activity', function(done) {
        var deleteActivity = {
          activityUri: 'http://trials.drugis.org/instances/activity2Uuid',
        };
        activityService.deleteItem(deleteActivity).then(function() {
          activityService.queryItems().then(function(items) {
            expect(items.length).toBe(1);
            done();
          });
        });
        rootScope.$digest();
      });

    });
  });
});
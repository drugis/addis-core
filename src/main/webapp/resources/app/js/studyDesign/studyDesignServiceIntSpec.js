'use strict';
define(['angular-mocks', './studyDesign'], function() {
  describe('the activity service', function() {

    var mockStudyUuid = 'mockStudyUuid';

    var rootScope, q, httpBackend;
    var studyService = jasmine.createSpyObj('StudyService', ['getStudy', 'save']);
    var epochService = jasmine.createSpyObj('EpochService', ['queryItems']);

    var studyDesignService;

    var baseStudy = {
          'has_activity': [{
            '@id': 'http://trials.drugis.org/instances/1afd0f6f-4975-40d4-a557-73ef4aa39046',
            '@type': 'ontology:TreatmentActivity',
            'has_activity_application': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4bac11005900000001',
              'applied_in_epoch': 'http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c',
              'applied_to_arm': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8'
            }],
            'has_drug_treatment': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001d',
              '@type': 'ontology:TitratedDoseDrugTreatment',
              'treatment_has_drug': 'http://trials.drugis.org/instances/d887928f-923b-49a9-8056-4cea570f9317',
              'treatment_max_dose': [{
                '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000009',
                'dosingPeriodicity': 'P1D',
                'unit': 'http://trials.drugis.org/instances/a82113a8-9353-4155-9758-57f03b467320',
                'value': '1.200000e+02'
              }],
              'treatment_min_dose': [{
                '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000001',
                'dosingPeriodicity': 'P1D',
                'unit': 'http://trials.drugis.org/instances/a82113a8-9353-4155-9758-57f03b467320',
                'value': '4.000000e+01'
              }]
            }],
            'label': 'Duloxetine'
          }, {
            '@id': 'http://trials.drugis.org/instances/aa54abb9-e81e-4492-b8bf-e46a68571971',
            '@type': 'ontology:RandomizationActivity',
            'has_activity_application': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000002',
              'applied_in_epoch': 'http://trials.drugis.org/instances/62bc14d7-4fbc-4973-8119-ffaca0bb18e4',
              'applied_to_arm': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb'
            }, {
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001b',
              'applied_in_epoch': 'http://trials.drugis.org/instances/62bc14d7-4fbc-4973-8119-ffaca0bb18e4',
              'applied_to_arm': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b'
            }, {
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000011',
              'applied_in_epoch': 'http://trials.drugis.org/instances/62bc14d7-4fbc-4973-8119-ffaca0bb18e4',
              'applied_to_arm': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8'
            }],
            'label': 'Randomization'
          }, {
            '@id': 'http://trials.drugis.org/instances/59690fd3-4cfc-42eb-a699-57d98eee4a9a',
            '@type': 'ontology:TreatmentActivity',
            'has_activity_application': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000005',
              'applied_in_epoch': 'http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c',
              'applied_to_arm': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b'
            }],
            'has_drug_treatment': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000014',
              '@type': 'ontology:FixedDoseDrugTreatment',
              'treatment_dose': [{
                '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000001',
                'dosingPeriodicity': 'P1D',
                'unit': 'http://trials.drugis.org/instances/a82113a8-9353-4155-9758-57f03b467320',
                'value': '2.000000e+01'
              }],
              'treatment_has_drug': 'http://trials.drugis.org/instances/4586c415-aa62-4995-bb47-1f92f7f95cb9'
            }],
            'label': 'Fluoxetine'
          }, {
            '@id': 'http://trials.drugis.org/instances/8ca7aa4c-1391-4222-88a6-819434efcb84',
            '@type': 'ontology:WashOutActivity',
            'has_activity_application': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000005',
              'applied_in_epoch': 'http://trials.drugis.org/instances/a02fdb05-4986-4f58-b20f-973572813c8d',
              'applied_to_arm': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb'
            }, {
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000013',
              'applied_in_epoch': 'http://trials.drugis.org/instances/a02fdb05-4986-4f58-b20f-973572813c8d',
              'applied_to_arm': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b'
            }, {
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c49ac11005900000006',
              'applied_in_epoch': 'http://trials.drugis.org/instances/a02fdb05-4986-4f58-b20f-973572813c8d',
              'applied_to_arm': 'http://trials.drugis.org/instances/61c97dd4-a9cf-4f1a-bcb8-9c90e9c943b8'
            }],
            'label': 'Wash out'
          }, {
            '@id': 'http://trials.drugis.org/instances/9557605d-e793-4977-8ada-9fb899fafb72',
            '@type': 'ontology:TreatmentActivity',
            'has_activity_application': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000006',
              'applied_in_epoch': 'http://trials.drugis.org/instances/2e545e50-b1f6-4a2a-813a-ab972cef804c',
              'applied_to_arm': 'http://trials.drugis.org/instances/fd352389-ad28-425e-ac76-a2694ed396bb'
            }],
            'has_drug_treatment': [{
              '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac1100590000001e',
              '@type': 'ontology:FixedDoseDrugTreatment',
              'treatment_dose': [{
                '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac1c4aac11005900000004',
                'dosingPeriodicity': 'P1D',
                'unit': 'http://trials.drugis.org/instances/a82113a8-9353-4155-9758-57f03b467320',
                'value': '0.000000e+00'
              }],
              'treatment_has_drug': 'http://trials.drugis.org/instances/131649ff-418f-40a7-8744-04a849811025'
            }],
            'label': 'Placebo'
          }]
        };

    beforeEach(angular.mock.module('trialverse.studyDesign', function($provide) {
      $provide.value('StudyService', studyService);
      $provide.value('EpochService', epochService);
    }));

    beforeEach(inject(function($q, $rootScope, $httpBackend, StudyDesignService) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;

      studyDesignService = StudyDesignService;
    }));

    describe('query activity coordinates', function() {
      var studyJsonObject;

      beforeEach(function() {
        studyJsonObject = angular.copy(baseStudy);
        var studyDefer = q.defer();
        var getStudyPromise = studyDefer.promise;
        studyDefer.resolve(studyJsonObject);
        studyService.getStudy.and.returnValue(getStudyPromise);
      });

      it('should return the activity coordinates contained in the study', function(done) {

        studyDesignService.queryItems().then(function(results) {
          expect(results.length).toBe(9);
          expect(results[0].activityUri).toBe(studyJsonObject.has_activity[0]['@id']);
          expect(results[0].epochUri).toBe(studyJsonObject.has_activity[0].has_activity_application[0].applied_in_epoch);
          expect(results[0].armUri).toBe(studyJsonObject.has_activity[0].has_activity_application[0].applied_to_arm);
          done();
        });
        rootScope.$digest();
      });
    });


    describe('set activity coordinates, as update', function() {

      var studyJsonObject;

      beforeEach(function(done) {

        studyJsonObject = angular.copy(baseStudy);
        var studyDefer = q.defer();
        var getStudyPromise = studyDefer.promise;
        studyDefer.resolve(studyJsonObject);
        studyService.getStudy.and.returnValue(getStudyPromise);

        done();
      });

      it('should return the activities contained in the study', function(done) {

        var coordinates = {
          epochUri: 'http://trials.drugis.org/instances/62bc14d7-4fbc-4973-8119-ffaca0bb18e4',
          armUri: 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
          activityUri: 'http://trials.drugis.org/instances/aa54abb9-e81e-4492-b8bf-e46a68571971'
        };

        studyDesignService.setActivityCoordinates(coordinates).then(function() {
          studyDesignService.queryItems(mockStudyUuid).then(function(result) {
            expect(result.length).toEqual(9); // length should not have changed
            expect(result[3]).toEqual(coordinates);
            done();
          });
        });

        rootScope.$digest();
      });
    });

    describe('set activity coordinates, as add', function() {

      var studyJsonObject;

      beforeEach(function(done) {

        studyJsonObject = angular.copy(baseStudy);
        var studyDefer = q.defer();
        var getStudyPromise = studyDefer.promise;
        studyDefer.resolve(studyJsonObject);
        studyService.getStudy.and.returnValue(getStudyPromise);

        done();
      });

      it('should return the activities contained in the study', function(done) {

        var coordinates = {
          epochUri: 'http://trials.drugis.org/instances/62bc14d7-4fbc-4973-8119-ffaca0bb18e4',
          armUri: 'http://trials.drugis.org/instances/some_new_arm',
          activityUri: 'http://trials.drugis.org/instances/aa54abb9-e81e-4492-b8bf-e46a68571971'
        };

        studyDesignService.setActivityCoordinates(coordinates).then(function() {
          studyDesignService.queryItems(mockStudyUuid).then(function(result) {
            expect(result.length).toEqual(10); // length should grow by one
            expect(result[4]).toEqual(coordinates);
            done();
          });
        });

        rootScope.$digest();
      });
    });


    describe('cleanup complex graph', function() {

      var studyJsonObject;

      beforeEach(function(done) {

        studyJsonObject = angular.copy(baseStudy);
        studyJsonObject.has_arm =  [{
          '@id': 'http://trials.drugis.org/instances/5b8352d0-5f45-47a3-8893-2110514e9d3b',
          '@type': 'ontology:Arm',
          'label': 'arm label'
        }];
        studyJsonObject.has_epochs = [{
          '@id': 'http://trials.drugis.org/instances/a02fdb05-4986-4f58-b20f-973572813c8d',
          '@type': 'ontology:Epoch',
          'duration': 'P14D',
          'label': 'Washout'
        }];
        var epochs = [{
          uri: 'http://trials.drugis.org/instances/a02fdb05-4986-4f58-b20f-973572813c8d'
        }];

        var studyDefer = q.defer();
        var getStudyPromise = studyDefer.promise;
        studyDefer.resolve(studyJsonObject);
        studyService.getStudy.and.returnValue(getStudyPromise);
        var epochsDefer = q.defer();
        var queryEpochsPromise = epochsDefer.promise;
        epochsDefer.resolve(epochs);
        epochService.queryItems.and.returnValue(queryEpochsPromise);
        done();
      });

      it('should remove coordinates that refer to missing arms, epochs of activities', function(done) {

        studyDesignService.cleanupCoordinates().then(function() {
          studyDesignService.queryItems().then(function(result) {
            expect(result.length).toEqual(1);
            done();
          });
        });

        rootScope.$digest();
      });
    });


  });
});

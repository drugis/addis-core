'use strict';
define(['angular', 'angular-mocks'], function(angular, angularMocks) {
  describe('the outcome service', function() {

    var rootScope, q,
      uUIDServiceMock,
      measurementMomentServiceMock = jasmine.createSpyObj('MeasurementMomentService', ['queryItems']),
      outcomeService,
      studyServiceMock = jasmine.createSpyObj('StudyService', ['getStudy', 'getJsonGraph', 'save']),
      studyDefer,
      measurementMomentsDefer;

    beforeEach(function() {
      module('trialverse.outcome', function($provide) {
        uUIDServiceMock = jasmine.createSpyObj('UUIDService', ['generate']);
        uUIDServiceMock.generate.and.returnValue('newUuid');
        $provide.value('UUIDService', uUIDServiceMock);
        $provide.value('MeasurementMomentService', measurementMomentServiceMock);
        $provide.value('StudyService', studyServiceMock);
      });
    });
    beforeEach(module('trialverse.outcome'));

    beforeEach(inject(function($q, $rootScope, OutcomeService) {
      q = $q;
      rootScope = $rootScope;
      outcomeService = OutcomeService;

      studyDefer = q.defer();
      studyServiceMock.getStudy.and.returnValue(studyDefer.promise);
      measurementMomentsDefer = q.defer();
      measurementMomentServiceMock.queryItems.and.returnValue(measurementMomentsDefer.promise);
    }));

    describe('query outcomes of specific type', function() {
      var jsonStudy = {
        has_outcome: [{
          '@id': 'http://trials.drugis.org/instances/popchar1',
          '@type': 'ontology:OutcomeType',
          'has_result_property': [
            'ontology:standard_deviation',
            'ontology:mean',
            'ontology:sample_size'
          ],
          'is_measured_at': 'http://instance/moment1',
          'of_variable': [{
            '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194dac11005900000003',
            '@type': 'ontology:Variable',
            'measurementType': 'ontology:continuous',
            'comment': [
              '',
              'years'
            ],
            'label': 'Age'
          }],
          'comment': '',
          'label': 'Age'
        }, {
          '@id': 'http://trials.drugis.org/instances/9bb96077-a8e0-4da1-bee2-011db8b7e560',
          '@type': 'ontology:OutcomeType',
          'has_result_property': [
            'ontology:sample_size',
            'ontology:count'
          ],
          'is_measured_at': ['http://instance/moment1', 'http://instance/moment2'],
          'of_variable': [{
            '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac1100590000000b',
            '@type': 'ontology:Variable',
            'measurementType': 'ontology:dichotomous',
            'comment': '',
            'label': 'is stupid'
          }],
          'comment': '',
          'label': 'is stupid'
        }]
      };

      var measurementMoments = [{
        uri: 'http://instance/moment1'
      }, {
        uri: 'http://instance/moment2'
      }];

      beforeEach(function() {
        studyDefer.resolve(jsonStudy);
        measurementMomentsDefer.resolve(measurementMoments);
      });

      it('should query the characteristics', function(done) {
        outcomeService.queryItems(function(outcome) {
          return outcome['@type'] === 'ontology:OutcomeType';
        }).then(function(items) {
          expect(items.length).toBe(2);
          expect(items[0].measurementType).toEqual('ontology:continuous');
          expect(items[0].measuredAtMoments).toEqual([measurementMoments[0]]);
          expect(items[1].measurementType).toEqual('ontology:dichotomous');
          expect(items[1].measuredAtMoments).toEqual(measurementMoments);
          done();
        });
        rootScope.$digest();
      });
    });


describe('query outcomes that a not measured', function() {
      var jsonStudy = {
        has_outcome: [{
          '@id': 'http://trials.drugis.org/instances/popchar1',
          '@type': 'ontology:OutcomeType',
          'has_result_property': [
            'ontology:standard_deviation',
            'ontology:mean',
            'ontology:sample_size'
          ],
          
          'of_variable': [{
            '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194dac11005900000003',
            '@type': 'ontology:Variable',
            'measurementType': 'ontology:continuous',
            'comment': [
              '',
              'years'
            ],
            'label': 'Age'
          }],
          'comment': '',
          'label': 'Age'
        }]
      };

      beforeEach(function() {
        studyDefer.resolve(jsonStudy);
        measurementMomentsDefer.resolve([]);
      });

      it('should query the characteristics', function(done) {
        outcomeService.queryItems(function(outcome) {
          return outcome['@type'] === 'ontology:OutcomeType';
        }).then(function(items) {
          expect(items.length).toBe(1);
          expect(items[0].measurementType).toEqual('ontology:continuous');
          expect(items[0].measuredAtMoments).toEqual([]);
          done();
        });
        rootScope.$digest();
      });
    });

    describe('add outcome of type', function() {
      var queryPromise;
      var outcomeUri = 'http://trials.drugis.org/instances/newUuid';
      var moment = 'http://mm/uri';

      var measuredAtMoment = {
        uri: moment
      };

      var newPopulationChar = {
        uri: outcomeUri,
        label: 'label',
        measurementType: 'ontology:dichotomous',
        measuredAtMoments: [measuredAtMoment]
      };

      beforeEach(function(done) {
        measurementMomentsDefer.resolve([{
          itemUri: moment
        }]);
        studyDefer.resolve({
          has_outcome: []
        });

        outcomeService.addItem(newPopulationChar, 'ontology:OutcomeType').then(done);
        rootScope.$digest();
      });

      it('should add the outcomes', function(done) {
        var expectedStudy = {
          has_outcome: [{
            '@id': outcomeUri,
            '@type': 'ontology:OutcomeType',
            is_measured_at: moment,
            has_result_property: ['http://trials.drugis.org/ontology#sample_size', 'http://trials.drugis.org/ontology#count'],
            of_variable: [{
              '@type': 'ontology:Variable',
              measurementType: 'ontology:dichotomous',
              label: 'label'
            }],
            label: 'label'
          }]
        };

        outcomeService.queryItems().then(function(queryResult) {
          expect(studyServiceMock.save).toHaveBeenCalledWith(expectedStudy);
          expect(queryResult.length).toEqual(1);
          expect(queryResult[0].label).toEqual(newPopulationChar.label);
          done();
        });
      });
    });

    describe('edit outcome', function() {
      var jsonStudy = {
        has_outcome: [{
          '@id': 'http://trials.drugis.org/instances/popchar1',
          '@type': 'ontology:OutcomeType',
          'has_result_property': [
            'ontology:standard_deviation',
            'ontology:mean',
            'ontology:sample_size'
          ],
          'is_measured_at': 'http://instance/moment1',
          'of_variable': [{
            '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194dac11005900000003',
            '@type': 'ontology:Variable',
            'measurementType': 'ontology:continuous',
            'label': 'Age'
          }],
          'comment': '',
          'label': 'Age'
        }, {
          '@id': 'http://trials.drugis.org/instances/9bb96077-a8e0-4da1-bee2-011db8b7e560',
          '@type': 'ontology:OutcomeType',
          'has_result_property': [
            'ontology:sample_size',
            'ontology:count'
          ],
          'is_measured_at': ['http://instance/moment1', 'http://instance/moment2'],
          'of_variable': [{
            '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac1100590000000b',
            '@type': 'ontology:Variable',
            'measurementType': 'ontology:dichotomous',
            'label': 'is stupid'
          }],
          'label': 'is stupid'
        }]
      };

      var moment1 = 'http://instance/moment1';
      var moment2 = 'http://instance/moment2';
      var outcomeUri = 'http://trials.drugis.org/instances/popchar1';
      var measuredAtMoment1 = {
        uri: moment1
      };
      var measuredAtMoment2 = {
        uri: moment2
      };

      var newPopulationChar = {
        uri: 'http://trials.drugis.org/instances/popchar1',
        label: 'new label',
        measurementType: 'ontology:dichotomous',
        measuredAtMoments: [measuredAtMoment1, measuredAtMoment2]
      };

      beforeEach(function(done) {
        studyDefer.resolve(jsonStudy);
        measurementMomentsDefer.resolve({});
        outcomeService.editItem(newPopulationChar).then(done);
        rootScope.$digest();
      });

      it('should have changed the outcomes', function(done) {
        outcomeService.queryItems().then(function(queryResult) {
          expect(queryResult.length).toEqual(2);
          expect(queryResult[0].label).toEqual(newPopulationChar.label);
          expect(queryResult[0].measurementType).toEqual('ontology:dichotomous');
          expect(queryResult[0].measuredAtMoments.length).toBe(2);
          done();
        });
      });
    });

    describe('delete outcome', function() {
      var jsonStudy = {
        has_outcome: [{
          '@id': 'http://trials.drugis.org/instances/popchar1',
          '@type': 'ontology:OutcomeType',
          'has_result_property': [
            'ontology:standard_deviation',
            'ontology:mean',
            'ontology:sample_size'
          ],
          'is_measured_at': 'http://instance/moment1',
          'of_variable': [{
            '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194dac11005900000003',
            '@type': 'ontology:Variable',
            'measurementType': 'ontology:continuous',
            'label': 'Age'
          }],
          'comment': '',
          'label': 'Age'
        }, {
          '@id': 'http://trials.drugis.org/instances/9bb96077-a8e0-4da1-bee2-011db8b7e560',
          '@type': 'ontology:OutcomeType',
          'has_result_property': [
            'ontology:sample_size',
            'ontology:count'
          ],
          'is_measured_at': ['http://instance/moment1', 'http://instance/moment2'],
          'of_variable': [{
            '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac1100590000000b',
            '@type': 'ontology:Variable',
            'measurementType': 'ontology:dichotomous',
            'label': 'is stupid'
          }],
          'label': 'is stupid'
        }]
      };


      var newPopulationChar = {
        uri: 'http://trials.drugis.org/instances/popchar1'
      };

      beforeEach(function(done) {
        studyDefer.resolve(jsonStudy);
        measurementMomentsDefer.resolve({});
        outcomeService.deleteItem(newPopulationChar).then(done);
        rootScope.$digest();
      });


      it('should have removed the outcomes', function(done) {
        outcomeService.queryItems().then(function(queryResult) {
          expect(queryResult.length).toEqual(1);
          done();
        });
      });
    });


  });
});

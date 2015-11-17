'use strict';
define(['angular', 'angular-mocks'], function(angular, angularMocks) {
  describe('the population characteristic service', function() {

    var rootScope, q,
      uUIDServiceMock,
      measurementMomentServiceMock = jasmine.createSpyObj('MeasurementMomentService', ['queryItems']),
      populationCharacteristicService,
      studyServiceMock = jasmine.createSpyObj('StudyService', ['getStudy', 'getJsonGraph', 'save']),
      studyDefer,
      measurementMomentsDefer;

    beforeEach(function() {
      module('trialverse.populationCharacteristic', function($provide) {
        uUIDServiceMock = jasmine.createSpyObj('UUIDService', ['generate']);
        uUIDServiceMock.generate.and.returnValue('newUuid');
        $provide.value('UUIDService', uUIDServiceMock);
        $provide.value('MeasurementMomentService', measurementMomentServiceMock);
        $provide.value('StudyService', studyServiceMock);
      });
    });
    beforeEach(module('trialverse.populationCharacteristic'));

    beforeEach(inject(function($q, $rootScope, PopulationCharacteristicService) {
      q = $q;
      rootScope = $rootScope;
      populationCharacteristicService = PopulationCharacteristicService;

      studyDefer = q.defer();
      studyServiceMock.getStudy.and.returnValue(studyDefer.promise);
      measurementMomentsDefer = q.defer();
      measurementMomentServiceMock.queryItems.and.returnValue(measurementMomentsDefer.promise);
    }));

    describe('query population characteristics', function() {
      var jsonStudy = {
        has_outcome: [{
          '@id': 'http://trials.drugis.org/instances/popchar1',
          '@type': 'ontology:PopulationCharacteristic',
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
          '@type': 'ontology:PopulationCharacteristic',
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
        populationCharacteristicService.queryItems().then(function(items) {
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

    describe('add population characteristic', function() {
      var queryPromise;
      var popCharUri = 'http://trials.drugis.org/instances/newUuid';
      var moment = 'http://mm/uri';

      var measuredAtMoment = {
        uri: moment
      };

      var newPopulationChar = {
        uri: popCharUri,
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

        populationCharacteristicService.addItem(newPopulationChar).then(done);
        rootScope.$digest();
      });

      it('should add the population characteristics', function(done) {
        var expectedStudy = {
          has_outcome: [{
            '@type': 'ontology:PopulationCharacteristic',
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

        populationCharacteristicService.queryItems().then(function(queryResult) {
          expect(studyServiceMock.save).toHaveBeenCalledWith(expectedStudy);
          expect(queryResult.length).toEqual(1);
          expect(queryResult[0].label).toEqual(newPopulationChar.label);
          done();
        });
      });
    });

    describe('edit population characteristic', function() {
      var jsonStudy = {
        has_outcome: [{
          '@id': 'http://trials.drugis.org/instances/popchar1',
          '@type': 'ontology:PopulationCharacteristic',
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
          '@type': 'ontology:PopulationCharacteristic',
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
      var popCharUri = 'http://trials.drugis.org/instances/popchar1';
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
        populationCharacteristicService.editItem(newPopulationChar).then(done);
        rootScope.$digest();
      });

      it('should have changed the population characteristics', function(done) {
        populationCharacteristicService.queryItems().then(function(queryResult) {
          expect(queryResult.length).toEqual(2);
          expect(queryResult[0].label).toEqual(newPopulationChar.label);
          expect(queryResult[0].measurementType).toEqual('ontology:dichotomous');
          expect(queryResult[0].measuredAtMoments.length).toBe(2);
          done();
        });
      });
    });

    describe('delete population characteristic', function() {
      var jsonStudy = {
        has_outcome: [{
          '@id': 'http://trials.drugis.org/instances/popchar1',
          '@type': 'ontology:PopulationCharacteristic',
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
          '@type': 'ontology:PopulationCharacteristic',
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
        populationCharacteristicService.deleteItem(newPopulationChar).then(done);
        rootScope.$digest();
      });


      it('should have removed the population characteristics', function(done) {
        populationCharacteristicService.queryItems().then(function(queryResult) {
          expect(queryResult.length).toEqual(1);
          done();
        });
      });
    });


  });
});

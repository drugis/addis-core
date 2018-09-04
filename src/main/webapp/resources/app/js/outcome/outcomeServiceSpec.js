'use strict';
define(['angular-mocks', './outcome'], function() {
  describe('the outcome service', function() {
    var rootScope, q,
      uUIDServiceMock,
      outcomeService,
      measurementMomentServiceMock = jasmine.createSpyObj('MeasurementMomentService', ['queryItems']),
      studyServiceMock = jasmine.createSpyObj('StudyService', ['getStudy', 'getJsonGraph', 'findStudyNode', 'save']),
      resultsServiceMock = jasmine.createSpyObj('ResultsService', ['queryResultsByOutcome', 'queryNonConformantMeasurementsByOutcomeUri']),
      repairServiceMock = jasmine.createSpyObj('RepairService', ['mergeResults']),
      sourceResultsDefer,
      targetResultsDefer,
      sourceNonConformantResultsDefer,
      targetNonConformantResultsDefer,
      mergeResultsDefer,
      getStudyDefer,
      getStudyGraphDefer,
      saveStudyDefer,
      measurementMomentsDefer;

    beforeEach(function() {
      angular.mock.module('trialverse.outcome', function($provide) {
        uUIDServiceMock = jasmine.createSpyObj('UUIDService', ['generate']);
        uUIDServiceMock.generate.and.returnValue('newUuid');
        $provide.value('UUIDService', uUIDServiceMock);
        $provide.value('MeasurementMomentService', measurementMomentServiceMock);
        $provide.value('StudyService', studyServiceMock);
        $provide.value('RepairService', repairServiceMock);
        $provide.value('ResultsService', resultsServiceMock);
      });
    });
    beforeEach(angular.mock.module('trialverse.outcome'));

    beforeEach(inject(function($q, $rootScope, OutcomeService) {
      q = $q;
      rootScope = $rootScope;
      outcomeService = OutcomeService;

      getStudyDefer = q.defer();
      studyServiceMock.getStudy.and.returnValue(getStudyDefer.promise);
      getStudyGraphDefer = q.defer();
      studyServiceMock.getJsonGraph.and.returnValue(getStudyGraphDefer.promise);
      measurementMomentsDefer = q.defer();
      measurementMomentServiceMock.queryItems.and.returnValue(measurementMomentsDefer.promise);
      sourceResultsDefer = q.defer();
      targetResultsDefer = q.defer();
      sourceNonConformantResultsDefer = q.defer();
      targetNonConformantResultsDefer = q.defer();
      resultsServiceMock.queryResultsByOutcome.and.returnValues(sourceResultsDefer.promise, targetResultsDefer.promise);
      resultsServiceMock.queryNonConformantMeasurementsByOutcomeUri.and.returnValues(sourceNonConformantResultsDefer.promise, targetNonConformantResultsDefer.promise);
      mergeResultsDefer = q.defer();
      repairServiceMock.mergeResults.and.returnValue(mergeResultsDefer.promise);

      saveStudyDefer = $q.defer();
      var saveStudyPromise = saveStudyDefer.promise;
      studyServiceMock.save.and.returnValue(saveStudyPromise);
    }));

    afterEach(function() {
      studyServiceMock.save.calls.reset();
    });

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
      var jsonStudyGraph = [jsonStudy];

      var measurementMoments = [{
        uri: 'http://instance/moment1'
      }, {
        uri: 'http://instance/moment2'
      }];

      beforeEach(function() {
        getStudyGraphDefer.resolve(jsonStudyGraph);
        measurementMomentsDefer.resolve(measurementMoments);
        studyServiceMock.findStudyNode.and.returnValue(jsonStudy);
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

    describe('query outcomes that are not measured', function() {
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
        getStudyGraphDefer.resolve([jsonStudy]);
        studyServiceMock.findStudyNode.and.returnValue(jsonStudy);
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
      var outcomeUri = 'http://trials.drugis.org/instances/newUuid';
      var moment = 'http://mm/uri';

      var measuredAtMoment = {
        uri: moment
      };

      var newPopulationChar = {
        uri: outcomeUri,
        label: 'label',
        measurementType: 'ontology:dichotomous',
        measuredAtMoments: [measuredAtMoment],
        resultProperties: ['http://trials.drugis.org/ontology#sample_size', 'http://trials.drugis.org/ontology#count']
      };

      beforeEach(function(done) {
        measurementMomentsDefer.resolve([{
          itemUri: moment
        }]);
        var jsonStudy = {
          has_outcome: []
        };
        getStudyDefer.resolve(jsonStudy);
        getStudyGraphDefer.resolve([jsonStudy]);
        studyServiceMock.findStudyNode.and.returnValue(jsonStudy);
        outcomeService.addItem(newPopulationChar, 'ontology:OutcomeType').then(done);
        saveStudyDefer.resolve();
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
        rootScope.$digest();
      });
    });

    describe('edit outcome', function() {

      var moment1 = 'http://instance/moment1';
      var moment2 = 'http://instance/moment2';
      var jsonStudy = {
        has_outcome: [{
          '@id': 'http://trials.drugis.org/instances/popchar1',
          '@type': 'ontology:OutcomeType',
          'has_result_property': [
            'ontology:standard_deviation',
            'ontology:mean',
            'ontology:sample_size'
          ],
          'is_measured_at': moment1,
          'of_variable': [{
            '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194dac11005900000003',
            '@type': 'ontology:Variable',
            'measurementType': 'ontology:continuous',
            'label': 'Age'
          }],
          'comment': '',
          'label': 'Age'
        }, {
          '@id': 'http://trials.drugis.org/instances/var2',
          '@type': 'ontology:OutcomeType',
          'has_result_property': [
            'ontology:sample_size',
            'ontology:count'
          ],
          'is_measured_at': [moment1, moment2],
          'of_variable': [{
            '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/0000014fdfac194eac1100590000000b',
            '@type': 'ontology:Variable',
            'measurementType': 'ontology:dichotomous',
            'label': 'is stupid'
          }],
          'label': 'is stupid'
        }]
      };

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
        getStudyDefer.resolve(jsonStudy);
        getStudyGraphDefer.resolve([jsonStudy]);
        studyServiceMock.findStudyNode.and.returnValue(jsonStudy);
        measurementMomentsDefer.resolve({});
        outcomeService.editItem(newPopulationChar).then(done);
        saveStudyDefer.resolve();
        rootScope.$digest();
      });

      it('should have changed the outcomes', function(done) {
        outcomeService.queryItems().then(function(queryResult) {
          expect(queryResult.length).toEqual(2);
          expect(queryResult[1].uri).toEqual(newPopulationChar.uri);
          expect(queryResult[1].label).toEqual(newPopulationChar.label);
          expect(queryResult[1].measurementType).toEqual('ontology:dichotomous');
          expect(queryResult[1].measuredAtMoments.length).toBe(2);
          done();
        });
        rootScope.$digest();
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
        getStudyDefer.resolve(jsonStudy);
        getStudyGraphDefer.resolve([jsonStudy]);
        studyServiceMock.findStudyNode.and.returnValue(jsonStudy);
        measurementMomentsDefer.resolve({});
        outcomeService.deleteItem(newPopulationChar).then(done);
        saveStudyDefer.resolve();
        rootScope.$digest();
      });


      it('should have removed the outcomes', function(done) {
        outcomeService.queryItems().then(function(queryResult) {
          expect(queryResult.length).toEqual(1);
          done();
        });
        rootScope.$digest();
      });
    });

    describe('merge', function() {
      var source = {
        uri: 'sourceUri',
        measuredAtMoments: [{
          uri: 'sourceMoment1Uri'
        }]
      };
      var target = {
        uri: 'targetUri',
        measuredAtMoments: [{
          uri: 'targetMoment1Uri'
        }]
      };
      var sourceResults = [{
        id: -1
      }];
      var targetResults = [{
        id: -10
      }];
      var sourceNonConformantResults = [{
        id: -100
      }];
      var targetNonConformantResults = [{
        id: -1000
      }];
      var study = {
        has_outcome: [{
          '@id': 'sourceUri'
        }, {
          '@id': 'targetUri'
        }]
      };
      var expectedSaveAfterMergeMeasurementMoments = {
        has_outcome: [{
          '@type': 'ontology:ItsAType',
          '@id': 'targetUri',
          is_measured_at: ['targetMoment1Uri', 'sourceMoment1Uri'],
          label: undefined,
          of_variable: [{
            '@type': 'ontology:Variable',
            measurementType: undefined,
            label: undefined
          }],
          has_result_property: undefined
        }]
      };

      beforeEach(function(done) {
        sourceResultsDefer.resolve(sourceResults);
        targetResultsDefer.resolve(targetResults);
        sourceNonConformantResultsDefer.resolve(sourceNonConformantResults);
        targetNonConformantResultsDefer.resolve(targetNonConformantResults);
        mergeResultsDefer.resolve([]);
        getStudyDefer.resolve(study);
        getStudyGraphDefer.resolve([study]);
        saveStudyDefer.resolve();

        // to test
        outcomeService.merge(source, target, 'ontology:ItsAType').then(done);

        rootScope.$digest();
      });

      it('should merge the outcome results (both conformant and nonconformant)', function() {
        expect(resultsServiceMock.queryResultsByOutcome).toHaveBeenCalledWith(source.uri);
        expect(resultsServiceMock.queryResultsByOutcome).toHaveBeenCalledWith(target.uri);
        expect(resultsServiceMock.queryResultsByOutcome.calls.count()).toBe(2);
        expect(resultsServiceMock.queryNonConformantMeasurementsByOutcomeUri).toHaveBeenCalledWith(source.uri);
        expect(resultsServiceMock.queryNonConformantMeasurementsByOutcomeUri).toHaveBeenCalledWith(target.uri);
        expect(resultsServiceMock.queryNonConformantMeasurementsByOutcomeUri.calls.count()).toBe(2);
        expect(repairServiceMock.mergeResults).toHaveBeenCalledWith(target.uri, sourceResults, targetResults, jasmine.any(Function), 'of_outcome');
        expect(repairServiceMock.mergeResults).toHaveBeenCalledWith(target.uri, sourceNonConformantResults, targetNonConformantResults, jasmine.any(Function), 'of_outcome');
        expect(repairServiceMock.mergeResults.calls.count()).toBe(2);
        expect(studyServiceMock.getStudy).toHaveBeenCalled();
        expect(studyServiceMock.save.calls.argsFor(1)).toEqual([expectedSaveAfterMergeMeasurementMoments]);
      });
    });
  });
});

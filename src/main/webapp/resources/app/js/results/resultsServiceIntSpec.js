'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('the resultsService service', function() {

    var rootScope, q;
    var studyService = jasmine.createSpyObj('StudyService', ['getJsonGraph', 'saveJsonGraph']);
    var uuidServiceMock = jasmine.createSpyObj('UUIDService', ['generate']);
    uuidServiceMock.generate.and.returnValue('newUuid');
    var resultsService;

    beforeEach(function() {
      module('trialverse.results', function($provide) {
        $provide.value('StudyService', studyService);
        $provide.value('UUIDService', uuidServiceMock);
      });
    });

    beforeEach(angularMocks.inject(function($q, $rootScope, ResultsService) {
      q = $q;
      rootScope = $rootScope;
      resultsService = ResultsService;

      rootScope.$digest();

    }));

    describe('query results', function() {

      var graphJsonObject = [{
        '@id': 'http://trials.drugis.org/instances/result1',
        'count': 24,
        'of_arm': 'http://trials.drugis.org/instances/arm1',
        'of_moment': 'http://trials.drugis.org/instances/moment1',
        'of_outcome': 'http://trials.drugis.org/instances/outcome1',
        'sample_size': 70
      }, {
        '@id': 'http://trials.drugis.org/instances/result2',
        'standard_deviation': 2,
        'mean': 5,
        'of_arm': 'http://trials.drugis.org/instances/arm2',
        'of_moment': 'http://trials.drugis.org/instances/moment1',
        'of_outcome': 'http://trials.drugis.org/instances/outcome1',
        'sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/result3',
        'count': 3,
        'of_arm': 'http://trials.drugis.org/instances/arm2',
        'of_moment': 'http://trials.drugis.org/instances/moment1',
        'of_outcome': 'http://trials.drugis.org/instances/outcome2',
        'sample_size': 33
      }];

      var queryOutcome = 'http://trials.drugis.org/instances/outcome1';

      beforeEach(function() {
        var graphDefer = q.defer();
        var getGraphPromise = graphDefer.promise;
        graphDefer.resolve(graphJsonObject);
        studyService.getJsonGraph.and.returnValue(getGraphPromise);
      });

      it('should return the results for a given variable', function(done) {
        resultsService.queryResults(queryOutcome).then(function(actualResults) {
          expect(actualResults.length).toEqual(5);
          expect(actualResults[0].instance).toEqual('http://trials.drugis.org/instances/result1');
          expect(actualResults[0].armUri).toEqual('http://trials.drugis.org/instances/arm1');
          expect(actualResults[0].momentUri).toEqual('http://trials.drugis.org/instances/moment1');
          expect(actualResults[0].outcomeUri).toEqual('http://trials.drugis.org/instances/outcome1');
          expect(actualResults[0].result_property).toEqual('sample_size');
          expect(actualResults[0].value).toEqual(70);

          expect(actualResults[1].instance).toEqual('http://trials.drugis.org/instances/result1');
          expect(actualResults[1].armUri).toEqual('http://trials.drugis.org/instances/arm1');
          expect(actualResults[1].momentUri).toEqual('http://trials.drugis.org/instances/moment1');
          expect(actualResults[1].outcomeUri).toEqual('http://trials.drugis.org/instances/outcome1');
          expect(actualResults[1].result_property).toEqual('count');
          expect(actualResults[1].value).toEqual(24);

          expect(actualResults[4].value).toEqual(5);
          done();
        });
        // fire in the hole !
        rootScope.$digest();
      });

    });


    describe('updateResultValue', function() {
      describe('when there is not yet data in the graph', function() {

        beforeEach(function() {
          var graphDefer = q.defer();
          var getGraphPromise = graphDefer.promise;
          graphDefer.resolve([]);
          studyService.getJsonGraph.and.returnValue(getGraphPromise);
        });

        it('should add the value to the graph', function(done) {

          var row = {
            variable: {
              uri: 'http://trials.drugis.org/instances/outcome1'
            },
            arm: {
              armURI: 'http://trials.drugis.org/instances/arm1'
            },
            measurementMoment: {
              uri: 'http://trials.drugis.org/instances/moment1'
            }
          };

          var inputColumn = {
            valueName: 'count',
            value: 123,
          };

          resultsService.updateResultValue(row, inputColumn).then(function(result) {
            var expextedGraph = [{
              '@id': 'http://trials.drugis.org/instances/newUuid',
              'count': 123,
              'of_arm': 'http://trials.drugis.org/instances/arm1',
              'of_moment': 'http://trials.drugis.org/instances/moment1',
              'of_outcome': 'http://trials.drugis.org/instances/outcome1',
            }];
            expect(result).toBeTruthy();
            expect(result).toEqual(expextedGraph[0]['@id']);
            expect(studyService.saveJsonGraph).toHaveBeenCalledWith(expextedGraph);
            done();
          });
          // fire in the hole !
          rootScope.$digest();

        });

      });


      describe('if the new value is a value', function() {

        var graphJsonObject = [{
          '@id': 'http://trials.drugis.org/instances/result1',
          'count': 24,
          'of_arm': 'http://trials.drugis.org/instances/arm1',
          'of_moment': 'http://trials.drugis.org/instances/moment1',
          'of_outcome': 'http://trials.drugis.org/instances/outcome1',
          'sample_size': 70
        }];

        beforeEach(function() {
          var graphDefer = q.defer();
          var getGraphPromise = graphDefer.promise;
          graphDefer.resolve(graphJsonObject);
          studyService.getJsonGraph.and.returnValue(getGraphPromise);
        });

        it('should save the value to the graph', function(done) {
          var row = {
            variable: {
              uri: 'http://trials.drugis.org/instances/outcome1'
            },
            arm: {
              armURI: 'http://trials.drugis.org/instances/arm1'
            },
            measurementMoment: {
              uri: 'http://trials.drugis.org/instances/moment1'
            },
            uri: 'http://trials.drugis.org/instances/result1'
          };

          var inputColumn = {
            value: 789,
            valueName: 'sample_size'
          };

          resultsService.updateResultValue(row, inputColumn).then(function(result) {
            var expextedGraph = [{
              '@id': 'http://trials.drugis.org/instances/result1',
              'count': 24,
              'of_arm': 'http://trials.drugis.org/instances/arm1',
              'of_moment': 'http://trials.drugis.org/instances/moment1',
              'of_outcome': 'http://trials.drugis.org/instances/outcome1',
              'sample_size': 789
            }];
            expect(result).toBeTruthy();
            expect(result).toEqual(expextedGraph[0]['@id']);
            expect(result).toEqual(row.uri);
            expect(studyService.saveJsonGraph).toHaveBeenCalledWith(expextedGraph);
            done();
          });
          // fire in the hole !
          rootScope.$digest();
        });

      });

      describe('if the new value is a null value', function() {

        var graphJsonObject = [];

        beforeEach(function() {
          var graphDefer = q.defer();
          var getGraphPromise = graphDefer.promise;
          graphDefer.resolve(graphJsonObject);
          studyService.getJsonGraph.and.returnValue(getGraphPromise);
        });

        it('should save the value to the graph', function(done) {
          var row = {
            variable: {
              uri: 'http://trials.drugis.org/instances/outcome1'
            },
            arm: {
              armURI: 'http://trials.drugis.org/instances/arm1'
            },
            measurementMoment: {
              uri: 'http://trials.drugis.org/instances/moment1'
            }
          };

          var inputColumn = {
            value: null,
            valueName: 'sample_size'
          };

          studyService.saveJsonGraph.calls.reset();
          resultsService.updateResultValue(row, inputColumn).then(function(result) {

            expect(result).toBeFalsy();
            expect(studyService.saveJsonGraph).not.toHaveBeenCalled();
            done();
          });
          // fire in the hole !
          rootScope.$digest();
        });

      });


      describe('if the new value is null', function() {

        var row = {
          variable: {
            uri: 'http://trials.drugis.org/instances/outcome1'
          },
          arm: {
            armURI: 'http://trials.drugis.org/instances/arm1'
          },
          measurementMoment: {
            uri: 'http://trials.drugis.org/instances/moment1'
          },
          uri: 'http://trials.drugis.org/instances/result1'
        };

        var inputColumn = {
          value: null,
          valueName: 'sample_size'
        };

        var graphJsonObject = [{
          '@id': 'http://trials.drugis.org/instances/result1',
          'count': 24,
          'of_arm': 'http://trials.drugis.org/instances/arm1',
          'of_moment': 'http://trials.drugis.org/instances/moment1',
          'of_outcome': 'http://trials.drugis.org/instances/outcome1',
          'sample_size': 70
        }];

        beforeEach(function() {
          var graphDefer = q.defer();
          var getGraphPromise = graphDefer.promise;
          graphDefer.resolve(graphJsonObject);
          studyService.getJsonGraph.and.returnValue(getGraphPromise);
        });

        it('should delete the value from the graph', function(done) {

          resultsService.updateResultValue(row, inputColumn).then(function(result) {
            var expextedGraph = [{
              '@id': 'http://trials.drugis.org/instances/result1',
              'count': 24,
              'of_arm': 'http://trials.drugis.org/instances/arm1',
              'of_moment': 'http://trials.drugis.org/instances/moment1',
              'of_outcome': 'http://trials.drugis.org/instances/outcome1',
            }];
            expect(result).toBeTruthy();
            expect(studyService.saveJsonGraph).toHaveBeenCalledWith(expextedGraph);
            done();
          });
          // fire in the hole !
          rootScope.$digest();
        });
      });

      describe('if the new value is null', function() {

        var row = {
          variable: {
            uri: 'http://trials.drugis.org/instances/outcome1'
          },
          arm: {
            armURI: 'http://trials.drugis.org/instances/arm1'
          },
          measurementMoment: {
            uri: 'http://trials.drugis.org/instances/moment1'
          },
          uri: 'http://trials.drugis.org/instances/result1'
        };

        var inputColumn = {
          value: null,
          valueName: 'sample_size'
        };

        var graphJsonObject = [{
          '@id': 'http://trials.drugis.org/instances/result1',
          'of_arm': 'http://trials.drugis.org/instances/arm1',
          'of_moment': 'http://trials.drugis.org/instances/moment1',
          'of_outcome': 'http://trials.drugis.org/instances/outcome1',
          'sample_size': 70
        }];

        beforeEach(function() {
          var graphDefer = q.defer();
          var getGraphPromise = graphDefer.promise;
          graphDefer.resolve(graphJsonObject);
          studyService.getJsonGraph.and.returnValue(getGraphPromise);
        });

        it('should delete the value from the graph', function(done) {

          resultsService.updateResultValue(row, inputColumn).then(function(result) {
            var expextedGraph = [{
              '@id': 'http://trials.drugis.org/instances/result1',
              'count': 24,
              'of_arm': 'http://trials.drugis.org/instances/arm1',
              'of_moment': 'http://trials.drugis.org/instances/moment1',
              'of_outcome': 'http://trials.drugis.org/instances/outcome1',
            }];
            expect(result).toBeFalsy();
            expect(studyService.saveJsonGraph).toHaveBeenCalledWith(expextedGraph);
            done();
          });
          // fire in the hole !
          rootScope.$digest();
        });
      });
    }); // end describe updateResultValue


    describe('cleanupMeasurements', function() {

      var arm1 = {
        '@id': 'http://trials.drugis.org/instances/a1',
        '@type': 'ontology:Arm',
        'label': 'arm label'
      };

      var arm2 = {
        '@id': 'http://trials.drugis.org/instances/a2',
        '@type': 'ontology:Arm',
        'label': 'arm label'
      };

      var outcome1 = {
        '@id': 'http://trials.drugis.org/instances/out1',
        '@type': 'ontology:OutcomeType',
        'has_result_property': [
          'ontology:standard_deviation',
          'ontology:mean',
          'ontology:sample_size'
        ],
        'is_measured_at': 'http://trials.drugis.org/instances/mm1',
        'of_variable': [{
          '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/var1',
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
      };

      var outcome2 = {
        '@id': 'http://trials.drugis.org/instances/out2',
        '@type': 'ontology:OutcomeType',
        'has_result_property': [
          'ontology:count',
          'ontology:sample_size'
        ],
        'is_measured_at': 'http://trials.drugis.org/instances/mm1',
        'of_variable': [{
          '@id': 'http://fuseki-test.drugis.org:3030/.well-known/genid/var2',
          '@type': 'ontology:Variable',
          'measurementType': 'ontology:continuous',
          'comment': [
            '',
            'years'
          ],
          'label': 'My variable'
        }],
        'comment': '',
        'label': 'My outcome'
      };

      var measurementMoment1 = {
        '@id': 'http://trials.drugis.org/instances/mm1',
        '@type': 'ontology:MeasurementMoment',
        'relative_to_anchor': 'ontology:anchorEpochStart',
        'relative_to_epoch': 'http://trials.drugis.org/instances/e1',
        'time_offset': 'PT0S',
        'label': 'At start of epoch 1'
      };

      var measurementMoment2 = {
        '@id': 'http://trials.drugis.org/instances/mm2',
        '@type': 'ontology:MeasurementMoment',
        'relative_to_anchor': 'ontology:anchorEpochStart',
        'relative_to_epoch': 'http://trials.drugis.org/instances/e2',
        'time_offset': 'PT0S',
        'label': 'At start of epoch 1'
      };

      var result1 = {
        '@id': 'http://trials.drugis.org/instances/result1',
        'of_arm': arm1['@id'],
        'of_moment': measurementMoment1['@id'],
        'of_outcome': outcome1['@id'],
        'sample_size': 70
      };

      var result2 = {
        '@id': 'http://trials.drugis.org/instances/result1',
        'of_arm': arm2['@id'],
        'of_moment': measurementMoment2['@id'],
        'of_outcome': outcome2['@id'],
        'count': 3
      };

      var result3 = {
        '@id': 'http://trials.drugis.org/instances/result1',
        'of_arm': 'non existent arm id',
        'of_moment': measurementMoment2['@id'],
        'of_outcome': outcome2['@id'],
        'count': 6
      };

      var result4 = {
        '@id': 'http://trials.drugis.org/instances/result1',
        'of_arm': arm1['@id'],
        'of_moment': 'non existent moment id',
        'of_outcome': outcome1['@id'],
        'sample_size': 140
      };

      var result5 = {
        '@id': 'http://trials.drugis.org/instances/result1',
        'of_arm': arm2['@id'],
        'of_moment': measurementMoment2['@id'],
        'of_outcome': 'non existent outcome id',
        'count': 6
      };

      var resultsToLeave = [result1, result2];
      var resultsToBeCleaned = [result3, result4, result5];


      var study = {
        '@id': 'http://trials.drugis.org/studies/s1',
        '@type': 'ontology:Study',
        has_epochs: [],
        label: 'study 1',
        comment: 'my study',
        has_outcome: [outcome1, outcome2],
        has_arm: [arm1, arm2],
        has_activity: [],
        has_indication: [],
        has_objective: [],
        has_publication: [],
        has_eligibility_criteria: []
      };

      var graphJsonObject = [study, measurementMoment1, measurementMoment2].concat(resultsToLeave).concat(resultsToBeCleaned);
      var expextedGraph = [study, measurementMoment1, measurementMoment2].concat(resultsToLeave);

      var queryOutcome = 'http://trials.drugis.org/instances/outcome1';

      beforeEach(function() {
        var graphDefer = q.defer();
        var getGraphPromise = graphDefer.promise;
        graphDefer.resolve(graphJsonObject);
        studyService.getJsonGraph.and.returnValue(getGraphPromise);
      });

      it('should clean the graph', function(done) {
        resultsService.cleanupMeasurements(queryOutcome).then(function() {
          expect(studyService.saveJsonGraph).toHaveBeenCalledWith(expextedGraph);
          done();
        });
        // fire in the hole !
        rootScope.$digest();
      });

    });

  });
});

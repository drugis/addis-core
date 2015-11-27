'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the resultsService service', function() {

    var INTEGER_TYPE = '<http://www.w3.org/2001/XMLSchema#integer>';
    var DOUBLE_TYPE = '<http://www.w3.org/2001/XMLSchema#double>';

    var rootScope, q, httpBackend;
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

    beforeEach(inject(function($q, $rootScope, ResultsService) {
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
        'count': 2,
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
          expect(actualResults.length).toEqual(4);
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
              uri: 'http://trials.drugis.org/instances/arm1'
            },
            measurementMoment: {
              uri: 'http://trials.drugis.org/instances/moment1'
            }
          };

          var inputColumn = {
            valueName: 'count',
            value: 123,
          };

          resultsService.updateResultValue(row, inputColumn).then(function() {
            var expextedGraph = [{
              '@id': 'http://trials.drugis.org/instances/newUuid',
              'count': 123,
              'of_arm': 'http://trials.drugis.org/instances/arm1',
              'of_moment': 'http://trials.drugis.org/instances/moment1',
              'of_outcome': 'http://trials.drugis.org/instances/outcome1',
            }];
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
              uri: 'http://trials.drugis.org/instances/arm1'
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

          resultsService.updateResultValue(row, inputColumn).then(function() {
            var expextedGraph = [{
              '@id': 'http://trials.drugis.org/instances/result1',
              'count': 24,
              'of_arm': 'http://trials.drugis.org/instances/arm1',
              'of_moment': 'http://trials.drugis.org/instances/moment1',
              'of_outcome': 'http://trials.drugis.org/instances/outcome1',
              'sample_size': 789
            }];
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
            uri: 'http://trials.drugis.org/instances/arm1'
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

          resultsService.updateResultValue(row, inputColumn).then(function() {
            var expextedGraph = [{
              '@id': 'http://trials.drugis.org/instances/result1',
              'count': 24,
              'of_arm': 'http://trials.drugis.org/instances/arm1',
              'of_moment': 'http://trials.drugis.org/instances/moment1',
              'of_outcome': 'http://trials.drugis.org/instances/outcome1',
            }];
            expect(studyService.saveJsonGraph).toHaveBeenCalledWith(expextedGraph);
            done();
          });
          // fire in the hole !
          rootScope.$digest();
        });

      });
    }); // end describe updateResultValue

  });
});

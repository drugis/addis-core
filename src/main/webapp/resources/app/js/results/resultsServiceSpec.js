'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the resultService service', function() {

    var INTEGER_TYPE = '<http://www.w3.org/2001/XMLSchema#integer>';
    var DOUBLE_TYPE = '<http://www.w3.org/2001/XMLSchema#double>';

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch';

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var studyService;
    var resultsService;
    var outcomeVariableUri;

    var updateResultValueQueryRaw;
    var queryResultsRaw;
    var addResultValueRaw;
    var deleteResultsRaw;
    var setOutcomeResultPropertyTemplate;
    var cleanUpMeasurementsTemplate;

    beforeEach(function() {
      module('trialverse.util', function($provide) {
        remotestoreServiceStub = jasmine.createSpyObj('RemoteRdfStoreService', [
          'create',
          'load',
          'executeUpdate',
          'executeQuery',
          'getGraph',
          'deFusekify'
        ]);
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
      });
    });

    beforeEach(module('trialverse.results'));

    beforeEach(inject(function($q, $rootScope, $httpBackend, ResultsService, StudyService, SparqlResource) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      resultsService = ResultsService;
      studyService = StudyService;

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load service templates and flush httpBackend
      addResultValueRaw = testUtils.loadTemplate('addResultValue.sparql', httpBackend);
      updateResultValueQueryRaw = testUtils.loadTemplate('updateResultValue.sparql', httpBackend);
      queryResultsRaw = testUtils.loadTemplate('queryResults.sparql', httpBackend);
      deleteResultsRaw = testUtils.loadTemplate('deleteResultValue.sparql', httpBackend);
      cleanUpMeasurementsTemplate = testUtils.loadTemplate('cleanUpMeasurements.sparql', httpBackend);

      SparqlResource.get('setOutcomeResultProperty.sparql');
      setOutcomeResultPropertyTemplate = testUtils.loadTemplate('setOutcomeResultProperty.sparql', httpBackend);

      httpBackend.flush();

      // create and load empty test store
      var createStoreDeferred = $q.defer();
      var createStorePromise = createStoreDeferred.promise;
      remotestoreServiceStub.create.and.returnValue(createStorePromise);

      var loadStoreDeferred = $q.defer();
      var loadStorePromise = loadStoreDeferred.promise;
      remotestoreServiceStub.load.and.returnValue(loadStorePromise);

      studyService.loadStore();
      createStoreDeferred.resolve(scratchStudyUri);
      loadStoreDeferred.resolve();

      outcomeVariableUri = 'http://uri.com/var';

      // add a outcome
      var addOutcomeQuery = 'PREFIX instance: <http://trials.drugis.org/instances/>' +
        ' PREFIX ontology: <http://trials.drugis.org/ontology#>' +
        ' PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
        ' ' +
        ' INSERT DATA {' +
        '  graph <' + graphUri + '> {' +
        '    <' + outcomeVariableUri + '> ' +
        '    rdfs:label "my outcome" ;' +
        '    ontology:measurementType <http://trials.drugis.org/ontology#dichotomous> ;' +
        '    rdfs:subClassOf ontology:Endpoint ; ' +
        '    ontology:has_result_property ontology:count ;' +
        '    ontology:has_result_property ontology:mean ; ' +
        '    ontology:has_result_property ontology:sample_size . ' +
        '  } ' +
        ' } ';

      testUtils.executeUpdateQuery(addOutcomeQuery);

      rootScope.$digest();

      // stub remotestoreServiceStub.executeUpdate method
      remotestoreServiceStub.executeUpdate.and.callFake(function(uri, query) {
        query = query.replace(/\$graphUri/g, graphUri);

        //// console.log('graphUri = ' + uri);
        //// console.log('query = ' + query);

        var updateResponce = testUtils.executeUpdateQuery(query);
        //// console.log('updateResponce ' + updateResponce);

        var executeUpdateDeferred = q.defer();
        executeUpdateDeferred.resolve();
        return executeUpdateDeferred.promise;
      });

    }));

    describe('updateResultValue', function() {
      describe('when there is not yet data in the graph', function() {

        it('should add the value to the graph', function(done) {

          var row = {
            variable: {
              uri: outcomeVariableUri
            },
            arm: {
              uri: 'http://uri.com/arm'
            },
            measurementMoment: {
              uri: 'http://uri.com/mm'
            }
          };
          var inputColumn = {
            valueName: 'count',
            value: 123,
            dataType: INTEGER_TYPE
          };
          // call the method to test
          var resultPromise = resultsService.updateResultValue(row, inputColumn);

          resultPromise.then(function() {
            var query = queryResultsRaw.replace(/\$graphUri/g, graphUri)
              .replace(/\$outcomeUri/g, row.variable.uri);
            var resultAsString = testUtils.queryTeststore(query);
            var resultAsObject = JSON.parse(resultAsString);
            expect('<' + resultAsObject.results.bindings[0].value.datatype + '>').toEqual(INTEGER_TYPE);
            var results = testUtils.deFusekify(resultAsString);
            expect(results.length).toBe(1);
            expect(results[0].value).toEqual(inputColumn.value.toString());
            done();
          });
          // fire in the hole !
          rootScope.$digest();
        });
      });

      describe('when there is data in the graph', function() {
        var inputColumn = {
          valueName: 'mean',
          value: 456,
          dataType: DOUBLE_TYPE
        };
        var results;

        beforeEach(function(done) {
          var row = {
            variable: {
              uri: outcomeVariableUri
            },
            arm: {
              uri: 'http://uri.com/arm'
            },
            measurementMoment: {
              uri: 'http://uri.com/mm'
            }
          };

          resultsService.updateResultValue(row, inputColumn).then(function() {
            var query = queryResultsRaw.replace(/\$graphUri/g, graphUri)
              .replace(/\$outcomeUri/g, row.variable.uri);
            var resultAsString = testUtils.queryTeststore(query);
            results = testUtils.deFusekify(resultAsString);
            done();
          });
          rootScope.$digest();
        });

        describe('if the new value is a value', function() {

          it('should save the value to the graph', function(done) {
            var row = {
              variable: {
                uri: outcomeVariableUri
              },
              arm: {
                uri: 'http://uri.com/arm'
              },
              measurementMoment: {
                uri: 'http://uri.com/mm'
              },
              uri: results[0].instance
            };

            inputColumn.value = 347856;
            resultsService.updateResultValue(row, inputColumn).then(function() {
              var query = queryResultsRaw.replace(/\$graphUri/g, graphUri)
                .replace(/\$outcomeUri/g, row.variable.uri);
              var resultAsString = testUtils.queryTeststore(query);
              var updatedResults = testUtils.deFusekify(resultAsString);
              var resultAsObject = JSON.parse(resultAsString);
              expect('<' + resultAsObject.results.bindings[0].value.datatype + '>').toEqual(DOUBLE_TYPE);
              expect(updatedResults.length).toBe(1);
              expect(updatedResults[0].value).toEqual(inputColumn.value.toString());
              // console.log('res = ' + JSON.stringify(updatedResults));
              done();
            });
          });

        });

        describe('if the new value is null', function() {

          it('should delete the value from the graph', function(done) {

            var row = {
              variable: {
                uri: outcomeVariableUri
              },
              arm: {
                uri: 'http://uri.com/arm'
              },
              measurementMoment: {
                uri: 'http://uri.com/mm'
              },
              uri: results[0].instance
            };
            inputColumn.value = null;
            inputColumn.valueName = 'mean';
            resultsService.updateResultValue(row, inputColumn).then(function() {
              var query = queryResultsRaw.replace(/\$graphUri/g, graphUri)
                .replace(/\$outcomeUri/g, row.variable.uri);
              var updatedResults = testUtils.deFusekify(testUtils.queryTeststore(query));
              expect(updatedResults.length).toBe(0);
              done();
            });
          });

        });

      });
    });
    // end describe updateResultValue


    describe('cleanUpMeasurements when deleting an arm', function() {

      beforeEach(function(done) {
        // load some mock graph with orphan results 
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/test_graphs/cleanUpTestArmDeletedDirtyGraph.ttl', false);
        xmlHTTP.send(null);
        var dirtyGraph = xmlHTTP.responseText;

        xmlHTTP.open('PUT', scratchStudyUri + '/data?graph=' + graphUri, false);
        xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
        xmlHTTP.send(dirtyGraph);

        // call cleanup
        resultsService.cleanUpMeasurements().then(done);
        rootScope.$digest();
      });

      it('should have removed the orphan items', function(done) {

        var variableToBeCleaned = 'http://trials.drugis.org/instances/69310a30-308f-4ee4-b70d-d29166acb9f3';

        var query = queryResultsRaw
          .replace(/\$graphUri/g, graphUri)
          .replace(/\$outcomeUri/g, variableToBeCleaned);

        var updatedResults = testUtils.deFusekify(testUtils.queryTeststore(query));
        expect(updatedResults.length).toBe(3);

        // verify orphan triples are missing
        done();
      });
    });

    describe('cleanUpMeasurements when changing the type of a variable', function() {

      beforeEach(function(done) {
        // load some mock graph with orphan results 
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/test_graphs/cleanupTestVariableTypeChangeDirtyGraph.ttl', false);
        xmlHTTP.send(null);
        var dirtyGraph = xmlHTTP.responseText;

        xmlHTTP.open('PUT', scratchStudyUri + '/data?graph=' + graphUri, false);
        xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
        xmlHTTP.send(dirtyGraph);

        // call cleanup
        resultsService.cleanUpMeasurements().then(done);
        rootScope.$digest();
      });

      it('should have removed the orphan items', function(done) {

        var variableToBeCleaned = 'http://trials.drugis.org/instances/69310a30-308f-4ee4-b70d-d29166acb9f3';

        var query = queryResultsRaw
          .replace(/\$graphUri/g, graphUri)
          .replace(/\$outcomeUri/g, variableToBeCleaned);

        var updatedResults = testUtils.deFusekify(testUtils.queryTeststore(query));
        // console.log(JSON.stringify(updatedResults[0]))
        // verify orphan triples are missing
        expect(updatedResults.length).toBe(1);
        expect(updatedResults[0].result_property).toBe('http://trials.drugis.org/ontology#sample_size');
        expect(updatedResults[0].value).toBe('3');
        done();
      });
    });

    describe('cleanUpMeasurements when changing measuredAt', function() {

      beforeEach(function(done) {
        // load some mock graph with orphan results 
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/test_graphs/cleanupTestMeasuredAtDirtyGraph.ttl', false);
        xmlHTTP.send(null);
        var dirtyGraph = xmlHTTP.responseText;

        xmlHTTP.open('PUT', scratchStudyUri + '/data?graph=' + graphUri, false);
        xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
        xmlHTTP.send(dirtyGraph);

        // call cleanup
        resultsService.cleanUpMeasurements().then(done);
        rootScope.$digest();
      });

      it('should have removed the orphan items', function(done) {

        var query = 'PREFIX instance: <http://trials.drugis.org/instances/> ' +
          ' PREFIX ontology: <http://trials.drugis.org/ontology#> ' +
          '' +
          ' SELECT * WHERE { ' +
          '   GRAPH <' + graphUri + '> { ' +
          '  ?instance ' +
          '     ontology:of_outcome <http://trials.drugis.org/instances/69310a30-308f-4ee4-b70d-d29166acb9f3> ;' +
          '     ontology:of_arm ?armUri ; ' +
          '     ontology:of_moment ?momentUri . ' +
          '     OPTIONAL { ?instance <http://trials.drugis.org/ontology#mean> ?mean . } . ' +
          '     OPTIONAL { ?instance <http://trials.drugis.org/ontology#sample_size> ?sample_size . } . ' +
          '     OPTIONAL { ?instance <http://trials.drugis.org/ontology#standard_deviation> ?standard_deviation . } . ' +
          '    } ' +
          '} ';


        var updatedResults = testUtils.deFusekify(testUtils.queryTeststore(query));

        // verify orphan triples are missing
        expect(updatedResults.length).toBe(1);
        expect(updatedResults[0].mean).toBe('4');
        expect(updatedResults[0].standard_deviation).toBe('5');
        expect(updatedResults[0].sample_size).toBe('6');
        done();
      });
    });


  });
});
'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the outcome service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch';

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var measurementMomentServiceMock;
    var outcomeService;
    var studyService;

    var addPopulationCharacteristicTemplate;
    var setOutcomeResultPropertyTemplate;

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

    beforeEach(module('trialverse.outcome'));


    beforeEach(inject(function($q, $rootScope, $httpBackend, OutcomeService, StudyService, SparqlResource) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      outcomeService = OutcomeService;
      studyService = StudyService;

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load service templates and flush httpBackend
      setOutcomeResultPropertyTemplate = testUtils.loadTemplate('setOutcomeResultProperty.sparql', httpBackend);
      SparqlResource.get('addPopulationCharacteristic.sparql');
      addPopulationCharacteristicTemplate = testUtils.loadTemplate('addPopulationCharacteristic.sparql', httpBackend);
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
      rootScope.$digest();

      // stub remotestoreServiceStub.executeUpdate method
      remotestoreServiceStub.executeUpdate.and.callFake(function(uri, query) {
        query = query.replace(/\$graphUri/g, graphUri);

        // console.log('graphUri = ' + uri);
        // console.log('query = ' + query);

        var result = testUtils.executeUpdateQuery(query);

        var executeUpdateDeferred = q.defer();
        executeUpdateDeferred.resolve();
        return executeUpdateDeferred.promise;
      });

    }));

    describe('setOutcomeProperty', function() {

      it('should add the triples for count and sampleSize when the outcome is dichotomous type outcome', function(done) {

        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);

        // the test item to add 
        var outcome = {
          uri: 'http://trials.drugis.org/instances/my-outcome',
          label: 'outcome label',
          measurementType: 'http://trials.drugis.org/ontology#dichotomous'
        };

        //first add an outcome 
        var addOutcomeQuery = addPopulationCharacteristicTemplate
            .replace(/\$UUID/g, 'my-outcome')
            .replace('$label', outcome.label)
            .replace('$measurementType', outcome.measurementType)
            .replace(/\$graphUri/g, graphUri);
        testUtils.executeUpdateQuery(addOutcomeQuery);

        // call the method to test
        var resultPromise = outcomeService.setOutcomeProperty(outcome);

        // setup verification, ready for digest cycle to kickoff 
        resultPromise.then(function(result) {
          // verify addAdverseEvent query
          var query = 'select ?property where { graph <' + graphUri + '> { <http://trials.drugis.org/instances/my-outcome> <http://trials.drugis.org/ontology#has_result_property> ?property . } }  ';
          var result = testUtils.queryTeststore(query);
          var resultObject = testUtils.deFusekify(result);
          expect(resultObject.length).toEqual(2);
          expect(resultObject[0].property).toEqual('http://trials.drugis.org/ontology#sample_size');
          expect(resultObject[1].property).toEqual('http://trials.drugis.org/ontology#count');

          // do not forget to signal async test is done !
          done();
        });

        // fire in the hole !
        rootScope.$digest();
      });

      it('should add the triples for mean, sd and sampleSize then the outcome is continous type outcome', function(done) {

        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);
        
        // the test item to add 
        var outcome = {
          uri: 'http://trials.drugis.org/instances/my-outcome',
          label: 'outcome label',
          measurementType: 'http://trials.drugis.org/ontology#continuous'
        };

        //first add a outcome 
        var addOutcomeQuery = addPopulationCharacteristicTemplate
            .replace(/\$UUID/g, 'my-outcome')
            .replace('$label', outcome.label)
            .replace('$measurementType', outcome.measurementType)
            .replace(/\$graphUri/g, graphUri);
        testUtils.executeUpdateQuery(addOutcomeQuery);

        // call the method to test
        var resultPromise = outcomeService.setOutcomeProperty(outcome);

        // setup verification, ready for digest cycle to kickoff 
        resultPromise.then(function(result) {
          // verify addAdverseEvent query
          var query = 'select ?property where { graph <' + graphUri + '> { <http://trials.drugis.org/instances/my-outcome> <http://trials.drugis.org/ontology#has_result_property> ?property . } }  ';
          var result = testUtils.queryTeststore(query);
          var resultObject = testUtils.deFusekify(result);
          expect(resultObject.length).toEqual(3);
          expect(resultObject[0].property).toEqual('http://trials.drugis.org/ontology#sample_size');
          expect(resultObject[1].property).toEqual('http://trials.drugis.org/ontology#standard_deviation');
          expect(resultObject[2].property).toEqual('http://trials.drugis.org/ontology#mean');

          // do not forget to signal async test is done !
          done();
        });

        // fire in the hole !
        rootScope.$digest();
      });

    });


  });
});
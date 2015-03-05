'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the activity service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch';

    var mockStudyUuid = 'mockStudyUuid';

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var commentServiceStub;
    var studyService;

    var studyDesignService;

    var setActivityCoordinatesTemplate;

    // mock remote rdf service
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

    beforeEach(module('trialverse.studyDesign'));

    beforeEach(inject(function($q, $rootScope, $httpBackend, StudyDesignService, StudyService, SparqlResource) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      studyService = StudyService;

      studyDesignService = StudyDesignService;

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load service templates and flush httpBackend
      testUtils.loadTemplate('queryActivityCoordinates.sparql', httpBackend);
      setActivityCoordinatesTemplate = testUtils.loadTemplate('setActivityCoordinates.sparql', httpBackend);

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
    }));

    describe('query activity coordinates', function() {

      beforeEach(function(done) {
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/test_graphs/activitiesCoordinatesMockGraph.ttl', false);
        xmlHTTP.send(null);
        var activitiesCoordinatesMockGraph = xmlHTTP.responseText;

        xmlHTTP.open('PUT', scratchStudyUri + '/data?graph=' + graphUri, false);
        xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
        xmlHTTP.send(activitiesCoordinatesMockGraph);

        remotestoreServiceStub.executeQuery.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);

          var result = testUtils.queryTeststore(query);
          console.log('queryResponce ' + result);
          var resultObject = testUtils.deFusekify(result)
          var executeUpdateDeferred = q.defer();
          executeUpdateDeferred.resolve(resultObject);
          return executeUpdateDeferred.promise;
        });

        done();
      });

      it('should return the activity coordinates contained in the study', function(done) {

        studyDesignService.queryItems(mockStudyUuid).then(function(results) {
          expect(results.length).toBe(3);
          expect(results[0].activityUri).toBe('http://trials.drugis.org/instances/activity1Uuid');
          expect(results[0].epochUri).toBe('http://trials.drugis.org/instances/epoch1Uuid');
          expect(results[0].armUri).toBe('http://trials.drugis.org/instances/arm1Uuid');
          done();
        });
        rootScope.$digest();
      });
    });


    describe('set activity coordinates', function() {

      beforeEach(function(done) {
        // stub remotestoreServiceStub.executeQuery method
        remotestoreServiceStub.executeUpdate.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);

          var result = testUtils.executeUpdateQuery(query);
          //console.log('queryResponce ' + result);

          var executeUpdateDeferred = q.defer();
          executeUpdateDeferred.resolve(result);
          return executeUpdateDeferred.promise;
        });

        done();
      });

      it('should return the activities contained in the study', function(done) {

        var coordinates = {
          epochUri: 'http://epochs/uri1',
          armUri: 'http://arms/uri1',
          activityUri: 'http://instances/uri1'
        };

        studyDesignService.setActivityCoordinates(mockStudyUuid, coordinates).then(function() {
          var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?subject ?p ?o } }';
          var result = testUtils.deFusekify(testUtils.queryTeststore(query));

          expect(result.length).toEqual(3);
          done();
        });
        rootScope.$digest();
      });
    });
  });


});
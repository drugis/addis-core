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
          var query = 'SELECT * WHERE { GRAPH <' + graphUri+ '> { ?subject ?p ?o } }';
          var result = testUtils.deFusekify(testUtils.queryTeststore(query));

          expect(result.length).toEqual(3);
          done();
        });
        rootScope.$digest();
      });
    });

  });
});
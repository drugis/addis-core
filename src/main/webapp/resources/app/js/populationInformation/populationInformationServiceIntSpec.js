'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  fdescribe('the population information service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch'; // NB proxied by karma to actual fuseki instance

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var studyService;

    var populationInformationService;


    beforeEach(module('trialverse.populationInformation'));
    beforeEach(function() {
      module('trialverse.util', function($provide) {
        remotestoreServiceStub = testUtils.createRemoteStoreStub();
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
      });
    });


    beforeEach(inject(function($q, $rootScope, $httpBackend, PopulationInformationService, StudyService) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      studyService = StudyService;

      populationInformationService = PopulationInformationService;

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load service templates and flush httpBackend
      testUtils.loadTemplate('queryPopulationInformation.sparql', httpBackend);
      //testUtils.loadTemplate('editPopulationInformation.sparql', httpBackend);

      httpBackend.flush();

      // create and load empty test store
      var createStoreDeferred = $q.defer();
      remotestoreServiceStub.create.and.returnValue(createStoreDeferred.promise);

      var loadStoreDeferred = $q.defer();
      remotestoreServiceStub.load.and.returnValue(loadStoreDeferred.promise);

      studyService.loadStore();
      createStoreDeferred.resolve(scratchStudyUri);
      loadStoreDeferred.resolve();

      rootScope.$digest();
    }));


    describe('query population information', function() {

      var result;

      beforeEach(function(done) {

        testUtils.loadTestGraph('studyWithIndication.ttl', graphUri);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        populationInformationService.queryItems().then(function(info){
          result = info;
          done();
        });
        rootScope.$digest();
      });
 
      it('should return the population information contained in the study', function() {
          expect(result.length).toBe(1);
          expect(result[0].label).toBe("Indication label");
      });

    });

    describe('query population information on study without a indication', function() {

      var result;

      beforeEach(function(done) {

        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        populationInformationService.queryItems().then(function(info){
          result = info;
          done();
        });
        rootScope.$digest();
      });
 
      it('should return the population information contained in the study', function() {
          expect(result.length).toBe(1);
          expect(result[0].label).toBe(undefined);
      });

    });


  });
});
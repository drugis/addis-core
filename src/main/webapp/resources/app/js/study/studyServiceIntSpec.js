'use strict';
define(['angular', 'angular-mocks', 'testUtils'],
 function(angular, angularMocks, testUtils) {
  describe('the study service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch'; // NB proxied by karma to actual fuseki instance

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;

    var studyService;


    beforeEach(function() {
      module('trialverse.util', function($provide) {
        remotestoreServiceStub = testUtils.createRemoteStoreStub();
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
      });
    });

    beforeEach(module('trialverse.study'));

    beforeEach(inject(function($q, $rootScope, $httpBackend, StudyService) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      studyService = StudyService;

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load service templates and flush httpBackend
      testUtils.loadTemplate('createEmptyStudy.sparql', httpBackend);
      testUtils.loadTemplate('queryStudyData.sparql', httpBackend);

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

    describe('query study', function() {


      var queryResult;

      beforeEach(function(done) {
        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        studyService.queryStudyData().then(function(study) {
          queryResult = study
          done();
        });

        rootScope.$digest();
      });

      it('return the study', function() {
        expect(queryResult.studyUri).toEqual('http://trials.drugis.org/studies/study1');
        expect(queryResult.label).toEqual('Study1');
        expect(queryResult.comment).toEqual('Description');
      });
    });


    describe('create empty study', function() {

      var study = {
        label: 'studyLabel',
        comment: 'studyDescription'
      };

      var queryResult;

      beforeEach(function(done) {
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        studyService.createEmptyStudy(study).then(function() {
          // query to verify
          studyService.queryStudyData().then(function(result) {
            queryResult = result
            done();
          });
        });
        rootScope.$digest();
      });

      it('should create a empty study with title and description', function() {
        expect(queryResult.label).toEqual(study.label);
      });
    });

  });
});
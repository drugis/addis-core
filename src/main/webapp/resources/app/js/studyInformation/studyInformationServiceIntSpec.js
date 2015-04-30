'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  fdescribe('the population information service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch'; // NB proxied by karma to actual fuseki instance

    var rootScope, q, httpBackend;
    var remotestoreServiceStub, uUIDServiceStub;
    var studyService;

    var studyInformationService;
    var mockGeneratedUuid = 'newUuid';

    beforeEach(module('trialverse.studyInformation'));
    beforeEach(function() {
      module('trialverse.util', function($provide) {
        remotestoreServiceStub = testUtils.createRemoteStoreStub();
        uUIDServiceStub = jasmine.createSpyObj('UUIDService', [
          'generate'
        ]);
        uUIDServiceStub.generate.and.returnValue(mockGeneratedUuid);
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
        $provide.value('UUIDService', uUIDServiceStub);
      });
    });


    beforeEach(inject(function($q, $rootScope, $httpBackend, StudyInformationService, StudyService) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      studyService = StudyService;

      studyInformationService = StudyInformationService;

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load service templates and flush httpBackend
      testUtils.loadTemplate('queryStudyInformation.sparql', httpBackend);
      testUtils.loadTemplate('editStudyInformation.sparql', httpBackend);

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


    describe('query study information', function() {

      var result;

      beforeEach(function(done) {

        testUtils.loadTestGraph('studyWithStudyInformation.ttl', graphUri);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        studyInformationService.queryItems().then(function(info) {
          result = info;
          done();
        });
        rootScope.$digest();
      });

      it('should return study information', function() {
        expect(result.length).toBe(1);
        expect(result[0].blinding.uri).toBe('http://trials.drugis.org/ontology#SingleBlind');
      });

    });

    describe('edit study information when there is no previous information', function() {

      var newInformation = {
        blinding: {
          uri: 'http://trials.drugis.org/ontology#SingleBlind'
        }
      };
      var studyInformation;

      beforeEach(function(done) {
        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        studyInformationService.editItem(newInformation).then(function() {
          studyInformationService.queryItems().then(function(resultInfo) {
            studyInformation = resultInfo;
            done();
          })
        });
        rootScope.$digest();
      });

      it('should make the new study information accessible', function() {
        expect(studyInformation).toBeDefined();
        expect(studyInformation[0].blinding.uri).toEqual(newInformation.blinding.uri);
      });

    });


  });
});
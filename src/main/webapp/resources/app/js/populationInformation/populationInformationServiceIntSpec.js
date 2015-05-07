'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the population information service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch'; // NB proxied by karma to actual fuseki instance

    var rootScope, q, httpBackend;
    var remotestoreServiceStub, uUIDServiceStub;
    var studyService;

    var populationInformationService;
    var mockGeneratedUuid = 'newUuid';

    beforeEach(module('trialverse.populationInformation'));
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


    beforeEach(inject(function($q, $rootScope, $httpBackend, PopulationInformationService, StudyService) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      studyService = StudyService;

      populationInformationService = PopulationInformationService;

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load study service templates
      testUtils.loadTemplate('createEmptyStudy.sparql', httpBackend);
      testUtils.loadTemplate('queryStudyData.sparql', httpBackend);

      // load service templates and flush httpBackend
      testUtils.loadTemplate('queryPopulationInformation.sparql', httpBackend);
      testUtils.loadTemplate('editPopulationInformation.sparql', httpBackend);

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

        testUtils.loadTestGraph('studyWithPopulationInformation.ttl', graphUri);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        populationInformationService.queryItems().then(function(info) {
          result = info;
          done();
        });
        rootScope.$digest();
      });

      it('should return the population information contained in the study', function() {
        expect(result.length).toBe(1);
        expect(result[0].indication.label).toBe('Indication label');
        expect(result[0].eligibilityCriteria.label).toBe('eligibility criterion');
      });

    });

    describe('query population information on study without a indication', function() {

      var result;

      beforeEach(function(done) {

        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        populationInformationService.queryItems().then(function(info) {
          result = info;
          done();
        });
        rootScope.$digest();
      });

      it('should return the population information contained in the study', function() {
        expect(result.length).toBe(1);
        expect(result[0].indication.label).toBe(undefined);
      });

    });

    describe('edit population information when there is no previous information', function() {

      var newInformation = {
        indication: {
          label: 'new label'
        },
        eligibilityCriteria: {
          label: 'eligibility label'
        }
      };
      var populationInformation;

      beforeEach(function(done) {
        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        populationInformationService.editItem(newInformation).then(function() {
          populationInformationService.queryItems().then(function(resultInfo) {
            populationInformation = resultInfo;
            done();
          })
        });
        rootScope.$digest();
      });

      it('should make the new population information accessible', function() {
        expect(populationInformation).toBeDefined();
        expect(populationInformation[0].indication.label).toEqual(newInformation.indication.label);
        expect(populationInformation[0].indication.uri).toBeDefined();
        expect(populationInformation[0].indication.uri).toEqual(populationInformationService.INSTANCE_PREFIX + mockGeneratedUuid);
        expect(populationInformation[0].eligibilityCriteria.label).toEqual(newInformation.eligibilityCriteria.label);
      });

    });


  });
});
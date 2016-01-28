'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the population information service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch'; // NB proxied by karma to actual fuseki instance

    var rootScope, q, httpBackend;
    var remotestoreServiceStub, uUIDServiceStub;
    var studyService;

    var studyInformationService;
    var mockGeneratedUuid = 'newUuid';

    beforeEach(module('trialverse'));
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

      // load study service templates
      testUtils.loadTemplate('createEmptyStudy.sparql', httpBackend);
      testUtils.loadTemplate('queryStudyData.sparql', httpBackend);

      // load service templates and flush httpBackend
      testUtils.loadTemplate('queryStudyInformation.sparql', httpBackend);
      testUtils.loadTemplate('editBlinding.sparql', httpBackend);
      testUtils.loadTemplate('deleteBlinding.sparql', httpBackend);
      testUtils.loadTemplate('editGroupAllocation.sparql', httpBackend);
      testUtils.loadTemplate('deleteGroupAllocation.sparql', httpBackend);
      testUtils.loadTemplate('editStatus.sparql', httpBackend);
      testUtils.loadTemplate('deleteStatus.sparql', httpBackend);
      testUtils.loadTemplate('editNumberOfCenters.sparql', httpBackend);
      testUtils.loadTemplate('deleteNumberOfCenters.sparql', httpBackend);
      testUtils.loadTemplate('editObjective.sparql', httpBackend);
      testUtils.loadTemplate('deleteObjective.sparql', httpBackend);

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
        expect(result[0].groupAllocation.uri).toBe('http://trials.drugis.org/ontology#AllocationRandomized');
        expect(result[0].status.uri).toBe('http://trials.drugis.org/ontology#StatusWithdrawn');
        expect(result[0].numberOfCenters).toBe(37);
        expect(result[0].objective).toBe('objective');
      });

    });

    describe('edit study information when there is no previous information', function() {

      var newInformation = {
        groupAllocation: {
          uri: 'http://trials.drugis.org/ontology#AllocationRandomized'
        },
        blinding: {
          uri: 'http://trials.drugis.org/ontology#SingleBlind'
        },
        status: {
          uri: 'http://trials.drugis.org/ontology#Completed'
        },
        numberOfCenters: 29,
        objective: 'new study objective'
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
          });
        });
        rootScope.$digest();
      });

      it('should make the new study information accessible', function() {
        expect(studyInformation).toBeDefined();
        expect(studyInformation[0].blinding.uri).toEqual(newInformation.blinding.uri);
        expect(studyInformation[0].groupAllocation.uri).toEqual(newInformation.groupAllocation.uri);
        expect(studyInformation[0].status.uri).toEqual(newInformation.status.uri);
        expect(studyInformation[0].numberOfCenters).toEqual(29);
        expect(studyInformation[0].objective).toBe(newInformation.objective);
      });

    });

    describe('edit study information with when there are previous results', function() {
      var result;
      var newInformation = {
        groupAllocation: {
          uri: 'http://trials.drugis.org/ontology#AllocationNonRandomized'
        },
        blinding: {
          uri: 'http://trials.drugis.org/ontology#DoubleBlind'
        },
        status: {
          uri: 'http://trials.drugis.org/ontology#StatusSuspended'
        },
        numberOfCenters: 28,
        objective: 'new study objective'
      };


      beforeEach(function(done) {

        testUtils.loadTestGraph('studyWithStudyInformation.ttl', graphUri);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);

        studyInformationService.editItem(newInformation).then(function() {
          studyInformationService.queryItems().then(function(resultInfo) {
            result = resultInfo;
            done();
          });
        });
        rootScope.$digest();
      });

      it('should overwrite previously selected values', function() {
        expect(result.length).toBe(1);
        expect(result[0].blinding.uri).toBe(newInformation.blinding.uri);
        expect(result[0].groupAllocation.uri).toBe(newInformation.groupAllocation.uri);
        expect(result[0].status.uri).toBe(newInformation.status.uri);
        expect(result[0].numberOfCenters).toBe(newInformation.numberOfCenters);
        expect(result[0].objective).toBe(newInformation.objective);
      });
    });

    describe('edit study information with "unknown" values in selects', function() {
      var result;
      var newInformation = {
        blinding: {
          uri: 'unknown'
        },
        groupAllocation: {
          uri: 'unknown'
        },
        status: {
          uri: 'unknown'
        }
      };

      beforeEach(function(done) {

        testUtils.loadTestGraph('studyWithStudyInformation.ttl', graphUri);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);

        studyInformationService.editItem(newInformation).then(function() {
          studyInformationService.queryItems().then(function(resultInfo) {
            result = resultInfo;
            done();
          });
        });
        rootScope.$digest();
      });

      it('should delete previously selected values', function() {
        expect(result.length).toBe(1);
        expect(result[0].blinding.uri).not.toBeDefined();
        expect(result[0].groupAllocation.uri).not.toBeDefined();
        expect(result[0].status.uri).not.toBeDefined();
      });
    });


  });
});

'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the arm service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch'; // NB proxied by karma to actual fuseki instance

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var studyService;
    var armService;

    beforeEach(function() {
      module('trialverse.util', function($provide) {
        remotestoreServiceStub = testUtils.createRemoteStoreStub();
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
      });
    });

    beforeEach(module('trialverse.activity'));

    beforeEach(inject(function($q, $rootScope, $httpBackend, ArmService, StudyService) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      studyService = StudyService;

      armService = ArmService;

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load study service templates
      testUtils.loadTemplate('createEmptyStudy.sparql', httpBackend);
      testUtils.loadTemplate('queryStudyData.sparql', httpBackend);

      // load service templates and flush httpBackend
      testUtils.loadTemplate('queryArm.sparql', httpBackend);
      testUtils.loadTemplate('addArmQuery.sparql', httpBackend);
      testUtils.loadTemplate('addArmCommentQuery.sparql', httpBackend);
      testUtils.loadTemplate('editArmWithComment.sparql', httpBackend);
      testUtils.loadTemplate('editArmWithoutComment.sparql', httpBackend);
      testUtils.loadTemplate('deleteSubject.sparql', httpBackend);
      testUtils.loadTemplate('deleteHasArm.sparql', httpBackend);

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

    describe('query arms', function() {
      beforeEach(function() {
        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);
        testUtils.loadTestGraph('testArmGraph.ttl', graphUri);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);
      });

      it('should query the arms', function(done) {
        armService.queryItems().then(function(result) {
          expect(result.length).toBe(1);
          expect(result[0].label).toEqual('arm label');
          done();
        });
        rootScope.$digest();
      });

    });

    describe('addItem', function() {
      var studyUuid = 'studyUuid';
      var newArm = {
        label: 'test label'
      };
      var armsResult;
      beforeEach(function(done) {
        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        armService.addItem(newArm).then(function() {
          armService.queryItems().then(function(result){
            armsResult = result;
            done();  
          })
          
        });
        rootScope.$digest();
      });

      it('should add the arm to the graph', function() {
        expect(armsResult.length).toBe(1);
      });
    });

    describe('edit arm', function() {
      var newArm = {
        label: 'test label'
      };
      var editedArm = {
        label: 'edited label'
      };

      var editResult;

      beforeEach(function(done) {
        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        armService.addItem(newArm).then(function() {
          armService.queryItems().then(function(result) {
            result[0].label = editedArm.label;
            armService.editItem(result[0]).then(function(){
              armService.queryItems().then(function(result) {
                editResult = result;
                done();
              });
            });
          })
        })
        rootScope.$digest();
      });

      it('should edit the arm', function() {
        expect(editResult.length).toBe(1);
        expect(editResult[0].label).toBe(editedArm.label);
      });
    });

    describe('delete arm', function() {
      var newArm = {
        label: 'test label'
      };
      var editedArm = {
        label: 'edited label'
      };

      beforeEach(function(done) {
        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        armService.addItem(newArm).then(function() {
          armService.queryItems().then(function(result) {
            armService.deleteItem(result[0]).then(done);
          });
        });
        rootScope.$digest();
      });

      it('should delete the arm', function(done) {
        armService.queryItems().then(function(result) {
          expect(result.length).toBe(0);
          done();
        });
      });
    });


  });
});
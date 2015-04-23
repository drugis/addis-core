'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  fdescribe('the arm service', function() {

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

      // load service templates and flush httpBackend
      testUtils.loadTemplate('queryArm.sparql', httpBackend);
      testUtils.loadTemplate('addArmQuery.sparql', httpBackend);
      testUtils.loadTemplate('addArmCommentQuery.sparql', httpBackend);
      testUtils.loadTemplate('editArmWithComment.sparql', httpBackend);
      testUtils.loadTemplate('editArmWithoutComment.sparql', httpBackend);
      testUtils.loadTemplate('deleteArm.sparql', httpBackend);
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
      var studyUuid = 'studyUuid';
      beforeEach(function() {
        testUtils.loadTestGraph('testArmGraph.ttl', graphUri);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);
      });

      it('should query the arms', function(done) {
        armService.queryItems(studyUuid).then(function(result) {
          expect(result.length).toBe(1);
          expect(result[0].label).toEqual('arm label');
          done();
        });
        rootScope.$digest();
      });

    });

    describe('add arm', function() {
      var studyUuid = 'studyUuid';
      var newArm = {
        label: 'test label'
      };
      beforeEach(function(done) {

        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        armService.addItem(newArm, studyUuid).then(function() {
          done();
        });
        rootScope.$digest();
      });

      it('should add the arm to the graph', function(done) {
        // call function under test
        var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s ?p ?o }}';
        var result = testUtils.queryTeststore(query);
        var resultTriples = testUtils.deFusekify(result);

        expect(resultTriples.length).toBe(3);

        var hasArmQuery = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s <http://trials.drugis.org/ontology#has_arm> ?o }}';
        result = testUtils.queryTeststore(hasArmQuery);
        var hasArmTriples = testUtils.deFusekify(result);

        expect(hasArmTriples.length).toBe(1);
        expect(hasArmTriples[0].s).toEqual('http://trials.drugis.org/studies/studyUuid');
        expect(hasArmTriples[0].o).toContain('http://trials.drugis.org/instances/');

        var isArmQuery = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s a ?o }}';
        result = testUtils.queryTeststore(isArmQuery);
        var isArmTriples = testUtils.deFusekify(result);

        expect(isArmTriples.length).toBe(1);
        expect(isArmTriples[0].s).toContain('http://trials.drugis.org/instances/');
        expect(isArmTriples[0].o).toEqual('http://trials.drugis.org/ontology#Arm');

        var hasLabelQuery = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?o }}';
        result = testUtils.queryTeststore(hasLabelQuery);
        var hasLabelTriples = testUtils.deFusekify(result);

        expect(hasLabelTriples.length).toBe(1);
        expect(hasLabelTriples[0].s).toContain('http://trials.drugis.org/instances/');
        expect(hasLabelTriples[0].o).toEqual(newArm.label);
        armService.queryItems(studyUuid).then(function(result) {
          expect(result.length).toBe(1);
          expect(result[0].label).toEqual(newArm.label);
          done();
        });

      });
    });

    describe('edit arm', function() {
      var studyUuid = 'studyUuid';
      var newArm = {
        label: 'test label'
      };
      var editedArm = {
        label: 'edited label'
      };

      beforeEach(function(done) {
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        armService.addItem(newArm, studyUuid).then(function() {
          armService.queryItems(studyUuid).then(function(result) {
            result[0].label = editedArm.label;
            armService.editItem(result[0]).then(done);
          })
        })
        rootScope.$digest();
      });

      it('should edit the arm', function() {
        var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s ?p ?o }}';
        var result = testUtils.queryTeststore(query);
        var resultTriples = testUtils.deFusekify(result);

        expect(resultTriples.length).toBe(3);
        var hasLabelQuery = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?o }}';
        result = testUtils.queryTeststore(hasLabelQuery);
        var hasLabelTriples = testUtils.deFusekify(result);

        expect(hasLabelTriples.length).toBe(1);
        expect(hasLabelTriples[0].s).toContain('http://trials.drugis.org/instances/');
        expect(hasLabelTriples[0].o).toEqual(editedArm.label);

      });
    });

    describe('delete arm', function() {
      var studyUuid = 'studyUuid';
      var newArm = {
        label: 'test label'
      };
      var editedArm = {
        label: 'edited label'
      };

      beforeEach(function(done) {
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        armService.addItem(newArm, studyUuid).then(function() {
          armService.queryItems(studyUuid).then(function(result) {
            armService.deleteItem(result[0]).then(done);
          });
        });
        rootScope.$digest();
      });

      it('should delete the arm', function(done) {
        armService.queryItems(studyUuid).then(function(result) {
          expect(result.length).toBe(0);
          done();
        });
      });
    });


  });
});
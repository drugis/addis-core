'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the mapping service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch'; // NB proxied by karma to actual fuseki instance

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var studyService;
    var mappingService;

    beforeEach(function() {
      module('trialverse.util', function($provide) {
        remotestoreServiceStub = testUtils.createRemoteStoreStub();
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
      });
    });

    beforeEach(module('trialverse.mapping'));

    beforeEach(inject(function($q, $rootScope, $httpBackend, MappingService, StudyService) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      studyService = StudyService;

      mappingService = MappingService;

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load study service templates
      testUtils.loadTemplate('createEmptyStudy.sparql', httpBackend);
      testUtils.loadTemplate('queryStudyData.sparql', httpBackend);

      // load service templates and flush httpBackend
      testUtils.loadTemplate('setDrugMapping.sparql', httpBackend);
      testUtils.loadTemplate('setVariableMapping.sparql', httpBackend);
      testUtils.loadTemplate('removeDrugMapping.sparql', httpBackend);
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


    describe('set drug mapping where none existed', function() {

      var studyConcept = {
          uri: 'http://testuri/1'
        },
        datasetConcept = {
          uri: 'http://testuri/2',
          type: 'http://trials.drugis.org/ontology#Drug'
        };
      beforeEach(function(done) {

        // stub remotestoreServiceStub.executeQuery method
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);

        mappingService.updateMapping(studyConcept, datasetConcept).then(function() {
          done();
        });
        rootScope.$digest();

      });

      it('should add the new mapping to the graph', function(done) {

        // call function under test
        var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s ?p ?o }}';
        var result = testUtils.queryTeststore(query);
        var resultTriples = testUtils.deFusekify(result);

        // verify results
        expect(resultTriples.length).toBe(1);

        expect(resultTriples[0].s).toEqual(studyConcept.uri);
        expect(resultTriples[0].p).toEqual('http://www.w3.org/2002/07/owl#sameAs');
        expect(resultTriples[0].o).toEqual(datasetConcept.uri);

        done();
      });
    });

    describe('set drug mapping where one existed', function() {

      var studyConcept = {
          uri: 'http://testuri/1'
        },
        datasetConcept1 = {
          uri: 'http://testuri/dataset/1',
          type: 'http://trials.drugis.org/ontology#Drug'

        },
        datasetConcept2 = {
          uri: 'http://testuri/dataset/2',
          type: 'http://trials.drugis.org/ontology#Drug'
        };

      beforeEach(function(done) {

        // stub remotestoreServiceStub.executeQuery method
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);

        mappingService.updateMapping(studyConcept, datasetConcept1).then(function() {
          mappingService.updateMapping(studyConcept, datasetConcept2).then(function() {
            done();
          });
        });
        rootScope.$digest();

      });

      it('should overwrite the old mapping with the new one', function(done) {

        // call function under test
        var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s ?p ?o }}';
        var result = testUtils.queryTeststore(query);
        var resultTriples = testUtils.deFusekify(result);

        // verify results
        expect(resultTriples.length).toBe(1);

        expect(resultTriples[0].s).toEqual(studyConcept.uri);
        expect(resultTriples[0].p).toEqual('http://www.w3.org/2002/07/owl#sameAs');
        expect(resultTriples[0].o).toEqual(datasetConcept2.uri);

        done();
      });
    });

    describe('remove drug mapping', function() {

      var studyConcept = {
          uri: 'http://testuri/1',
          type: 'http://trials.drugis.org/ontology#Drug'
        },
        datasetConcept = {
          uri: 'http://testuri/dataset/1',
          type: 'http://trials.drugis.org/ontology#Drug'
        };

      beforeEach(function(done) {

        // stub remotestoreServiceStub.executeQuery method
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);

        mappingService.updateMapping(studyConcept, datasetConcept).then(function() {
          mappingService.removeMapping(studyConcept, datasetConcept).then(function() {
            done();
          });
        });
        rootScope.$digest();

      });

      it('should remove the old mapping', function(done) {

        // call function under test
        var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s ?p ?o }}';
        var result = testUtils.queryTeststore(query);
        var resultTriples = testUtils.deFusekify(result);

        // verify results
        expect(resultTriples.length).toBe(0);
        done();
      });
    });

    describe('set variable mapping where none existed', function() {
      var studyConcept = {
          uri: 'http://trials.drugis.org/instances/instance1'
        },
        datasetConcept = {
          uri: 'http://trials.drugis.org/entities/entities1',
          type: 'http://trials.drugis.org/ontology#AdverseEvent'
        };
      beforeEach(function(done) {
        testUtils.loadTestGraph('mappingsTestGraph.ttl', graphUri);

        // stub remotestoreServiceStub.executeQuery method
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);

        mappingService.updateMapping(studyConcept, datasetConcept).then(function() {
          done();
        });
        rootScope.$digest();

      });

      it('should add the new variable mapping to the graph', function(done) {

        // call function under test
        var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s ?p ?o }}';
        var result = testUtils.queryTeststore(query);
        var resultTriples = testUtils.deFusekify(result);

        // verify results
        expect(resultTriples.length).toBe(4);

        var sameAsTriple = _.find(resultTriples, function(triple) {
          return triple.s === 'b0' && triple.p === 'http://www.w3.org/2002/07/owl#sameAs';
        });

        expect(sameAsTriple.o).toEqual(datasetConcept.uri);

        done();
      });
    });

    describe('set variable mapping where one existed', function() {
      var studyConcept = {
          uri: 'http://trials.drugis.org/instances/instance1'
        },
        datasetConcept1 = {
          uri: 'http://trials.drugis.org/entities/entities1',
          type: 'http://trials.drugis.org/ontology#AdverseEvent'
        },
        datasetConcept2 = {
          uri: 'http://trials.drugis.org/entities/entities2',
          type: 'http://trials.drugis.org/ontology#AdverseEvent'
        };
      beforeEach(function(done) {
        testUtils.loadTestGraph('mappingsTestGraph.ttl', graphUri);

        // stub remotestoreServiceStub.executeQuery method
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);

        // first set one mapping then replace it
        mappingService.updateMapping(studyConcept, datasetConcept1).then(function() {
          mappingService.updateMapping(studyConcept, datasetConcept2).then(function() {
            done();
          });
        });
        rootScope.$digest();

      });

      it('should overwrite the old variable mapping to the graph', function(done) {

        // call function under test
        var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s ?p ?o }}';
        var result = testUtils.queryTeststore(query);
        var resultTriples = testUtils.deFusekify(result);

        // verify results
        expect(resultTriples.length).toBe(4);

        var sameAsTriple = _.find(resultTriples, function(triple) {
          return triple.s === 'b0' && triple.p === 'http://www.w3.org/2002/07/owl#sameAs';
        });

        expect(sameAsTriple.s).toEqual('b0');
        expect(sameAsTriple.o).toEqual(datasetConcept2.uri);

        done();
      });
    });

    describe('remove variable mapping', function() {
      var studyConcept = {
          uri: 'http://trials.drugis.org/instances/instance1'
        },
        datasetConcept = {
          uri: 'http://trials.drugis.org/entities/entities1',
          type: 'http://trials.drugis.org/ontology#AdverseEvent'
        };
      beforeEach(function(done) {
        testUtils.loadTestGraph('mappingsTestGraph.ttl', graphUri);

        // stub remotestoreServiceStub.executeQuery method
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);

        mappingService.updateMapping(studyConcept, datasetConcept).then(function() {
          mappingService.removeMapping(studyConcept, datasetConcept).then(function() {
            done();
          });
        });
        rootScope.$digest();

      });

      it('should remove the variable mapping', function(done) {

        // call function under test
        var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s ?p ?o }}';
        var result = testUtils.queryTeststore(query);
        var resultTriples = testUtils.deFusekify(result);

        // verify results
        expect(resultTriples.length).toBe(3);

        done();
      });
    });

  });
});

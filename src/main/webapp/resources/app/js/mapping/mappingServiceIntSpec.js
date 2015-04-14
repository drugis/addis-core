'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the mapping service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch'; // NB proxied by karma to actual fuseki instance

    var mockStudyUuid = 'mockStudyUuid';

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

      // load service templates and flush httpBackend
      testUtils.loadTemplate('setDrugMapping.sparql', httpBackend);
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


    fdescribe('set drug mapping where none existed', function() {

      var studyConcept = {
          uri: 'http://testuri/1'
        },
        datasetConcept = {
          uri: 'http://testuri/2'
        };
      beforeEach(function(done) {

        // stub remotestoreServiceStub.executeQuery method
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);

        mappingService.updateMapping(studyConcept, datasetConcept).then(function() {
          console.log('mapping updated')
          done()
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

    fdescribe('set drug mapping where one existed', function() {

      var studyConcept = {
          uri: 'http://testuri/1'
        },
        datasetConcept1 = {
          uri: 'http://testuri/dataset/1'
        },
        datasetConcept2 = {
          uri: 'http://testuri/dataset/2'
        };

      beforeEach(function(done) {

        // stub remotestoreServiceStub.executeQuery method
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);

        mappingService.updateMapping(studyConcept, datasetConcept1).then(function() {
          mappingService.updateMapping(studyConcept, datasetConcept2).then(function() {
            console.log('mapping updated')
            done()
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


  });
});
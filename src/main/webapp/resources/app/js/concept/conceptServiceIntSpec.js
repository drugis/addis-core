'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  xdescribe('concept service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStoreUri = 'http://localhost:9876/scratch'; // NB proxied by karma to actual fuseki instance

    var scope, httpBackend, q,
      remoteRdfStoreServiceMock, conceptService,
      loadDefer, createDefer;

    beforeEach(module('trialverse', function($provide) {
      remoteRdfStoreServiceMock = testUtils.createRemoteStoreStub();

      $provide.value('RemoteRdfStoreService', remoteRdfStoreServiceMock);
    }));

    beforeEach(module('trialverse.concept'));

    function loadMockGraph() {
      // load mock concepts graph
      var xmlHTTP = new XMLHttpRequest();
      xmlHTTP.open('GET', 'base/test_graphs/conceptsQueryMockGraph.ttl', false);
      xmlHTTP.send(null);
      var conceptsQueryMockGraph = xmlHTTP.responseText;

      xmlHTTP.open('PUT', scratchStoreUri + '/data?graph=' + graphUri, false);
      xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
      xmlHTTP.send(conceptsQueryMockGraph);

    }

    beforeEach(inject(function($rootScope, $q, $httpBackend, ConceptService) {
      scope = $rootScope;
      q = $q;
      httpBackend = $httpBackend;
      conceptService = ConceptService;

      // reset the test graph
      testUtils.dropGraph(graphUri);

      testUtils.loadTemplate('queryConcepts.sparql', httpBackend);
      testUtils.loadTemplate('addConcept.sparql', httpBackend);

      httpBackend.flush();

      // create and load empty test store
      loadDefer = $q.defer();
      remoteRdfStoreServiceMock.load.and.returnValue(loadDefer.promise);
      createDefer = $q.defer();
      remoteRdfStoreServiceMock.create.and.returnValue(createDefer.promise);

      conceptService.loadStore();
      createDefer.resolve(scratchStoreUri);
      loadDefer.resolve();

      scope.$digest();
    }));

    beforeEach(function(done) {
      loadMockGraph();
      testUtils.remoteStoreStubQuery(remoteRdfStoreServiceMock, graphUri, q);
      done();
    });

    describe('queryItems', function() {

      it('should retrieve the concepts', function(done) {
        conceptService.queryItems().then(function(result) {
          var concepts = result;
          expect(concepts.length).toBe(3);
          expect(concepts[0].label).toBe('endpoint');
          expect(concepts[1].label).toBe('conc 2 the druggening');
          expect(concepts[2].label).toBe('conc 1');
          expect(concepts[0].type).toBe('http://trials.drugis.org/ontology#Variable');
          expect(concepts[1].type).toBe('http://trials.drugis.org/ontology#Drug');
          expect(concepts[2].type).toBe('http://trials.drugis.org/ontology#Drug');
          done();
        });
        scope.$digest();
      });
    });

    describe('addItem', function() {
      it('should add the new concept to the graph', function(done) {
        var newConcept = {
          title: 'added concept',
          type: {
            uri: 'http://trials.drugis.org/ontology#AdverseEvent'
          }
        };
        testUtils.remoteStoreStubUpdate(remoteRdfStoreServiceMock, graphUri, q);
        conceptService.addItem(newConcept).then(function() {
          conceptService.queryItems().then(function(result) {
            var concepts = result;
            expect(concepts.length).toBe(4);
            expect(concepts[1].label).toEqual(newConcept.title);
            expect(concepts[1].type).toEqual(newConcept.type.uri);
            done();
          });
        });
        scope.$digest();
      });
    })
  });
});

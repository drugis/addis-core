'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the activity comment service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch';

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var studyService;

    var commentService;
    var addCommentTemplate;

    beforeEach(function() {
      module('trialverse.util', function($provide) {
        remotestoreServiceStub = jasmine.createSpyObj('RemoteRdfStoreService', [
          'create',
          'load',
          'executeUpdate',
          'executeQuery',
          'getGraph',
          'deFusekify'
        ]);
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
      });
    });

    beforeEach(module('trialverse.util'));
    beforeEach(module('trialverse.study'));

    beforeEach(inject(function($q, $rootScope, $httpBackend, CommentService, StudyService, SparqlResource) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      studyService = StudyService;

      commentService = CommentService;

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load service templates and flush httpBackend
      addCommentTemplate = testUtils.loadTemplate('addComment.sparql', httpBackend);

      httpBackend.flush();

      // create and load empty test store
      var createStoreDeferred = $q.defer();
      var createStorePromise = createStoreDeferred.promise;
      remotestoreServiceStub.create.and.returnValue(createStorePromise);

      var loadStoreDeferred = $q.defer();
      var loadStorePromise = loadStoreDeferred.promise;
      remotestoreServiceStub.load.and.returnValue(loadStorePromise);

      studyService.loadStore();
      createStoreDeferred.resolve(scratchStudyUri);
      loadStoreDeferred.resolve();

      rootScope.$digest();
    }));


    describe('addComment', function() {

          beforeEach(function(done) {
            remotestoreServiceStub.executeUpdate.and.callFake(function(uri, query) {
              query = query.replace(/\$graphUri/g, graphUri);

              var result = testUtils.executeUpdateQuery(query);
              console.log('queryResponce ' + result);

              var executeUpdateDeferred = q.defer();
              executeUpdateDeferred.resolve(result);
              return executeUpdateDeferred.promise;
            });

            commentService.addComment('instanceUuid', 'my comment').then(function(result){
               done();
            });

            rootScope.$digest();
          });

          it('should add the comment to the graph', function(done) {

            var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s ?p ?o }}';
            var result = testUtils.queryTeststore(query);
            var resultTriples = testUtils.deFusekify(result);

            expect(resultTriples.length).toBe(1);
            expect(resultTriples[0].s).toBeDefined('http://trials.drugis.org/instances/instanceUuid');
            expect(resultTriples[0].p).toEqual('http://www.w3.org/2000/01/rdf-schema#comment');
            expect(resultTriples[0].o).toBeDefined('my comment');

            done();
          });
        });


  });
});
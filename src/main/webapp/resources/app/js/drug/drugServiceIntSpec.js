'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the drug service service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch';

    var mockStudyUuid = 'mockStudyUuid';

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var commentServiceStub;
    var studyService;

    var drugService;
    var queryDrugTemplate;


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
        commentServiceStub = jasmine.createSpyObj('CommentService', [
          'addComment'
        ]);
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
        $provide.value('CommentService', commentServiceStub);
      });
    });

    beforeEach(module('trialverse.activity'));

    beforeEach(inject(function($q, $rootScope, $httpBackend, DrugService, StudyService, SparqlResource) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      studyService = StudyService;
      drugService = DrugService;

      testUtils.dropGraph(graphUri);

      queryDrugTemplate = testUtils.loadTemplate('queryDrug.sparql', httpBackend);

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


    describe('query drugs', function() {

      beforeEach(function(done) {
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/test_graphs/drugsQueryMockGraph.ttl', false);
        xmlHTTP.send(null);
        var drugsQueryMockGraph = xmlHTTP.responseText;

        xmlHTTP.open('PUT', scratchStudyUri + '/data?graph=' + graphUri, false);
        xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
        xmlHTTP.send(drugsQueryMockGraph);

        // stub remotestoreServiceStub.executeQuery method
        remotestoreServiceStub.executeQuery.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);

          // console.log('graphUri = ' + uri);
          // console.log('query = ' + query);

          var result = testUtils.queryTeststore(query);
          // console.log('queryResponce ' + result);
          var resultObject = testUtils.deFusekify(result)

          var executeUpdateDeferred = q.defer();
          executeUpdateDeferred.resolve(resultObject);
          return executeUpdateDeferred.promise;
        });

        done();
      });

      it('should return the drugs contained in the graph', function(done) {

        // call function under test
        drugService.queryItems(mockStudyUuid).then(function(result){
          var drugs = result;

          // verify query result
          expect(drugs.length).toBe(2);
          expect(drugs[0].label).toEqual('Sertraline');
          expect(drugs[1].label).toEqual('Bupropion');
          expect(drugs[1].conceptMapping).toEqual('http://some/dataset/concepts/buproprionEntity');
          done();
        });
        rootScope.$digest();
      });
    });



  });
});

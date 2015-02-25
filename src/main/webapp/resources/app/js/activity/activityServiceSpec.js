'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the activity service service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch';

    var mockStudyUuid = 'mockStudyUuid';

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var studyService;

    var activityService;
    var queryActivityTemplate;


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

    beforeEach(module('trialverse.activity'));

    beforeEach(inject(function($q, $rootScope, $httpBackend, ActivityService, StudyService, SparqlResource) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      studyService = StudyService;

      activityService = ActivityService;

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load service templates and flush httpBackend
      queryActivityTemplate = testUtils.loadTemplate('queryActivity.sparql', httpBackend);

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


    describe('query activities', function() {

      beforeEach(function(done) {
        // load some mock graph with activities
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/test_graphs/activitiesQueryMockGraph.ttl', false);
        xmlHTTP.send(null);
        var activitiesQueryMockGraph = xmlHTTP.responseText;

        xmlHTTP.open('PUT', scratchStudyUri + '/data?graph=' + graphUri, false);
        xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
        xmlHTTP.send(activitiesQueryMockGraph);

        // stub remotestoreServiceStub.executeQuery method
        remotestoreServiceStub.executeQuery.and.callFake(function(uri, query) {
        query = query.replace(/\$graphUri/g, graphUri);

        //console.log('graphUri = ' + uri);
        //console.log('query = ' + query);

        var result = testUtils.queryTeststore(query);
        console.log('queryResponce ' + result);
        var resultObject = testUtils.deFusekify(result)

        var executeUpdateDeferred = q.defer();
        executeUpdateDeferred.resolve(resultObject);
        return executeUpdateDeferred.promise;
      });

        done();
      });

      it('should return the activities contained in the study', function(done) {

        // call function under test
        activityService.queryItems(mockStudyUuid).then(function(result){
          var activities = result;

          // verify query result
          expect(activities.length).toBe(2);
          expect(activities[0].label).toEqual('activity 1');
          expect(activities[1].label).toEqual('activity 2');
          expect(activities[0].activityType).toEqual('http://trials.drugis.org/ontology#RandomizationActivity');
          expect(activities[1].activityType).toEqual('http://trials.drugis.org/ontology#WashOutActivity');
          expect(activities[1].activityDescription).toEqual('activity description');
          done();
        });
        rootScope.$digest();



      });
    });


  });
});
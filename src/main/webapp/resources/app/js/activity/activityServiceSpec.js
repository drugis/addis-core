'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the activity service service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch';

    var mockStudyUuid = 'mockStudyUuid';

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var commentServiceStub;
    var studyService;

    var activityService;
    var queryActivityTemplate;
    var addActivityTemplate;
    var editActivityTemplate;
    var deleteActivityTemplate;


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
      addActivityTemplate =  testUtils.loadTemplate('addActivity.sparql', httpBackend);
      editActivityTemplate =  testUtils.loadTemplate('editActivity.sparql', httpBackend);
      deleteActivityTemplate =  testUtils.loadTemplate('deleteActivity.sparql', httpBackend);


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
          expect(activities[0].activityType).toEqual(activityService.ACTIVITY_TYPE_OPTIONS['http://trials.drugis.org/ontology#RandomizationActivity']);
          expect(activities[1].activityType).toEqual(activityService.ACTIVITY_TYPE_OPTIONS['http://trials.drugis.org/ontology#WashOutActivity']);
          expect(activities[1].activityDescription).toEqual('activity description');
          done();
        });
        rootScope.$digest();
      });
    });

    describe('add activity', function() {

          beforeEach(function(done) {
            remotestoreServiceStub.executeUpdate.and.callFake(function(uri, query) {
              query = query.replace(/\$graphUri/g, graphUri);

              var result = testUtils.executeUpdateQuery(query);
              //console.log('queryResponce ' + result);

              var executeUpdateDeferred = q.defer();
              executeUpdateDeferred.resolve(result);
              return executeUpdateDeferred.promise;
            });

            var newActivity = {
              activityUri: 'http://trials.drugis.org/instances/newActivityUuid',
              label: 'newActivityLabel',
              activityType: {
                uri: 'http://mockActivityUri'
              },
              activityDescription: 'some description'
            };

            activityService.addItem(mockStudyUuid, newActivity).then(function(result){
               done();
            });

            rootScope.$digest();
          });

          it('should add the new activity to the graph', function(done) {

            // call function under test
            var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s ?p ?o }}';
            var result = testUtils.queryTeststore(query);
            var resultTriples = testUtils.deFusekify(result);

            expect(commentServiceStub.addComment).toHaveBeenCalled();

            // verify results
            expect(resultTriples.length).toBe(3);
            var hasActivityTriple = _.find(resultTriples, function(item){
              return item.s === 'http://trials.drugis.org/studies/mockStudyUuid';
            });
            expect(hasActivityTriple.s).toBeDefined();
            expect(hasActivityTriple.p).toEqual('http://trials.drugis.org/ontology#has_activity');
            expect(hasActivityTriple.o).toBeDefined();

            var activityLabelTriple = _.find(resultTriples, function(item){
              return item.p ===  'http://www.w3.org/2000/01/rdf-schema#label';
            });
            expect(activityLabelTriple.s).toBeDefined();
            expect(activityLabelTriple.p).toBeDefined();
            expect(activityLabelTriple.o).toEqual('newActivityLabel');

            var activityTypeTriple = _.find(resultTriples, function(item){
              return item.p ===  'http://www.w3.org/1999/02/22-rdf-syntax-ns#type';
            });
            expect(activityTypeTriple.s).toBeDefined();
            expect(activityTypeTriple.p).toBeDefined();
            expect(activityTypeTriple.o).toEqual('http://mockActivityUri');

            done();
          });
        });

    describe('edit activities', function() {

      beforeEach(function(done) {
        // load some mock graph with activities
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/test_graphs/activitiesEditMockGraph.ttl', false);
        xmlHTTP.send(null);
        var activitiesEditMockGraph = xmlHTTP.responseText;

        xmlHTTP.open('PUT', scratchStudyUri + '/data?graph=' + graphUri, false);
        xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
        xmlHTTP.send(activitiesEditMockGraph);

        // stub remotestoreServiceStub.executeQuery method
        remotestoreServiceStub.executeQuery.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);

          //console.log('graphUri = ' + uri);
          //console.log('query = ' + query);

          var result = testUtils.queryTeststore(query);
          //console.log('queryResponce ' + result);
          var resultObject = testUtils.deFusekify(result)

          var executeUpdateDeferred = q.defer();
          executeUpdateDeferred.resolve(resultObject);
          return executeUpdateDeferred.promise;
        });

        remotestoreServiceStub.executeUpdate.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);

          var result = testUtils.executeUpdateQuery(query);
          //console.log('queryResponce ' + result);

          var executeUpdateDeferred = q.defer();
          executeUpdateDeferred.resolve(result);
          return executeUpdateDeferred.promise;
        });

        var editActivity = {
          activityUri: 'http://trials.drugis.org/instances/activity1Uuid',
          label: 'edit label',
          activityType: {
            uri: 'http://trials.drugis.org/ontology#FollowUpActivity'
          },
          activityDescription: undefined
        };

        activityService.editItem(mockStudyUuid, editActivity).then(function(result){
           done();
        });

        rootScope.$digest();
      });

      it('should edit the activity', function(done) {

        activityService.queryItems(mockStudyUuid).then(function(activities){
          // verify query result
          expect(activities.length).toBe(1);
          expect(activities[0].label).toEqual('edit label');
          expect(activities[0].activityType).toEqual(activityService.ACTIVITY_TYPE_OPTIONS['http://trials.drugis.org/ontology#FollowUpActivity']);
          expect(activities[0].activityDescription).not.toBeDefined();
          expect(commentServiceStub.addComment).not.toHaveBeenCalled();
          done();
        });
      });
    });

    describe('delete activity', function() {

      beforeEach(function(done) {
        // load some mock graph with activities
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/test_graphs/activitiesQueryMockGraph.ttl', false);
        xmlHTTP.send(null);
        var activitiesQueryMockGraph = xmlHTTP.responseText;

        xmlHTTP.open('PUT', scratchStudyUri + '/data?graph=' + graphUri, false);
        xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
        xmlHTTP.send(activitiesQueryMockGraph);

        remotestoreServiceStub.executeQuery.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);
          var resultObject = testUtils.deFusekify(testUtils.queryTeststore(query))
          var executeUpdateDeferred = q.defer();
          executeUpdateDeferred.resolve(resultObject);
          return executeUpdateDeferred.promise;
        });

        remotestoreServiceStub.executeUpdate.and.callFake(function(uri, query) {
          var result = testUtils.executeUpdateQuery(query.replace(/\$graphUri/g, graphUri));
          var executeUpdateDeferred = q.defer();
          executeUpdateDeferred.resolve(result);
          return executeUpdateDeferred.promise;
        });

        var deleteActivity = {
          activityUri: 'http://trials.drugis.org/instances/activity1Uuid',
          activityType: {uri: 'some uri'}
        };

        activityService.deleteItem(deleteActivity, mockStudyUuid).then(function(result){
           done();
        });
        rootScope.$digest();

      });

      it('should remove the activity', function(done) {

        var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s ?p ?o }}';
        var result = testUtils.queryTeststore(query);
        var resultTriples = testUtils.deFusekify(result);

        // verify results
        expect(resultTriples.length).toBe(6);
        done();
      });
    });


  });
});
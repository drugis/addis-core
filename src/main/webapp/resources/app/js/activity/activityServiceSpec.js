'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the activity service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch'; // NB proxied by karma to actual fuseki instance

    var mockStudyUuid = 'mockStudyUuid';

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var commentServiceStub;
    var studyService;

    var activityService;
    var drugService;
    var unitService;

    beforeEach(function() {
      module('trialverse.util', function($provide) {
        remotestoreServiceStub = testUtils.createRemoteStoreStub();
        commentServiceStub = jasmine.createSpyObj('CommentService', [
          'addComment'
        ]);
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
        $provide.value('CommentService', commentServiceStub);
      });
    });

    beforeEach(module('trialverse.activity'));

    beforeEach(inject(function($q, $rootScope, $httpBackend, DrugService, UnitService, ActivityService, StudyService) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      studyService = StudyService;

      drugService = DrugService;
      unitService = UnitService;
      activityService = ActivityService;

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load service templates and flush httpBackend
      testUtils.loadTemplate('queryDrug.sparql', httpBackend);
      testUtils.loadTemplate('queryUnit.sparql', httpBackend);
      testUtils.loadTemplate('queryActivity.sparql', httpBackend);
      testUtils.loadTemplate('queryActivityTreatment.sparql', httpBackend);
      testUtils.loadTemplate('addActivity.sparql', httpBackend);
      testUtils.loadTemplate('addTitratedTreatment.sparql', httpBackend);
      testUtils.loadTemplate('addFixedDoseTreatment.sparql', httpBackend);
      testUtils.loadTemplate('editActivity.sparql', httpBackend);
      testUtils.loadTemplate('deleteActivity.sparql', httpBackend);

      httpBackend.flush();

      // create and load empty test store
      var createStoreDeferred = $q.defer();
      var createStorePromise = createStoreDeferred.promise;
      remotestoreServiceStub.create.and.returnValue(createStorePromise);

      var loadStoreDeferred = $q.defer();
      remotestoreServiceStub.load.and.returnValue(loadStoreDeferred.promise);

      studyService.loadStore();
      createStoreDeferred.resolve(scratchStudyUri);
      loadStoreDeferred.resolve();

      rootScope.$digest();
    }));


    describe('query activities', function() {

      beforeEach(function(done) {
        // load some mock graph with activities
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/test_graphs/activitiesMockGraph.ttl', false);
        xmlHTTP.send(null);
        var activitiesQueryMockGraph = xmlHTTP.responseText;

        xmlHTTP.open('PUT', scratchStudyUri + '/data?graph=' + graphUri, false);
        xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
        xmlHTTP.send(activitiesQueryMockGraph);

        // stub remotestoreServiceStub.executeQuery method
        remotestoreServiceStub.executeQuery.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);

          // console.log('graphUri = ' + uri);
          // console.log('query = ' + query);

          var result = testUtils.queryTeststore(query);
          // console.log('queryResponce ' + result);
          var resultObject = testUtils.deFusekify(result);

          var executeUpdateDeferred = q.defer();
          executeUpdateDeferred.resolve(resultObject);
          return executeUpdateDeferred.promise;
        });

        done();
      });

      it('should return the activities contained in the study', function(done) {

        // call function under test
        activityService.queryItems(mockStudyUuid).then(function(result) {
          var activities = result;

          // verify query result
          expect(activities.length).toBe(2);
          expect(activities[0].label).toEqual('activity 1');
          expect(activities[1].label).toEqual('activity 2');
          expect(activities[0].activityType).toEqual(activityService.ACTIVITY_TYPE_OPTIONS['http://trials.drugis.org/ontology#RandomizationActivity']);
          expect(activities[1].activityType).toEqual(activityService.ACTIVITY_TYPE_OPTIONS['http://trials.drugis.org/ontology#TreatmentActivity']);
          expect(activities[1].activityDescription).toEqual('activity description');

          expect(activities[0].treatments).not.toBeDefined();
          expect(activities[1].treatments.length).toBe(2);
          expect(activities[1].treatments[0].treatmentDoseType).toEqual('http://trials.drugis.org/ontology#TitratedDoseDrugTreatment');
          expect(activities[1].treatments[0].drug.uri).toEqual('http://trials.drugis.org/instances/drug1Uuid');
          expect(activities[1].treatments[0].drug.label).toEqual('Sertraline');
          expect(activities[1].treatments[0].doseUnit.uri).toEqual('http://trials.drugis.org/instances/unit1Uuid');
          expect(activities[1].treatments[0].doseUnit.label).toEqual('milligram');
          expect(activities[1].treatments[0].dosingPeriodicity).toEqual('P1D');
          expect(activities[1].treatments[0].maxValue).toEqual('1.500000e+02');
          expect(activities[1].treatments[0].minValue).toEqual('5.000000e+01');
          expect(activities[1].treatments[0].treatmentUri).toEqual('http://trials.drugis.org/instances/treatment1Uuid');

          expect(activities[1].treatments[1].treatmentDoseType).toEqual('http://trials.drugis.org/ontology#FixedDoseDrugTreatment');
          expect(activities[1].treatments[1].drug.label).toEqual('Bupropion');
          expect(activities[1].treatments[1].doseUnit.label).toEqual('liter');
          expect(activities[1].treatments[1].dosingPeriodicity).toEqual('P1D');
          expect(activities[1].treatments[1].fixedValue).toEqual('0.000000e+00');

          done();
        });
        rootScope.$digest();
      });
    });

    describe('add non-treatment activity', function() {

      beforeEach(function(done) {
        remotestoreServiceStub.executeUpdate.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);

          var result = testUtils.executeUpdateQuery(query);
          //// console.log('queryResponce ' + result);

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

        activityService.addItem(mockStudyUuid, newActivity).then(function() {
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
        var hasActivityTriple = _.find(resultTriples, function(item) {
          return item.s === 'http://trials.drugis.org/studies/mockStudyUuid';
        });
        expect(hasActivityTriple.s).toBeDefined();
        expect(hasActivityTriple.p).toEqual('http://trials.drugis.org/ontology#has_activity');
        expect(hasActivityTriple.o).toBeDefined();

        var activityLabelTriple = _.find(resultTriples, function(item) {
          return item.p === 'http://www.w3.org/2000/01/rdf-schema#label';
        });
        expect(activityLabelTriple.s).toBeDefined();
        expect(activityLabelTriple.p).toBeDefined();
        expect(activityLabelTriple.o).toEqual('newActivityLabel');

        var activityTypeTriple = _.find(resultTriples, function(item) {
          return item.p === 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type';
        });
        expect(activityTypeTriple.s).toBeDefined();
        expect(activityTypeTriple.p).toBeDefined();
        expect(activityTypeTriple.o).toEqual('http://mockActivityUri');

        done();
      });
    });

    describe('add treatment activity', function() {

      beforeEach(function(done) {

        remotestoreServiceStub.executeUpdate.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);
          var result = testUtils.executeUpdateQuery(query);
          // console.log('queryResponce ' + result);
          var executeUpdateDeferred = q.defer();
          executeUpdateDeferred.resolve(result);
          return executeUpdateDeferred.promise;
        });

        remotestoreServiceStub.executeQuery.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);
          //// console.log('graphUri = ' + uri);
          //// console.log('query = ' + query);
          var result = testUtils.queryTeststore(query);
          //// console.log('queryResponce ' + result);
          var resultObject = testUtils.deFusekify(result);
          var executeUpdateDeferred = q.defer();
          executeUpdateDeferred.resolve(resultObject);
          return executeUpdateDeferred.promise;
        });

        var fixedTreatment = {
          treatmentDoseType: 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment',
          drug: {
            uri: 'http://drug/newDrugUuid',
            label: 'new drug'
          },
          doseUnit: {
            uri: 'http://unit/oldUnit',
            label: 'old unit label'
          },
          fixedValue: '1.5e+02',
          dosingPeriodicity: 'P3W'
        };

        var titRatedTreatment = {
          treatmentDoseType: 'http://trials.drugis.org/ontology#TitratedDoseDrugTreatment',
          drug: {
            uri: 'http://drug/oldDrugUuid',
            label: 'old drug'
          },
          doseUnit: {
            uri: 'http://unit/oldUnit',
            label: 'old unit label'
          },
          minValue: '1.2e+02',
          maxValue: '1.3e+03',
          dosingPeriodicity: 'P2W'
        };

        var newActivity = {
          activityUri: 'http://trials.drugis.org/instances/newActivityUuid',
          label: 'newActivityLabel',
          activityType: {
            uri: 'http://trials.drugis.org/ontology#TreatmentActivity'
          },
          treatments: [fixedTreatment, titRatedTreatment]
        };

        activityService.addItem(mockStudyUuid, newActivity).then(function() {
          done();
        });

        rootScope.$digest();
      });

      it('should add the new activity to the graph', function(done) {
        activityService.queryItems(mockStudyUuid).then(function(resultActivities) {
          expect(resultActivities.length).toBe(1);
          var activity = resultActivities[0];
          expect(activity.treatments.length).toBe(2);

          expect(activity.treatments[0].treatmentDoseType).toEqual('http://trials.drugis.org/ontology#TitratedDoseDrugTreatment');
          expect(activity.treatments[0].drug.label).toEqual('old drug');
          expect(activity.treatments[0].doseUnit.label).toEqual('old unit label');
          expect(activity.treatments[0].dosingPeriodicity).toEqual('P2W');
          expect(activity.treatments[0].minValue).toEqual('1.2e+02');
          expect(activity.treatments[0].maxValue).toEqual('1.3e+03');

          expect(activity.treatments[1].treatmentDoseType).toEqual('http://trials.drugis.org/ontology#FixedDoseDrugTreatment');
          expect(activity.treatments[1].drug.label).toEqual('new drug');
          expect(activity.treatments[1].doseUnit.label).toEqual('old unit label');
          expect(activity.treatments[1].dosingPeriodicity).toEqual('P3W');
          expect(activity.treatments[1].fixedValue).toEqual('1.5e+02');
          drugService.queryItems(mockStudyUuid).then(function(drugResults) {
            expect(drugResults.length).toBe(2);
            expect(drugResults[0].label).toEqual('old drug');
            expect(drugResults[1].label).toEqual('new drug');
            unitService.queryItems(mockStudyUuid).then(function(unitResults) {
              expect(unitResults.length).toBe(1);
              expect(unitResults[0].label).toEqual('old unit label');
              done();
            });
          });
        });
      });
    });

    describe('edit activities', function() {

      beforeEach(function(done) {
        // load some mock graph with activities
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/test_graphs/activitiesMockGraph.ttl', false);
        xmlHTTP.send(null);
        var activitiesEditMockGraph = xmlHTTP.responseText;

        xmlHTTP.open('PUT', scratchStudyUri + '/data?graph=' + graphUri, false);
        xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
        xmlHTTP.send(activitiesEditMockGraph);

        // stub remotestoreServiceStub.executeQuery method
        remotestoreServiceStub.executeQuery.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);

          //// console.log('graphUri = ' + uri);
          //// console.log('query = ' + query);

          var result = testUtils.queryTeststore(query);
          //// console.log('queryResponce ' + result);
          var resultObject = testUtils.deFusekify(result);

          var executeUpdateDeferred = q.defer();
          executeUpdateDeferred.resolve(resultObject);
          return executeUpdateDeferred.promise;
        });

        remotestoreServiceStub.executeUpdate.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);

          var result = testUtils.executeUpdateQuery(query);
          //// console.log('queryResponce ' + result);

          var executeUpdateDeferred = q.defer();
          executeUpdateDeferred.resolve(result);
          return executeUpdateDeferred.promise;
        });

        var oldTreatment = {
          treatmentUri: 'http://trials.drugis.org/instances/treatment2Uuid',
          treatmentDoseType: 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment',
          drug: {
            uri: 'http://trials.drugis.org/instances/drug1Uuid',
            label: 'Sertraline'
          },
          doseUnit: {
            uri: 'http://trials.drugis.org/instances/unit1Uuid',
            label: 'milligram'
          },
          fixedValue: '1.5e+02',
          dosingPeriodicity: 'P3W'
        };
        var newTreatment = {
          treatmentDoseType: 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment',
          drug: {
            uri: 'http://drug/newDrugUuid2',
            label: 'new drug 2'
          },
          doseUnit: {
            uri: 'http://trials.drugis.org/instances/unit1Uuid',
            label: 'milligram'
          },
          fixedValue: '1.5e+02',
          dosingPeriodicity: 'P3W'
        };

        var editActivity = {
          activityUri: 'http://trials.drugis.org/instances/activity2Uuid',
          label: 'edit label',
          activityType: {
            uri: 'http://trials.drugis.org/ontology#TreatmentActivity'
          },
          treatments: [oldTreatment, newTreatment],
          activityDescription: undefined
        };

        activityService.editItem(mockStudyUuid, editActivity).then(function() {
          done();
        });

        rootScope.$digest();
      });

      it('should edit the activity', function(done) {

        activityService.queryItems(mockStudyUuid).then(function(activities) {
          // verify query result
          expect(activities.length).toBe(2);
          expect(activities[1].label).toEqual('edit label');
          expect(activities[1].activityType).toEqual(activityService.ACTIVITY_TYPE_OPTIONS['http://trials.drugis.org/ontology#TreatmentActivity']);
          expect(activities[1].activityDescription).not.toBeDefined();
          expect(activities[1].treatments.length).toBe(3);
          expect(activities[1].treatments[0].drug.label).toEqual('new drug 2');
          expect(commentServiceStub.addComment).not.toHaveBeenCalled();
          done();
        });
      });
    });

    describe('delete activity', function() {

      beforeEach(function(done) {
        // load some mock graph with activities
        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/test_graphs/activitiesMockGraph.ttl', false);
        xmlHTTP.send(null);
        var activitiesQueryMockGraph = xmlHTTP.responseText;

        xmlHTTP.open('PUT', scratchStudyUri + '/data?graph=' + graphUri, false);
        xmlHTTP.setRequestHeader('Content-type', 'text/turtle');
        xmlHTTP.send(activitiesQueryMockGraph);

        remotestoreServiceStub.executeQuery.and.callFake(function(uri, query) {
          query = query.replace(/\$graphUri/g, graphUri);
          var resultObject = testUtils.deFusekify(testUtils.queryTeststore(query));
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
          activityUri: 'http://trials.drugis.org/instances/activity2Uuid',
          activityType: {
            uri: 'some uri'
          }
        };

        activityService.deleteItem(deleteActivity, mockStudyUuid).then(function() {
          done();
        });
        rootScope.$digest();

      });

      it('should remove the activity', function(done) {

        var query = 'SELECT * WHERE { GRAPH <' + graphUri + '> { ?s ?p ?o }}';
        var result = testUtils.queryTeststore(query);
        var resultTriples = testUtils.deFusekify(result);

        // verify results
        expect(resultTriples.length).toBe(13); // todo needs to be six the delete does cleanup of treamtmentStuff
        done();
      });
    });


  });
});

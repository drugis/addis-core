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
      remotestoreServiceStub.create.and.returnValue(createStoreDeferred.promise);

      var loadStoreDeferred = $q.defer();
      remotestoreServiceStub.load.and.returnValue(loadStoreDeferred.promise);

      studyService.loadStore();
      createStoreDeferred.resolve(scratchStudyUri);
      loadStoreDeferred.resolve();

      rootScope.$digest();
    }));


    describe('query activities', function() {

      beforeEach(function(done) {

        testUtils.loadTestGraph('activitiesMockGraph.ttl', graphUri);

        // stub remotestoreServiceStub.executeQuery method
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        done();
      });

      it('should return the activities contained in the study', function(done) {

        // call function under test
        activityService.queryItems().then(function(result) {
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

      var queryPromise;
      var newActivity = {
        activityUri: 'http://trials.drugis.org/instances/newActivityUuid',
        label: 'newActivityLabel',
        activityType: {
          uri: 'http://mockActivityUri'
        },
        activityDescription: 'some description'
      };

      beforeEach(function(done) {

        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);
        activityService.addItem(newActivity).then(function() {
          queryPromise = activityService.queryItems();
          done();
        });
        rootScope.$digest();
      });

      it('should add the new activity to the graph', function() {
        queryPromise.then(function(result) {
          expect(result.length).toBe(1);
          expect(activities[0].label).toEqual(newActivity.label);
          expect(activities[0].activityType).toEqual(newActivity.activityType);
          expect(activities[0].activityDescription).toEqual(newActivity.activityDescription);
          expect(activities[0].treatments).not.toBeDefined();
        });
      });
    });

    describe('add treatment activity', function() {

      var queryActivityPromise, queryUnitPromise, queryDrugsPromise;
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
        fixedValue: '1500',
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
        minValue: '120',
        maxValue: '1300',
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

      beforeEach(function(done) {

        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        activityService.addItem(newActivity).then(function() {
          queryActivityPromise = activityService.queryItems();
          queryDrugsPromise = drugService.queryItems();
          queryUnitPromise = unitService.queryItems()
          done();
        });

        rootScope.$digest();
      });

      it('should add the new activity to the graph', function(done) {
        queryActivityPromise.then(function(resultActivities) {
          expect(resultActivities.length).toBe(1);
          var activity = resultActivities[0];
          expect(activity.treatments.length).toBe(2);

          expect(activity.treatments[0].treatmentDoseType).toEqual('http://trials.drugis.org/ontology#TitratedDoseDrugTreatment');
          expect(activity.treatments[0].drug.label).toEqual('old drug');
          expect(activity.treatments[0].doseUnit.label).toEqual('old unit label');
          expect(activity.treatments[0].dosingPeriodicity).toEqual('P2W');
          expect(activity.treatments[0].minValue).toEqual('1.2e2');
          expect(activity.treatments[0].maxValue).toEqual('1.3e3');

          expect(activity.treatments[1].treatmentDoseType).toEqual('http://trials.drugis.org/ontology#FixedDoseDrugTreatment');
          expect(activity.treatments[1].drug.label).toEqual('new drug');
          expect(activity.treatments[1].doseUnit.label).toEqual('old unit label');
          expect(activity.treatments[1].dosingPeriodicity).toEqual('P3W');
          expect(activity.treatments[1].fixedValue).toEqual('1.5e3');

        });

        queryDrugsPromise.then(function(drugResults) {
          expect(drugResults.length).toBe(2);
          expect(drugResults[0].label).toEqual('old drug');
          expect(drugResults[1].label).toEqual('new drug');
        });

        queryUnitPromise.then(function(unitResults) {
          expect(unitResults.length).toBe(1);
          expect(unitResults[0].label).toEqual('old unit label');
          done();
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
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

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
          fixedValue: '150',
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
          fixedValue: '150',
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

        activityService.editItem(editActivity).then(function() {
          done();
        });

        rootScope.$digest();
      });

      it('should edit the activity', function(done) {

        activityService.queryItems().then(function(activities) {
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

        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);

        var deleteActivity = {
          activityUri: 'http://trials.drugis.org/instances/activity2Uuid',
          activityType: {
            uri: 'some uri'
          }
        };

        activityService.deleteItem(deleteActivity).then(function() {
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
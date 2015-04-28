'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the population characteristic service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch';

    var rootScope, q, httpBackend;
    var remotestoreServiceStub, uUIDServiceStub, measurementMomentServiceStub;;
    var populationCharacteristicService;
    var outcomeService;
    var studyService;

    beforeEach(module('trialverse.populationCharacteristic'));
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
        uUIDServiceStub = jasmine.createSpyObj('UUIDService', [
          'generate'
        ]);
        uUIDServiceStub.generate.and.returnValue('newUuid');
        measurementMomentServiceStub = jasmine.createSpyObj('MeasurementMomentService', [
          'queryItems'
        ]);
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
        $provide.value('UUIDService', uUIDServiceStub);
        $provide.value('MeasurementMomentService', measurementMomentServiceStub);
      });
    });


    beforeEach(inject(function($q, $rootScope, $httpBackend, StudyService,
      PopulationCharacteristicService, OutcomeService) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      populationCharacteristicService = PopulationCharacteristicService;
      studyService = StudyService;
      outcomeService = OutcomeService

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load service templates and flush httpBackend

      testUtils.loadTemplate('setOutcomeResultProperty.sparql', httpBackend);
      testUtils.loadTemplate('addTemplate.sparql', httpBackend);
      testUtils.loadTemplate('addPopulationCharacteristic.sparql', httpBackend);
      testUtils.loadTemplate('queryPopulationCharacteristic.sparql', httpBackend);
      testUtils.loadTemplate('deletePopulationCharacteristic.sparql', httpBackend);
      testUtils.loadTemplate('editVariable.sparql', httpBackend);
      testUtils.loadTemplate('queryMeasuredAt.sparql', httpBackend);
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

    describe('add population characteristic', function() {

      var popCharUri = 'http://trials.drugis.org/instances/newUUid';
      var queryPromise;
      var moment = 'http://mm/uri';
      var measuredAtMoment = {
        uri: popCharUri,
        measurementMoment: moment
      };

      var newPopulationChar = {
        uri: popCharUri,
        label: 'label',
        measurementType: 'http://trials.drugis.org/ontology#dichotomous',
        measuredAtMoments: [measuredAtMoment]
      };

      beforeEach(function(done) {
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);
        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);

        var queryMeasurementMomentsDefer = q.defer();
        queryMeasurementMomentsDefer.resolve([{
          itemUri: popCharUri,
          measurementMoment: moment
        }]);
        measurementMomentServiceStub.queryItems.and.returnValue(queryMeasurementMomentsDefer.promise);

        populationCharacteristicService.addItem(newPopulationChar).then(function() {
          queryPromise = populationCharacteristicService.queryItems();
          done();
        });
        rootScope.$digest();
      });

      it('should add the population characteristics', function(done) {

        queryPromise.then(function(queryRestult) {
          expect(queryRestult.length).toEqual(1);
          done();
        });
      });
    });

    describe('edit population characteristic', function() {

      var editItem;
      var popCharUri = 'http://trials.drugis.org/instances/newUUid';
      var queryPromise;
      var moment = 'http://mm/uri';
      var measuredAtMoment = {
        uri: popCharUri,
        measurementMoment: moment
      };

      var newPopulationChar = {
        uri: popCharUri,
        label: 'label',
        measurementType: 'http://trials.drugis.org/ontology#dichotomous',
        measuredAtMoments: [measuredAtMoment]
      };

      beforeEach(function(done) {
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);
        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);

        var queryMeasurementMomentsDefer = q.defer();
        queryMeasurementMomentsDefer.resolve([{
          itemUri: popCharUri,
          measurementMoment: moment
        }]);
        measurementMomentServiceStub.queryItems.and.returnValue(queryMeasurementMomentsDefer.promise);
        populationCharacteristicService.addItem(newPopulationChar).then(function() {
          editItem = newPopulationChar;
          editItem.label = 'edit label';
          populationCharacteristicService.editItem(editItem).then(function() {
            queryPromise = populationCharacteristicService.queryItems();
            done();
          });
        });
        rootScope.$digest();
      });

      it('should have changed the population characteristics', function(done) {

        queryPromise.then(function(queryRestult) {
          expect(queryRestult.length).toEqual(1);
          expect(queryRestult[0].label).toEqual(editItem.label);
          done();
        });
      });
    });

    describe('delete population characteristic', function() {
      var popCharUri = 'http://trials.drugis.org/instances/newUUid';
      var queryPromise;
      var moment = 'http://mm/uri';
      var measuredAtMoment = {
        uri: popCharUri,
        measurementMoment: moment
      };

      var newPopulationChar = {
        uri: popCharUri,
        label: 'label',
        measurementType: 'http://trials.drugis.org/ontology#dichotomous',
        measuredAtMoments: [measuredAtMoment]
      };

      beforeEach(function(done) {
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);
        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);

        var queryMeasurementMomentsDefer = q.defer();
        queryMeasurementMomentsDefer.resolve([{
          itemUri: popCharUri,
          measurementMoment: moment
        }]);
        measurementMomentServiceStub.queryItems.and.returnValue(queryMeasurementMomentsDefer.promise);
        populationCharacteristicService.addItem(newPopulationChar).then(function() {
          populationCharacteristicService.deleteItem(newPopulationChar).then(function() {
            queryPromise = populationCharacteristicService.queryItems();
            done();
          });
        });
        rootScope.$digest();
      });

      it('should have removed the population characteristics', function(done) {

        queryPromise.then(function(queryRestult) {
          expect(queryRestult.length).toEqual(0);
          done();
        });
      });
    });


  });
});
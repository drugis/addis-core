'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the measurement moment service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch'; // NB proxied by karma to actual fuseki instance

    var mockStudyUuid = 'mockStudyUuid';

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var studyService;

    var measurementMomentService;
    var epochServiceStub;

    // wire the real stuff
    beforeEach(module('trialverse.measurementMoment'));

    // mock the stuff not being tested
    beforeEach(function() {
      module('trialverse.util', function($provide) {
        remotestoreServiceStub = testUtils.createRemoteStoreStub();
        epochServiceStub = jasmine.createSpyObj('EpochService', ['queryItems']);
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
        $provide.value('EpochService', epochServiceStub);
      });
    });

    beforeEach(inject(function($q, $rootScope, $httpBackend, MeasurementMomentService, StudyService) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      studyService = StudyService;

      measurementMomentService = MeasurementMomentService;

      // mock stub services
      var queryEpochsDeferred = $q.defer();
      var mockEpochs = [{
        uri: 'http://trials.drugis.org/instances/4be1f8d0-7d6c-45f0-a651-69bb9d4df948'
      }];
      queryEpochsDeferred.resolve(mockEpochs);
      epochServiceStub.queryItems.and.returnValue(queryEpochsDeferred.promise);


      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load study service templates
      testUtils.loadTemplate('createEmptyStudy.sparql', httpBackend);
      testUtils.loadTemplate('queryStudyData.sparql', httpBackend);

      // load service templates and flush httpBackend
      testUtils.loadTemplate('queryMeasurementMoment.sparql', httpBackend);
      testUtils.loadTemplate('addMeasurementMoment.sparql', httpBackend);
      testUtils.loadTemplate('editMeasurementMoment.sparql', httpBackend);
      testUtils.loadTemplate('deleteMeasurementMoment.sparql', httpBackend);

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


    describe('query measurement moments', function() {
      beforeEach(function(done) {
        testUtils.loadTestGraph('queryMeasurementMomentsGraph.ttl', graphUri);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);
        done();
      });
      it('should return the measurements', function(done) {
        measurementMomentService.queryItems().then(function(result) {
          expect(result.length).toBe(2);
          var measurementMoment = result[0];
          expect(measurementMoment.label).toEqual('At start of epoch 1');
          expect(measurementMoment.epochLabel).toEqual('epoch 1');
          expect(measurementMoment.relativeToAnchor).toEqual('http://trials.drugis.org/ontology#anchorEpochStart');
          expect(measurementMoment.offset).toEqual('PT0S');
          done();
        });
        rootScope.$digest();
      });
    });

    describe('add measurement moments', function() {
      var newMoment = {
        label: 'At start of new epoch',
        epoch: {
          uri: 'http://trials.drugis.org/instances/epoch1uuid'
        },
        relativeToAnchor: 'http://trials.drugis.org/ontology#anchorEpochStart',
        offset: 'PT0S'
      };

      beforeEach(function(done) {
        testUtils.loadTestGraph('singleEpoch.ttl', graphUri);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        done();
      });

      it('should add the measurement', function(done) {
        measurementMomentService.addItem(newMoment).then(function() {
          measurementMomentService.queryItems().then(function(result) {
            expect(result.length).toBe(1);
            var measurementMoment = result[0];
            expect(measurementMoment.label).toEqual('At start of new epoch');
            expect(measurementMoment.epochLabel).toEqual('epoch 1');
            expect(measurementMoment.relativeToAnchor).toEqual('http://trials.drugis.org/ontology#anchorEpochStart');
            expect(measurementMoment.offset).toEqual('PT0S');
            done();
          });
        });
        rootScope.$digest();
      });
    });

    describe('delete measurement moment', function() {
      beforeEach(function(done) {
        testUtils.loadTestGraph('queryMeasurementMomentsGraph.ttl', graphUri);
        testUtils.remoteStoreStubQuery(remotestoreServiceStub, graphUri, q);
        testUtils.remoteStoreStubUpdate(remotestoreServiceStub, graphUri, q);
        done();
      });

      it('should remove the measurementmoment', function(done) {
        var mmToRemove = {
          label: 'At start of new epoch',
          uri: 'http://trials.drugis.org/instances/2634b5b6-d557-4b38-8c48-ec08fa12d435'
        };
        measurementMomentService.deleteItem(mmToRemove).then(function() {
          measurementMomentService.queryItems().then(function(result) {
            expect(result.length).toBe(1);
            done();
          });
        });
        rootScope.$digest();
      });
    });


  });
});
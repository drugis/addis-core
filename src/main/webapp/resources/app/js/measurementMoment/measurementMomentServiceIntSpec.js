'use strict';
define(['angular', 'angular-mocks'], function(angular) {
  describe('the measurement moment service', function() {

    var mockStudyUuid = 'mockStudyUuid';
    var rootScope, q;
    var remotestoreServiceStub;
    var studyService = jasmine.createSpyObj('StudyService', ['getJsonGraph', 'saveJsonGraph']);
    var epochServiceStub = jasmine.createSpyObj('EpochService', ['queryItems']);
    var measurementMomentService;


    beforeEach(function() {
      module('trialverse.measurementMoment', function($provide) {
        $provide.value('StudyService', studyService);
        $provide.value('EpochService', epochServiceStub);
      });
    });

    beforeEach(module('trialverse.util'));

    beforeEach(inject(function($q, $rootScope, MeasurementMomentService, StudyService) {
      q = $q;
      rootScope = $rootScope;
      measurementMomentService = MeasurementMomentService;

      studyService = StudyService;

      // mock stub services
      var queryEpochsDeferred = $q.defer();
      var mockEpochs = [{
        uri: 'http://trials.drugis.org/instances/aaa',
        duration: 'P14D',
        label: 'Washout',
        pos: 0,
        isPrimary: false
      }, {
        uri: 'http://trials.drugis.org/instances/bbb',
        label: 'Randomization',
        duration: 'PT0S',
        pos: 1,
        isPrimary: true
      }];
      queryEpochsDeferred.resolve(mockEpochs);
      epochServiceStub.queryItems.and.returnValue(queryEpochsDeferred.promise);

      rootScope.$digest();
    }));


    describe('query measurement moments', function() {
      beforeEach(function() {
        var graphJsonObject = [{
          "@id": "http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234",
          "@type": "ontology:MeasurementMoment",
          "relative_to_anchor": "ontology:anchorEpochStart",
          "relative_to_epoch": "http://trials.drugis.org/instances/bbb",
          "time_offset": "PT0S",
          "label": "At start of epoch 1"
        }];
        var graphDefer = q.defer();
        var getGraphPromise = graphDefer.promise;
        graphDefer.resolve(graphJsonObject);
        studyService.getJsonGraph.and.returnValue(getGraphPromise);
      });
      it('should return the measurements', function(done) {
        measurementMomentService.queryItems().then(function(result) {
          expect(result.length).toBe(1);
          var measurementMoment = result[0];
          expect(measurementMoment.label).toEqual('At start of epoch 1');
          expect(measurementMoment.epochLabel).toEqual('Randomization');
          expect(measurementMoment.relativeToAnchor).toEqual('ontology:anchorEpochStart');
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
          uri: 'http://trials.drugis.org/instances/bbb'
        },
        relativeToAnchor: 'ontology:anchorEpochStart',
        offset: 'PT0S'
      };

      beforeEach(function() {
        var graphJsonObject = [];
        var graphDefer = q.defer();
        var getGraphPromise = graphDefer.promise;
        graphDefer.resolve(graphJsonObject);
        studyService.getJsonGraph.and.returnValue(getGraphPromise);
      });

      it('should add the measurement', function(done) {
        measurementMomentService.addItem(newMoment).then(function() {
          measurementMomentService.queryItems().then(function(result) {
            expect(result.length).toBe(1);
            var measurementMoment = result[0];
            expect(measurementMoment.label).toEqual('At start of new epoch');
            expect(measurementMoment.epochLabel).toEqual('Randomization');
            expect(measurementMoment.relativeToAnchor).toEqual('ontology:anchorEpochStart');
            expect(measurementMoment.offset).toEqual('PT0S');
            done();
          });
        });
        rootScope.$digest();
      });
    });

    describe('edit measurement moments', function() {
      var editMoment = {
        label: 'new Label',
        epoch: {
          uri: 'http://trials.drugis.org/instances/aaa'
        },
        relativeToAnchor: 'ontology:anchorEpochEnd',
        offset: 'PT4D',
        uri: 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234'
      };

      beforeEach(function() {
        var graphJsonObject = [{
          "@id": "http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234",
          "@type": "ontology:MeasurementMoment",
          "relative_to_anchor": "ontology:anchorEpochStart",
          "relative_to_epoch": "http://trials.drugis.org/instances/bbb",
          "time_offset": "PT0S",
          "label": "At start of epoch 1"
        }];
        var graphDefer = q.defer();
        var getGraphPromise = graphDefer.promise;
        graphDefer.resolve(graphJsonObject);
        studyService.getJsonGraph.and.returnValue(getGraphPromise);
      });

      it('should add the measurement', function(done) {
        measurementMomentService.editItem(editMoment).then(function() {
          measurementMomentService.queryItems().then(function(result) {
            expect(result.length).toBe(1);
            var measurementMoment = result[0];
            expect(measurementMoment.label).toEqual('new Label');
            expect(measurementMoment.epochLabel).toEqual('Washout');
            expect(measurementMoment.relativeToAnchor).toEqual('ontology:anchorEpochEnd');
            expect(measurementMoment.offset).toEqual('PT4D');
            done();
          });
        });
        rootScope.$digest();
      });
    });

    describe('delete measurement moments', function() {
      var deleteMoment = {
        label: 'At start of epoch 1',
        epoch: {
          uri: 'http://trials.drugis.org/instances/bbb'
        },
        relativeToAnchor: 'ontology:anchorEpochStart',
        offset: 'PT0S',
        uri: 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234'
      };

      beforeEach(function() {
        var graphJsonObject = [{
          "@id": "http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234",
          "@type": "ontology:MeasurementMoment",
          "relative_to_anchor": "ontology:anchorEpochStart",
          "relative_to_epoch": "http://trials.drugis.org/instances/bbb",
          "time_offset": "PT0S",
          "label": "At start of epoch 1"
        }];
        var graphDefer = q.defer();
        var getGraphPromise = graphDefer.promise;
        graphDefer.resolve(graphJsonObject);
        studyService.getJsonGraph.and.returnValue(getGraphPromise);
      });

      it('should add the measurement', function(done) {
        measurementMomentService.deleteItem(deleteMoment).then(function() {
          measurementMomentService.queryItems().then(function(result) {
            expect(result.length).toBe(0);
            done();
          });
        });
        rootScope.$digest();
      });
    });


  });
});
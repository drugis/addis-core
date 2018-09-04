'use strict';
define(['angular-mocks', './measurementMoment'], function() {
  describe('the measurement moment service', function() {

    var rootScope, q;
    var studyService = jasmine.createSpyObj('StudyService', ['getJsonGraph', 'saveJsonGraph', 'getStudy', 'save']);
    var epochServiceStub = jasmine.createSpyObj('EpochService', ['queryItems']);
    var uuidServiceMock = jasmine.createSpyObj('UUIDService', ['generate']);
    var resultsServiceMock = jasmine.createSpyObj('ResultsService', ['queryResultsByMeasurementMoment']);
    var repairServiceMock = jasmine.createSpyObj('RepairService', ['findOverlappingResults', 'mergeResults']);
    var sourceResultsDefer, targetResultsDefer, mergeResultsDefer, getStudyDefer, saveStudyDefer;
    var measurementMomentService;


    beforeEach(function() {
      angular.mock.module('trialverse.measurementMoment', function($provide) {
        $provide.value('StudyService', studyService);
        $provide.value('EpochService', epochServiceStub);
        $provide.value('UUIDService', uuidServiceMock);
        $provide.value('ResultsService', resultsServiceMock);
        $provide.value('RepairService', repairServiceMock);
      });
    });

    beforeEach(angular.mock.module('trialverse.util'));

    beforeEach(inject(function($q, $rootScope, MeasurementMomentService) {
      q = $q;
      rootScope = $rootScope;
      measurementMomentService = MeasurementMomentService;
      uuidServiceMock.generate.and.returnValue('generatedUUID');

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

      getStudyDefer = q.defer();
      studyService.getStudy.and.returnValue(getStudyDefer.promise);

      saveStudyDefer = q.defer();
      studyService.save.and.returnValue(saveStudyDefer.promise);

      sourceResultsDefer = q.defer();
      targetResultsDefer = q.defer();
      resultsServiceMock.queryResultsByMeasurementMoment.and.returnValues(sourceResultsDefer.promise, targetResultsDefer.promise);
      mergeResultsDefer = q.defer();
      repairServiceMock.mergeResults.and.returnValue(mergeResultsDefer.promise);

      rootScope.$digest();
    }));

    afterEach(function() {
      resultsServiceMock.queryResultsByMeasurementMoment.calls.reset();
    });

    describe('query measurement moments', function() {
      beforeEach(function() {
        var graphJsonObject = [{
          '@id': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
          '@type': 'ontology:MeasurementMoment',
          'relative_to_anchor': 'ontology:anchorEpochStart',
          'relative_to_epoch': 'http://trials.drugis.org/instances/bbb',
          'time_offset': 'PT0S',
          'label': 'At start of epoch 1'
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

          expect(studyService.saveJsonGraph).toHaveBeenCalledWith([{
            '@id': 'http://trials.drugis.org/instances/generatedUUID',
            '@type': 'ontology:MeasurementMoment',
            relative_to_anchor: 'ontology:anchorEpochStart',
            relative_to_epoch: 'http://trials.drugis.org/instances/bbb',
            time_offset: 'PT0S',
            label: 'At start of new epoch'
          }]);

          done();

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
          '@id': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
          '@type': 'ontology:MeasurementMoment',
          'relative_to_anchor': 'ontology:anchorEpochStart',
          'relative_to_epoch': 'http://trials.drugis.org/instances/bbb',
          'time_offset': 'PT0S',
          'label': 'At start of epoch 1'
        }];
        var graphDefer = q.defer();
        var getGraphPromise = graphDefer.promise;
        graphDefer.resolve(graphJsonObject);
        studyService.getJsonGraph.and.returnValue(getGraphPromise);
      });

      it('should add the measurement', function(done) {
        measurementMomentService.editItem(editMoment).then(function() {
          expect(studyService.saveJsonGraph).toHaveBeenCalledWith([{
            '@id': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
            '@type': 'ontology:MeasurementMoment',
            relative_to_anchor: 'ontology:anchorEpochEnd',
            relative_to_epoch: 'http://trials.drugis.org/instances/aaa',
            time_offset: 'PT4D',
            label: 'new Label'
          }]);
        });
        done();
        rootScope.$digest();
      });
    });

    describe('delete unused measurement moments', function() {
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
          '@id': 'http://trials.drugis.org/instances/4bf82c72-02de-48cb-8925-2062b6b82234',
          '@type': 'ontology:MeasurementMoment',
          'relative_to_anchor': 'ontology:anchorEpochStart',
          'relative_to_epoch': 'http://trials.drugis.org/instances/bbb',
          'time_offset': 'PT0S',
          'label': 'At start of epoch 1'
        }, {
          '@type': 'ontology:Study'
        }];
        var graphDefer = q.defer();
        var getGraphPromise = graphDefer.promise;
        graphDefer.resolve(graphJsonObject);
        studyService.getJsonGraph.and.returnValue(getGraphPromise);
      });
      afterEach(function() {
        studyService.getJsonGraph.calls.reset();
      });

      it('should delete the measurement', function(done) {
        measurementMomentService.deleteItem(deleteMoment).then(function() {
          measurementMomentService.queryItems().then(function(result) {
            expect(result.length).toBe(0);
            done();
          });
        });
        rootScope.$digest();
      });
    });
    describe('delete measurement moments used in results', function() {
      var deleteMoment = {
        label: 'At start of epoch 1',
        epoch: {
          uri: 'http://trials.drugis.org/instances/bbb'
        },
        relativeToAnchor: 'ontology:anchorEpochStart',
        offset: 'PT0S',
        uri: 'http://trials.drugis.org/instances/moment1'
      };

      beforeEach(function() {
        var graphJsonObject = [{
          '@id': 'http://trials.drugis.org/instances/result1',
          'count': 7,
          'of_moment': 'http://trials.drugis.org/instances/moment1',
          'sample_size': 142
        }, {
          '@id': 'http://trials.drugis.org/instances/result1',
          'count': 7,
          'of_moment': 'http://trials.drugis.org/instances/moment2',
          'sample_size': 142
        }, {
          '@id': 'http://trials.drugis.org/instances/moment1',
          '@type': 'ontology:MeasurementMoment',
          'relative_to_anchor': 'ontology:anchorEpochStart',
          'relative_to_epoch': 'http://trials.drugis.org/instances/bbb',
          'time_offset': 'PT0S',
          'label': 'At start of epoch 1'
        }, {
          '@type': 'ontology:Study',
          has_outcome: []
        }];
        var graphDefer = q.defer();
        var getGraphPromise = graphDefer.promise;
        graphDefer.resolve(graphJsonObject);
        studyService.getJsonGraph.and.returnValue(getGraphPromise);
      });

      it('should delete the measurement and any results referring to it', function(done) {
        measurementMomentService.deleteItem(deleteMoment).then(function() {
          expect(studyService.saveJsonGraph).toHaveBeenCalledWith([{
            '@id': 'http://trials.drugis.org/instances/result1',
            'count': 7,
            'of_moment': 'http://trials.drugis.org/instances/moment2',
            'sample_size': 142
          }, {
            '@type': 'ontology:Study',
            has_outcome: []
          }]);
          done();
        });
        rootScope.$digest();
      });
    });
    describe('delete measurement moments used in results', function() {
      var deleteMoment = {
        uri: 'http://trials.drugis.org/instances/moment1'
      };

      beforeEach(function() {

        var studyNode = {
          '@id': 'http://trials.drugis.org/studies/study1',
          '@type': 'ontology:Study',
          'has_outcome': [{
            '@id': 'http://trials.drugis.org/instances/outcome1',
            '@type': 'ontology:AdverseEvent',
            'is_measured_at': 'http://trials.drugis.org/instances/moment1',
            'label': 'Agitation'
          }, {
            '@id': 'http://trials.drugis.org/instances/b3acd954-665e-478f-bea7-a551ebbfa66b',
            '@type': 'ontology:PopulationCharacteristic',
            'is_measured_at': ['http://trials.drugis.org/instances/moment1', 'http://trials.drugis.org/instances/moment2'],
            'label': 'Sex'
          }, {
            '@id': 'http://trials.drugis.org/instances/b3acd954-665e-478f-bea7-a551ebbfa66b',
            '@type': 'ontology:PopulationCharacteristic',
            'is_measured_at': 'http://trials.drugis.org/instances/moment2',
            'label': 'Age'
          }]
        };

        var graphJsonObject = [{
            '@id': 'http://trials.drugis.org/instances/moment1',
            '@type': 'ontology:MeasurementMoment',
            'relative_to_anchor': 'ontology:anchorEpochStart',
            'relative_to_epoch': 'http://trials.drugis.org/instances/bbb',
            'time_offset': 'PT0S',
            'label': 'At start of epoch 1'
          },
          studyNode
        ];
        var graphDefer = q.defer();
        var getGraphPromise = graphDefer.promise;
        graphDefer.resolve(graphJsonObject);
        studyService.getJsonGraph.and.returnValue(getGraphPromise);
      });

      it('should delete the measurement and any results referring to it', function(done) {

        var expected = [{
          '@id': 'http://trials.drugis.org/studies/study1',
          '@type': 'ontology:Study',
          'has_outcome': [{
            '@id': 'http://trials.drugis.org/instances/outcome1',
            '@type': 'ontology:AdverseEvent',
            'label': 'Agitation'
          }, {
            '@id': 'http://trials.drugis.org/instances/b3acd954-665e-478f-bea7-a551ebbfa66b',
            '@type': 'ontology:PopulationCharacteristic',
            'is_measured_at': 'http://trials.drugis.org/instances/moment2',
            'label': 'Sex'
          }, {
            '@id': 'http://trials.drugis.org/instances/b3acd954-665e-478f-bea7-a551ebbfa66b',
            '@type': 'ontology:PopulationCharacteristic',
            'is_measured_at': 'http://trials.drugis.org/instances/moment2',
            'label': 'Age'
          }]
        }];

        measurementMomentService.deleteItem(deleteMoment).then(function() {
          expect(studyService.saveJsonGraph).toHaveBeenCalledWith(expected);
          done();
        });
        rootScope.$digest();
      });
    });

    describe('merge', function() {
      var source = {
        uri: 'sourceUri'
      };
      var target = {
        uri: 'targetUri'
      };
      var sourceResults = [{
        id: -1
      }];
      var targetResults = [{
        id: -10
      }];
      var expectedSave = [{
        '@type': 'ontology:Study',
        has_outcome: []
      }];
      var outcome1 = {
        is_measured_at: source.uri
      };
      var outcome2 = {
        is_measured_at: ['1234567', source.uri, target.uri]
      };
      var study = {
        has_outcome: [outcome1, outcome2]
      };
      var expectedStudyAfterMeasuredAtUpdate = {
        has_outcome: [{
          is_measured_at: 'targetUri'
        }, {
          is_measured_at: ['1234567', 'targetUri']
        }]
      };
      beforeEach(function(done) {
        var graphJsonObject = [{
          '@type': 'ontology:Study',
          has_outcome: []
        }, {
          '@id': 'sourceUri',
          '@type': 'ontology:MeasurementMoment'
        }];
        var graphDefer = q.defer();
        var getGraphPromise = graphDefer.promise;
        graphDefer.resolve(graphJsonObject);
        studyService.getJsonGraph.and.returnValue(getGraphPromise);

        getStudyDefer.resolve(study);
        saveStudyDefer.resolve();
        sourceResultsDefer.resolve(sourceResults);
        targetResultsDefer.resolve(targetResults);
        mergeResultsDefer.resolve();

        // to test
        measurementMomentService.merge(source, target).then(done);

        rootScope.$digest();
      });

      it('should merge the results connected to the measurementMoments', function() {
        expect(studyService.save).toHaveBeenCalledWith(expectedStudyAfterMeasuredAtUpdate);
        expect(resultsServiceMock.queryResultsByMeasurementMoment).toHaveBeenCalledWith(source.uri);
        expect(resultsServiceMock.queryResultsByMeasurementMoment).toHaveBeenCalledWith(target.uri);
        expect(resultsServiceMock.queryResultsByMeasurementMoment.calls.count()).toBe(2);
        expect(repairServiceMock.mergeResults).toHaveBeenCalledWith(target.uri, sourceResults, targetResults,
          jasmine.any(Function), 'of_moment');
        expect(repairServiceMock.mergeResults.calls.count()).toBe(1);
        expect(studyService.saveJsonGraph.calls.mostRecent().args).toEqual([expectedSave]);

      });
    });

    describe('hasOverlap', function() {
      var source = {
        uri: 'sourceUri'
      };
      var target = {
        uri: 'targetUri'
      };
      var sourceResults = [{
        id: -1
      }];
      var targetResults = [{
        id: -10
      }];
      beforeEach(function(done) {
        sourceResultsDefer.resolve(sourceResults);
        targetResultsDefer.resolve(targetResults);

        var overlapDefer = q.defer();
        var promise = overlapDefer.promise;
        repairServiceMock.findOverlappingResults.and.returnValue(promise);
        overlapDefer.resolve([1, 2, 3]);
        // to test
        measurementMomentService.hasOverlap(source, target).then(done);

        rootScope.$digest();
      });
      it('should fetch the result and call the overlap function', function() {
        expect(resultsServiceMock.queryResultsByMeasurementMoment).toHaveBeenCalledWith(source.uri);
        expect(resultsServiceMock.queryResultsByMeasurementMoment).toHaveBeenCalledWith(target.uri);
        expect(resultsServiceMock.queryResultsByMeasurementMoment.calls.count()).toBe(2);
        expect(repairServiceMock.findOverlappingResults).toHaveBeenCalledWith(sourceResults, targetResults,
          jasmine.any(Function));
      });
    });

  });
});

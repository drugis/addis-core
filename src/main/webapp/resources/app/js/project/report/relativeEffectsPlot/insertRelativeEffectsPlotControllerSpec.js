'use strict';
define(['angular-mocks'], function() {
  describe('the insert relative effects plot controller', function() {
    var scope,
      stateParamsMock = {},
      modalInstanceMock = {
        close: function() {}
      },
      intervention1 = {
        id: 1
      },
      intervention2 = {
        id: 2
      },
      intervention3 = {
        id: 3
      },
      cacheServiceMock = jasmine.createSpyObj('CacheService', ['getModelsByProject','getAnalyses']),
      interventionResourceMock = jasmine.createSpyObj('InterventionResource', ['queryByProject']),
      reportDirectiveServiceMock = jasmine.createSpyObj('ReportDirectiveService', ['getDirectiveBuilder']),
      callbackMock = jasmine.createSpy('callback');

    var analysesDefer;
    var modelsDefer;

    beforeEach(module('addis.project'));
    beforeEach(inject(function($rootScope, $controller, $q) {
      scope = $rootScope;

      analysesDefer = $q.defer();
      var analysesQueryResult = {
        $promise: analysesDefer.promise
      };
      cacheServiceMock.getAnalyses.and.returnValue(analysesQueryResult.$promise);

      modelsDefer = $q.defer();
      var modelQueryResult = {
        $promise: modelsDefer.promise
      };
      cacheServiceMock.getModelsByProject.and.returnValue(modelQueryResult.$promise);
      reportDirectiveServiceMock.getDirectiveBuilder.and.returnValue(function() {});

      var interventionsDefer = $q.defer();
      interventionResourceMock.queryByProject.and.returnValue({
        $promise: interventionsDefer.promise
      });
      interventionsDefer.resolve([intervention1, intervention2, intervention3]);

      $controller('InsertRelativeEffectsPlotController', {
        $scope: scope,
        $q: $q,
        $stateParams: stateParamsMock,
        $modalInstance: modalInstanceMock,
        'CacheService': cacheServiceMock,
        'InterventionResource': interventionResourceMock,
        'ReportDirectiveService': reportDirectiveServiceMock,
        callback: callbackMock
      });
    }));

    describe('on load', function() {
      it('should get the analyses and models', function() {
        expect(cacheServiceMock.getAnalyses).toHaveBeenCalled();
        expect(cacheServiceMock.getModelsByProject).toHaveBeenCalled();
      });
      it('loading.loaded should be false', function() {
        expect(scope.loading.loaded).toBe(false);
      });
    });

    describe('once the analyses and models are loaded', function() {
      var interventionInclusion1 = {
          interventionId: 1,
          analysisId: 1
        },
        interventionInclusion2 = {
          interventionId: 2,
          analysisId: 4
        },
        interventionInclusion3 = {
          interventionId: 3,
          analysisId: 1
        };
      var analyses = [{
        analysisType: 'Evidence synthesis',
        id: 1,
        interventionInclusions: [interventionInclusion1, interventionInclusion3]
      }, {
        analysisType: 'Not evidence synthesis',
        id: 2,
      }, {
        analysisType: 'Evidence synthesis',
        archived: true,
        id: 3
      }, {
        analysisType: 'Evidence synthesis',
        id: 4,
        interventionInclusions: [interventionInclusion2]
      }];

      var models = [{
        id: 31,
        analysisId: 4,
        modelType: {
          type: 'network'
        }
      }, {
        id: 42,
        analysisId: 1,
        archived: true,
        modelType: {
          type: 'network'
        }
      }, {
        id: 27,
        analysisId: 1,
        modelType: {
          type: 'regression'
        },
        regressor: {
          levels: [1, 2, 3]
        }
      }, {
        id: 43,
        analysisId: 1,
        modelType: {
          type: 'pairwise'
        }
      }, {
        id: 44,
        analysisId: 1,
        modelType: {
          type: 'node-split'
        }
      }];
      beforeEach(function() {
        analysesDefer.resolve(analyses);
        modelsDefer.resolve(models);
        scope.$digest();
      });
      it('loading.loaded should be true', function() {
        expect(scope.loading.loaded).toBe(true);
      });
      it('the analyses should be put on the scope, containing the appropriate models, without archived analyses and models', function() {
        var expectedAnalyses = [{
          analysisType: 'Evidence synthesis',
          id: 1,
          models: [models[2], models[3]],
          interventionInclusions: [interventionInclusion1, interventionInclusion3],
          interventions: [intervention1, intervention3]
        }, {
          analysisType: 'Evidence synthesis',
          id: 4,
          models: [models[0]],
          interventionInclusions: [interventionInclusion2],
          interventions: [intervention2]
        }];
        expect(scope.analyses[1]).toEqual(expectedAnalyses[1]);
      });
      it('selectedAnalysisChanged should update the selected model and clear the regressionLevel if needed', function() {
        scope.selections.analysis = analyses[3];
        scope.selectedAnalysisChanged();
        expect(scope.selections.model).toEqual(models[0]);
        expect(scope.selections.regressionLevel).not.toBeDefined();
      });
    });
  });
});

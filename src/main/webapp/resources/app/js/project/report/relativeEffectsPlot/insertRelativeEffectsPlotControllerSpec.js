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
      mockAnalysis = {
        id: 1,
        primaryModel: -1,
        models: [{
          id: -1,
          modelType: {
            type: 'regression',
          },
          regressor: {
            levels: ['centering', 1, 2]
          }
        }],
        interventions: [{
          id: -10
        }]
      },
      mockNonRegressionAnalysis = {
        id: 2,
        primaryModel: -2,
        models: [{
          id: -2,
          modelType: {
            type: 'network',
          }
        }],
        interventions: [{
          id: -10
        }]
      },
      cacheServiceMock = jasmine.createSpyObj('CacheService', ['getModelsByProject', 'getAnalyses', 'getInterventions']),
      reportDirectiveServiceMock = jasmine.createSpyObj('ReportDirectiveService', ['getDirectiveBuilder', 'getNonNodeSplitModels', 'getDecoratedSyntheses']),
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
      reportDirectiveServiceMock.getDecoratedSyntheses.and.returnValue([mockAnalysis, mockNonRegressionAnalysis]);

      var interventionsDefer = $q.defer();
      cacheServiceMock.getInterventions.and.returnValue(interventionsDefer.promise);
      interventionsDefer.resolve([intervention1, intervention2, intervention3]);

      $controller('InsertRelativeEffectsPlotController', {
        $scope: scope,
        $q: $q,
        $stateParams: stateParamsMock,
        $modalInstance: modalInstanceMock,
        'CacheService': cacheServiceMock,
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
      beforeEach(function() {
        analysesDefer.resolve();
        modelsDefer.resolve();
        scope.$digest();
      });
      it('loading.loaded should be true', function() {
        expect(scope.loading.loaded).toBe(true);
      });
      it('the analyses should be put on the scope, containing the appropriate models, without archived analyses and models', function() {
        var expectedAnalyses = [mockAnalysis, mockNonRegressionAnalysis];
        expect(scope.analyses).toEqual(expectedAnalyses);
        expect(scope.selections.analysis).toEqual(mockAnalysis);
        expect(scope.selections.model).toEqual(mockAnalysis.models[0]);
        expect(scope.selections.regressionLevel).toEqual('centering');
      });
      it('selectedAnalysisChanged should update the selected model and clear the regressionLevel if needed', function() {
        scope.selections.analysis = mockNonRegressionAnalysis;
        scope.selectedAnalysisChanged();
        expect(scope.selections.model).toEqual({
          id: -2,
          modelType: {
            type: 'network',
          }
        });
        expect(scope.selections.regressionLevel).not.toBeDefined();
      });
    });
  });
});

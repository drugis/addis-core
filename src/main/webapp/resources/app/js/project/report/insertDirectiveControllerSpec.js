'use strict';
define(['angular-mocks'], function() {
  fdescribe('the insert directive controller', function() {
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
      cacheServiceMock = jasmine.createSpyObj('CacheService', ['getModelsByProject', 'getAnalyses', 'getInterventions']),
      reportDirectiveServiceMock = jasmine.createSpyObj('ReportDirectiveService', ['getDirectiveBuilder', 'getNonNodeSplitModels', 'getDecoratedSyntheses']),
      callbackMock = jasmine.createSpy('callback'),
      pataviServiceMock = jasmine.createSpy('PataviService', ['listen']);

    var analysesDefer;
    var modelsDefer;
    var interventionsDefer;

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
      interventionsDefer = $q.defer();
      cacheServiceMock.getInterventions.and.returnValue(interventionsDefer.promise);
      interventionsDefer.resolve([intervention1, intervention2, intervention3]);

      var directiveName = 'relative-effects-table';

      $controller('InsertDirectiveController', {
        $scope: scope,
        $q: $q,
        $stateParams: stateParamsMock,
        $modalInstance: modalInstanceMock,
        'ReportDirectiveService': reportDirectiveServiceMock,
        'CacheService': cacheServiceMock,
        'PataviService': pataviServiceMock,
        callback: callbackMock,
        directiveName: directiveName
      });
    }));

    describe('on load', function() {
      it('should get the analyses and models', function() {
        expect(cacheServiceMock.getAnalyses).toHaveBeenCalled();
        expect(cacheServiceMock.getModelsByProject).toHaveBeenCalled();
        expect(cacheServiceMock.getInterventions).toHaveBeenCalled();
      });
      it('loading.loaded should be false', function() {
        expect(scope.loading.loaded).toBe(false);
      });
    });

    describe('once the analyses, models, and interventions are loaded', function() {
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
      var analyses = [{
        analysisType: 'Evidence synthesis',
        id: 1,
        interventionInclusions: [interventionInclusion1, interventionInclusion3],
        models: [models[1], models[2], models[3], models[4]]
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
        interventionInclusions: [interventionInclusion2],
        models: [models[0]]
      }];
      reportDirectiveServiceMock.getNonNodeSplitModels.and.returnValue(models);
      reportDirectiveServiceMock.getDecoratedSyntheses.and.returnValue(analyses);

      beforeEach(function() {
        analysesDefer.resolve(analyses);
        modelsDefer.resolve(models);
        scope.$digest();
      });

      it('loading.loaded should be true', function() {
        expect(scope.loading.loaded).toBe(true);
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

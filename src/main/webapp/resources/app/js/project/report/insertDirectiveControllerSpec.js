'use strict';
define(['angular-mocks'], function() {
  var scope,
    stateParamsMock = {},
    modalInstanceMock = {
      close: function() {}
    },
    intervention1 = {
      id: 1,
      name: 'flopsetine'
    },
    intervention2 = {
      id: 2,
      name: 'plopsetine'
    },
    intervention3 = {
      id: 3,
      name: 'hopsetine'
    },
    cacheServiceMock = jasmine.createSpyObj('CacheService', ['getModelsByProject', 'getAnalyses', 'getInterventions']),
    reportDirectiveServiceMock = jasmine.createSpyObj('ReportDirectiveService', ['getDirectiveBuilder', 'getAllowedModels',
      'getDecoratedSyntheses', 'getShowSettings'
    ]),
    callbackMock = jasmine.createSpy('callback'),
    pataviServiceMock = jasmine.createSpyObj('PataviService', ['listen']),
    analysesDefer,
    modelsDefer,
    interventionsDefer,
    pataviResultsDefer;

  describe('the insert directive controller for directives other than comparison result', function() {
    beforeEach(angular.mock.module('addis.project'));
    beforeEach(inject(function($rootScope, $controller, $q) {
      initController($rootScope, $controller, $q, 'relative-effects-table');
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
        id: 27,
        analysisId: 1,
        modelType: {
          type: 'regression'
        },
        regressor: {
          levels: [1, 2, 3]
        }
      }, {
        id: 42,
        analysisId: 1,
        archived: true,
        modelType: {
          type: 'network'
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
        interventions: [interventionInclusion1, interventionInclusion3],
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
        interventions: [interventionInclusion2],
        models: [models[0]]
      }];

      beforeEach(function() {
        reportDirectiveServiceMock.getAllowedModels.and.returnValue(models);
        reportDirectiveServiceMock.getDecoratedSyntheses.and.returnValue(analyses);

        analysesDefer.resolve(analyses);
        modelsDefer.resolve(models);
        scope.$digest();
      });

      it('loading.loaded should be true', function() {
        expect(scope.loading.loaded).toBe(true);
      });

      it('should automatically select defaults', function() {
        expect(scope.selections.analysis).toEqual(analyses[0]);
        expect(scope.selections.model).toEqual(models[1]);
        expect(scope.selections.regressionLevel).toBe(1);
      });

      it('selectedAnalysisChanged should update the selected model and clear the regressionLevel if needed', function() {
        scope.selections.analysis = analyses[3];
        scope.selectedAnalysisChanged();
        expect(scope.selections.model).toEqual(models[0]);
        expect(scope.selections.regressionLevel).not.toBeDefined();
      });
    });
  });

  describe('the insert directive controller for the comparison result directive', function() {
    beforeEach(angular.mock.module('addis.project'));
    beforeEach(inject(function($rootScope, $controller, $q) {
      initController($rootScope, $controller, $q, 'comparison-result');
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
      var models = [{
        id: 31,
        analysisId: 1,
        taskUrl: 'taskUrl1',
        modelType: {
          type: 'network'
        }
      }, {
        id: 27,
        analysisId: 1,
        taskUrl: 'taskUrl2',
        modelType: {
          type: 'regression'
        },
        regressor: {
          levels: [1, 2, 3]
        }
      }];
      var analyses = [{
        analysisType: 'Evidence synthesis',
        id: 1,
        interventionInclusions: [],
        models: [models[0], models[1]],
        interventions: [{label: 'intervention1'}]
      }];


      beforeEach(function() {
        reportDirectiveServiceMock.getAllowedModels.and.returnValue(models);
        reportDirectiveServiceMock.getDecoratedSyntheses.and.returnValue(analyses);

        pataviServiceMock.listen.and.returnValue(pataviResultsDefer.promise);
        pataviResultsDefer.resolve({
          relativeEffects: {
            centering: [{
              t1: 1,
              t2: 2
            }]
          }
        });
        analysesDefer.resolve(analyses);
        modelsDefer.resolve(models);
        scope.$digest();
      });

      it('loading.loaded should be true', function() {
        expect(scope.loading.loaded).toBe(true);
      });

      it('should automatically select defaults', function() {
        expect(scope.selections.analysis).toEqual(analyses[0]);
        expect(scope.selections.model).toEqual(models[0]);
        expect(scope.selections.model.comparisons).toEqual([{
          label: 'flopsetine - plopsetine',
          t1: 1,
          t2: 2
        }]);
        expect(scope.selections.regressionLevel).toBeUndefined();
      });
    });
  });

  function initController($rootScope, $controller, $q, directiveName) {
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
    interventionsDefer = $q.defer();


    pataviResultsDefer = $q.defer();

    cacheServiceMock.getInterventions.and.returnValue(interventionsDefer.promise);
    interventionsDefer.resolve([intervention1, intervention2, intervention3]);

    reportDirectiveServiceMock.getDirectiveBuilder.and.returnValue(function() {});

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
  }
});

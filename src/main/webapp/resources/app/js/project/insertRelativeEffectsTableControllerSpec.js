'use strict';
define(['angular-mocks'], function() {
  fdescribe('the insert relative effects table controller', function() {
    var scope, q,
      stateParamsMock = {},
      modalInstanceMock = {
        close: function() {}
      },
      analysisResourceMock = jasmine.createSpyObj('AnalysisResource', ['query']),
      modelResourceMock = jasmine.createSpyObj('ModelResource', ['queryByProject']),
      reportDirectiveServiceMock = jasmine.createSpyObj('ReportDirectiveService', ['getDirectiveBuilder']),
      callbackMock = jasmine.createSpy('callback');

    var analysesDefer;
    var modelsDefer;

    beforeEach(module('addis.project'));
    beforeEach(inject(function($rootScope, $controller, $q) {
      scope = $rootScope;
      q = $q;

      analysesDefer = $q.defer();
      var analysesQueryResult = {
        $promise: analysesDefer.promise
      };
      analysisResourceMock.query.and.returnValue(analysesQueryResult);

      modelsDefer = $q.defer();
      var modelQueryResult = {
        $promise: modelsDefer.promise
      };
      modelResourceMock.queryByProject.and.returnValue(modelQueryResult);
      reportDirectiveServiceMock.getDirectiveBuilder.and.returnValue(function() {});

      $controller('InsertRelativeEffectsTableController', {
        $scope: scope,
        $q: $q,
        $stateParams: stateParamsMock,
        $modalInstance: modalInstanceMock,
        'AnalysisResource': analysisResourceMock,
        'ModelResource': modelResourceMock,
        'ReportDirectiveService': reportDirectiveServiceMock,
        callback: callbackMock
      });
    }));

    describe('on load', function() {
      it('should get the analyses and models', function() {
        expect(analysisResourceMock.query).toHaveBeenCalled();
        expect(modelResourceMock.queryByProject).toHaveBeenCalled();
      });
      it('loading.loaded should be false', function() {
        expect(scope.loading.loaded).toBe(false);
      });
    });

    describe('once the analyses and models are loaded', function() {
      var analyses = [{
        analysisType: 'Evidence synthesis',
        id: 1
      }, {
        analysisType: 'Not evidence synthesis',
        id: 2,
      }, {
        analysisType: 'Evidence synthesis',
        archived: true,
        id: 3,
      }, {
        analysisType: 'Evidence synthesis',
        id: 4,
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
          models: [models[2], models[3]]
        }, {
          analysisType: 'Evidence synthesis',
          id: 4,
          models: [models[0]]
        }];
        expect(scope.analyses).toEqual(expectedAnalyses);
        expect(scope.selections.analysis).toEqual(expectedAnalyses[0]);
        expect(scope.selections.model).toEqual(models[2]);
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
});

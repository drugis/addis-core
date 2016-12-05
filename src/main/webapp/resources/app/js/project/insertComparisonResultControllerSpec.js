'use strict';
define(['angular-mocks'], function() {
  describe('the insert comparison result controller', function() {
    var scope,
      stateParamsMock = {},
      modalInstanceMock = {
        close: function() {}
      },
      analysisResourceMock = jasmine.createSpyObj('AnalysisResource', ['query']),
      modelResourceMock = jasmine.createSpyObj('ModelResource', ['getConsistencyModels']),
      reportDirectiveServiceMock = jasmine.createSpyObj('ReportDirectiveService', ['getDirectiveBuilder']),
      interventionResourceMock = jasmine.createSpyObj('InterventionResource', ['query']),
      pataviServiceMock = jasmine.createSpyObj('PataviService', ['listen']),
      callbackMock = jasmine.createSpy('callback');

    var analysesDefer;
    var modelsDefer;
    var interventionsDefer;

    beforeEach(module('addis.project'));
    beforeEach(inject(function($rootScope, $controller, $q) {
      scope = $rootScope;

      analysesDefer = $q.defer();
      var getAnalyses = {
        $promise: analysesDefer.promise
      };
      analysisResourceMock.query.and.returnValue(getAnalyses);

      modelsDefer = $q.defer();
      var getModels = {
        $promise: modelsDefer.promise
      };
      modelResourceMock.getConsistencyModels.and.returnValue(getModels);

      interventionsDefer = $q.defer();
      var getInterventions = {
        $promise: interventionsDefer.promise
      };
      interventionResourceMock.query.and.returnValue(getInterventions);

      reportDirectiveServiceMock.getDirectiveBuilder.and.returnValue(function() {});

      $controller('InsertComparisonResultController', {
        $scope: scope,
        $q: $q,
        $stateParams: stateParamsMock,
        $modalInstance: modalInstanceMock,
        'AnalysisResource': analysisResourceMock,
        'ModelResource': modelResourceMock,
        'ReportDirectiveService': reportDirectiveServiceMock,
        'PataviService': pataviServiceMock,
        'InterventionResource': interventionResourceMock,
        callback: callbackMock
      });
    }));

    describe('on load', function() {
      it('should get the analyses, models and interventions', function() {
        expect(analysisResourceMock.query).toHaveBeenCalled();
        expect(modelResourceMock.getConsistencyModels).toHaveBeenCalled();
        expect(interventionResourceMock.query).toHaveBeenCalled();
      });
      it('should place insertComparisonResult on the scope which calls the callback with a newly-built result comparison', function() {
        scope.selections = {
          analysis: {
            analysisType: 'Evidence synthesis',
            id: 1
          },
          model: {
            id: 2
          },
          comparison: {
            t1: 1,
            t2: 2
          }
        };
        expect(scope.insertComparisonResult).toBeDefined();
        scope.insertComparisonResult();
        expect(reportDirectiveServiceMock.getDirectiveBuilder).toHaveBeenCalledWith('result-comparison');
        expect(callbackMock).toHaveBeenCalled();
      });
    });
    describe('once the analyses, models and interventions are loaded', function() {
      beforeEach(function() {
        analysesDefer.resolve([{
          analysisType: 'Evidence synthesis',
          id: 1
        }, {
          analysisType: 'Not evidence synthesis',
          id: 2,
        }]);
        modelsDefer.resolve([{
          id: 31,
          analysisId: 2
        }, {
          id: 42,
          analysisId: 1
        }]);
        interventionsDefer.resolve([{
          id: 51
        }, {
          id: 62
        }]);
        scope.$digest();
      });
      it('the analyses should be put on the scope, filtered on the analysis type "evidence synthesis", containing the appropriate models', function() {
        var expectedResult = [{
          analysisType: 'Evidence synthesis',
          id: 1,
          models: [{
            id: 42,
            analysisId: 1
          }]
        }];
        expect(scope.analyses).toEqual(expectedResult);
      });
    });
  });
});

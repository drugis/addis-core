'use strict';
define(['angular-mocks'], function() {
  describe('the insert comparison result controller', function() {
    var scope, q,
      stateParamsMock = {},
      modalInstanceMock = {
        close: function() {}
      },
      cacheServiceMock = jasmine.createSpyObj('CacheService', ['getConsistencyModels','getAnalyses']),
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
      q = $q;

      analysesDefer = $q.defer();
      var getAnalyses = {
        $promise: analysesDefer.promise
      };
      cacheServiceMock.getAnalyses.and.returnValue(getAnalyses.$promise);

      modelsDefer = $q.defer();
      var getModels = {
        $promise: modelsDefer.promise
      };
      cacheServiceMock.getConsistencyModels.and.returnValue(getModels.$promise);

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
        'CacheService': cacheServiceMock,
        'ReportDirectiveService': reportDirectiveServiceMock,
        'PataviService': pataviServiceMock,
        'InterventionResource': interventionResourceMock,
        callback: callbackMock
      });
    }));

    describe('on load', function() {
      it('should get the analyses, models and interventions', function() {
        expect(cacheServiceMock.getAnalyses).toHaveBeenCalled();
        expect(cacheServiceMock.getConsistencyModels).toHaveBeenCalled();
        expect(interventionResourceMock.query).toHaveBeenCalled();
      });
      it('loading.loaded should be false', function() {
        expect(scope.loading.loaded).toBe(false);
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
          t1: {
            id: 't1id'
          },
          t2: {
            id: 't2id'
          }
        };
        expect(scope.insertComparisonResult).toBeDefined();
        scope.insertComparisonResult();
        expect(reportDirectiveServiceMock.getDirectiveBuilder).toHaveBeenCalledWith('result-comparison');
        expect(callbackMock).toHaveBeenCalled();
      });
    });
    describe('once the analyses, models and interventions are loaded', function() {
      var pataviResultDefer;
      beforeEach(function() {
        pataviResultDefer = q.defer();
        pataviServiceMock.listen.and.returnValue(pataviResultDefer.promise);
        analysesDefer.resolve([{
          analysisType: 'Evidence synthesis',
          id: 1
        }, {
          analysisType: 'Not evidence synthesis',
          id: 2,
        }]);
        modelsDefer.resolve([{
          id: 31,
          analysisId: 2,
          taskUrl: 'taskUrl3',
          modelType: {
            type: 'network'
          }
        }, {
          id: 42,
          analysisId: 1,
          taskUrl: 'taskUrl1',
          modelType: {
            type: 'network'
          }
        }, {
          id: 43,
          analysisId: 1,
          taskUrl: 'taskUrl2',
          modelType: {
            type: 'pairwise'
          }
        }]);
        interventionsDefer.resolve([{
          id: 51,
          name: 'treatment 1'
        }, {
          id: 62,
          name: 'treatment 2'
        }]);
        scope.$digest();
      });
      it('should retrieve model results for each model', function() {
        expect(pataviServiceMock.listen).toHaveBeenCalledWith('taskUrl1');
      });
      describe('once the model results are loaded', function() {
        beforeEach(function() {
          var modelResults = {
            relativeEffects: {
              centering: [{
                t1: 51,
                t2: 62
              }]
            }
          };
          pataviResultDefer.resolve(modelResults);
          scope.$digest();
        });
        it('loading.loaded should be true', function() {
          expect(scope.loading.loaded).toBe(true);
        });
        it('the analyses should be put on the scope, filtered on the analysis type "evidence synthesis", containing the appropriate models', function() {
          var expectedResult = [{
            analysisType: 'Evidence synthesis',
            id: 1,
            models: [{
              id: 42,
              analysisId: 1,
              taskUrl: 'taskUrl1',
              modelType: {
                type: 'network'
              },
              comparisons: [{
                t1: 51,
                t2: 62,
                label: 'treatment 1 - treatment 2'
              }]
            }]
          }];
          expect(scope.analyses).toEqual(expectedResult);
        });
      });
    });
  });
});

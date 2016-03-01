'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('meta benefit-risk step 1 controller', function() {

    var scope, q,
      stateParamsMock = {
        projectId: 1
      },
      analysisResourceMock = jasmine.createSpyObj('AnalysisResource', ['get', 'query']),
      interventionResourceMock = jasmine.createSpyObj('InterventionResource', ['query']),
      outcomeResourceMock = jasmine.createSpyObj('OutcomeResource', ['query']),
      analysisDefer,
      analysisQueryDefer,
      interventionDefer,
      outcomeDefer,
      metaBenefitRiskService = jasmine.createSpyObj('MetaBenefitRiskService', ['buildOutcomesWithAnalyses']);

    beforeEach(module('addis.analysis'));


    beforeEach(angularMocks.inject(function($rootScope, $q, $controller) {
      scope = $rootScope;
      q = $q;
      analysisDefer = q.defer();
      analysisQueryDefer = q.defer();
      interventionDefer = q.defer();
      outcomeDefer = q.defer();

      analysisResourceMock.get.and.returnValue({
        $promise: analysisDefer.promise
      });
      analysisResourceMock.query.and.returnValue({
        $promise: analysisQueryDefer.promise
      });
      interventionResourceMock.query.and.returnValue({
        $promise: interventionDefer.promise
      });
      outcomeResourceMock.query.and.returnValue({
        $promise: outcomeDefer.promise
      });

      metaBenefitRiskService.buildOutcomesWithAnalyses.and.returnValue([]);

      $controller('MetaBenefitRiskStep1Controller', {
        $scope: scope,
        $q: q,
        $stateParams: stateParamsMock,
        AnalysisResource: analysisResourceMock,
        InterventionResource: interventionResourceMock,
        OutcomeResource: outcomeResourceMock,
        MetaBenefitRiskService: metaBenefitRiskService
      });

    }));

    describe('when the analysis, outcomes and alternatives are loaded', function() {
      beforeEach(function() {
        var analysis = {
          includedAlternatives: [{
            id: 1
          }],
          mbrOutcomeInclusions: [{
            outcome: {
              id: 1
            },
            networkMetaAnalysis: undefined
          }]
        };
        var interventions = [{
          id: 1
        }, {
          id: 2
        }];
        var outcomes = [{
          id: 1
        }, {
          id: 2
        }];
        var networkAnalyses = [{
          outcome: {
            id: 1
          }
        }, {
          outcome: {
            id: 2
          }
        }];
        analysisDefer.resolve(analysis);
        interventionDefer.resolve(interventions);
        outcomeDefer.resolve(outcomes);
        analysisQueryDefer.resolve(networkAnalyses);
        scope.$digest();
      });

      it('should build the outcomesWithAnalyses', function() {
        expect(metaBenefitRiskService.buildOutcomesWithAnalyses).toHaveBeenCalled();
      });

    });

    describe('when updateMbrOutcomeInclusions is called by checking the outcome', function() {
      var outcomeWithAnalysis;
      beforeEach(function() {
        outcomeWithAnalysis = {
          outcome: {
            id: 3,
            isIncluded: true
          },
          networkMetaAnalyses: [{
            id: 5
          }]
        };
        scope.analysis = {
          id: 1,
          $save: function() {}
        };
        scope.outcomesWithAnalyses = [outcomeWithAnalysis];

        scope.updateMbrOutcomeInclusions(outcomeWithAnalysis);
      });

      it('should select the first analysis belonging to the outcome', function() {
        expect(outcomeWithAnalysis.selectedAnalysisId).toBe(5);
      });

      it('should build the metabenefitrisk analysis inclusions', function() {
        expect(scope.analysis.mbrOutcomeInclusions).toEqual([{
          metaBenefitRiskAnalysisId: 1,
          outcomeId: 3,
          networkMetaAnalysisId: 5
        }]);
      });
    });


    describe('when updateMbrOutcomeInclusions is called by UNchecking the outcome', function() {
      var outcomeWithAnalysis;
      beforeEach(function() {
        outcomeWithAnalysis = {
          outcome: {
            id: 3,
            isIncluded: false
          },
          networkMetaAnalyses: [{
            id: 5
          }]
        };
        scope.analysis = {
          id: 1,
          mbrOutcomeInclusions: [{
            metaBenefitRiskAnalysisId: 1,
            outcomeId: 3,
            networkMetaAnalysisId: 5
          }],
          $save: function() {}
        };
        scope.outcomesWithAnalyses = [outcomeWithAnalysis];

        scope.updateMbrOutcomeInclusions(outcomeWithAnalysis);
      });

      it('should unselect the analysis belonging to the outcome', function() {
        expect(outcomeWithAnalysis.selectedAnalysisId).toBeUndefined();
      });

      it('should remove the unchecked outcome', function() {
        expect(scope.analysis.mbrOutcomeInclusions).toEqual([]);
      });
    });


  });
});

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
      modelResourceMock = jasmine.createSpyObj('OutcomeResource', ['getConsistencyModels']),
      analysisDefer,
      analysisQueryDefer,
      interventionDefer,
      outcomeDefer,
      modelsDefer,
      metaBenefitRiskService = jasmine.createSpyObj('MetaBenefitRiskService', [
        'addModelsGroup',
        'compareAnalysesByModels',
        'joinModelsWithAnalysis',
        'buildOutcomesWithAnalyses',
        'numberOfSelectedInterventions',
        'numberOfSelectedOutcomes',
        'isModelWithMissingAlternatives',
        'isModelWithoutResults',
        'findMissingAlternatives'
      ]);

    beforeEach(module('addis.analysis'));


    beforeEach(angularMocks.inject(function($rootScope, $q, $controller) {
      scope = $rootScope;
      q = $q;
      analysisDefer = q.defer();
      analysisQueryDefer = q.defer();
      interventionDefer = q.defer();
      outcomeDefer = q.defer();
      modelsDefer = q.defer();

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
      modelResourceMock.getConsistencyModels.and.returnValue({
        $promise: modelsDefer.promise
      });

      metaBenefitRiskService.compareAnalysesByModels.and.returnValue(0);
      metaBenefitRiskService.joinModelsWithAnalysis.and.returnValue([]);

      $controller('MetaBenefitRiskStep1Controller', {
        $scope: scope,
        $q: q,
        $stateParams: stateParamsMock,
        AnalysisResource: analysisResourceMock,
        InterventionResource: interventionResourceMock,
        OutcomeResource: outcomeResourceMock,
        MetaBenefitRiskService: metaBenefitRiskService,
        ModelResource: modelResourceMock
      });

    }));

    describe('when the analysis, outcomes and alternatives are loaded', function() {
      beforeEach(function() {
        metaBenefitRiskService.buildOutcomesWithAnalyses.and.returnValue({
          networkMetaAnalyses: []
        });

        analysisDefer.resolve({
          mbrOutcomeInclusions: []
        });
        interventionDefer.resolve([]);
        outcomeDefer.resolve([{}]);
        analysisQueryDefer.resolve([{}]);
        modelsDefer.resolve([]);
        scope.$digest();
      });

      it('should build the outcomesWithAnalyses', function() {
        expect(metaBenefitRiskService.joinModelsWithAnalysis).toHaveBeenCalled();
        expect(metaBenefitRiskService.addModelsGroup).toHaveBeenCalled();
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
            id: 5,
            models: [{
              id: 1
            }]
          }]
        };
        scope.analysis = {
          id: 1,
          $save: function() {},
          includedAlternatives: []
        };
        scope.outcomesWithAnalyses = [outcomeWithAnalysis];

        scope.updateMbrOutcomeInclusions(outcomeWithAnalysis);
      });

      it('should select the first analysis belonging to the outcome', function() {
        expect(outcomeWithAnalysis.selectedAnalysis.id).toBe(5);
      });

      it('should build the metabenefitrisk analysis inclusions', function() {
        expect(scope.analysis.mbrOutcomeInclusions).toEqual([{
          metaBenefitRiskAnalysisId: 1,
          outcomeId: 3,
          networkMetaAnalysisId: 5,
          modelId: 1
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
            id: 5,
            models: []
          }],
          selectedAnalysis: {
            id: 1
          },
          selectedModel: {}
        };
        scope.analysis = {
          id: 1,
          mbrOutcomeInclusions: [{
            metaBenefitRiskAnalysisId: 1,
            outcomeId: 3,
            networkMetaAnalysisId: 5
          }],
          $save: function() {},
          includedAlternatives: []
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

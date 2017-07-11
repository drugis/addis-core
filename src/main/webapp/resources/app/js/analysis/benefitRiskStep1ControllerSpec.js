'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('benefit-risk step 1 controller', function() {

    var scope, q,
      stateParamsMock = {
        projectId: 1
      },
      analysisResourceMock = jasmine.createSpyObj('AnalysisResource', ['get', 'query', 'save']),
      interventionResourceMock = jasmine.createSpyObj('InterventionResource', ['query']),
      outcomeResourceMock = jasmine.createSpyObj('OutcomeResource', ['query']),
      modelResourceMock = jasmine.createSpyObj('OutcomeResource', ['getConsistencyModels']),
      projectResourceMock = jasmine.createSpyObj('ProjectResource', ['get']),
      projectStudiesResourceMock = jasmine.createSpyObj('ProjectStudiesResource', ['query']),
      userServiceMock = jasmine.createSpyObj('UserService', ['isLoginUserId']),
      singleStudyBenefitRiskServiceMock = jasmine.createSpyObj('SingleStudyBenefitRiskService', ['addMissingInterventionsToStudies',
        'addHasMatchedMixedTreatmentArm', 'addOverlappingInterventionsToStudies']),
      analysisDefer,
      analysisQueryDefer,
      interventionDefer,
      outcomeDefer,
      studiesDefer,
      modelsDefer,
      projectDefer,
      benefitRiskService = jasmine.createSpyObj('BenefitRiskService', [
        'addModelsGroup',
        'compareAnalysesByModels',
        'joinModelsWithAnalysis',
        'buildOutcomeWithAnalyses',
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
      projectDefer = q.defer();
      studiesDefer = q.defer();

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
      projectResourceMock.get.and.returnValue({
        $promise: projectDefer.promise
      });
      projectStudiesResourceMock.query.and.returnValue({
        $promise: studiesDefer.promise
      });
      userServiceMock.isLoginUserId.and.returnValue(true);

      benefitRiskService.compareAnalysesByModels.and.returnValue(0);
      benefitRiskService.joinModelsWithAnalysis.and.returnValue([]);

      $controller('BenefitRiskStep1Controller', {
        $scope: scope,
        $q: q,
        $stateParams: stateParamsMock,
        $state: {
          go: function() {}
        },
        ProjectStudiesResource: projectStudiesResourceMock,
        AnalysisResource: analysisResourceMock,
        InterventionResource: interventionResourceMock,
        OutcomeResource: outcomeResourceMock,
        BenefitRiskService: benefitRiskService,
        ModelResource: modelResourceMock,
        ProjectResource: projectResourceMock,
        UserService: userServiceMock,
        SingleStudyBenefitRiskService: singleStudyBenefitRiskServiceMock
      });

    }));

    fdescribe('when the analysis, outcomes, models, studies and alternatives are loaded', function() {
      beforeEach(function() {
        benefitRiskService.buildOutcomeWithAnalyses.and.returnValue({
          networkMetaAnalyses: []
        });

        analysisDefer.resolve({
          benefitRiskNMAOutcomeInclusions: [],
          benefitRiskStudyOutcomeInclusions: []
        });
        interventionDefer.resolve([]);
        outcomeDefer.resolve([{}]);
        analysisQueryDefer.resolve([]);
        modelsDefer.resolve([]);
        studiesDefer.resolve([]);
        scope.$digest();
      });

      it('should build the outcomesWithAnalyses', function() {
        expect(benefitRiskService.joinModelsWithAnalysis).toHaveBeenCalled();
        expect(benefitRiskService.addModelsGroup).toHaveBeenCalled();
        expect(benefitRiskService.buildOutcomeWithAnalyses).toHaveBeenCalled();
      });

    });

    describe('when updateBenefitRiskNMAOutcomeInclusions is called by checking the outcome', function() {
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
          interventionInclusions: []
        };
        scope.outcomesWithAnalyses = [outcomeWithAnalysis];

        scope.updateBenefitRiskNMAOutcomeInclusions(outcomeWithAnalysis);
      });

      it('should select the first analysis belonging to the outcome', function() {
        expect(outcomeWithAnalysis.selectedAnalysis.id).toBe(5);
      });

      it('should build the benefitrisk analysis inclusions', function() {
        expect(scope.analysis.benefitRiskNMAOutcomeInclusions).toEqual([{
          benefitRiskAnalysisId: 1,
          outcomeId: 3,
          networkMetaAnalysisId: 5,
          modelId: 1
        }]);
      });
    });

    describe('when updateBenefitRiskNMAOutcomeInclusions is called by UNchecking the outcome', function() {
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
          benefitRiskNMAOutcomeInclusions: [{
            benefitRiskAnalysisId: 1,
            outcomeId: 3,
            networkMetaAnalysisId: 5
          }],
          $save: function() {},
          interventionInclusions: []
        };
        scope.outcomesWithAnalyses = [outcomeWithAnalysis];

        scope.updateBenefitRiskNMAOutcomeInclusions(outcomeWithAnalysis);
      });

      it('should unselect the analysis belonging to the outcome', function() {
        expect(outcomeWithAnalysis.selectedAnalysisId).toBeUndefined();
      });

      it('should remove the unchecked outcome', function() {
        expect(scope.analysis.benefitRiskNMAOutcomeInclusions).toEqual([]);
      });
    });


  });
});

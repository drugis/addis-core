'use strict';
define(['angular-mocks', './analysis'], function() {
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
      singleStudyBenefitRiskServiceMock = jasmine.createSpyObj('SingleStudyBenefitRiskService', [
        'getStudiesWithErrors'
      ]),
      scenarioResourceMock = jasmine.createSpyObj('ScenarioResource', ['query']),
      subProblemResourceMock = jasmine.createSpyObj('SubProblemResource', ['query']),
      problemResourceMock = jasmine.createSpyObj('ProblemResource', ['get']),
      workspaceServiceMock = jasmine.createSpyObj('WorkspaceService', ['reduceProblem']),
      pageTitleServiceMock = jasmine.createSpyObj('PageTitleService', ['setPageTitle']),
      DEFAULT_VIEW = 'default view',
      analysisDefer,
      analysisQueryDefer,
      interventionDefer,
      outcomeDefer,
      studiesDefer,
      modelsDefer,
      projectDefer,
      benefitRiskService = jasmine.createSpyObj('BenefitRiskService', [
        'finalizeAndGoToDefaultScenario',
        'getOutcomesWithInclusions'
        
      ]);
      var benefitRiskStep1ServiceMock = jasmine.createSpyObj('BenefitRiskStep1Service',[
        'analysisUpdateCommand',
        'buildOutcomesWithAnalyses',
        'findOverlappingInterventions',
        'getNMAOutcomeInclusions',
        'getStep1Errors',
        'getStudyOutcomeInclusions',
        'isContrastStudySelected',
        'updateOutcomeInclusion',
        'updateMissingAlternatives'
      ]);

    beforeEach(angular.mock.module('addis.analysis'));

    beforeEach(inject(function($rootScope, $q, $controller) {
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
        owner: {
          id: 1
        },
        $promise: projectDefer.promise
      });
      projectStudiesResourceMock.query.and.returnValue({
        $promise: studiesDefer.promise
      });
      userServiceMock.isLoginUserId.and.returnValue($q.resolve(true));

      benefitRiskStep1ServiceMock.findOverlappingInterventions.and.returnValue([]);
      benefitRiskStep1ServiceMock.getStep1Errors.and.returnValue([]);
      benefitRiskStep1ServiceMock.buildOutcomesWithAnalyses.and.returnValue([]);
      benefitRiskStep1ServiceMock.getStudyOutcomeInclusions.and.returnValue([]);
      benefitRiskStep1ServiceMock.getNMAOutcomeInclusions.and.returnValue([]);

      $controller('BenefitRiskStep1Controller', {
        $scope: scope,
        $q: q,
        $stateParams: stateParamsMock,
        $state: {
          go: function() { }
        },
        ProjectStudiesResource: projectStudiesResourceMock,
        AnalysisResource: analysisResourceMock,
        InterventionResource: interventionResourceMock,
        OutcomeResource: outcomeResourceMock,
        BenefitRiskService: benefitRiskService,
        BenefitRiskStep1Service: benefitRiskStep1ServiceMock,
        ModelResource: modelResourceMock,
        ProjectResource: projectResourceMock,
        UserService: userServiceMock,
        SingleStudyBenefitRiskService: singleStudyBenefitRiskServiceMock,
        ScenarioResource: scenarioResourceMock,
        SubProblemResource: subProblemResourceMock,
        ProblemResource: problemResourceMock,
        WorkspaceService: workspaceServiceMock,
        DEFAULT_VIEW: DEFAULT_VIEW,
        PageTitleService: pageTitleServiceMock
      });

    }));

    describe('when the analysis, outcomes, models, studies and alternatives are loaded', function() {
      beforeEach(function() {
        projectDefer.resolve({
          owner: {
            id: 1
          }
        });
        analysisDefer.resolve({
          benefitRiskNMAOutcomeInclusions: [{}],
          benefitRiskStudyOutcomeInclusions: [{}]
        });
        interventionDefer.resolve([{}]);
        outcomeDefer.resolve([{
        }]);
        analysisQueryDefer.resolve([{}]);
        modelsDefer.resolve([{}]);
        studiesDefer.resolve([{}]);
        scope.$digest();
      });

      it('should build the outcomesWithAnalyses', function() {
        expect(benefitRiskService.getOutcomesWithInclusions).toHaveBeenCalled();
      });
    });

    describe('when updateOutcomeInclusion is called by (un-)checking the outcome', function() {
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
          }],
          selectedAnalysis: {
            id: 1
          },
          selectedModel: {},
          dataType: 'network'
        };
        scope.analysis = {
          id: 1,
          benefitRiskNMAOutcomeInclusions: [{
            analysisId: 1,
            outcomeId: 3,
            networkMetaAnalysisId: 5
          }],
          benefitRiskStudyOutcomeInclusions: [{
            analysisId: 1,
            outcomeId: 3,
            studyGraphUri: 'http://ryfari.com'
          }],
          $save: function() { },
          interventionInclusions: []
        };
        scope.outcomesWithAnalyses = [outcomeWithAnalysis];
        scope.includedAlternatives = [];
        scope.overlappingInterventions = [];
        scope.updateOutcomeInclusion({});
      });

      it('should call the benefitriskservice', function() {
        expect(benefitRiskStep1ServiceMock.updateOutcomeInclusion).toHaveBeenCalled();
      });
    });
  });
});

'use strict';
define(['angular-mocks', './analysis'], function() {
  describe('benefit-risk step 2 controller', function() {

    var scope, q,
      stateParamsMock = {
        projectId: 37
      },
      versionUuid = 'version-1',
      datasetOwnerId = 'datasetOwnerId',
      project = {
        id: 37,
        namespaceUid: 'namespace1',
        datasetVersion: 'https://trials.drugis.org/versions/' + versionUuid,
        owner: {
          id: 1
        }
      },
      analysis = {
        title: 'benefit-risk analysis',
        interventionInclusions: [{
          interventionId: 10
        }, {
          interventionId: 100
        }],
        benefitRiskNMAOutcomeInclusions: [{
          outcomeId: 2
        }, {
          outcomeId: 20
        }],
        benefitRiskStudyOutcomeInclusions: []
      },
      analysisResourceMock = jasmine.createSpyObj('AnalysisResource', ['get', 'query', 'save']),
      interventionResourceMock = jasmine.createSpyObj('InterventionResource', ['query']),
      outcomeResourceMock = jasmine.createSpyObj('OutcomeResource', ['query']),
      modelResourceMock = jasmine.createSpyObj('OutcomeResource', ['getConsistencyModels']),
      projectResourceMock = jasmine.createSpyObj('ProjectResource', ['get']),
      projectStudiesResourceMock = jasmine.createSpyObj('ProjectStudiesResource', ['query']),
      userServiceMock = jasmine.createSpyObj('UserService', ['isLoginUserId']),
      modalMock = jasmine.createSpyObj('$modal', ['open']),
      problemResourceMock = jasmine.createSpyObj('ProblemResource', ['get']),
      workspaceServiceMock = jasmine.createSpyObj('WorkspaceService', ['getObservedScales']),
      pageTitleServiceMock = jasmine.createSpyObj('PageTitleService', ['setPageTitle']),
      trialverseResourceMock = jasmine.createSpyObj('TrialverseResource', ['get']),
      analysisDefer,
      analysisQueryDefer,
      interventionDefer,
      outcomeDefer,
      studiesDefer,
      modelsDefer,
      projectDefer,
      datasetDefer,
      benefitRiskService = jasmine.createSpyObj('BenefitRiskService', [
        'addModelsGroup',
        'compareAnalysesByModels',
        'joinModelsWithAnalysis',
        'buildOutcomeWithAnalyses',
        'buildOutcomesWithAnalyses',
        'numberOfSelectedInterventions',
        'numberOfSelectedOutcomes',
        'isModelWithMissingAlternatives',
        'isModelWithoutResults',
        'isMissingAnalysis',
        'isMissingDataType',
        'findMissingAlternatives',
        'findOverlappingInterventions',
        'isInvalidStudySelected',
        'hasMissingStudy',
        'findOverlappingOutcomes',
        'addStudiesToOutcomes',
        'addModelBaseline',
        'analysisToSaveCommand',
        'finalizeAndGoToDefaultScenario'
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
      datasetDefer = q.defer();

      analysisResourceMock.get.and.returnValue({
        $promise: analysisDefer.promise
      });
      analysisResourceMock.query.and.returnValue({
        $promise: analysisQueryDefer.promise
      });
      analysisResourceMock.save.and.returnValue({
        $promise: $q.resolve({})
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
      project.$promise = projectDefer.promise;
      projectResourceMock.get.and.returnValue(project);
      projectStudiesResourceMock.query.and.returnValue({
        $promise: studiesDefer.promise
      });

      problemResourceMock.get.and.returnValue({
        $promise: $q.resolve({
          performanceTable: [],
          criteria: {}
        })
      });

      trialverseResourceMock.get.and.returnValue({
        $promise: datasetDefer.promise
      });
      workspaceServiceMock.getObservedScales.and.returnValue($q.resolve({}));

      userServiceMock.isLoginUserId.and.returnValue($q.resolve(true));

      benefitRiskService.compareAnalysesByModels.and.returnValue(0);
      benefitRiskService.joinModelsWithAnalysis.and.returnValue([]);
      benefitRiskService.findOverlappingInterventions.and.returnValue([]);
      benefitRiskService.findOverlappingOutcomes.and.returnValue([]);
      benefitRiskService.addStudiesToOutcomes.and.returnValue([]);

      $controller('BenefitRiskStep2Controller', {
        $scope: scope,
        $q: q,
        $stateParams: stateParamsMock,
        $state: {
          go: function() { }
        },
        $modal: modalMock,
        AnalysisResource: analysisResourceMock,
        BenefitRiskService: benefitRiskService,
        InterventionResource: interventionResourceMock,
        ModelResource: modelResourceMock,
        OutcomeResource: outcomeResourceMock,
        PageTitleService: pageTitleServiceMock,
        ProblemResource: problemResourceMock,
        ProjectResource: projectResourceMock,
        ProjectStudiesResource: projectStudiesResourceMock,
        TrialverseResource: trialverseResourceMock,
        UserService: userServiceMock,
        WorkspaceService: workspaceServiceMock
      });

    }));

    describe('when the project and analysis are loaded', function() {
      beforeEach(function() {
        projectDefer.resolve(project);
        analysisDefer.resolve(analysis);
        datasetDefer.resolve({
          ownerId: datasetOwnerId
        });
        scope.$apply();
      });
      it('should check whether editing is allowed, place the versionuuid on the scpe and set the datasetOwnerId', function() {
        expect(scope.editMode.allowEditing).toBe(true);
        expect(scope.projectVersionUuid).toEqual(versionUuid);
        expect(scope.datasetOwnerId).toEqual(datasetOwnerId);
      });
    });

    describe('when the analysis, outcomes, models, studies and alternatives are loaded', function() {
      beforeEach(function() {
        benefitRiskService.buildOutcomeWithAnalyses.and.returnValue({
          networkMetaAnalyses: [],
          outcome: {
            id: 1
          }
        });
        benefitRiskService.addModelBaseline.and.returnValue({
          benefitRiskStudyOutcomeInclusions: []
        });
        analysisDefer.resolve(analysis);

        interventionDefer.resolve([{
          id: 1
        }, {
          id: 10
        }, {
          id: 100
        }]);
        outcomeDefer.resolve([{
          id: 2
        }, {
          id: 20
        }, {
          id: 200
        }]);
        analysisQueryDefer.resolve([{
          archived: true
        }, {
          archived: false
        }]);
        modelsDefer.resolve([]);
        studiesDefer.resolve([]);
        benefitRiskService.addModelsGroup.and.returnValue({
          withModel: true
        });
        scope.$digest();
      });
      it('should determine which alternatives are included', function() {
        expect(scope.alternatives).toEqual([{
          id: 1,
          isIncluded: false
        }, {
          id: 10,
          isIncluded: true
        }, {
          id: 100,
          isIncluded: true
        }]);
      });
      it('should determine which outcomes are included', function() {
        expect(scope.outcomes).toEqual([{
          id: 2,
          isIncluded: true
        }, {
          id: 20,
          isIncluded: true
        }, {
          id: 200,
          isIncluded: false
        }]);
      });

      it('should set the effects table promise, query the NMAs for the ', function() {
        expect(benefitRiskService.joinModelsWithAnalysis).toHaveBeenCalled();
        expect(benefitRiskService.addModelsGroup).toHaveBeenCalled();
        expect(benefitRiskService.buildOutcomesWithAnalyses).toHaveBeenCalled();
      });
      it('should set the page title', function() {
        expect(pageTitleServiceMock.setPageTitle).toHaveBeenCalledWith('BenefitRiskStep2Controller', analysis.title + ' step 2');
      });
      it('should query for analysis about included outcomes', function() {
        expect(analysisResourceMock.query).toHaveBeenCalledWith({
          projectId: project.id,
          outcomeIds: [2, 20]
        });
      });
      it('should set non-archived NMAs on the scope, with models added', function() {
        expect(scope.networkMetaAnalyses).toEqual([{
          withModel: true
        }]);
      });
    });
  });
});

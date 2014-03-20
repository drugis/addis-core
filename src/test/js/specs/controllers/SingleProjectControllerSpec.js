define(['angular', 'angular-mocks', 'underscore'], function () {
  describe('SingleProjectController', function () {
    beforeEach(module('addis.controllers'));

    var scope, state, window,
      projectDeferred, analysisDeferred, studiesDeferred,
      projectsService, trialverseService, semanticOutcomeService, semanticInterventionService,
      outcomeService, interventionService, analysisService, trialverseStudyService,
      mockSemanticOutcomes, mockSemanticInterventions,
      mockProject = {
        id: 1,
        owner: {
          id: 1
        },
        name: 'projectName',
        description: 'testDescription',
        namespace: 'testNamespace',
        trialverseId: 1,
        $save: function () {}
      },
      mockTrialverse = {
        id: 1,
        name: 'trialverseName',
        description: 'trialverseDescription'
      },
      mockOutcomes = [1, 2, 3],
      mockInterventions = [4, 5, 6],
      mockAnalyses = [7, 8, 9],
      mockAnalysis = {
        projectId: 1,
        id: 2
      },
      mockStudy = {
        id: 5,
        name: 'testName'
      },
      mockStudies = [mockStudy];

    beforeEach(inject(function ($controller, $q, $rootScope) {
      var mockStateParams = {
        projectId: mockProject.id
      };

      mockSemanticOutcomes = ["a", "b", 'c'];
      mockSemanticInterventions = ["e", "f", 'g'];
      projectsService = jasmine.createSpyObj('projectsService', ['get', 'save']);
      projectsService.get.and.returnValue(mockProject);
      trialverseService = jasmine.createSpyObj('trialverseService', ['get']);
      trialverseService.get.and.returnValue(mockTrialverse);
      semanticOutcomeService = jasmine.createSpyObj('semanticOutcomeService', ['query']);
      semanticOutcomeService.query.and.returnValue(mockSemanticOutcomes);
      outcomeService = jasmine.createSpyObj('outcomeService', ['query', 'save']);
      outcomeService.query.and.returnValue(mockOutcomes);
      semanticInterventionService = jasmine.createSpyObj('semanticInterventionService', ['query']);
      semanticInterventionService.query.and.returnValue(mockSemanticInterventions);
      interventionService = jasmine.createSpyObj('interventionService', ['query', 'save']);
      interventionService.query.and.returnValue(mockInterventions);
      analysisService = jasmine.createSpyObj('analysisService', ['query', 'save']);
      analysisService.query.and.returnValue(mockAnalyses);
      analysisService.save.and.returnValue(mockAnalysis);

      trialverseStudyService = jasmine.createSpyObj('trialverseStudyService', ['query']);
      trialverseStudyService.query.and.returnValue(mockStudies);

      scope = $rootScope;
      scope.createOutcomeModal = jasmine.createSpyObj('createOutcomeModal', ['close']);
      scope.createInterventionModal = jasmine.createSpyObj('createInterventionModal', ['close']);
      scope.createAnalysisModal = jasmine.createSpyObj('createAnalysisModal', ['close']);

      projectDeferred = $q.defer();
      mockProject.$promise = projectDeferred.promise;
      analysisDeferred = $q.defer();
      mockAnalysis.$promise = analysisDeferred.promise;
      studiesDeferred = $q.defer();
      mockStudies.$promise = studiesDeferred.promise;

      window = {
        config: {
          user: {
            id: 1
          }
        }
      };
      state = jasmine.createSpyObj('state', ['go']);

      $controller('SingleProjectController', {
        $scope: scope,
        $window: window,
        $state: state,
        $stateParams: mockStateParams,
        'ProjectsService': projectsService,
        'TrialverseService': trialverseService,
        'SemanticOutcomeService': semanticOutcomeService,
        'OutcomeService': outcomeService,
        'SemanticInterventionService': semanticInterventionService,
        'InterventionService': interventionService,
        'AnalysisService': analysisService,
        'TrialverseStudyService': trialverseStudyService
      });

    }));

    it('should place project information on the scope', function () {
      expect(projectsService.get).toHaveBeenCalledWith({
        projectId: mockProject.id
      });
      expect(scope.project).toEqual(mockProject);
    });

    it('should place the outcome and intervention information on the scope once the project has been loaded', function () {
      projectDeferred.resolve();
      studiesDeferred.resolve();
      scope.$apply();
      expect(scope.outcomes).toEqual(mockOutcomes);
      expect(scope.interventions).toEqual(mockInterventions);
      expect(scope.analyses).toEqual(mockAnalyses);
      expect(scope.loading.loaded).toBeTruthy();
    });


    it('should tell the scope whether the resource is loaded', function () {
      expect(scope.loading.loaded).toBeFalsy();
      projectDeferred.resolve();
      scope.$apply();
      expect(scope.loading.loaded).toBeTruthy();
    });

    it("should make an update call when an outcome is added", function () {
      var newOutcome = {
        name: "name",
        motivation: "motivation",
        semanticOutcome: "semantics"
      };
      var newOutcomeWithProjectId = _.extend({
        projectId: 1
      }, newOutcome);
      scope.model = newOutcome;
      scope.addOutcome(newOutcome);
      expect(scope.createOutcomeModal.close).toHaveBeenCalled();
      expect(outcomeService.save).toHaveBeenCalledWith(newOutcomeWithProjectId, jasmine.any(Function));
      expect(scope.model).toEqual({});
    });

    it("should place the associated trialverse information on the scope on resolution", function () {
      projectDeferred.resolve();
      scope.$apply();
      expect(trialverseService.get).toHaveBeenCalledWith({
        id: mockProject.trialverseId
      });
      expect(scope.trialverse).toEqual(mockTrialverse);
    });

    it("should place the possible semanticOutcomes on the scope on resolution", function () {
      projectDeferred.resolve();
      scope.$apply();
      expect(semanticOutcomeService.query).toHaveBeenCalledWith({
        id: mockProject.trialverseId
      });
      expect(scope.semanticOutcomes).toEqual(mockSemanticOutcomes);
    });

    it("isOwnProject should be true if the project is owned by the logged-in user", function () {
      projectDeferred.resolve();
      scope.$apply();
      expect(scope.editMode.allowEditing).toBeTruthy();
    });

    it("isOwnProject should be false if the project is not owned by the logged-in user", function () {
      window.config.user.id = 2;
      projectDeferred.resolve();
      scope.$apply();
      expect(scope.editMode.allowEditing).toBeFalsy();
    });

    it("should make an update call when an intervention is added", function () {
      var newIntervention = {
        name: "name",
        motivation: "motivation",
        semanticIntervention: "semantics"
      };
      var newInterventionWithProjectId = _.extend(newIntervention, {
        projectId: 1
      });
      scope.model = newIntervention;
      scope.addIntervention(newIntervention);
      expect(scope.createInterventionModal.close).toHaveBeenCalled();
      expect(interventionService.save).toHaveBeenCalledWith(newInterventionWithProjectId, jasmine.any(Function));
      expect(scope.model).toEqual({});
    });

    it("should place the possible semanticInterventions on the scope on resolution", function () {
      projectDeferred.resolve();
      scope.$apply();
      expect(semanticInterventionService.query).toHaveBeenCalledWith({
        id: mockProject.trialverseId
      });
      expect(scope.semanticInterventions).toEqual(mockSemanticInterventions);
    });

    it("should make an update call when an analysis is added", function () {
      var newAnalysis = {
        name: "name",
        type: "SingleStudyBenefitRisk",
        study: "Hansen et al. 2005"
      };
      var newAnalysisWithProjectId = _.extend(newAnalysis, {
        projectId: 1
      });
      scope.addAnalysis(newAnalysis);
      expect(analysisService.save).toHaveBeenCalledWith(newAnalysisWithProjectId);
      analysisDeferred.resolve();
      scope.$apply();
      expect(state.go).toHaveBeenCalledWith('analysis', {
        projectId: mockAnalysis.projectId,
        analysisId: mockAnalysis.id
      });
    });
  });
});
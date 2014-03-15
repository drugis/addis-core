define(['angular', 'angular-mocks', 'controllers'], function () {
  describe("The analysisController", function () {
    var scope,
      analysisService, projectsService, outcomeService,
      mockOutcome1 = {
        id: 1,
        name: 'mockOutcome1',
        semanticOutcome: 'mockSemantic1'
      },
      mockOutcome2 = {
        id: 2,
        name: 'mockOutcome2',
        semanticOutcome: 'mockSemantic2'
      },
      mockAnalysis = {
        name: 'analysisName',
        type: 'Single-study Benefit-Risk',
        study: null,
        selectedOutcomes: [mockOutcome1],
        $save: function() {}
      },
      mockProject = {
        id: 1,
        name: 'projectName'
      },
      mockOutcomes = [mockOutcome1, mockOutcome2],
      projectDeferred, analysisDeferred,
      ctrl;

    beforeEach(module('addis.controllers'));

    beforeEach(inject(function ($controller, $q, $rootScope) {
      var mockStateParams = {
        projectId: mockProject.id,
        analysisId: mockAnalysis.id
      };

      scope = $rootScope;
      analysisService = jasmine.createSpyObj('analysisService', ['get', 'save']);
      analysisService.get.and.returnValue(mockAnalysis);
      projectsService = jasmine.createSpyObj('projectService', ['get']);
      projectsService.get.and.returnValue(mockProject);
      outcomeService = jasmine.createSpyObj('outcomeService', ['query']);
      outcomeService.query.and.returnValue(mockOutcomes);

      projectDeferred = $q.defer();
      mockProject.$promise = projectDeferred.promise;
      analysisDeferred = $q.defer();
      mockAnalysis.$promise = analysisDeferred.promise;

      spyOn(mockAnalysis, '$save');

      ctrl = $controller('AnalysisController', {
        $scope: scope,
        $stateParams: mockStateParams,
        'ProjectsService': projectsService,
        'AnalysisService': analysisService,
        'OutcomeService': outcomeService
      });
    }));

    it('should only make loading.loaded true when both project and analysis are loaded', function () {
      expect(scope.loading.loaded).toBeFalsy();
      projectDeferred.resolve();
      scope.$apply();
      expect(scope.loading.loaded).toBeFalsy();

      analysisDeferred.resolve();
      scope.$apply();
      expect(scope.loading.loaded).toBeTruthy();
    });

    it('should place project and analysis information on the scope when it is loaded', function () {
      expect(scope.selectedOutcomeIds).toEqual([]);
      analysisDeferred.resolve();
      projectDeferred.resolve();
      scope.$apply();
      expect(scope.analysis).toEqual(mockAnalysis);
      expect(scope.project).toEqual(mockProject);
      expect(scope.selectedOutcomeIds).toEqual(['1']);
    });

    it('should place a list of outcomes on the scope when it is loaded', function () {
      expect(scope.outcomes).toEqual(mockOutcomes);
    });

    it('should save the analysis when the selected outcomes change', function () {
      analysisDeferred.resolve();
      projectDeferred.resolve();
      scope.$apply();
      scope.selectedOutcomeIds = ['1', '2'];
      scope.$apply();
      expect(scope.analysis.selectedOutcomes).toEqual([mockOutcome1, mockOutcome2]);
      expect(scope.analysis.$save).toHaveBeenCalled();
    })
  });
});
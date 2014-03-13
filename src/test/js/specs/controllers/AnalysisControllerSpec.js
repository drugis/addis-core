define(['angular', 'angular-mocks', 'controllers'], function() {
  describe("The analysisController", function() {
    var scope,
      analysisService, projectsService,
      mockAnalysis = {
        name: 'analysisName',
        type: 'Single-study Benefit-Risk',
        study: null
      },
      mockProject = {
        name: 'projectName'
      },
      projectDeferred, analysisDeferred;

    beforeEach(module('addis.controllers'));

    beforeEach(inject(function($controller, $q, $rootScope) {
      var mockStateParams = {
        projectId: mockProject.id,
        analysisId: mockAnalysis.id
      };

      scope = $rootScope;
      analysisService = jasmine.createSpyObj('analysisService', ['get']);
      analysisService.get.andReturn(mockAnalysis)
      projectsService = jasmine.createSpyObj('projectService', ['get']);
      projectsService.get.andReturn(mockProject)

      projectDeferred = $q.defer();
      mockProject.$promise = projectDeferred.promise;
      analysisDeferred = $q.defer();
      mockAnalysis.$promise = analysisDeferred.promise;

      ctrl = $controller('AnalysisController', {
        $scope: scope,
        $stateParams: mockStateParams,
        'ProjectsService': projectsService,
        'AnalysisService': analysisService
      });
    }));

    it('should only make loading.loaded true when both project and analysis are loaded', function() {
      expect(scope.loading.loaded).toBeFalsy();
      projectDeferred.resolve();
      scope.$apply();
      expect(scope.loading.loaded).toBeFalsy();

      analysisDeferred.resolve();
      scope.$apply();
      expect(scope.loading.loaded).toBeTruthy();
    });

    it('should place project and analysis information on the scope when it is loaded', function() {
      analysisDeferred.resolve();
      projectDeferred.resolve();
      expect(scope.analysis).toEqual(mockAnalysis);
      expect(scope.project).toEqual(mockProject);
    });
  });
});
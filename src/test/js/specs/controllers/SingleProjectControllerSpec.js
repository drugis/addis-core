define(['angular', 'angular-mocks'], function () {
  describe('SingleProjectController', function () {
    beforeEach(module('addis.controllers'));
    var scope, projectsService, deferred
      mockProject = {id: 1, name: 'projectName', description: 'testDescription', namespace: 'testNamespace', outcomes: [], trialverseId: 1}
      mockTrialverse = {id: 1, name: 'trialverseName', description: 'trialverseDescription'};

    beforeEach(inject(function ($controller, $q, $rootScope) {
      var mockStateParams = {id: mockProject.id};
      projectsService = jasmine.createSpyObj('projectsService', ['get', 'save']);
      projectsService.get.andReturn(mockProject);
      trialverseService = jasmine.createSpyObj('trialverseService', ['get']);
      trialverseService.get.andReturn(mockTrialverse);

      scope = $rootScope;
      deferred = $q.defer();
      mockProject.$promise = deferred.promise;

      $controller('SingleProjectController', {$scope: scope, 'ProjectsService': projectsService, 'TrialverseService': trialverseService, $stateParams: mockStateParams});
    }));

    it('should place project information on the scope', function () {
      expect(projectsService.get).toHaveBeenCalledWith({id: mockProject.id});
      expect(scope.project).toEqual(mockProject);
    });

    it('should tell the scope whether the resource is loaded', function() {
      expect(scope.loading.loaded).toBeFalsy();
      deferred.resolve();
      scope.$apply();
      expect(scope.loading.loaded).toBeTruthy();
    });

    it("should make an update call when an outcome is added", function() {
      scope.addOutcome({name: "name", motivation: "motivation", semanticOutcome: "semantics"});
      expect(projectsService.save).toHaveBeenCalled();
    });

    it("should place the associated trialverse information on the scope on resolution", function() {
      deferred.resolve();
      scope.$apply();
      expect(trialverseService.get).toHaveBeenCalledWith({id: mockProject.trialverseId});
      expect(scope.trialverse).toEqual(mockTrialverse);
    });
  });
});
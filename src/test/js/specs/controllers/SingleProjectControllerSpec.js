define(['angular', 'angular-mocks'], function () {
  describe('SingleProjectController', function () {
    beforeEach(module('addis.controllers'));
    var scope, projectsService, deferred
      mockProject = {id: 1, name: 'projectName', description: 'testDescription', namespace: 'testNamespace'};

    beforeEach(inject(function ($controller, $q, $rootScope) {
      var mockStateParams = {id: mockProject.id};
      projectsService = jasmine.createSpyObj('projectsService', ['get']);
      projectsService.get.andReturn(mockProject);
      scope = $rootScope;
      deferred = $q.defer();
      mockProject.$promise = deferred.promise;

      $controller('SingleProjectController', {$scope: scope, 'ProjectsService': projectsService, $stateParams: mockStateParams});
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

  });
});
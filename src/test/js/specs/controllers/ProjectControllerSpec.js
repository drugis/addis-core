define(['angular', 'angular-mocks', 'controllers'], function () {
  describe("SingleProjectController", function () {
    beforeEach(module('addis.controllers'));
      var scope, projectsService,
       mockProject = {id: 1, name: 'projectName', description: 'testDescription', namespace: 'testNamespace'};

      beforeEach(inject(function ($controller) {
        var mockStateParams = {id:mockProject.id};
        projectsService = jasmine.createSpyObj('projectsService', ['query']);
        projectsService.query.andReturn(mockProject);
        scope = {};

        $controller('SingleProjectController', {$scope: scope, 'ProjectsService': projectsService, $stateParams: mockStateParams});
      }));

      it('should place project information on the scope', function() {
        expect(projectsService.query).toHaveBeenCalledWith({id: mockProject.id});
        expect(scope.project).toEqual(mockProject);
      });

  });
});
define(['angular', 'angular-mocks', 'controllers'],
  function(projectsController) {
  describe("ProjectsController", function() {
    var scope, window, controller;
    var mockProjects = 
          [{name: "testName1", description: "testDesc1"},
           {name: "testName2", description: "testDesc2"}];

    beforeEach(module('addis.controllers'));

    beforeEach(inject(function($controller) {
      var mockResource = {query: function() {
        return mockProjects;
      }};
      scope = {};
      window = {config: {user:{id:12345}}};
      ctrl = $controller('ProjectsController', {$scope:scope,$window: window, 'ProjectsService':mockResource});
    }));

    it("should make a list of projects available from the resource", function() {
      expect(scope.projects.length).toBe(2);
    });

    it("should place the user id on the scope", function() {
      expect(scope.user.id).toBe(window.config.user.id);
    });

  });
});

define(['angular', 'angular-mocks', 'controllers'],
  function(projectsController) {
  describe("ProjectsController", function() {
    var scope, controller;
    var mockProjects = 
          [{name: "testName1", description: "testDesc1"},
           {name: "testName2", description: "testDesc2"}];

    beforeEach(module('addis.controllers'));

    beforeEach(inject(function($controller) {
      var mockResource = {query: function() {
        return mockProjects;
      }};
      scope = {};
      ctrl = $controller('ProjectsController', {$scope:scope, 'ProjectsService':mockResource});
    }));

    it("should make a list of projects available from the resource", function() {
      expect(scope.projects.length).toBe(2);
    });

  });
});

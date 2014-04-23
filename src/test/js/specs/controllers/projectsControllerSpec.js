define(['angular', 'angular-mocks', 'controllers'],
  function () {
    describe("The projectsController", function () {
      var scope, window, ctrl, projectResource, trialverseResource;
      var mockProjects =
        [
          {name: "testName1", description: "testDesc1"},
          {name: "testName2", description: "testDesc2"}
        ];

      beforeEach(module('addis.controllers'));

      beforeEach(inject(function ($controller) {
        trialverseResource = jasmine.createSpyObj('trialverseResource', ['query']);
        projectResource = jasmine.createSpyObj('projectResource', ['query', 'save']);

        projectResource.query.and.returnValue(mockProjects);
        trialverseResource.query.and.returnValue([{key:'val'}]);

        scope = {};
        window = {config: {user: {id: 12345}}};

        ctrl = $controller('ProjectsController', {$scope: scope, $window: window, 'ProjectResource': projectResource, 'TrialverseResource': trialverseResource});
      }));

      it("should make a list of projects available from the resource", function () {
        expect(scope.projects.length).toBe(2);
      });

      it("should place the user id on the scope", function () {
        expect(scope.user.id).toBe(window.config.user.id);
      });

      it("should make a list of trialverse namespaces available from the trialVerseResource", function() {
        expect(scope.trialverse.length).toBe(1);
      });

      it("should make a save call when createProject is called, and clear the scope model so that the form is cleared", function () {
        var newProject = {name: 'testName', description: 'testDescription', namespace: 'testnamespace'};
        scope.model = newProject;
        scope.createProject(newProject);
        expect(projectResource.save).toHaveBeenCalled();
        expect(scope.model).toEqual({});
      });
    });
  });

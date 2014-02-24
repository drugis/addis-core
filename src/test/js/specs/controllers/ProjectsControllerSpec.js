define(['angular', 'angular-mocks', 'controllers'],
  function () {
    describe("ProjectsController", function () {
      var scope, window, ctrl, projectsService, trialverseService;
      var mockProjects =
        [
          {name: "testName1", description: "testDesc1"},
          {name: "testName2", description: "testDesc2"}
        ];

      beforeEach(module('addis.controllers'));

      beforeEach(inject(function ($controller) {
        trialverseService = jasmine.createSpyObj('trialverseService', ['query']);
        projectsService = jasmine.createSpyObj('projectsService', ['query', 'save']);

        projectsService.query.andReturn(mockProjects);
        trialverseService.query.andReturn([{key:'val'}]);

        scope = {};
        window = {config: {user: {id: 12345}}};

        ctrl = $controller('ProjectsController', {$scope: scope, $window: window, 'ProjectsService': projectsService, 'TrialverseService': trialverseService});
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

      it("should make a save call when createProject is called", function () {
        scope.createProject({name: 'testName', description: 'testDescription', namespace: 'testnamespace'});
        expect(projectsService.save).toHaveBeenCalled();
      });
    });
  });

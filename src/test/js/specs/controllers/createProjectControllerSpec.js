'use strict';
define(['angular', 'angular-mocks', 'controllers'],
  function() {
    describe("The createProjectController", function() {
      var scope, ctrl, projectResource, trialverseResource,
        modalSpy;

      beforeEach(module('addis.controllers'));

      beforeEach(inject(function($controller) {
        trialverseResource = jasmine.createSpyObj('trialverseResource', ['query']);
        projectResource = jasmine.createSpyObj('projectResource', ['save']);
        modalSpy = jasmine.createSpyObj('createProjectModal', ['open']);

        trialverseResource.query.and.returnValue([{
          key: 'val',
          uid: 'uid'
        }]);

        scope = {
          createProjectModal: modalSpy
        };
        ctrl = $controller('CreateProjectController', {
          $scope: scope,
          $state: {params: {userUid: 1}},
          'ProjectResource': projectResource,
          'TrialverseResource': trialverseResource
        });
      }));

      it("should make a list of trialverse namespaces available from the trialVerseResource", function() {
        expect(scope.namespaces.length).toBe(1);
      });

      it('should place the selected namespace on createProjectModal and show the modal dialog', function() {
        var selectedNamespace = {
          uid: 'uid',
          version: 'version',
          name: 'name'
        };
        scope.showCreateProjectModal(selectedNamespace);
        expect(scope.createProjectModal.selectedNamespace).toBe(selectedNamespace);
        expect(scope.createProjectModal.open).toHaveBeenCalled();
      });

      it("should make a save call when createProject is called, and clear the scope model so that the form is cleared", function() {
        var newProject = {
          name: 'testName',
          description: 'testDescription'
        };
        var selectedNamespace = {
          uid: 'uid',
          version: 'version',
          name: 'name'
        };
        scope.showCreateProjectModal(selectedNamespace);
        scope.model = newProject;
        scope.createProject(newProject);
        expect(projectResource.save).toHaveBeenCalled();
        expect(newProject.namespaceUid).toEqual(selectedNamespace.uid);
        expect(newProject.datasetVersion).toEqual(selectedNamespace.version);
        expect(scope.model).toEqual({});
      });
    });
  });

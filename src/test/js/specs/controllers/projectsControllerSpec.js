'use strict';
define(['angular', 'angular-mocks', 'controllers'],
  function() {
    describe("The projectsController", function() {
      var scope, ctrl, projectResource, trialverseResource;
      var mockProjects = [{
        name: "testName1",
        description: "testDesc1"
      }, {
        name: "testName2",
        description: "testDesc2"
      }];

      beforeEach(module('addis.controllers'));

      beforeEach(inject(function($controller) {
        trialverseResource = jasmine.createSpyObj('trialverseResource', ['query']);
        projectResource = jasmine.createSpyObj('projectResource', ['query', 'save']);

        projectResource.query.and.returnValue(mockProjects);
        trialverseResource.query.and.returnValue([{
          key: 'val'
        }]);

        scope = {};

        var stateParams = {userUid: 1};

        ctrl = $controller('ProjectsController', {
          $scope: scope,
          $stateParams: stateParams,
          'ProjectResource': projectResource,
          'TrialverseResource': trialverseResource
        });
      }));

      it("should make a list of projects available from the resource", function() {
        expect(scope.projects.length).toBe(2);
      });

    });
  });

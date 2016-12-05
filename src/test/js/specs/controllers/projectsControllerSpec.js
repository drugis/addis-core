'use strict';
define(['angular', 'angular-mocks', 'controllers'],
  function() {
    describe('The projectsController', function() {
      var scope, ctrl, projectResource, trialverseResource, projectsDefer;
      beforeEach(module('addis.controllers'));

      beforeEach(inject(function($rootScope, $controller, $q) {
        trialverseResource = jasmine.createSpyObj('trialverseResource', ['query']);
        projectResource = jasmine.createSpyObj('projectResource', ['query', 'save']);

        projectsDefer = $q.defer();
        var projects = {};
        projects.$promise = projectsDefer.promise;
        projectResource.query.and.returnValue(projects);
        trialverseResource.query.and.returnValue([{
          key: 'val'
        }]);

        scope = $rootScope;

        var stateParams = {userUid: 1};

        ctrl = $controller('ProjectsController', {
          $scope: scope,
          $stateParams: stateParams,
          'ProjectResource': projectResource,
          'TrialverseResource': trialverseResource
        });
      }));

      it('should make a list of projects available from the resource', function() {
        projectsDefer.resolve([{project: 'projectg1'}, {project: 'project2'}]);
        scope.$digest();
        expect(scope.projects.length).toBe(2);
      });

    });
  });

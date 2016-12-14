'use strict';
define(['angular', 'angular-mocks', 'controllers'],
  function() {
    describe('The projectsController', function() {
      var scope, ctrl, projectResource, projectsDefer;
      beforeEach(module('addis.controllers'));

      beforeEach(inject(function($rootScope, $controller, $q) {
        projectResource = jasmine.createSpyObj('projectResource', ['query', 'save']);

        projectsDefer = $q.defer();
        var projects = [{
          project: 'projectg1'
        }, {
          project: 'project2'
        }];
        projects.$promise = projectsDefer.promise;
        projectResource.query.and.returnValue(projects);

        scope = $rootScope;

        var stateParams = {
          userUid: 1
        };

        ctrl = $controller('ProjectsController', {
          $scope: scope,
          $stateParams: stateParams,
          'ProjectResource': projectResource
        });
      }));

      it('should make a list of projects available from the resource', function() {
        projectsDefer.resolve();
        scope.$digest();
        expect(scope.projects.length).toBe(2);
      });

    });
  });

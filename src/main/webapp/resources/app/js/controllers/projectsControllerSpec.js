'use strict';
define(['angular', 'angular-mocks', '../controllers'],
  function() {
    describe('The projectsController', function() {
      var scope,
        projectResourceMock = jasmine.createSpyObj('ProjectResource', ['query', 'save', 'setArchived']),
        userServiceMock = jasmine.createSpyObj('UserService', ['isLoginUserId']),
        pageTitleServiceMock = jasmine.createSpyObj('PageTitleService', ['setPageTitle']),
        projectsDefer;
      beforeEach(angular.mock.module('addis.controllers'));

      beforeEach(inject(function($rootScope, $controller, $q) {
        projectsDefer = $q.defer();
        var projects = [{
          id: 1,
          owner: {
            id: 1
          }
        }, {
          id: 2,
          owner: {
            id: 1
          }
        }];
        projects.$promise = projectsDefer.promise;
        projectResourceMock.query.and.returnValue(projects);

        var setArchivedDefer = $q.defer();
        var setArchivedReturnValue = {
          $promise: setArchivedDefer.promise
        };
        setArchivedDefer.resolve();
        projectResourceMock.setArchived.and.returnValue(setArchivedReturnValue);

        scope = $rootScope;

        var stateParams = {
          userUid: 1
        };

        $controller('ProjectsController', {
          $scope: scope,
          $stateParams: stateParams,
          ProjectResource: projectResourceMock,
          UserService: userServiceMock,
          PageTitleService: pageTitleServiceMock
        });
      }));

      it('should make a list of projects available from the resource', function() {
        projectsDefer.resolve();
        scope.$digest();
        expect(scope.projects.length).toBe(2);
      });
      it('should make an archiveProject function available on the scope that sets a project to archived', function() {
        scope.archiveProject({
          id: 1
        });
        expect(projectResourceMock.setArchived).toHaveBeenCalledWith({
          projectId: 1
        }, {
          isArchived: true
        });
      });

      it('should make an unarchiveProject function available on the scope that sets a project to not archived', function() {
        scope.unarchiveProject({
          id: 1
        });
        expect(projectResourceMock.setArchived).toHaveBeenCalledWith({
          projectId: 1
        }, {
          isArchived: false
        });
      });
    });
  });

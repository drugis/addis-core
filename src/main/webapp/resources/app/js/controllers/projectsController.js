'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = ['$scope', '$stateParams', 'ProjectResource', 'UserService'];
  var ProjectsController = function($scope, $stateParams, ProjectResource, UserService) {
    $scope.loadedProjects = false;
    $scope.userId = Number($stateParams.userUid);
    $scope.showArchived = false;
    $scope.numberOfProjectsArchived = 0;
    $scope.editMode = {
      allowEditing: false
    };

    $scope.archiveProject = function(project) {
      var params = angular.copy($stateParams);
      params.projectId = project.id;
      ProjectResource.setArchived(
        params, {
          isArchived: true
        }
      ).$promise.then(loadProjects);
    };

    $scope.unarchiveProject = function(project) {
      var params = angular.copy($stateParams);
      params.projectId = project.id;
      ProjectResource.setArchived(
        params, {
          isArchived: false
        }
      ).$promise.then(loadProjects);
    };
    loadProjects();

    $scope.toggleShowArchived = function() {
      $scope.showArchived = !$scope.showArchived;
    };

    function loadProjects() {
      $scope.projects = ProjectResource.query();
      $scope.projects.$promise.then(function() {
        $scope.loadedProjects = true;
        $scope.projects = _.sortBy($scope.projects, ['id']);
        $scope.numberOfProjectsArchived = _.reduce($scope.projects, function(accum, project) {
          return project.archived ? ++accum : accum;
        }, 0);
      });
      $scope.editMode.allowEditing = UserService.isLoginUserId($scope.userId);
    }
  };
  return dependencies.concat(ProjectsController);
});

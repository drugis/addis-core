'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'ProjectResource'];
  var ProjectsController = function($scope, $stateParams, ProjectResource) {
    $scope.loadedProjects = false;
    $scope.userId = $stateParams.userUid;

    $scope.archiveProject = function(project) {
      ProjectResource.setArchived({
        projectId: project.id
      }, true).$promise.then(loadProjects);
    };
    loadProjects();

    function loadProjects() {
      $scope.projects = ProjectResource.query();
      $scope.projects.$promise.then(function(projects) {
        $scope.loadedProjects = true;
      });
    }
  };
  return dependencies.concat(ProjectsController);
});

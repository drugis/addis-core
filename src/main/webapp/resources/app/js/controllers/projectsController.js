'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'ProjectResource'];
  var ProjectsController = function($scope, $stateParams, ProjectResource) {
    $scope.loadedProjects = false;
    $scope.projects = ProjectResource.query().$promise.then(function(projects){
      $scope.loadedProjects = true;
      $scope.projects = projects;
    });
    $scope.userId = $stateParams.userUid;
  };
  return dependencies.concat(ProjectsController);
});

'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'ProjectResource'];
  var ProjectsController = function($scope, $stateParams, ProjectResource) {
    console.log('projects controller');
    $scope.projects = ProjectResource.query();
    $scope.userId = $stateParams.userUid;
  };
  return dependencies.concat(ProjectsController);
});

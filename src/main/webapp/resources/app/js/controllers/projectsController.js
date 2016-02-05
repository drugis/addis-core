'use strict';
define([], function() {
  var dependencies = ['$scope', 'ProjectResource'];
  var ProjectsController = function($scope, ProjectResource) {
    console.log('projects controller');
    $scope.projects = ProjectResource.query();
  };
  return dependencies.concat(ProjectsController);
});

'use strict';
define([], function() {
  var dependencies = ['$scope', '$window', 'ProjectResource'];
  var ProjectsController = function($scope, $window, ProjectResource) {
    $scope.projects = ProjectResource.query();
  };
  return dependencies.concat(ProjectsController);
});

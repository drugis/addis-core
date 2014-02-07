'use strict';
define([], function() {
  var dependencies = ['$scope','$window', 'ProjectsService'];
  var ProjectsController = function($scope, $window, ProjectsService) {
    $scope.user = $window.config.user;
    $scope.projects = ProjectsService.query();
  };
  return dependencies.concat(ProjectsController);
});

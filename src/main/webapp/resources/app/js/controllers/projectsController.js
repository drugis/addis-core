'use strict';
define([], function() {
  var dependencies = ['$scope', '$window', '$location', 'ProjectResource', 'TrialverseResource'];
  var ProjectsController = function($scope, $window, $location, ProjectResource, TrialverseResource) {
    $scope.user = $window.config.user;
    $scope.projects = ProjectResource.query();
  };
  return dependencies.concat(ProjectsController);
});
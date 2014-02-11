'use strict';
define([], function() {
  var dependencies = ['$scope','$window', 'ProjectsService', 'TrialverseService'];
  var ProjectsController = function($scope, $window, ProjectsService, TrialverseService) {
    $scope.user = $window.config.user;
    $scope.projects = ProjectsService.query();
    $scope.trialverse = TrialverseService.query();

    $scope.createProject = function (newProject) {
      ProjectsService.save(newProject);
    };
  };
  return dependencies.concat(ProjectsController);
});

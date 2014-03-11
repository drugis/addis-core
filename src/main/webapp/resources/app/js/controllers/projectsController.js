'use strict';
define([], function() {
  var dependencies = ['$scope', '$window', '$location', 'ProjectsService', 'TrialverseService'];
  var ProjectsController = function($scope, $window, $location, ProjectsService, TrialverseService) {
    $scope.user = $window.config.user;
    $scope.projects = ProjectsService.query();

    $scope.trialverse = TrialverseService.query();

    $scope.createProject = function (newProject) {
      // clear modal form by resetting model in current scope
      this.model = {};
      ProjectsService.save(newProject, function(savedProject) {
        $scope.projects = ProjectsService.query(function(){
          $scope.createProjectModal.close();
        });
      });

    };

  };
  return dependencies.concat(ProjectsController);
});

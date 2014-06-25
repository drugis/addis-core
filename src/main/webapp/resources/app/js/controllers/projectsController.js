'use strict';
define([], function() {
  var dependencies = ['$scope', '$window', '$location', 'ProjectResource', 'TrialverseResource'];
  var ProjectsController = function($scope, $window, $location, ProjectResource, TrialverseResource) {
    $scope.user = $window.config.user;
    $scope.projects = ProjectResource.query();
    $scope.trialverse = TrialverseResource.query();

    $scope.createProject = function (newProject) {
      // clear modal form by resetting model in current scope
      this.model = {};
      ProjectResource.save(newProject, function() {
        $scope.projects = ProjectResource.query(function(){
          $scope.createProjectModal.close();
        });
      });
    };

  };
  return dependencies.concat(ProjectsController);
});

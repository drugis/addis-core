'use strict';
define([], function() {
  var dependencies = ['$scope', '$window', '$location', 'ProjectsResource', 'TrialverseResource'];
  var ProjectsController = function($scope, $window, $location, ProjectsResource, TrialverseResource) {
    $scope.user = $window.config.user;
    $scope.projects = ProjectsResource.query();
    $scope.$parent.showBreadcrumbs = false;
    $scope.trialverse = TrialverseResource.query();

    $scope.createProject = function (newProject) {
      // clear modal form by resetting model in current scope
      this.model = {};
      ProjectsResource.save(newProject, function(savedProject) {
        $scope.projects = ProjectsResource.query(function(){
          $scope.createProjectModal.close();
        });
      });

    };

  };
  return dependencies.concat(ProjectsController);
});

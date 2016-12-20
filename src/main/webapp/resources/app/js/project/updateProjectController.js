'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', 'ProjectResource', 'callback'];
  var UpdateProjectController = function($scope, $stateParams, $modalInstance, ProjectResource, callback) {

    $scope.updateProject = updateProject;

    function updateProject() {
      ProjectResource.copy($scope.projectId, {
        projectId: $scope.project.id
      }, function(response) {
        callback(response.newProjectId);
        $modalInstance.close();
      });
    }

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(UpdateProjectController);
});

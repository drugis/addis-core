'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', 'ProjectResource', 'callback'];
  var UpdateProjectController = function($scope, $stateParams, $modalInstance, ProjectResource, callback) {
    $scope.updateProject = updateProject;
    $scope.isUpdating=false;

    function updateProject() {
      $scope.isUpdating = true;
      ProjectResource.update($scope.project.id, {
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

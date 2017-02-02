'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', 'ProjectResource', 'callback'];
  var CopyProjectController = function($scope, $stateParams, $modalInstance, ProjectResource, callback) {
    $scope.copyProject = copyProject;

    function copyProject(newTitle) {
      ProjectResource.copy({
        projectId: $scope.project.id
      }, {
        newTitle: newTitle
      }, function(response) {
        callback(response.newProjectId);
        $modalInstance.close();
      });
    }

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(CopyProjectController);
});

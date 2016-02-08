'use strict';
define([], function() {
  var dependencies = ['$scope', '$state', '$modalInstance', 'ProjectResource', 'dataset'];
  var CreateProjectModalController = function($scope, $state, $modalInstance, ProjectResource, dataset) {

    $scope.dataset = dataset;

    $scope.createProject = function(newProject) {
      this.model = {}; // clear modal form by resetting model in current scope
      newProject.namespaceUid = dataset.uri.split('/datasets/')[1];
      newProject.datasetVersion = dataset.headVersion;
      ProjectResource.save(newProject, function(savedProject) {
        $modalInstance.close();
        $state.go('project', {
          userUid: $state.params.userUid,
          projectId: savedProject.id
        });
      });
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(CreateProjectModalController);
});

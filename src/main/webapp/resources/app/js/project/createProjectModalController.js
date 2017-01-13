'use strict';
define([], function() {
  var dependencies = ['$scope', '$state', '$modalInstance', 'ProjectResource', 'dataset'];
  var CreateProjectModalController = function($scope, $state, $modalInstance, ProjectResource, dataset) {

    $scope.dataset = dataset;

    $scope.createProject = function(newProject) {
      // datasetsController and datasetController use a different property to store the namespaceUuid
      newProject.namespaceUid = dataset.datasetUuid || dataset.uri.split('/datasets/')[1];

      newProject.datasetVersion = $state.params.versionUuid ?
        dataset.headVersion.split('/versions/')[0] + '/versions/' + $state.params.versionUuid :
        dataset.headVersion;
      ProjectResource.save(newProject, function(savedProject) {
        $modalInstance.close();
        $state.go('project', {
          userUid: savedProject.owner.id,
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

'use strict';
define([], function() {
  var dependencies = ['$scope', '$modal', '$filter', '$stateParams', '$state', 'DatasetResource'];

  var DatasetsController = function($scope, $modal, $filter, $stateParams, $state, DatasetResource) {
    $scope.stripFrontFilter = $filter('stripFrontFilter');
    $scope.datasetsLoaded = false;
    $scope.reloadDatasets = reloadDatasets;

    reloadDatasets();

    function reloadDatasets() {
      $scope.datasetsLoaded = false;
      DatasetResource.queryForJson($stateParams, function(datasets) {
        $scope.datasets = datasets;
        $scope.datasetsLoaded = true;
      });
    }

    $scope.createDatasetDialog = function() {
      $modal.open({
        templateUrl: 'app/js/user/createDataset.html',
        controller: 'CreateDatasetController',
        resolve: {
          callback: function() {
            return reloadDatasets;
          }
        }
      });
    };

    $scope.createProjectDialog = function(dataset) {
      $modal.open({
        templateUrl: 'app/js/project/createProjectModal.html',
        controller: 'CreateProjectModalController',
        resolve: {
          callback: function() {
            return function(newProject) {
              $state.go('project', {
                projectId: newProject.id
              });
            };
          },
          dataset: function() {
            return dataset;
          }
        }
      });
    };

  };
  return dependencies.concat(DatasetsController);
});

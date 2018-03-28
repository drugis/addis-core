'use strict';
define([], function() {
  var dependencies = ['$scope', '$modal', '$filter', '$stateParams', '$state', 'DatasetResource', 'UserService'];

  var DatasetsController = function($scope, $modal, $filter, $stateParams, $state, DatasetResource, UserService) {
    // functions
    $scope.reloadDatasets = reloadDatasets;
    $scope.createDatasetDialog = createDatasetDialog;
    $scope.createProjectDialog = createProjectDialog;

    // init
    reloadDatasets();
    $scope.stripFrontFilter = $filter('stripFrontFilter');
    $scope.loginUser = UserService.getLoginUser();
    $scope.showCreateProjectButton = UserService.hasLoggedInUser();


    function reloadDatasets() {
      $scope.datasetsPromise = DatasetResource.queryForJson($stateParams, function(datasets) {
        $scope.datasets = datasets;
      }).$promise;
    }

    function createDatasetDialog() {
      $modal.open({
        templateUrl: 'app/js/user/createDataset.html',
        controller: 'CreateDatasetController',
        resolve: {
          callback: function() {
            return reloadDatasets;
          }
        }
      });
    }

    function createProjectDialog(dataset) {
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
    }

  };
  return dependencies.concat(DatasetsController);
});
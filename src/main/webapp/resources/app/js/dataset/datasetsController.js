'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$scope', '$modal', '$filter', '$stateParams', '$state',
    'DatasetResource',
    'UserService',
    'PageTitleService'
  ];

  var DatasetsController = function(
    $scope, $modal, $filter, $stateParams, $state,
    DatasetResource,
    UserService,
    PageTitleService
  ) {
    // functions
    $scope.reloadDatasets = reloadDatasets;
    $scope.createDatasetDialog = createDatasetDialog;
    $scope.createProjectDialog = createProjectDialog;

    // init
    $scope.$watch('user', function(user) {
      if (user && user.id) {
        PageTitleService.setPageTitle('DatasetsController', user.firstName + ' ' + user.lastName + '\'s datasets');
      }
    });
    reloadDatasets();
    $scope.stripFrontFilter = $filter('stripFrontFilter');
    
    UserService.getLoginUser().then(function(user) {
      $scope.loginUser = user;
      $scope.showCreateProjectButton = !!user;
    });


    function reloadDatasets() {
      $scope.datasetsPromise = DatasetResource.queryForJson($stateParams, function(datasets) {
        $scope.datasets = datasets;
      }).$promise;
    }

    function createDatasetDialog() {
      $modal.open({
        templateUrl: './createDataset.html',
        controller: 'CreateDatasetController',
        resolve: {
          callback: function() {
            return reloadDatasets;
          },
          datasetTitles: function() {
            return _.map($scope.datasets, 'title');
          }
        }
      });
    }

    function createProjectDialog(dataset) {
      $modal.open({
        templateUrl: '../project/createProjectModal.html',
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

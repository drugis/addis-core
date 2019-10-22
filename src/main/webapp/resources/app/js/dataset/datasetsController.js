'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$scope',
    '$modal',
    '$filter',
    '$stateParams',
    '$state',
    'DatasetResource',
    'UserService',
    'PageTitleService'
  ];

  var DatasetsController = function(
    $scope,
    $modal,
    $filter,
    $stateParams,
    $state,
    DatasetResource,
    UserService,
    PageTitleService
  ) {
    // functions
    $scope.reloadDatasets = reloadDatasets;
    $scope.createDatasetDialog = createDatasetDialog;
    $scope.createProjectDialog = createProjectDialog;
    $scope.setArchivedStatus = setArchivedStatus;
    $scope.toggleShowArchived = toggleShowArchived;

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
      $scope.editMode = {
        showCreateProjectButton: !!user,
        isUserOwner: user && (user.id === $scope.userUid)
      };
    });

    function reloadDatasets() {
      $scope.datasetsPromise = DatasetResource.queryForJson($stateParams, function(datasets) {
        $scope.datasets = datasets;
        $scope.numberOfDatasetsArchived = _.filter($scope.datasets, 'archived').length;
        if ($scope.numberOfDatasetsArchived === 0) {
          $scope.showArchived = false;
        }
      }).$promise;
    }

    function setArchivedStatus(dataset) {
      var newArchivedStatus = !dataset.archived;
      var datasetUuid = dataset.uri.split('datasets/', 2)[1];
      DatasetResource.setArchived({
        userUid: $scope.userUid,
        datasetUuid: datasetUuid,
      }, {
        archived: newArchivedStatus
      }).$promise.then(reloadDatasets);
    }

    function toggleShowArchived() {
      $scope.showArchived = !$scope.showArchived;
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

'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'StudyHistoryResource', 'DatasetResource', 'StudiesWithDetailsService'];
    var StudyHistoryController = function($scope, $stateParams, StudyHistoryResource, DatasetResource, StudiesWithDetailsService) {

      $scope.userUid = $stateParams.userUid;
      $scope.datasetUUID = $stateParams.datasetUUID;
      $scope.studyGraphUuid = $stateParams.studyGraphUuid;

      function getDataset() {
        DatasetResource.getForJson($stateParams).$promise.then(function(response) {
          $scope.dataset = {
            datasetUri: $scope.datasetUUID,
            label: response['http://purl.org/dc/terms/title'],
            comment: response['http://purl.org/dc/terms/description'],
            creator: response['http://purl.org/dc/terms/creator']
          };
          $scope.dataset.uuid = $stateParams.datasetUUID;
        });
      }

      function getStudyTitle() {
        StudiesWithDetailsService.getWithoutDetails($scope.userUid, $scope.datasetUUID, null /* version */ , $scope.studyGraphUuid)
          .then(function(response) {
            $scope.studyTitle = response[0].label;
          });
      }

      getDataset();
      getStudyTitle();

      $scope.historyItems = StudyHistoryResource.query($stateParams);
      $scope.historyItems.$promise.then(function() {
        function oldToNew(a, b) {
          return b.i - a.i;
        }
        $scope.historyItems.sort(oldToNew);
        var headItemUri = ($scope.historyItems[0].uri);
        $scope.headVersion = headItemUri.substr(headItemUri.lastIndexOf('/') + 1);
      });
    };
    return dependencies.concat(StudyHistoryController);
  });

'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'StudyHistoryResource', 'DatasetResource', 'StudiesWithDetailsService'];
    var StudyHistoryController = function($scope, $stateParams, StudyHistoryResource, DatasetResource, StudiesWithDetailsService) {

      $scope.userUid = $stateParams.userUid;
      $scope.datasetUuid = $stateParams.datasetUuid;
      $scope.studyGraphUuid = $stateParams.studyGraphUuid;

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

      function getDataset() {
        DatasetResource.getForJson($stateParams).$promise.then(function(response) {
          $scope.dataset = {
            datasetUri: $scope.datasetUuid,
            label: response['http://purl.org/dc/terms/title'],
            comment: response['http://purl.org/dc/terms/description'],
            creator: response['http://purl.org/dc/terms/creator']
          };
          $scope.dataset.uuid = $stateParams.datasetUuid;
        });
      }

      function getStudyTitle() {
        StudiesWithDetailsService.getWithoutDetails($scope.userUid, $scope.datasetUuid, null /* version */ , $scope.studyGraphUuid)
          .then(function(response) {
            $scope.studyTitle = response[0].label;
          });
      }
    };
    return dependencies.concat(StudyHistoryController);
  });

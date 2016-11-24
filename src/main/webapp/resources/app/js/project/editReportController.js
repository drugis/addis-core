'use strict';
define(['angular', 'lodash'],
  function() {
    var dependencies = ['$scope', '$q', '$stateParams', 'ProjectResource', 'ReportResource', '$timeout'];
    var EditReportcontroller = function($scope, $q, $stateParams, ProjectResource, ReportResource, $timeout) {
      $scope.project = ProjectResource.get($stateParams);
      $scope.reportText = '';
      $scope.showSaving = false;
      $scope.showSaved = false;
      $scope.userId = $stateParams.userUid;
      $scope.saveChanges = saveChanges;
      $scope.resetToDefault = resetToDefault;
      $q.all([$scope.project.$promise, ReportResource.get($stateParams).$promise]).then(function(values) {
        $scope.loading = {
          loaded: true
        };
        $scope.reportText = values[1].data;
      });

      function saveChanges(newText) {
        ReportResource.put($stateParams, newText);
        $scope.showSaving = true;
        $timeout(function() {
          $scope.showSaving = false;
          $scope.showSaved = true;
        }, 1000);
        $timeout(function() {
          $scope.showSaved = false;
        }, 3000);
      }

      function resetToDefault() {
        ReportResource.delete($stateParams).$promise.then(function(value) {
          $scope.reportText = value.data;
        });
        return $scope.reportText;
      }

    };
    return dependencies.concat(EditReportcontroller);
  });

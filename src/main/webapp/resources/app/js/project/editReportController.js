'use strict';
define(['angular', 'lodash'],
  function() {
    var dependencies = ['$scope', '$q', '$stateParams', 'ProjectResource', 'ReportResource'];
    var EditReportcontroller = function($scope, $q, $stateParams, ProjectResource, ReportResource) {
      $scope.project = ProjectResource.get($stateParams);
      $scope.reportText = '';

      $q.all([$scope.project.$promise, ReportResource.get($stateParams).$promise]).then(function(values) {
        $scope.loading = {
          loaded: true
        };
        $scope.reportText = values[1].data;
      });

      $scope.userId = $stateParams.userUid;
      $scope.saveChanges = saveChanges;

      function saveChanges(newText) {
        ReportResource.put($stateParams, newText);
      }

    };
    return dependencies.concat(EditReportcontroller);
  });

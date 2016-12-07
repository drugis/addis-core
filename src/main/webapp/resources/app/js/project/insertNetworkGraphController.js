'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', 'AnalysisResource', 'ReportDirectiveService', 'callback'];
  var InsertNetworkGraphController = function($scope, $stateParams, $modalInstance, AnalysisResource, ReportDirectiveService, callback) {
    $scope.selection = {};

    AnalysisResource.query($stateParams).$promise.then(function(analyses) {
      $scope.analyses = _.filter(analyses, ['analysisType', 'Evidence synthesis']);
      if ($scope.analyses.length) {
        $scope.selection.analysis = $scope.analyses[0];
      }
    });

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };

    $scope.insertNetworkGraph = function() {
      callback(ReportDirectiveService.getDirectiveBuilder('network-plot')($scope.selection.analsysis.id));
      $modalInstance.close();
    };

  };
  return dependencies.concat(InsertNetworkGraphController);
});

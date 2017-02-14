'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', 'CacheService', 'ReportDirectiveService', 'callback'];
  var InsertNetworkGraphController = function($scope, $stateParams, $modalInstance, CacheService, ReportDirectiveService, callback) {
    $scope.selection = {};

    CacheService.getAnalyses($stateParams).then(function(analyses) {
      $scope.analyses = _.filter(analyses, ['analysisType', 'Evidence synthesis']);
      if ($scope.analyses.length) {
        $scope.selection.analysis = $scope.analyses[0];
      }
    });

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };

    $scope.insertNetworkGraph = function() {
      callback(ReportDirectiveService.getDirectiveBuilder('network-plot')($scope.selection.analysis.id));
      $modalInstance.close();
    };

  };
  return dependencies.concat(InsertNetworkGraphController);
});

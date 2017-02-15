'use strict';
define([], function() {
  var dependencies = ['$controller', '$scope', '$stateParams', '$modalInstance', '$q',
    'ReportDirectiveService', 'CacheService', 'callback'
  ];
  var InsertRankProbabilitiesTableController = function($controller, $scope, $stateParams, $modalInstance, $q,
    ReportDirectiveService, CacheService, callback) {
    $controller('BaseInsertDirectiveController', {
      $scope: $scope,
      $stateParams: $stateParams,
      $modalInstance: $modalInstance,
      $q: $q,
      ReportDirectiveService: ReportDirectiveService,
      CacheService: CacheService,
      callback: callback,
      directiveName: 'rank-probabilities-plot'
    });
  };
  return dependencies.concat(InsertRankProbabilitiesTableController);
});

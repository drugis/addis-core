'use strict';
define([], function() {
  var dependencies = ['$controller', '$scope', '$stateParams', '$modalInstance', '$q',
    'ReportDirectiveService', 'CacheService', 'callback'
  ];
  var InsertRelativeEffectsPlotController = function($controller, $scope, $stateParams, $modalInstance, $q,
    ReportDirectiveService, CacheService, callback) {
    $controller('BaseInsertDirectiveController', {
      $scope: $scope,
      $stateParams: $stateParams,
      $modalInstance: $modalInstance,
      $q: $q,
      ReportDirectiveService: ReportDirectiveService,
      CacheService: CacheService,
      callback: callback,
      directiveName: 'relative-effects-plot'
    });
  };
  return dependencies.concat(InsertRelativeEffectsPlotController);
});

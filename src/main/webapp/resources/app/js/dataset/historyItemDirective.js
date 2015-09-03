'use strict';
define([], function() {
  var depencencies = ['$stateParams'];
  var HistoryItemDirective = function($stateParams) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/dataset/historyItemDirective.html',
      scope: {
        item: '='
      },
      link: function(scope) {
        var uri = scope.item.uri;
        scope.userUid = $stateParams.userUid;
        scope.versionUuid = uri.substr(uri.lastIndexOf('/') + 1);
        scope.datasetUUID = $stateParams.datasetUUID;
      }
    };
  };
  return depencencies.concat(HistoryItemDirective);
});

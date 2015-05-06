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
        var itemId = scope.item['@id'];
        scope.userUid = $stateParams.userUid;
        scope.versionUuid = itemId.substr(itemId.lastIndexOf('/') + 1);
        scope.datasetUUID = $stateParams.datasetUUID;
      }
    };
  };
  return depencencies.concat(HistoryItemDirective);
});
'use strict';
define([], function() {
  var depencencies = ['$stateParams'];
  var HistoryItemDirective = function($stateParams) {
    return {
      restrict: 'E',
      templateUrl: './dataset/studyHistoryItemDirective.html',
      scope: {
        item: '='
      },
      link: function(scope) {
        function segmentAfterLastSlash(str) {
          return str.substr(str.lastIndexOf('/') + 1);
        }
        var uri = scope.item.uri;
        scope.userUid = $stateParams.userUid;
        scope.versionUuid = segmentAfterLastSlash(uri);
        scope.datasetUuid = $stateParams.datasetUuid;
        scope.studyGraphUuid = $stateParams.studyGraphUuidD;
        if(scope.item.merge) {
          var sourceUserUuid = scope.item.merge.sourceUserUuid;
          var sourceDatasetUri = scope.item.merge.sourceDatasetUri;
          var sourceVersionUri = scope.item.merge.version;
          var sourceGraphUri = scope.item.merge.graph;
          scope.sourceUserUuid = sourceUserUuid;
          scope.sourceDatasetUuid = segmentAfterLastSlash(sourceDatasetUri);
          scope.sourceVersionUuid = segmentAfterLastSlash(sourceVersionUri);
          scope.sourceGraphUuid = segmentAfterLastSlash(sourceGraphUri);
        }
      }
    };
  };
  return depencencies.concat(HistoryItemDirective);
});

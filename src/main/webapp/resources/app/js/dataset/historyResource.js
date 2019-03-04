'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var HistoryResource = function($resource) {
    return $resource('/users/:userUid/datasets/:datasetUuid/history/:versionUuid', {
      userUid: '@userUid',
      datasetUuid: '@datasetUuid',
      versionUuid: '@versionUuid'
    });
  };
  return dependencies.concat(HistoryResource);
});

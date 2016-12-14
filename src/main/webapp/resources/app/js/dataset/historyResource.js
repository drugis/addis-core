'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var HistoryResource = function($resource) {
    return $resource('/users/:userUid/datasets/:datasetUUID/history/:versionUuid', {
      userUid: '@userUid',
      datasetUUID: '@datasetUUID',
      versionUuid: '@versionUuid'
    });
  };
  return dependencies.concat(HistoryResource);
});

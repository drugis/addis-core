'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var HistoryResource = function($resource) {
    return $resource('/users/:userUid/datasets/:datasetUuid/history/:versionUuid', {
      userUuid: '@userUid',
      datasetUuid: '@datasetUuid',
      versionUuid: '@versionUuid'
    });
  };
  return dependencies.concat(HistoryResource);
});

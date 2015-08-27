'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var copyStudyResource = function($resource) {
    return $resource(
      '/users/:userUid/datasets/:datasetUUID/copy', {
        userUid: '@userUid',
        datasetUUID: '@datasetUUID',
        targetDatasetUuid: '@targetDatasetUuid',
        targetGraph: '@targetGraph',
        sourceGraph: '@sourceGraph',
        sourceDatasetUuid: '@sourceDatasetUuid',
        sourceVersion: '@sourceVersion'
      });
  };
  return dependencies.concat(copyStudyResource);
});

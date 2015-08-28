'use strict';
define([], function() {

  var dependencies = ['$resource', '$stateParams'];
  var copyStudyResource = function($resource, $stateParams) {
    return $resource(
      '/users/:userUid/datasets/:datasetUUID/copy', {
        userUid: $stateParams.userUid,
        datasetUUID: '@targetDatasetUuid',
        targetGraph: '@targetGraph',
        sourceGraph: '@sourceGraph',
        sourceDatasetUri: '@sourceDatasetUri',
        sourceVersion: '@sourceVersion'
      });
  };
  return dependencies.concat(copyStudyResource);
});

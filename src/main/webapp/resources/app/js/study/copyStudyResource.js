'use strict';
define([], function() {

  var dependencies = ['$resource', '$stateParams'];
  var copyStudyResource = function($resource, $stateParams) {
    return $resource(
      '/users/:userUid/datasets/:datasetUuid/graphs/:graphUuid', {
        userUid: $stateParams.userUid,
        datasetUuid: '@targetDatasetUuid',
        graphUuid: '@targetGraphUuid',
        copyOf: '@copyOf'
      }, {
        'copy': {
          method: 'PUT'
        }
      });
  };
  return dependencies.concat(copyStudyResource);
});

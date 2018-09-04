'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var ImportStudyResource = function($resource) {

    return $resource(
      '/users/:userUid/datasets/:datasetUuid/graphs/:graphUuid/import/:importStudyRef', {
        userUid: '@userUid',
        datasetUuid: '@datasetUuid',
        graphUuid: '@graphUuid',
        importStudyRef: '@importStudyRef',
        commitTitle: '@commitTitle',
        commitDescription: '@commitDescription'
      }, {
        import: {
          method: 'post',
        },
      }
    );
  };
  return dependencies.concat(ImportStudyResource);
});

'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var ImportEudraCTResource = function($resource) {

    return $resource(
      '/users/:userUid/datasets/:datasetUuid/graphs/:graphUuid/import/eudract', {
        userUid: '@userUid',
        datasetUuid: '@datasetUuid',
        graphUuid: '@graphUuid',
        commitTitle: '@commitTitle'
      }, {
        import: {
          method: 'post',
        },
      }
    );
  };
  return dependencies.concat(ImportEudraCTResource);
});

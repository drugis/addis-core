'use strict';
define([], function() {
  var PATH = '/users/:userUid/datasets/:datasetUuid/graphs/import-eudract/';
  var dependencies = ['$resource'];
  var ImportEudraCTResource = function($resource) {

    return $resource(
      PATH,{
        userUid : '@userUid',
        datasetUuid: '@datasetUuid'
      }, {
        import: {
          method: 'post',
          headers: {
            'Content-Type': 'application/xml'
          }
        },
      }
    );
  };
  return dependencies.concat(ImportEudraCTResource);
});

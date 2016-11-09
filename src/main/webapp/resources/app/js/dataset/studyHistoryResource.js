'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var HistoryResource = function($resource) {
    return $resource('/users/:userUid/datasets/:datasetUUID/graphs/:studyGraphUuid/history', {
      userUid: '@userUid',
      datasetUUID: '@datasetUUID',
      studyGraphUuid: '@studyGraphUuid'
    }, {
      'query': {
        isArray: true,
        method: 'get',
        headers: {
          'Accept': 'application/ld+json'
        }
      }
    });
  };
  return dependencies.concat(HistoryResource);
});

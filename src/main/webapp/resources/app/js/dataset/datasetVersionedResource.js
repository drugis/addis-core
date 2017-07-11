'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var DatasetVersionedResource = function($resource) {
    return $resource('/users/:userUid/datasets/:datasetUuid/versions/:versionUuid', {
      userUid: '@userUid',
      datasetUuid: '@datasetUuid',
      versionUuid: '@versionUuid'
    }, {
      'get': {
        method: 'get',
        headers: {
          'Accept': 'text/turtle'
        },
        transformResponse: function(data) {
          return {
            data: data // property on response object to access raw result data
          };
        },
        cache: true
      },
      'getForJson': {
        method: 'get',
        headers: {
          'Accept': 'application/json; charset=UTF-8'
        },
        cache: true
      },
      'query': {
        method: 'GET',
        headers: {
          'Accept': 'text/turtle'
        },
        isArray: false,
        transformResponse: function(data) {
          return {
            data: data // property on response object to access raw result data
          };
        },
        cache: true
      },
      'queryForJson': {
        method: 'GET',
        isArray: true,
        headers: {
          'Accept': 'application/json; charset=UTF-8'
        },
        cache: true
      }
    });
  };
  return dependencies.concat(DatasetVersionedResource);
});

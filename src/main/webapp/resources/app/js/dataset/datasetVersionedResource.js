'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var DatasetVersionedResource = function($resource) {
    return $resource('/users/:userUid/datasets/:datasetUUID/versions/:versionUuid', {
      userUid: '@userUid',
      datasetUUID: '@datasetUUID',
      versionUuid: '@versionUuid'
    }, {
      'get': {
        method: 'get',
        headers: {
          'Accept': 'text/turtle'
        },
        transformResponse: function(data) {
          return {
            data: data // property on Responce object to access raw result data
          };
        }
      },
      'getForJson': {
        method: 'get',
        headers: {
           'Accept': 'application/json; charset=UTF-8'
        }
      },
      'query': {
        method: 'GET',
        headers: {
          'Accept': 'text/turtle'
        },
        isArray: false,
        transformResponse: function(data) {
          return {
            data: data // property on Responce object to access raw result data
          };
        },
      },
      'queryForJson': {
        method: 'GET',
        isArray: true,
        headers: {
          'Accept': 'application/json; charset=UTF-8'
        }
      }
    });
  };
  return dependencies.concat(DatasetVersionedResource);
});
'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var DatasetResource = function($resource) {
    return $resource('/users/:userUid/datasets/:datasetUuid', {
      userUid: '@userUid',
      datasetUuid: '@datasetUuid'
    }, {
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
        }
      },
      'queryForJson': {
        method: 'GET',
        isArray: true,
        headers: {
          'Accept': 'application/json; charset=UTF-8'
        }
      },
      'get': {
        method: 'get',
        headers: {
          'Accept': 'text/turtle'
        },
        transformResponse: function(data) {
          return {
            data: data // property on response object to access raw result data
          };
        }
      },
      'getForJson': {
        method: 'get',
        headers: {
          'Accept': 'application/json; charset=UTF-8'
        }
      },
      'getFeatured': {
        url: '/featured',
        method: 'get',
        isArray: true
      }
    });
  };
  return dependencies.concat(DatasetResource);
});

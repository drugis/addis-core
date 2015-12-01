'use strict';
define(['util/transformJsonLd'], function(transformJsonLd) {

  var dependencies = ['$resource'];
  var VersionedGraphResource = function($resource) {
    return $resource(
      '/users/:userUid/datasets/:datasetUUID/versions/:versionUuid/graphs/:graphUuid', {
        usersUid: '@usersUid',
        datasetUUID: '@datasetUUID',
        graphUuid: '@graphUuid',
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
        'getJson': {
          method: 'get',
          headers: {
            'Accept': 'application/ld+json'
          },
          transformResponse: function(data) {
            return transformJsonLd(JSON.parse(data))
          }
        },
        'getJsonNoTransform': {
          method: 'get',
          headers: {
            'Accept': 'application/ld+json'
          },
          transformResponse: function(data) {
            return JSON.parse(data);
          }
        }
      });
  };
  return dependencies.concat(VersionedGraphResource);
});
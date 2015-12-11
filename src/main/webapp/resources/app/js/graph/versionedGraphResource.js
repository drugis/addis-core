'use strict';
define(['util/transformJsonLd', 'util/transformConceptJsonLd'], function(transformStudyJsonLd, transformConceptJsonLd) {

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
            return transformStudyJsonLd(JSON.parse(data))
          }
        },
        'getConceptJson': {
          method: 'get',
          headers: {
            'Accept': 'application/ld+json'
          },
          transformResponse: function(data) {
            return transformConceptJsonLd(JSON.parse(data));
          }
        }
      });
  };
  return dependencies.concat(VersionedGraphResource);
});
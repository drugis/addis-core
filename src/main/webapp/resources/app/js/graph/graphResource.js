'use strict';
define(['util/transformJsonLd', 'util/transformConceptJsonLd'], function(transformStudyJsonLd, transformConceptJsonLd) {

  var dependencies = ['$resource'];
  var GraphResource = function($resource) {

    return $resource(
      '/users/:userUid/datasets/:datasetUUID/graphs/:graphUuid', {
        userUid: '@userUid',
        datasetUUID: '@datasetUUID',
        graphUuid: '@graphUuid',
        commitTitle: '@commitTitle',
        commitDescription: '@commitDescription'
      }, {
        'put': {
          method: 'put'
        },
        'getJson': {
          method: 'get',
          headers: {
            'Accept': 'application/ld+json'
          },
          transformResponse: function(data) {
            return transformStudyJsonLd(JSON.parse(data));
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
        },
        'putJson': {
          method: 'put',
          headers: {
            'Content-Type': 'application/ld+json'
          }
        }
      }
    );
  };
  return dependencies.concat(GraphResource);
});
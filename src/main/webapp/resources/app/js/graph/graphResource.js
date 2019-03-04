'use strict';
define(['../util/transformJsonLd',
  '../util/transformConceptJsonLd'],
  function(transformStudyJsonLd, transformConceptJsonLd) {

    var dependencies = ['$resource', 'DataModelService'];
    var GraphResource = function($resource, DataModelService) {
      return $resource(
        '/users/:userUid/datasets/:datasetUuid/graphs/:graphUuid', {
          userUid: '@userUid',
          datasetUuid: '@datasetUuid',
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
              var graphData = JSON.parse(data);
              graphData = DataModelService.applyOnLoadCorrections(graphData);
              return transformStudyJsonLd(graphData);
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
  }
);

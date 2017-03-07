'use strict';
define(['util/transformJsonLd', 'util/transformConceptJsonLd'], function(transformStudyJsonLd, transformConceptJsonLd) {

  var dependencies = ['$resource', 'DataModelService'];
  var VersionedGraphResource = function($resource, DataModelService) {
    return $resource(
      '/users/:userUid/datasets/:datasetUuid/versions/:versionUuid/graphs/:graphUuid', {
        usersUid: '@usersUid',
        datasetUuid: '@datasetUuid',
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
            var graphData = JSON.parse(data);
            graphData = DataModelService.normalizeFirstAndRest(graphData);
            graphData = DataModelService.updateCategories(graphData);
            graphData = DataModelService.addTypeToUnits(graphData);
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
        }
      });
  };
  return dependencies.concat(VersionedGraphResource);
});

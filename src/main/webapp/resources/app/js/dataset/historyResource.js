'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var HistoryResource = function($resource) {
    return $resource('/users/:userUid/datasets/:datasetUUID/versions', {
      userUid: '@userUid',
      datasetUUID: '@datasetUUID'
    }, {
      'query': {
        isArray: true,
        method: 'get',
        headers: {
          'Accept': 'application/ld+json'
        },
        transformResponse: function(data) {
          var versionItems = _.filter(JSON.parse(data)['@graph'], function(graphItem) {
            return graphItem['@id'].indexOf('/versions/') > 0; // filter to only contain actual history nodes
          });
          return _.map(versionItems, function(versionItem) {
            return _.extend(versionItem, {
              title: versionItem['http://purl.org/dc/terms/title'],
              description: versionItem['http://purl.org/dc/terms/description']
            });
          });
        }
      }
    });
  };
  return dependencies.concat(HistoryResource);
});

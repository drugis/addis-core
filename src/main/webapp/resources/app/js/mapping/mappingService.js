'use strict';
define([], function() {
  var dependencies = [];

  var MappingService = function() {

    function queryItems(datasetUuid) {
      return [{
        label: 'test'
      }];
    }

    return {
      queryItems: queryItems
    };

  };
  return dependencies.concat(MappingService);
});

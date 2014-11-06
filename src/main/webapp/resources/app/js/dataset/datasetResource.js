'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var DatasetResource = function($resource) {
    return $resource('/datasets/:datasetUID', {
      datasetUID: '@datasetUID'
    });
  };
  return dependencies.concat(DatasetResource);
});
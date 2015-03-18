'use strict';
define([], function(rdfstore) {

  var dependencies = ['$resource', '$q'];
  var StudiesWithDetailResource = function($resource, $q) {
    return $resource('/datasets/:datasetUUID/studiesWithDetail', {
      datasetUUID: '@datasetUUID'
    });
  };
  return dependencies.concat(StudiesWithDetailResource);
});

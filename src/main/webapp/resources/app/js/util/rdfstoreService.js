'use strict';
define(['rdfstore'], function(rdfstore) {
  var dependencies = [];
  var RdfstoreService = function() {
    return rdfstore;
  };
  return dependencies.concat(RdfstoreService);
});

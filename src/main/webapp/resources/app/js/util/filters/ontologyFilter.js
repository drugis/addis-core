'use strict';
define([], function() {
  var dependencies = [];
  var OntologyFilter = function() {
    return function(ontologyUri) {
      if (ontologyUri) {
        return ontologyUri.substring(ontologyUri.lastIndexOf(':') + 1, ontologyUri.length);
      }
      return ontologyUri;
    };
  };
  return dependencies.concat(OntologyFilter);
});

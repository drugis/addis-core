'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var SemanticInterventionResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/interventions', {
      namespaceUid: '@id'
    });
  };
  return dependencies.concat(SemanticInterventionResource);
});
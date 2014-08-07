'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var SemanticOutcomeResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/outcomes', {
      namespaceUid: '@id'
    });
  };
  return dependencies.concat(SemanticOutcomeResource);
});
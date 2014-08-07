'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var TrialverseStudyResource = function($resource) {
    return $resource('/namespaces/:namespaceUid/studies', {
      namespaceUid: '@id'
    });
  };
  return dependencies.concat(TrialverseStudyResource);
});
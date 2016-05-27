'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var ImportStudyInfoResource = function($resource) {
    return $resource('/import/:nctId', {});
  };
  return dependencies.concat(ImportStudyInfoResource);
});

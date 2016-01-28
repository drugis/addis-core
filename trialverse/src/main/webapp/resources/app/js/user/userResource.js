'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var UserResource = function($resource) {
    return $resource('/users/:userUid');
  };
  return dependencies.concat(UserResource);
});
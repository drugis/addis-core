'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var UserResource = function($resource) {
    return $resource('/users/:userUid', {}, {
      'query': {
        method: 'GET',
        headers: {
          'Accept': 'text/turtle'
        },
        isArray: false,
        transformResponse: function(data) {
          return {
            data: data // property on Responce object to access raw result data
          };
        }
      }
    });
  };
  return dependencies.concat(UserResource);
});
'use strict';
define(['angular'], function() {
  var dependencies = ['$q', '$rootScope'];
  var SessionExpiredInterceptor = function($q, $rootScope) {
    return {
      responseError: function(response) {
        if(response.status === 403) {
          $rootScope.$broadcast('sessionExpired');
          return $q.resolve(response);
        }
        return $q.reject(response);
      }
    };
  };
  return dependencies.concat(SessionExpiredInterceptor);
});
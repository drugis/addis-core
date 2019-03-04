'use strict';
define(['angular'], function() {
  var dependencies = ['$rootScope','$location', '$anchorScroll', '$modal'];
  var sessionExpiredDirective = function($rootScope, $location, $anchorScroll, $modal) {
    return {
      restrict: 'E',
      link: function(scope) {
        scope.$on('sessionExpired', function() {
          $location.hash('logo');
          $anchorScroll();
          $modal.open({
            templateUrl: './sessionExpiredDirective.html'
          });
        });
      }
    };
  };

  return dependencies.concat(sessionExpiredDirective);
});

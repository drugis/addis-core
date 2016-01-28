'use strict';
define(['angular'], function() {
  var dependencies = ['$location', '$anchorScroll', '$modal'];
  var sessionExpiredDirective = function($location, $anchorScroll, $modal) {
    return {
      restrict: 'E',
      link: function(scope) {
        scope.$on('sessionExpired', function() {
          $location.hash('logo');
          $anchorScroll();
          $modal.open({
            templateUrl: 'app/js/util/directives/sessionExpired/sessionExpiredDirective.html'
          });
        });
      }
    };
  };

  return dependencies.concat(sessionExpiredDirective);
});
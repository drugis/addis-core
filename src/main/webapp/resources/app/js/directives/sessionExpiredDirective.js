'use strict';
define(['angular'], function() {
  var dependencies = ['$location', '$anchorScroll'];
  var sessionExpiredDirective = function($location, $anchorScroll) {
    return {
      restrict: "E",

      link: function(scope, element, attrs) {
        
        scope.$on('sessionExpired', function() {
          $location.hash('addis-logo');
          $anchorScroll();
          scope.sessionExpiredModal.open();
        });

      },
      templateUrl: '/app/partials/sessionExpired.html'
    };
  };

  return dependencies.concat(sessionExpiredDirective);
});
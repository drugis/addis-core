'use strict';
define([], function() {
  var dependencies = ['$window'];

  var NavbarDirective = function($window) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/util/directives/navbar/navbarDirective.html',
      transclude: true,
      link: function(scope) {
        scope.userInfo = {
          imageUrl: 'https://secure.gravatar.com/avatar/' + $window.config.user.userMd5 + '?s=43&d=mm',
          name: $window.config.user.firstName + " " + $window.config.user.lastName
        }
      }
    };
  };
  return dependencies.concat(NavbarDirective);
});

'use strict';
define([], function() {
  var dependencies = ['$window', '$state', '$stateParams', 'md5', 'UserResource'];
  var NavbarDirective = function($window, $state, $stateParams, md5, UserResource) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/util/directives/navbar/navbarDirective.html',
      transclude: true,
      link: function(scope) {
        scope.loginUserInfo = {
          imageUrl: 'https://secure.gravatar.com/avatar/' + md5.createHash($window.config.user.userEmail) + '?s=43&d=mm',
          name: $window.config.user.firstName + ' ' + $window.config.user.lastName,
          userNameHash: $window.config.user.userNameHash,
          id: $window.config.user.id
        };
        scope.isOwnUserPage = $state.current.name === 'user' &&
         $window.config.user.id === $stateParams.userUid;

        scope.user = UserResource.get($stateParams);
      }
    };
  };
  return dependencies.concat(NavbarDirective);
});
'use strict';
define([], function() {
  var dependencies = ['$state', '$stateParams', 'md5', 'UserResource', 'UserService', '$cookies', '$location'];
  var NavbarDirective = function($state, $stateParams, md5, UserResource, UserService, $cookies, $location) {
    return {
      restrict: 'E',
      templateUrl: './navbarDirective.html',
      transclude: true,
      link: function(scope) {

        scope.isAnonymous = true;

        if (UserService.hasLoggedInUser()) {
          scope.isAnonymous = false;
          var loggedInUser = UserService.getLoginUser();
          scope.loginUserInfo = {
            imageUrl: 'https://secure.gravatar.com/avatar/' + md5.createHash(loggedInUser.userEmail) + '?s=43&d=mm',
            name: loggedInUser.firstName + ' ' + loggedInUser.lastName,
            userNameHash: loggedInUser.userNameHash,
            id: loggedInUser.id
          };
          scope.isOwnUserPage = $state.current.name === 'user' && loggedInUser.id === $stateParams.userUid;
        }

        if ($stateParams.userUid) {
          scope.user = UserResource.get($stateParams);
        }

        scope.signin = function() {
          $cookies.put('returnToPage', $location.path());
        };
      }
    };
  };
  return dependencies.concat(NavbarDirective);
});

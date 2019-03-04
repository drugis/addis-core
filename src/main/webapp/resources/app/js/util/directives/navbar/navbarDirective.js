'use strict';
define([], function() {
  var dependencies = ['$state', '$stateParams', 'UserResource', 'UserService', '$cookies', '$location'];
  var NavbarDirective = function($state, $stateParams, UserResource, UserService, $cookies, $location) {
    return {
      restrict: 'E',
      templateUrl: './navbarDirective.html',
      transclude: true,
      link: function(scope) {

        scope.isAnonymous = true;
        scope.signOut = UserService.logOut;

        UserService.getLoginUser().then(function(loggedInUser) {
          if(loggedInUser) {
            scope.isAnonymous = false;
            scope.loginUserInfo = {
              imageUrl: loggedInUser.imageUrl,
              name: loggedInUser.firstName + ' ' + loggedInUser.lastName,
              userNameHash: loggedInUser.userNameHash,
              id: loggedInUser.id
            };
            scope.isOwnUserPage = $state.current.name === 'user' && loggedInUser.id === $stateParams.userUid;
          }
        });

        if ($stateParams.userUid) {
          scope.user = UserResource.get($stateParams);
        }

        scope.signin = function() {
          UserService.logOut();
          $cookies.put('returnToPage', $location.path());
        };
      }
    };
  };
  return dependencies.concat(NavbarDirective);
});

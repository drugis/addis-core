'use strict';
define([], function() {
  var dependencies = ['$state', '$stateParams', 'md5', 'UserResource', 'UserService', '$cookies', '$location'];
  var NavbarDirective = function($state, $stateParams, md5, UserResource, UserService, $cookies, $location) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/util/directives/navbar/navbarDirective.html',
      transclude: true,
      link: function(scope) {

        scope.isAnonimous = true;

        if (UserService.hasLogedInUser()) {
          scope.isAnonimous = false;
          var logedInUser = UserService.getLoginUser();
          scope.loginUserInfo = {
            imageUrl: 'https://secure.gravatar.com/avatar/' + md5.createHash(logedInUser.userEmail) + '?s=43&d=mm',
            name: logedInUser.firstName + ' ' + logedInUser.lastName,
            userNameHash: logedInUser.userNameHash,
            id: logedInUser.id
          };
          scope.isOwnUserPage = $state.current.name === 'user' && logedInUser.id === $stateParams.userUid;
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

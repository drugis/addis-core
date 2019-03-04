'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$q', '$window', 'UserResource'];
    var UserService = function($q, $window, UserResource) {

      function getLoginUser() {
        if ($window.sessionStorage.getItem('user')) {
          return $q.resolve(JSON.parse($window.sessionStorage.getItem('user')));
        } else {
          return UserResource.get({ 'userUid': 'me' }).$promise.then(function(user) {
            var loggedInUser = unResource(user);
            var userFound = loggedInUser.id;
            if (userFound) {
              $window.sessionStorage.setItem('user', JSON.stringify(loggedInUser));
            }
            return userFound ? loggedInUser : undefined;
          });
        }
      }

      function unResource(result) {
        return _.pick(result, ['id', 'firstName', 'lastName', 'email', 'imageUrl']);
      }

      function isLoginUserId(id) {
        return getLoginUser().then(function(user) {
          return user && user.id === id;
        });
      }

      function isLoginUserEmail(email) {
        return getLoginUser().then(function(user) {
          return user && user.email === email;
        });
      }

      function logOut() {
        $window.sessionStorage.removeItem('user');
      }

      return {
        getLoginUser: getLoginUser,
        isLoginUserId: isLoginUserId,
        isLoginUserEmail: isLoginUserEmail,
        logOut: logOut
      };
    };

    return dependencies.concat(UserService);
  });

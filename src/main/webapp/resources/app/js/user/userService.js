'use strict';
define([],
  function() {
    var dependencies = ['$q', 'UserResource'];
    var UserService = function($q, UserResource) {

      function hasLoggedInUser(callback) {
        return getLoginUser.then(callback);
      }

      function getLoginUser() {
        if (window.sessionStorage.user) {
          return $q.resolve(window.sessionStorage.user);
        } else {
          return UserResource.get({'userUid': 'me'}).$promise.then(function(user) {
            window.sessionStorage.user = user ? user : undefined;
            return user;
          });
        }
      }

      function isLoginUserId(id) {
        return getLoginUser().then(function(user) {
            return user && user.id === id;
        });
      }

      function isLoginUserEmail(email) {
        return getLoginUser().then(function(user) {
            return user && user.userEmail === email;
        });
      }

      function logOut() {
        delete window.sessionStorage.user;
      }

      return {
        hasLoggedInUser: hasLoggedInUser,
        getLoginUser: getLoginUser,
        isLoginUserId: isLoginUserId,
        isLoginUserEmail: isLoginUserEmail,
        logOut: logOut
      };
    };

    return dependencies.concat(UserService);
  });

'use strict';
define([],
  function() {
    var dependencies = ['$window'];
    var UserService = function($window) {

      function hasLoggedInUser() {
        return !!($window.config && $window.config.user);
      }

      function getLoginUser() {
        return $window.config.user;
      }

      function isLoginUserId(id) {
        return hasLoggedInUser() && $window.config.user.id === id;
      }

      function isLoginUserEmail(email) {
        return hasLoggedInUser() && $window.config.user.userEmail === email;
      }

      return {
        hasLoggedInUser: hasLoggedInUser,
        getLoginUser: getLoginUser,
        isLoginUserId: isLoginUserId,
        isLoginUserEmail: isLoginUserEmail
      };
    };

    return dependencies.concat(UserService);
  });

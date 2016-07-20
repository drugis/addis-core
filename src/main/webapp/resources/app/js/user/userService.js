'use strict';
define([],
  function() {
    var dependencies = ['$window'];
    var UserService = function($window) {

      function hasLogedInUser() {
        return !!($window.config && $window.config.user);
      }

      function getLoginUser() {
        return $window.config.user;
      }

      function isLoginUserId(id) {
        return hasLogedInUser() && $window.config.user.id === id;
      }

      function isLoginUserEmail(email) {
        return hasLogedInUser() && $window.config.user.userEmail === email;
      }

      return {
        hasLogedInUser: hasLogedInUser,
        getLoginUser: getLoginUser,
        isLoginUserId: isLoginUserId,
        isLoginUserEmail: isLoginUserEmail
      };
    };

    return dependencies.concat(UserService);
  });

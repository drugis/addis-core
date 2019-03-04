'use strict';

define([
  './userController',
  './userService',
  './userResource',

  'angular',
  'angular-resource',
  '../dataset/dataset'
],
  function(
    UserController,
    UserService,
    UserResource,
    angular
  ) {
    return angular.module('trialverse.user', ['ngResource', 'trialverse.dataset'])
      //controllers
      .controller('UserController', UserController)

      //services
      .factory('UserService', UserService)

      //resources
      .factory('UserResource', UserResource)
  }
);

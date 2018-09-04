'use strict';

define([
  './userController',
  './createDatasetController',
  './userService',
  './userResource',
  '../dataset/dataset',
  'angular',
  'angular-resource'
],
  function(
    UserController,
    CreateDatasetController,
    UserService,
    UserResource,
    DatasetResource,
    angular
  ) {
    return angular.module('trialverse.user', ['ngResource', 'trialverse.dataset'])
      //controllers
      .controller('UserController', UserController)
      .controller('CreateDatasetController', CreateDatasetController)

      //services
      .factory('UserService', UserService)

      //resources
      .factory('UserResource', UserResource)
      .factory('DatasetResource', DatasetResource);
  }
);

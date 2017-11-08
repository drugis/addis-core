'use strict';
var requires = [
  'user/userController',
  'user/createDatasetController',
  'user/userService',
  'user/userResource',
  'dataset/datasetResource'
];
define(requires.concat(['angular', 'angular-resource']), function(
  UserController,
  CreateDatasetController,
  UserService,
  UserResource,
  DatasetResource,
  angular
) {
  return angular.module('trialverse.user', ['ngResource', 'trialverse.util', 'trialverse.dataset'])
    //controllers
    .controller('UserController', UserController)
    .controller('CreateDatasetController', CreateDatasetController)

    //services
    .factory('UserService', UserService)

    //resources
    .factory('UserResource', UserResource)
    .factory('DatasetResource', DatasetResource);
});
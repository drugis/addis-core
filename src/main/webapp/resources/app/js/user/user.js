'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('trialverse.user', ['ngResource', 'trialverse.util', 'trialverse.dataset'])
    //controllers
    .controller('UserController', require('user/userController'))
    .controller('CreateDatasetController', require('user/createDatasetController'))

    //services

    //resources
    .factory('UserResource', require('user/userResource'))
    .factory('DatasetResource', require('dataset/datasetResource'))
    ;
});

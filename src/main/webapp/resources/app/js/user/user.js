'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('trialverse.user', ['ngResource', 'trialverse.util'])
    //controllers
    .controller('UserController', require('user/userController'))
    .controller('CreateDatasetController', require('user/createDatasetController'))

    //services
    .factory('DatasetService', require('dataset/datasetService'))

    //resources
    .factory('UserResource', require('user/userResource'))
    .factory('DatasetVersionedResource', require('dataset/datasetVersionedResource'))
    .factory('DatasetResource', require('dataset/datasetResource'))
    ;
});

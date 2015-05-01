'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('trialverse.user', ['ngResource', 'trialverse.util'])
    //controllers
    .controller('DatasetsController', require('user/datasetsController'))
    .controller('CreateDatasetController', require('user/createDatasetController'))

    //services
    .factory('DatasetService', require('dataset/datasetService'))

    //resources
    .factory('DatasetVersionedResource', require('dataset/datasetVersionedResource'))
    .factory('DatasetResource', require('dataset/datasetResource'))
    ;
});

'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('trialverse.dataset', ['ngResource'])
    //controllers
    .controller('DatasetsController', require('dataset/datasetsController'))
    .controller('CreateDatasetController', require('dataset/createDatasetController'))
    .controller('DatasetController', require('dataset/datasetController'))

    //services
    .factory('DatasetService', require('dataset/datasetService'))

    //resources
    .factory('DatasetResource', require('dataset/datasetResource'))
    ;
});

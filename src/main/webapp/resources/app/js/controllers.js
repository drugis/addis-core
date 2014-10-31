'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('trialverse.controllers', [])
    .controller('DatasetsController', require('controllers/datasetsController'))
    .controller('CreateDatasetController', require('controllers/createDatasetController'))
    ;
});

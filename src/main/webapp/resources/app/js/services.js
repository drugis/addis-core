'use strict';
define(function(require) {
  var angular = require('angular');
  return angular.module('trialverse.services', [])
    .factory('DatasetService', require('services/datasetService'));
});

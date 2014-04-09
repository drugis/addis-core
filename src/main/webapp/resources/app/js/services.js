'use strict';
define(function(require) {
  var angular = require('angular', '');
  return angular.module('addis.services', [])
    .factory('Select2UtilService', require('services/select2UtilService'))
    .factory('analysisService', require('services/analysisService'));
});
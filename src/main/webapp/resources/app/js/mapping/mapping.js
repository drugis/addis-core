'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('trialverse.mapping', ['trialverse.study'])
  	.factory('MappingService', require('mapping/mappingService'))
  	;
  });
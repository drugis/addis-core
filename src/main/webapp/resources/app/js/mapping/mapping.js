'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('trialverse.mapping', ['trialverse.study'])

    //services
  	.factory('MappingService', require('mapping/mappingService'))

  	//directives
    .directive('conceptMappingList', require('mapping/conceptMappingListDirective'))
    .directive('conceptMappingItem', require('mapping/conceptMappingItemDirective'))

  	;
  });
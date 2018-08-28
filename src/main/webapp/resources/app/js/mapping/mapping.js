'use strict';
define([
  './mappingService',
  './conceptMappingListDirective',
  './conceptMappingItemDirective',
  'angular'
],
  function(
    MappingService,
    conceptMappingList,
    conceptMappingItem,
    angular
  ) {
    return angular.module('trialverse.mapping', ['trialverse.study'])

      //services
      .factory('MappingService', MappingService)

      //directives
      .directive('conceptMappingList', conceptMappingList)
      .directive('conceptMappingItem', conceptMappingItem)

      ;
  }
);

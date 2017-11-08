'use strict';
var requires = [
  'mapping/mappingService',
  'mapping/conceptMappingListDirective',
  'mapping/conceptMappingItemDirective'
];
define(requires.concat(['angular']), function(
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
});
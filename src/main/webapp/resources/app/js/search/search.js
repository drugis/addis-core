'use strict';
var requires = [
  'search/searchController',
  'search/searchService',
  'search/searchResultDirective'
];
define(requires.concat(['angular', 'angular-resource']), function(
  SearchController,
  SearchService,
  searchResult,
  angular
) {
  return angular.module('trialverse.search', ['ngResource', 'trialverse.util'])
    // controllers
    .controller('SearchController', SearchController)

    // services
    .factory('SearchService', SearchService)

    //directives
    .directive('searchResult', searchResult);
});
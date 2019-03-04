'use strict';
define([
  './searchController',
  './searchService',
  './searchResultDirective',
  'angular',
  'angular-resource'],
  function(
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
  }
);

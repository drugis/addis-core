'use strict';

define(function(require) {
	var angular = require('angular');

	return angular.module('trialverse.search', ['ngResource', 'trialverse.util'])
		// controllers
		.controller('SearchController', require('search/searchController'))

		// services
		.factory('SearchService', require('search/searchService'))

    //directives
    .directive('searchResult', require('search/searchResultDirective'))
    ;
});

'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.util', [])
    // services
    .factory('UUIDService', require('util/uuidService'))
    .factory('RemoteRdfStoreService', require('util/remoteRdfStoreService'))
    .factory('DurationService', require('util/durationService'))
    .factory('SubsetSelectService', require('util/directives/subsetSelect/subsetSelectService'))
    .factory('CommentService', require('util/commentService'))

    // resources
    .factory('SparqlResource', require('util/sparqlResource'))

    // filters
    .filter('ontologyFilter', require('util/filters/ontologyFilter'))
    .filter('durationFilter', require('util/filters/durationFilter'))
    .filter('stripFrontFilter', require('util/filters/stripFrontFilter'))
    .filter('exponentialFilter', require('util/filters/exponentialFilter'))

    //directives
    .directive('navbarDirective', require('util/directives/navbar/navbarDirective'))
    .directive('subsetSelect', require('util/directives/subsetSelect/subsetSelectDirective'))
    .directive('durationInput', require('util/directives/durationInput/durationInputDirective'))
    .directive('sessionExpired', require('util/directives/sessionExpired/sessionExpiredDirective'))

    //interceptors
    .factory('SessionExpiredInterceptor', require('util/interceptors/sessionExpiredInterceptor'));

    ;
});

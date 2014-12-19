'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.util', [])
    // services
    .factory('UUIDService', require('util/uuidService'))
    .factory('JsonLdService', require('util/jsonLdService'))
    .factory('RdfStoreService', require('util/rdfstoreService'))

    // resources
    .factory('SparqlResource', require('util/sparqlResource'))

    // filters
    .filter('ontologyFilter', require('util/filters/ontologyFilter'))
    .filter('durationFilter', require('util/filters/durationFilter'))

    //directives
    .directive('navbarDirective', require('navbar/navbarDirective'))
    ;
});

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
    ;
});

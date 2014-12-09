'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.util', [])
    .factory('UUIDService', require('util/uuidService'))
    .factory('JsonLdService', require('util/jsonLdService'))
    .factory('RdfstoreService', require('util/rdfstoreService'))
    ;
});
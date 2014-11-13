'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('trialverse.rdfstoreWrap', [])

    //services
    .factory('RdfstoreService', require('rdfstoreWrap/rdfstoreService'))

    ;
});

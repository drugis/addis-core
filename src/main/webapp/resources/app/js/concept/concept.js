'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.concept', ['ngResource'])
    // services
    .factory('ConceptService', require('concept/conceptService'))

    //resources
    .factory('ConceptResource', require('concept/conceptResource'))
    
    //controllers
    .controller('ConceptController', require('concept/conceptController'))
    ;
});
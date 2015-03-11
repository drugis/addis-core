'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.concept', [])
     // services
    .factory('ConceptService', require('concept/conceptService'))

    //controllers
    .controller('ConceptController', require('concept/conceptController'))
    ;
});
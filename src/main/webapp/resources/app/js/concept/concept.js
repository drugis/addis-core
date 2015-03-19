'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.concept', ['ngResource',
      'trialverse.graph',
      'trialverse.util'])
    // services
    .factory('ConceptService', require('concept/conceptService'))

    //controllers
    .controller('ConceptController', require('concept/conceptController'))

     //resources
    .factory('DatasetResource', require('dataset/datasetResource')) // only need resource, not entire module

    ;
});

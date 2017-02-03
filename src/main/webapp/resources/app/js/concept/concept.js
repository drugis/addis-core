'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.concept', ['ngResource',
      'trialverse.graph',
      'trialverse.util'])
    // services
    .factory('ConceptsService', require('concept/conceptsService'))

    //controllers
    .controller('ConceptsController', require('concept/conceptsController'))
    .controller('CreateConceptController', require('concept/createConceptController'))

     //resources
    .factory('DatasetVersionedResource', require('dataset/datasetVersionedResource')) // only need resource, not entire module

    ;
});

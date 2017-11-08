'use strict';
var requires = [
  'concept/conceptsService',
  'concept/conceptsController',
  'concept/createConceptController',
  'dataset/datasetVersionedResource'
];
define(requires.concat('angular', 'angular-resource'), function(
  ConceptsService,
  ConceptsController,
  CreateConceptController,
  DatasetVersionedResource,
  angular
) {
  return angular.module('trialverse.concept', ['ngResource',
      'trialverse.graph',
      'trialverse.util'
    ])
    // services
    .factory('ConceptsService', ConceptsService)

    //controllers
    .controller('ConceptsController', ConceptsController)
    .controller('CreateConceptController', CreateConceptController)

    //resources
    .factory('DatasetVersionedResource', DatasetVersionedResource) // only need resource, not entire module
  ;
});
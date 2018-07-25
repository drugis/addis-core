'use strict';
var requires = [
  'util/uuidService',
  'util/durationService',
  'util/directives/subsetSelect/subsetSelectService',
  'util/sanitizeService',
  'util/abstractGroupService',
  'util/rdfListService',
  'util/dataModelService',
  'util/sparqlResource',
  'util/filters/ontologyFilter',
  'util/filters/durationFilter',
  'util/filters/durationOffsetFilter',
  'util/filters/stripFrontFilter',
  'util/filters/exponentialFilter',
  'util/directives/navbar/navbarDirective',
  'util/directives/subsetSelect/subsetSelectDirective',
  'util/directives/durationInput/durationInputDirective',
  'util/directives/sessionExpired/sessionExpiredDirective',
  'util/directives/spinner/spinnerDirective',
  'util/directives/enumOptions/enumOptionsDirective',
  'util/interceptors/sessionExpiredInterceptor'
];
define(['angular'].concat(requires), function(
  angular,
  UUIDService,
  DurationService,
  SubsetSelectService,
  SanitizeService,
  AbstractGroupService,
  RdfListService,
  DataModelService,
  SparqlResource,
  ontologyFilter,
  durationFilter,
  durationOffsetFilter,
  stripFrontFilter,
  exponentialFilter,
  navbarDirective,
  subsetSelect,
  durationInput,
  sessionExpired,
  spinner,
  enumOptions,
  SessionExpiredInterceptor
) {
  return angular.module('trialverse.util', [])
    // services
    .factory('UUIDService', UUIDService)
    .factory('DurationService', DurationService)
    .factory('SubsetSelectService', SubsetSelectService)
    .factory('SanitizeService', SanitizeService)
    .factory('AbstractGroupService', AbstractGroupService)
    .factory('RdfListService', RdfListService)
    .factory('DataModelService', DataModelService) 
   
    // resources
    .factory('SparqlResource', SparqlResource)

    // filters
    .filter('ontologyFilter', ontologyFilter)
    .filter('durationFilter', durationFilter)
    .filter('durationOffsetFilter', durationOffsetFilter)
    .filter('stripFrontFilter', stripFrontFilter)
    .filter('exponentialFilter', exponentialFilter)

    //directives
    .directive('navbarDirective', navbarDirective)
    .directive('subsetSelect', subsetSelect)
    .directive('durationInput', durationInput)
    .directive('sessionExpired', sessionExpired)
    .directive('enumOptions', enumOptions)
    .directive('spinner', spinner)

    //interceptors
    .factory('SessionExpiredInterceptor', SessionExpiredInterceptor);
});

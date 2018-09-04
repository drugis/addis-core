'use strict';
define([
  'angular',
  './uuidService',
  './durationService',
  './directives/subsetSelect/subsetSelectService',
  './sanitizeService',
  './abstractGroupService',
  './rdfListService',
  './dataModelService',
  './sparqlResource',
  './filters/ontologyFilter',
  './filters/durationFilter',
  './filters/durationOffsetFilter',
  './filters/stripFrontFilter',
  './filters/exponentialFilter',
  './directives/navbar/navbarDirective',
  './directives/subsetSelect/subsetSelectDirective',
  './directives/durationInput/durationInputDirective',
  './directives/sessionExpired/sessionExpiredDirective',
  './directives/spinner/spinnerDirective',
  './directives/enumOptions/enumOptionsDirective',
  './interceptors/sessionExpiredInterceptor',
  '../measurementMoment/measurementMoment'
], function(
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
    return angular.module('trialverse.util', ['trialverse.measurementMoment'])
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
  }
);

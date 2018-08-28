'use strict';
define([
  './graphResource',
  './versionedGraphResource',
  'angular', 'angular-resource'], 
  function(
    GraphResource,
    VersionedGraphResource,
    angular
  ) {
    return angular.module('trialverse.graph', ['ngResource'])
      //resources
      .factory('GraphResource', GraphResource)
      .factory('VersionedGraphResource', VersionedGraphResource)
      ;
  });

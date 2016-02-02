'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.graph', ['ngResource'])
    //resources
    .factory('GraphResource', require('graph/graphResource'))
    .factory('VersionedGraphResource', require('graph/versionedGraphResource'))
    ;
});

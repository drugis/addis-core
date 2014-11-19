'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.util', [])
    .factory('UUIDService', require('util/uuidService'))
    ;
});
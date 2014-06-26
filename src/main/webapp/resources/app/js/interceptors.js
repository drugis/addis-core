'use strict';
define(function(require) {
  var angular = require('angular');
  return angular.module('addis.interceptors', [])
    .factory('SessionExpiredInterceptor', require('interceptors/sessionExpiredInterceptor'));
});
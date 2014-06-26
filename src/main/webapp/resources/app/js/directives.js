'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('addis.directives', [])
    .directive('sessionExpiredDirective', require('directives/sessionExpiredDirective'));
});

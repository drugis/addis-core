'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('addis.directives', [])
    .directive('modal', require('directives/modalDirective'))
    .directive('alert', require('directives/alertDirective'))
    ;
});

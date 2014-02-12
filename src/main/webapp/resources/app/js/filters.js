'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('addis.filters', [])
    .controller('ownProjectFilter', require('filters/ownProjectFilter'));
});
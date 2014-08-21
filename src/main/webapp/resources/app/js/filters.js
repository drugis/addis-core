'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('addis.filters', [])
    .filter('ownProjectsFilter', require('filters/ownProjectsFilter'))
    .filter('durationFilter', require('filters/durationFilter'))
    .filter('splitOnTokenFilter', require('filters/splitOnTokenFilter'))
    .filter('ontologyFilter', require('filters/ontologyFilter'))
    .filter('anchorEpochFilter', require('filters/anchorEpochFilter'))
    .filter('activityTypeFilter', require('filters/activityTypeFilter'));
});
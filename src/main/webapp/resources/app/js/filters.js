'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('addis.filters', [])
    .filter('ownProjectsFilter', require('filters/ownProjectsFilter'))
    .filter('durationFilter', require('filters/durationFilter'))
    .filter('durationOffsetFilter', require('filters/durationOffsetFilter'))
    .filter('splitOnTokenFilter', require('filters/splitOnTokenFilter'))
    .filter('addisOntologyFilter', require('filters/addisOntologyFilter'))
    .filter('anchorEpochFilter', require('filters/anchorEpochFilter'))
    .filter('categoricalFilter', require('filters/categoricalFilter'))
    .filter('activityTypeFilter', require('filters/activityTypeFilter'));
});

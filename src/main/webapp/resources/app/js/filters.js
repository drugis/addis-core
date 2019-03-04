'use strict';
define(
  ['angular',
    './filters/ownProjectsFilter',
    './filters/splitOnTokenFilter',
    './filters/addisOntologyFilter',
    './filters/anchorEpochFilter',
    './filters/categoricalFilter',
    './filters/activityTypeFilter',
    './filters/dosingFilter',
    './measurementMoment/measurementMoment'
  ],
  function(
    angular,
    ownProjectsFilter,
    splitOnTokenFilter,
    addisOntologyFilter,
    anchorEpochFilter,
    categoricalFilter,
    activityTypeFilter,
    dosingFilter
  ) {
    return angular.module('addis.filters', ['trialverse.measurementMoment'])
      .filter('ownProjectsFilter', ownProjectsFilter)
      .filter('splitOnTokenFilter', splitOnTokenFilter)
      .filter('addisOntologyFilter', addisOntologyFilter)
      .filter('anchorEpochFilter', anchorEpochFilter)
      .filter('categoricalFilter', categoricalFilter)
      .filter('activityTypeFilter', activityTypeFilter)
      .filter('dosingFilter', dosingFilter);

      ;
  });

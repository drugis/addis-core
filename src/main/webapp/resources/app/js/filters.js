'use strict';
define(
  ['angular',
    './filters/ownProjectsFilter',
    './filters/splitOnTokenFilter',
    './filters/addisOntologyFilter',
    './filters/anchorEpochFilter',
    './filters/categoricalFilter',
    './filters/activityTypeFilter'
  ],
  function(
    angular,
    ownProjectsFilter,
    splitOnTokenFilter,
    addisOntologyFilter,
    anchorEpochFilter,
    categoricalFilter,
    activityTypeFilter
  ) {
    return angular.module('addis.filters', [])
      .filter('ownProjectsFilter', ownProjectsFilter)
      .filter('splitOnTokenFilter', splitOnTokenFilter)
      .filter('addisOntologyFilter', addisOntologyFilter)
      .filter('anchorEpochFilter', anchorEpochFilter)
      .filter('categoricalFilter', categoricalFilter)
      .filter('activityTypeFilter', activityTypeFilter);
  });

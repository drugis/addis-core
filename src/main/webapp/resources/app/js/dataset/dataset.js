'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('trialverse.dataset', ['ngResource', 'trialverse.util'])
    //controllers
    .controller('DatasetController', require('dataset/datasetController'))
    .controller('CreateStudyController', require('dataset/createStudyController'))
    .controller('DatasetHistoryController', require('dataset/datasetHistoryController'))

    //services
    .factory('DatasetService', require('dataset/datasetService'))
    .factory('HistoryService', require('dataset/historyService'))
    .factory('StudiesWithDetailsService', require('dataset/studiesWithDetailsService'))

    //resources
    .factory('DatasetVersionedResource', require('dataset/datasetVersionedResource'))
    .factory('DatasetResource', require('dataset/datasetResource'))
    .factory('HistoryResource', require('dataset/historyResource'))

    //directives
    .directive('historyItem', require('dataset/historyItemDirective'))
    .directive('versionInfo', require('dataset/versionInfoDirective'))

    //filters
    .filter('splitOnTokenFilter', require('dataset/splitOnTokenFilter'))
    .filter('dosingFilter', require('dataset/dosingFilter'))
    ;
});

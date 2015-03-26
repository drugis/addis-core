'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('trialverse.dataset', ['ngResource', 'trialverse.util'])
    //controllers
    .controller('DatasetsController', require('dataset/datasetsController'))
    .controller('CreateDatasetController', require('dataset/createDatasetController'))
    .controller('DatasetController', require('dataset/datasetController'))
    .controller('CreateStudyController', require('dataset/createStudyController'))
    .controller('DatasetHistoryController', require('dataset/datasetHistoryController'))

    //services
    .factory('DatasetService', require('dataset/datasetService'))
    .factory('HistoryService', require('dataset/historyService'))

    //resources
    .factory('DatasetResource', require('dataset/datasetResource'))
    .factory('DatasetOverviewResource', require('dataset/datasetOverviewResource'))
    .factory('HistoryResource', require('dataset/historyResource'))
    .factory('StudiesWithDetailResource', require('dataset/studiesWithDetailResource'))

    //directives
    .directive('historyItem', require('dataset/historyItemDirective'))

    //filters
    .filter('splitOnTokenFilter', require('dataset/splitOnTokenFilter'))
    .filter('dosingFilter', require('dataset/dosingFilter'))
    ;
});

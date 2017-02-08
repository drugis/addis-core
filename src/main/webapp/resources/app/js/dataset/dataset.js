'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('trialverse.dataset', ['ngResource', 'trialverse.util'])
    //controllers
    .controller('DatasetsController', require('dataset/datasetsController'))
    .controller('DatasetController', require('dataset/datasetController'))
    .controller('CreateStudyController', require('dataset/createStudyController'))
    .controller('DeleteStudyController', require('dataset/deleteStudyController'))
    .controller('DatasetHistoryController', require('dataset/datasetHistoryController'))
    .controller('StudyHistoryController', require('dataset/studyHistoryController'))
    .controller('EditDatasetController', require('dataset/editDatasetController'))
    .controller('FilterDatasetController', require('dataset/filterDatasetController'))

    //services
    .factory('StudiesWithDetailsService', require('dataset/studiesWithDetailsService'))
    .factory('DatasetService', require('dataset/datasetService'))

    //resources
    .factory('DatasetVersionedResource', require('dataset/datasetVersionedResource'))
    .factory('DatasetResource', require('dataset/datasetResource'))
    .factory('HistoryResource', require('dataset/historyResource'))
    .factory('StudyHistoryResource', require('dataset/studyHistoryResource'))
    .factory('GraphResource', require('graph/graphResource'))
    .factory('ImportStudyInfoResource', require('dataset/importStudyInfoResource'))
    .factory('ImportStudyResource', require('dataset/importStudyResource'))

    //directives
    .directive('historyItem', require('dataset/historyItemDirective'))
    .directive('versionInfo', require('dataset/versionInfoDirective'))
    .directive('featuredDatasets', require('dataset/featuredDatasetsDirective'))

    //filters
    .filter('splitOnTokenFilter', require('dataset/splitOnTokenFilter'))
    .filter('dosingFilter', require('dataset/dosingFilter'))
    ;
});

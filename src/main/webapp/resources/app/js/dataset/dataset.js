'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('trialverse.dataset', ['ngResource', 'trialverse.util'])
    //controllers
    .controller('DatasetsController', require('dataset/datasetsController'))
    .controller('CreateDatasetController', require('dataset/createDatasetController'))
    .controller('DatasetController', require('dataset/datasetController'))
    .controller('CreateStudyController', require('dataset/createStudyController'))

    //services
    .factory('DatasetService', require('dataset/datasetService'))

    //resources
    .factory('DatasetResource', require('dataset/datasetResource'))
    .factory('StudiesWithDetailResource', require('dataset/studiesWithDetailResource'))

    //filters
    .filter('splitOnTokenFilter', require('dataset/splitOnTokenFilter'))
    .filter('dosingFilter', require('dataset/dosingFilter'))
    .filter('stripFrontFilter', require('dataset/stripFrontFilter'))
    ;
});

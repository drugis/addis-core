'use strict';

define([
  'angular',
  './datasetsController',
  './datasetController',
  './createStudyController',
  './deleteStudyController',
  './datasetHistoryController',
  './studyHistoryController',
  './editDatasetController',
  './filterDatasetController',
  './studiesWithDetailsService',
  './datasetService',
  './datasetVersionedResource',
  './datasetResource',
  './historyResource',
  './studyHistoryResource',
  '../graph/graphResource',
  './importStudyInfoResource',
  './importStudyResource',
  './historyItemDirective',
  './versionInfoDirective',
  './featuredDatasetsDirective',
  './splitOnTokenFilter',
  './dosingFilter',
  '../util/util',
  '../excelIO/excelIO',
  ], function(
    angular,
    DatasetsController,
    DatasetController,
    CreateStudyController,
    DeleteStudyController,
    DatasetHistoryController,
    StudyHistoryController,
    EditDatasetController,
    FilterDatasetController,
    StudiesWithDetailsService,
    DatasetService,
    DatasetVersionedResource,
    DatasetResource,
    HistoryResource,
    StudyHistoryResource,
    GraphResource,
    ImportStudyInfoResource,
    ImportStudyResource,
    historyItem,
    versionInfo,
    featuredDatasets,
    splitOnTokenFilter,
    dosingFilter
  ) {
    return angular.module('trialverse.dataset', ['ngResource', 'trialverse.util', 'addis.excelIO'])
      //controllers
      .controller('DatasetsController', DatasetsController)
      .controller('DatasetController', DatasetController)
      .controller('CreateStudyController', CreateStudyController)
      .controller('DeleteStudyController', DeleteStudyController)
      .controller('DatasetHistoryController', DatasetHistoryController)
      .controller('StudyHistoryController', StudyHistoryController)
      .controller('EditDatasetController', EditDatasetController)
      .controller('FilterDatasetController', FilterDatasetController)

      //services
      .factory('StudiesWithDetailsService', StudiesWithDetailsService)
      .factory('DatasetService', DatasetService)

      //resources
      .factory('DatasetVersionedResource', DatasetVersionedResource)
      .factory('DatasetResource', DatasetResource)
      .factory('HistoryResource', HistoryResource)
      .factory('StudyHistoryResource', StudyHistoryResource)
      .factory('GraphResource', GraphResource)
      .factory('ImportStudyInfoResource', ImportStudyInfoResource)
      .factory('ImportStudyResource', ImportStudyResource)

      //directives
      .directive('historyItem', historyItem)
      .directive('versionInfo', versionInfo)
      .directive('featuredDatasets', featuredDatasets)

      //filters
      .filter('splitOnTokenFilter', splitOnTokenFilter)
      .filter('dosingFilter', dosingFilter);
  });

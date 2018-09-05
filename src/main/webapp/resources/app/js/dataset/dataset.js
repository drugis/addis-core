'use strict';

define([
  'angular',
  './datasetsController',
  './datasetController',
  './createDatasetController',
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
  '../util/util',
  '../excelIO/excelIO',
  '../user/user'
  ], function(
    angular,
    DatasetsController,
    DatasetController,
    CreateDatasetController,
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
    featuredDatasets
  ) {
    return angular.module('trialverse.dataset', ['ngResource', 'trialverse.util', 'trialverse.user', 'addis.excelIO'])
      //controllers
      .controller('DatasetsController', DatasetsController)
      .controller('DatasetController', DatasetController)
      .controller('CreateDatasetController', CreateDatasetController)
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

      ;
  });

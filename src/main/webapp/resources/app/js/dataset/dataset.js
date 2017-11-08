'use strict';
var requires = [
  'dataset/datasetsController',
  'dataset/datasetController',
  'dataset/createStudyController',
  'dataset/deleteStudyController',
  'dataset/datasetHistoryController',
  'dataset/studyHistoryController',
  'dataset/editDatasetController',
  'dataset/filterDatasetController',
  'dataset/studiesWithDetailsService',
  'dataset/datasetService',
  'dataset/datasetVersionedResource',
  'dataset/datasetResource',
  'dataset/historyResource',
  'dataset/studyHistoryResource',
  'graph/graphResource',
  'dataset/importStudyInfoResource',
  'dataset/importStudyResource',
  'dataset/historyItemDirective',
  'dataset/versionInfoDirective',
  'dataset/featuredDatasetsDirective',
  'dataset/splitOnTokenFilter',
  'dataset/dosingFilter'
];
define(requires.concat(['angular', 'angular-resource']), function(DatasetsController,
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
  dosingFilter,
  angular
) {
  return angular.module('trialverse.dataset', ['ngResource', 'trialverse.util'])
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
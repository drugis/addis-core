'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', 'DatasetService', 'DatasetResource',
     'UUIDService', 'StudyService', 'StudyResource'
  ];
  var CreateStudyController = function($scope, $stateParams, $modalInstance, DatasetService, DatasetResource,
     UUIDService, StudyService, StudyResource) {
    $scope.isUniqueShortName = function(shortName) {
      return !$scope.studiesWithDetail.length > 0 || !_.find($scope.studiesWithDetail, function(existingStudy) {
        return existingStudy.label === shortName;
      });
    };

    $scope.createStudy = function(study) {
      var uuid = UUIDService.generate();
      var newStudy = StudyService.createEmptyStudyJsonLD(uuid, study);
      StudyResource.put({
        datasetUUID: $stateParams.datasetUUID,
        studyUUID: uuid
      }, newStudy).$promise.then(function() {
        $scope.datasetJSON = DatasetService.addStudyToDatasetGraph(uuid, $scope.datasetJSON);
        DatasetResource.save({
          datasetUUID: $stateParams.datasetUUID
        }, $scope.datasetJSON).$promise.then(function() {
          $scope.loadStudiesWithDetail();
          $modalInstance.close();
        });
      });
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(CreateStudyController);
});
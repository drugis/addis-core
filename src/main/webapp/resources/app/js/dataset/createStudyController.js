'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', 'DatasetService', 'DatasetResource',
    'UUIDService', 'StudyService', 'GraphResource'
  ];
  var CreateStudyController = function($scope, $stateParams, $modalInstance, DatasetService, DatasetResource,
    UUIDService, StudyService, GraphResource) {

    $scope.isCreatingStudy = false;

    $scope.isUniqueShortName = function(shortName) {
      var anyduplicateName = _.find($scope.studiesWithDetail, function(existingStudy) {
        return existingStudy.label === shortName;
      });
      return !anyduplicateName;
    };

    $scope.createStudy = function(study) {
      $scope.isCreatingStudy = true;
      StudyService.createEmptyStudy(study).then(function() {
        StudyService.getStudyGraph().then(function(queryResult) {
          var uuid = StudyService.getStudyUUID();

          GraphResource.put({
            datasetUUID: $stateParams.datasetUUID,
            graphUuid: uuid
          }, queryResult.data, function() {
            $scope.loadStudiesWithDetail();
            $scope.isCreatingStudy = true;
            $modalInstance.close();

          });
        });
      });

    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(CreateStudyController);
});

'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$scope',
    '$stateParams',
    '$modalInstance',
    'successCallback',
    'StudyService',
    'ImportStudyResource',
    'ImportStudyInfoResource',
    'UUIDService',
    'ExcelImportService'
  ];

  var CreateStudyController = function(
    $scope,
    $stateParams,
    $modalInstance,
    successCallback,
    StudyService,
    ImportStudyResource,
    ImportStudyInfoResource,
    UUIDService,
    ExcelImportService) {
    // functions 
    $scope.checkUniqueShortName = checkUniqueShortName;
    $scope.createStudy = createStudy;
    $scope.isValidNct = isValidNct;
    $scope.isValidUpload = false;
    $scope.cancel = cancel;
    $scope.getInfo = getInfo;
    $scope.uploadExcel = uploadExcel;
    $scope.importExcel = importExcel;

    // init
    $scope.isCreatingStudy = false;
    $scope.importing = false;
    $scope.isUniqueIdentifier = true;
    $scope.studyImport = {};
    $scope.excelUpload = undefined;

    function uploadExcel(uploadedElement) {
      ExcelImportService.uploadExcel(uploadedElement, 
        $scope, 
        ExcelImportService.checkSingleStudyWorkbook,
        function(workbook){
          return workbook.Sheets['Study data'].A4.v;
        },
        _.map($scope.studiesWithDetail, 'label'));
    }

    function importExcel() {
      $scope.isCreatingStudy = true;
      var newStudy = ExcelImportService.createStudy($scope.excelUpload);
      ExcelImportService.commitStudy(newStudy).then(function(newVersionUuid) {
        successCallback(newVersionUuid);
        $scope.isCreatingStudy = false;
        $modalInstance.close();
      });
    }

    function checkUniqueShortName(shortName) {
      $scope.isUniqueIdentifier = !_.find($scope.studiesWithDetail, ['label', shortName]);
    }

    function createStudy(study) {
      $scope.isCreatingStudy = true;
      StudyService.createEmptyStudy(study, $stateParams.userUid, $stateParams.datasetUuid)
        .then(function(newVersion) {
          successCallback(newVersion);
          $scope.isCreatingStudy = false;
          $modalInstance.close();
        });
    }

    function isValidNct(nctId) {
      return new RegExp('^' + 'NCT+').test(nctId);
    }

    function getInfo(studyImport) {
      studyImport.error = undefined;
      studyImport.notFound = false;
      studyImport.nctId = studyImport.nctId.toUpperCase();
      if ($scope.isValidNct(studyImport.nctId)) {
        $scope.studyImport.basicInfo = {};
        $scope.studyImport.loading = true;
        ImportStudyInfoResource.get({
          nctId: studyImport.nctId
        }).$promise.then(function(basicInfo) {
          $scope.studyImport.loading = false;
          if (!basicInfo.id) {
            studyImport.notFound = true;
          } else {
            $scope.studyImport.basicInfo = basicInfo;
          }
        }, function(reason) {
          console.error('error', reason);
          $modalInstance.close();
        }, function() {
          $scope.studyImport.loading = false;
        });
      }
    }

    //putting it on the scope this style, because import is a reserved name
    $scope.import = function(studyImport) { // NCT import
      $scope.isCreatingStudy = true;
      var uuid = UUIDService.generate();
      var importStudyRef = studyImport.basicInfo.id;
      ImportStudyResource.import({
        userUid: $stateParams.userUid,
        datasetUuid: $stateParams.datasetUuid,
        graphUuid: uuid,
        importStudyRef: importStudyRef,
        commitTitle: 'Import ' + importStudyRef + ' from ClinicalTrials.gov',
        commitDescription: studyImport.basicInfo.title
      }, function(value, responseHeaders) {
        var newVersionUri = responseHeaders('X-EventSource-Version');
        var newVersionUuid = UUIDService.getUuidFromNamespaceUrl(newVersionUri);
        successCallback(newVersionUuid);
        $scope.isCreatingStudy = false;
        $modalInstance.close();
      }, function(error) {
        console.error('error', error);
        $scope.isCreatingStudy = false;
        $modalInstance.close();
      });
    };

    function cancel() {
      $modalInstance.close();
    }

  };
  return dependencies.concat(CreateStudyController);
});

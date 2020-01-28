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
    'ExcelImportService',
    'ImportEudraCTResource'
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
    ExcelImportService,
    ImportEudraCTResource
  ) {
    // functions 
    $scope.checkUniqueShortName = checkUniqueShortName;
    $scope.createStudy = createStudy;
    $scope.isValidNct = isValidNct;
    $scope.cancel = cancel;
    $scope.getNCTInfo = getNCTInfo;
    $scope.uploadExcel = uploadExcel;
    $scope.importExcel = importExcel;
    $scope.uploadEudract = uploadEudract;
    $scope.importEudract = importEudract;
    $scope.importNCT = importNCT;
    $scope.selectTab = selectTab;
    
    // init
    $scope.isValidUpload = false;
    $scope.isCreatingStudy = false;
    $scope.importing = false;
    $scope.isUniqueIdentifier = true;
    $scope.studyImport = {};
    $scope.excelUpload = undefined;
    $scope.eudractUpload = undefined;
    $scope.activeTab = 'empty';

    function uploadExcel(uploadedElement) {
      ExcelImportService.uploadExcel(
        uploadedElement,
        $scope,
        ExcelImportService.checkSingleStudyWorkbook,
        getExcelStudyTitle,
        _.map($scope.studiesWithDetail, 'label')
      );
    }

    function getExcelStudyTitle(workbook) {
      return workbook.Sheets['Study data'].A4.v;
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
      return new RegExp('^' + 'NCT+').test(nctId.toUpperCase());
    }

    function getNCTInfo(studyImport) {
      studyImport.error = undefined;
      studyImport.notFound = false;
      if (isValidNct(studyImport.nctId)) {
        studyImport.nctId = studyImport.nctId.toUpperCase();
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
        }, errorCallback, function() {
          $scope.studyImport.loading = false;
        });
      }
    }

    function importNCT(studyImport) { // NCT import
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
      }, success, errorCallback);
    }

    function uploadEudract(uploadedElement) {
      $scope.eudractUpload = uploadedElement.files[0];
    }

    function importEudract() {
      $scope.isCreatingStudy = true;
      ImportEudraCTResource.import(_.pick($stateParams, ['userUid', 'datasetUuid']),
        $scope.eudractUpload, 
        success, 
        errorCallback);
    }

    function success(value, responseHeaders) {
      var newVersionUri = responseHeaders('X-EventSource-Version');
      var newVersionUuid = UUIDService.getUuidFromNamespaceUrl(newVersionUri);
      successCallback(newVersionUuid);
      $scope.isCreatingStudy = false;
      $modalInstance.close();
    }

    function errorCallback(error) {
      console.error('error', error);
      $scope.isCreatingStudy = false;
      $modalInstance.close();
    }

    function selectTab(tab) {
      $scope.activeTab = tab;
    }

    function cancel() {
      $modalInstance.close();
    }

  };
  return dependencies.concat(CreateStudyController);
});

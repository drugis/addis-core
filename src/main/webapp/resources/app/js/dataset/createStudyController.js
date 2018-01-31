'use strict';
define(['lodash', 'xlsx-shim'], function(_, XLSX) {
  var dependencies = [
    '$scope',
    '$stateParams',
    '$q',
    '$modalInstance',
    '$timeout',
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
    $q,
    $modalInstance,
    $timeout,
    successCallback,
    StudyService,
    ImportStudyResource,
    ImportStudyInfoResource,
    UUIDService,
    ExcelImportService) {
    // functions 
    $scope.isUniqueShortName = isUniqueShortName;
    $scope.createStudy = createStudy;
    $scope.isValidNct = isValidNct;
    $scope.cancel = cancel;
    $scope.getInfo = getInfo;
    $scope.uploadExcel = uploadExcel;
    $scope.importExcel = importExcel;

    // init
    $scope.isCreatingStudy = false;
    $scope.importing = false;
    $scope.studyImport = {};
    $scope.excelUpload = undefined;

    function uploadExcel(uploadedElement) {
      var file = uploadedElement.files[0];
      var reader = new FileReader();
      $scope.errors = [];
      var workbook;
      reader.onload = function(file) {
        var data = file.target.result;
        try {
          workbook = XLSX.read(data, {
            type: 'binary'
          });
          $scope.errors = ExcelImportService.checkWorkbook(workbook);
        } catch (error) {
          $scope.errors.push('Invalid file');
        }
        if (!$scope.errors.length) {
          $scope.excelUpload = workbook;
        }
        $timeout(function() {}, 0); // ensures errors are rendered in the html
      };
      reader.readAsBinaryString(file);
      return;
    }

    function importExcel() {
      $scope.isCreatingStudy = true;
      var newStudyVersionPromises = ExcelImportService.createStudy($scope.excelUpload);
      $q.all(newStudyVersionPromises).then(function(result) {
        var newStudy = result[0];
        successCallback(newStudy);
        $scope.isCreatingStudy = false;
        $modalInstance.close();
      });
    }

    function isUniqueShortName(shortName) {
      var anyduplicateName = _.find($scope.studiesWithDetail, function(existingStudy) {
        return existingStudy.label === shortName;
      });
      return !anyduplicateName;
    }

    function createStudy(study) {
      $scope.isCreatingStudy = true;
      var newStudyVersionPromises = StudyService.createEmptyStudy(study, $stateParams.userUid, $stateParams.datasetUuid);
      newStudyVersionPromises.then(function(newVersion) {
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
'use strict';
define(['lodash'],
  function(_) {
    var dependencies = [
      '$scope',
      '$stateParams',
      '$modalInstance',
      '$q',
      'DatasetResource',
      'ExcelImportService',
      'datasetTitles',
      'callback'
    ];
    var CreateDatasetController = function(
      $scope,
      $stateParams,
      $modalInstance,
      $q,
      DatasetResource,
      ExcelImportService,
      datasetTitles,
      callback) {
      // functions
      $scope.createDataset = createDataset;
      $scope.cancel = cancel;
      $scope.checkDatasetName = checkDatasetName;
      $scope.uploadExcel = uploadExcel;
      $scope.importDataset = importDataset;

      // init
      $scope.dataset = {};
      $scope.isUniqueIdentifier = true;
      $scope.excelUpload = undefined;

      function checkDatasetName(newName) {
        $scope.isUniqueIdentifier = datasetTitles.indexOf(newName) === -1;
      }

      function uploadExcel(uploadedElement) {
        ExcelImportService.uploadExcel(uploadedElement,
          $scope,
          ExcelImportService.checkDatasetWorkbook,
          function(workbook) {
            return workbook.Sheets['Dataset information'].A2.v;
          },
          datasetTitles);
      }

      function importDataset() {
        $scope.isCreatingDataset = true;
        $scope.dataset = ExcelImportService.createDataset($scope.excelUpload);
        var studies = ExcelImportService.createDatasetStudies($scope.excelUpload);
        var concepts = ExcelImportService.createDatasetConcepts($scope.excelUpload);
        DatasetResource.save($stateParams, $scope.dataset).$promise.then(function() {
          var studiePromises = _.map(studies, function(study) {
            return ExcelImportService.commitStudy(study);
          });
          var conceptPromise = ConceptService.commit(concepts); //FIXME
          
          $q.all(studiePromises.concat(conceptPromise)).then(function() {
            $scope.isCreatingDataset = false;
            callback();
            $modalInstance.close();
          });
        });
      }

      function createDataset() {
        DatasetResource.save($stateParams, $scope.dataset).$promise.then(function() {
          callback();
        });
        $modalInstance.close();
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(CreateDatasetController);
  });
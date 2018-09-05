'use strict';
define(['lodash', '../util/context'],
  function(_, externalContext) {
    var dependencies = [
      '$scope',
      '$stateParams',
      '$modalInstance',
      '$q',
      'DatasetResource',
      'GraphResource',
      'ExcelImportService',
      'UUIDService',
      'datasetTitles',
      'callback'
    ];
    var CreateDatasetController = function(
      $scope,
      $stateParams,
      $modalInstance,
      $q,
      DatasetResource,
      GraphResource,
      ExcelImportService,
      UUIDService,
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
        ExcelImportService.uploadExcel(
          uploadedElement,
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
        DatasetResource.save($stateParams, $scope.dataset).$promise.then(function(result) {
          var datasetUri = '';
          var index = 0;
          while (result[index]) {
            datasetUri = datasetUri.concat(result[index]);
            ++index;
          }
          var datasetUuid = datasetUri.split('/datasets/')[1];
          var studiePromises = _.map(studies, function(study) {
            return ExcelImportService.commitStudy(study, datasetUuid); // needs dataset uuid
          });
          var conceptsPromise = GraphResource.putJson({
            userUid: $stateParams.userUid,
            datasetUuid: datasetUuid,
            graphUuid: 'concepts',
            commitTitle: 'Import concepts',
            commitDescription: ''
          }, {
              '@graph': concepts,
              '@context': externalContext
            });
          $q.all(studiePromises.concat(conceptsPromise)).then(function() {
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

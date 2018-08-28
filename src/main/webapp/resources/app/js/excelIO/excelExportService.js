'use strict';
define(['lodash', 'xlsx', 'file-saver'], function(_, XLSX, saveAs) {
  var dependencies = ['$q', '$location',
    'GROUP_ALLOCATION_OPTIONS',
    'BLINDING_OPTIONS',
    'STATUS_OPTIONS',
    'ExcelExportUtilService',
    'StudyService',
    'ArmService',
    'StudyInformationService',
    'PopulationInformationService',
    'EpochService',
    'ActivityService',
    'StudyDesignService',
    'MeasurementMomentService',
    'DrugService',
    'PopulationCharacteristicService',
    'EndpointService',
    'AdverseEventService',
    'UnitService',
    'VersionedGraphResource',
    'ConceptsService'
  ];
  var ExcelExportService = function($q, $location,
    GROUP_ALLOCATION_OPTIONS,
    BLINDING_OPTIONS,
    STATUS_OPTIONS,
    ExcelExportUtilService,
    StudyService,
    ArmService,
    StudyInformationService,
    PopulationInformationService,
    EpochService,
    ActivityService,
    StudyDesignService,
    MeasurementMomentService,
    DrugService,
    PopulationCharacteristicService,
    EndpointService,
    AdverseEventService,
    UnitService,
    VersionedGraphResource,
    ConceptsService
  ) {
    var excelUtils = XLSX.utils;

    function exportStudy(coordinates) {
      var workBook = ExcelExportUtilService.buildWorkBook();
      var startRows = ExcelExportUtilService.buildStartRows(0);
      appendStudy(workBook, coordinates, startRows).then(function(workBook) {
        saveWorkBook(workBook, workBook.Sheets['Study data'].A4.v);
      });
    }

    function exportDataset(datasetWithCoordinates, graphUuids, percentageUpdate) {
      var workBook = ExcelExportUtilService.buildWorkBook();
      var startRows = ExcelExportUtilService.buildStartRows(1);

      var graphCoordinates = _.map(graphUuids, function(graphUuid) {
        return _.extend({}, datasetWithCoordinates, {
          graphUuid: graphUuid
        });
      });

      var initialPromise = $q.resolve(workBook);

      var overallPromise = _.reduce(graphCoordinates, function(accum, coordinates) {
        return accum.then(function(workBook) {
          return VersionedGraphResource.getJson(coordinates).$promise.then(function(graph) {
            StudyService.loadJson($q.resolve(graph));
            percentageUpdate();
            return appendStudy(workBook, coordinates, startRows).then(function(workBook) {
              var newWorkBook = ExcelExportUtilService.addStudyHeaders(workBook, startRows);
              startRows = ExcelExportUtilService.updateStartRows(newWorkBook);
              return newWorkBook;
            });
          });
        });
      }, initialPromise);

      return overallPromise.then(function(workBook) {
        var datasetInformationSheet = ExcelExportUtilService.buildDatasetInformationSheet(datasetWithCoordinates);
        excelUtils.book_append_sheet(workBook, datasetInformationSheet, 'Dataset information');

        return ConceptsService.queryItems().then(function(datasetConcepts) {
          var datasetConceptsSheet = ExcelExportUtilService.buildDatasetConceptsSheet(datasetConcepts);
          excelUtils.book_append_sheet(workBook, datasetConceptsSheet, 'Dataset concepts');
          saveWorkBook(workBook, datasetWithCoordinates.title);
        });
      });
    }

    //private
    function appendStudy(workBook, coordinates, startRows) {
      var newWorkBook = _.cloneDeep(workBook);
      var promises = [StudyService.getJsonGraph()];
      var variablePromises = [
        PopulationCharacteristicService.queryItems(),
        EndpointService.queryItems(),
        AdverseEventService.queryItems()
      ];
      promises = promises.concat(_.map([
        ArmService,
        StudyInformationService,
        PopulationInformationService,
        EpochService,
        ActivityService,
        StudyDesignService,
        MeasurementMomentService,
        DrugService,
        UnitService
      ], function(service) {
        return service.queryItems();
      }));

      return $q.all(variablePromises)
        .then(_.partial(ExcelExportUtilService.getVariableResults, promises))
        .then(function(results) {
          var root = $location.absUrl().split('/users/')[0];
          var studyUrl = ExcelExportUtilService.getStudyUrl(root, coordinates);
          var study = StudyService.findStudyNode(results[0]);
          var arms = results[1];
          var studyInformation = results[2][0];
          var populationInformation = results[3][0];
          var epochs = results[4];
          var activities = results[5];
          var studyDesign = results[6];
          var measurementMoments = results[7];
          var drugs = ExcelExportUtilService.addConceptType(results[8], 'drug');
          var units = ExcelExportUtilService.addConceptType(results[9], 'unit');
          var populationCharacteristics = results[10];
          var outcomes = results[11];
          var adverseEvents = results[12];

          var resultsByVariableUri = _.keyBy(results.slice(13), 'uri');
          var variables = _.map(populationCharacteristics.concat(outcomes, adverseEvents), function(variable) {
            return _.merge({}, variable, {
              results: resultsByVariableUri[variable.uri].results
            });
          });

          var conceptsSheet = ExcelExportUtilService.buildConceptsSheet(startRows, drugs.concat(populationCharacteristics, outcomes, adverseEvents, units));
          var epochSheet = ExcelExportUtilService.buildEpochSheet(startRows, epochs);
          var measurementMomentSheet = ExcelExportUtilService.buildMeasurementMomentSheet(startRows, measurementMoments, epochSheet);
          var studyDataSheet = ExcelExportUtilService.buildStudyDataSheet(startRows, study, studyInformation, studyUrl, arms, epochs, activities, studyDesign,
            populationInformation, variables, conceptsSheet, measurementMomentSheet);
          var activitiesSheet = ExcelExportUtilService.buildActivitiesSheet(startRows, activities, conceptsSheet);
          var studyDesignSheet = ExcelExportUtilService.buildStudyDesignSheet(startRows, epochs, arms, studyDesign, epochSheet, activitiesSheet, studyDataSheet);

          newWorkBook.Sheets['Study data'] = ExcelExportUtilService.mergePreservingRange(newWorkBook.Sheets['Study data'], studyDataSheet);
          newWorkBook.Sheets.Activities = ExcelExportUtilService.mergePreservingRange(newWorkBook.Sheets.Activities, activitiesSheet);
          newWorkBook.Sheets.Epochs = ExcelExportUtilService.mergePreservingRange(newWorkBook.Sheets.Epochs, epochSheet);
          newWorkBook.Sheets['Study design'] = ExcelExportUtilService.mergePreservingRange(newWorkBook.Sheets['Study design'], studyDesignSheet);
          newWorkBook.Sheets['Measurement moments'] = ExcelExportUtilService.mergePreservingRange(newWorkBook.Sheets['Measurement moments'], measurementMomentSheet);
          newWorkBook.Sheets.Concepts = ExcelExportUtilService.mergePreservingRange(newWorkBook.Sheets.Concepts, conceptsSheet);
          return newWorkBook;
        });
    }

    function saveWorkBook(workBook, fileName) {
      var workBookOut = XLSX.write(workBook, {
        bookType: 'xlsx',
        type: 'array'
      });
      saveAs(new Blob([workBookOut], {
        type: 'application/octet-stream'
      }), fileName + '.xlsx');
    }

    // interface
    return {
      exportStudy: exportStudy,
      exportDataset: exportDataset
    };

  };
  return dependencies.concat(ExcelExportService);
});

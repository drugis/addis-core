'use strict';
define(['lodash', 'xlsx-shim', 'file-saver'], function(_, XLSX, saveAs) {
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
    'VersionedGraphResource'
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
    VersionedGraphResource
  ) {
    var excelUtils = XLSX.utils;

    function exportStudy(coordinates) {
      var workBook = buildWorkBook();
      var startRows = buildStartRows();
      workBook = appendStudy(workBook, coordinates, startRows);
      saveWorkBook(workBook, 'whatevrs');
    }

    function exportDataset(datasetCoordinates, datasetGraphCoordinates) {
      var workBook = buildWorkBook();
      var startRows = buildStartRows();
      // excelUtils.book_append_sheet(workBook, {}, 'Dataset information');
      // excelUtils.book_append_sheet(workBook, {}, 'Dataset concepts');

      var studyExportPromises = _.reduce(datasetGraphCoordinates, function(accum, studyGraphUri, index) {
        var coordinates = {
          userUid: datasetCoordinates.userUid,
          datasetUuid: datasetCoordinates.datasetUuid,
          graphUuid: studyGraphUri.graphUri.split('/graphs/')[1],
          studyGraphUuid: studyGraphUri.graphUri.split('/graphs/')[1],
          versionUuid: datasetCoordinates.versionUuid
        };
        var studyPromise = VersionedGraphResource.getJson(coordinates).$promise;
        StudyService.loadJson(studyPromise);
        var studyAndPreviousStudyPromise = [studyPromise];
        if (index > 0) { // if not the first study, wait for the previous one to finish
          studyAndPreviousStudyPromise.push(accum[index - 1]);
        }
        var appendedWorkbookPromise = $q.all(studyAndPreviousStudyPromise).then(function() {
          return appendStudy(workBook, coordinates, startRows);
        });
        var promises = [studyPromise, appendedWorkbookPromise];
        if (index > 0) {
          promises.push(accum[index - 1]);
        }
        accum.push($q.all(promises).then(function(result) {
          workBook = result[1];
          startRows = updateStartRows(workBook);
          return true;
        }));
        return accum;
      }, []);
      $q.all(studyExportPromises).then(function() {
        saveWorkBook(workBook, datasetCoordinates.title);
      });
    }

    //private
    function getStudyUrl(coordinates) {
      var root = $location.absUrl().split('/users/')[0];
      return root + '/users/' + coordinates.userUid + '/datasets/' +
        coordinates.datasetUuid + '/versions/' +
        coordinates.versionUuid + '/studies/' +
        coordinates.studyGraphUuid;
    }

    function buildWorkBook() {
      var workBook = excelUtils.book_new();
      excelUtils.book_append_sheet(workBook, {}, 'Study data');
      excelUtils.book_append_sheet(workBook, {}, 'Activities');
      excelUtils.book_append_sheet(workBook, {}, 'Epochs');
      excelUtils.book_append_sheet(workBook, {}, 'Study design');
      excelUtils.book_append_sheet(workBook, {}, 'Measurement moments');
      excelUtils.book_append_sheet(workBook, {}, 'Concepts');
      return workBook;
    }

    function buildStartRows() {
      return {
        'Study data': 0,
        Activities: 0,
        Epochs: 0,
        'Study design': 0,
        'Measurement moments': 0,
        Concepts: 0
      };
    }

    function updateStartRows(workBook) {
      return _.reduce(workBook.Sheets, function(accum, sheet, key) {
        accum[key] = nextStartRow(sheet);
        return accum;
      }, {});
    }

    function nextStartRow(sheet) {
      var ref = excelUtils.decode_range(sheet['!ref']);
      return ref.e.r + 2;
    }

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
          var studyUrl = getStudyUrl(coordinates);
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
      var workBookout = XLSX.write(workBook, {
        bookType: 'xlsx',
        type: 'array'
      });
      saveAs(new Blob([workBookout], {
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
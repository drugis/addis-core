'use strict';
define(['lodash', 'xlsx-shim', 'file-saver'], function(_, XLSX, saveAs) {
  var dependencies = ['$q', 'GROUP_ALLOCATION_OPTIONS', 'BLINDING_OPTIONS', 'STATUS_OPTIONS',
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
    'UnitService'
  ];
  var ExcelExportService = function($q, GROUP_ALLOCATION_OPTIONS, BLINDING_OPTIONS, STATUS_OPTIONS,
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
    UnitService
  ) {
    var excelUtils = XLSX.utils;

    function exportStudy() {
      var promises = [StudyService.getJsonGraph()];
      var variablePromises = [PopulationCharacteristicService.queryItems(),
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

          var workBook = excelUtils.book_new();

          var conceptsSheet = ExcelExportUtilService.buildConceptsSheet(drugs.concat(populationCharacteristics, outcomes, adverseEvents, units));
          var epochSheet = ExcelExportUtilService.buildEpochSheet(epochs);
          var measurementMomentSheet = ExcelExportUtilService.buildMeasurementMomentSheet(measurementMoments, epochSheet);
          var studyDataSheet = ExcelExportUtilService.buildStudyDataSheet(study, studyInformation, arms, epochs, studyDesign,
            populationInformation, variables, conceptsSheet, measurementMomentSheet);
          var activitiesSheet = ExcelExportUtilService.buildActivitiesSheet(activities, conceptsSheet);
          var studyDesignSheet = ExcelExportUtilService.buildStudyDesignSheet(epochs, arms, activities, studyDesign, epochSheet, activitiesSheet, studyDataSheet);

          excelUtils.book_append_sheet(workBook, studyDataSheet, 'Study data');
          excelUtils.book_append_sheet(workBook, activitiesSheet, 'Activities');
          excelUtils.book_append_sheet(workBook, epochSheet, 'Epochs');
          excelUtils.book_append_sheet(workBook, studyDesignSheet, 'Study design');
          excelUtils.book_append_sheet(workBook, measurementMomentSheet, 'Measurement moments');
          excelUtils.book_append_sheet(workBook, conceptsSheet, 'Concepts');
          var workBookout = XLSX.write(workBook, {
            bookType: 'xlsx',
            type: 'array'
          });
          saveAs(new Blob([workBookout], {
            type: 'application/octet-stream'
          }), study.label + '.xlsx');
        });
    }

    //private


    // interface
    return {
      exportStudy: exportStudy
    };

  };
  return dependencies.concat(ExcelExportService);
});
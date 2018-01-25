'use strict';
define(['lodash', 'xlsx-shim', 'file-saver'], function(_, XLSX, saveAs) {
  var dependencies = ['$q', '$stateParams', '$location',
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
    'HistoryResource'
  ];
  var ExcelExportService = function($q, $stateParams, $location,
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
    HistoryResource
  ) {
    var excelUtils = XLSX.utils;

    function exportStudy() {
      var promises = [StudyService.getJsonGraph(),
        HistoryResource.query({
          userUid: $stateParams.userUid,
          datasetUuid: $stateParams.datasetUuid
        }).$promise
      ];
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
          var studyUrl = getStudyUrl(results[1]);
          var arms = results[2];
          var studyInformation = results[3][0];
          var populationInformation = results[4][0];
          var epochs = results[5];
          var activities = results[6];
          var studyDesign = results[7];
          var measurementMoments = results[8];
          var drugs = ExcelExportUtilService.addConceptType(results[9], 'drug');
          var units = ExcelExportUtilService.addConceptType(results[10], 'unit');
          var populationCharacteristics = results[11];
          var outcomes = results[12];
          var adverseEvents = results[13];

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
          var studyDataSheet = ExcelExportUtilService.buildStudyDataSheet(study, studyInformation, studyUrl, arms, epochs, activities, studyDesign,
            populationInformation, variables, conceptsSheet, measurementMomentSheet);
          var activitiesSheet = ExcelExportUtilService.buildActivitiesSheet(activities, conceptsSheet);
          var studyDesignSheet = ExcelExportUtilService.buildStudyDesignSheet(epochs, arms, studyDesign, epochSheet, activitiesSheet, studyDataSheet);

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
    function getStudyUrl(studyHistory) {
      var studyUrl = $location.absUrl();
      if ($stateParams.versionUuid) {
        return studyUrl;
      }
      var currentRevisionUri = studyHistory[studyHistory.length - 1].uri;
      var versionUuid = currentRevisionUri.substr(currentRevisionUri.lastIndexOf('/') + 1);
      var splitUrl = studyUrl.split('studies/');
      return splitUrl[0] + 'versions/' + versionUuid + '/studies/' + splitUrl[1];
    }

    // interface
    return {
      exportStudy: exportStudy
    };

  };
  return dependencies.concat(ExcelExportService);
});
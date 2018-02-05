'use strict';
define(['lodash', 'xlsx-shim', 'util/context', 'util/constants', 'angular', 'angular-mocks'], function(_, XLSX, externalContext, constants) {
  var ONTOLOGY_URI = 'http://trials.drugis.org/ontology#';
  var excelImportService;
  var uuidServiceMock = {
    generate: function() {
      return 'uuid';
    }
  };
  var stateParams = {
    userUid: 1,
    datasetUuid: 2,
    graphUuid: 3
  };

  beforeEach(module('addis.excelImport', function($provide) {
    $provide.value('UUIDService', uuidServiceMock);
    $provide.value('$stateParams', stateParams);
    $provide.value('GROUP_ALLOCATION_OPTIONS', constants.GROUP_ALLOCATION_OPTIONS);
    $provide.value('BLINDING_OPTIONS', constants.BLINDING_OPTIONS);
    $provide.value('STATUS_OPTIONS', constants.STATUS_OPTIONS);
  }));

  beforeEach(inject(function(ExcelImportService) {
    excelImportService = ExcelImportService;
  }));

  fdescribe('the excel import service', function() {
    describe('for a valid upload', function() {
      var workbook;
      beforeEach(function(done) {
        var oReq = new XMLHttpRequest();

        function reqListener() {
          var response = oReq.response;
          var reader = new FileReader();
          reader.onloadend = function(file) {
            var data = file.target.result;
            try {
              workbook = XLSX.read(data, {
                type: 'binary'
              });
              done();
            } catch (error) {
              console.log(error);
            }
          };
          reader.readAsBinaryString(response);
        }
        oReq.open('GET', '/base/src/test/resources/excelImport/teststudy.xlsx');
        oReq.responseType = 'blob';
        oReq.addEventListener('load', reqListener);
        oReq.send();
      });
      it('checkWorkbook should not return errors', function() {
        var result = excelImportService.checkWorkbook(workbook);
        expect(result.length).toBe(0);
      });

      it('createStudy should create a valid study', function() {
        var result = excelImportService.createStudy(workbook);
        var age = {
          '@id': 'http://trials.drugis.org/instances/uuid',
          '@type': 'ontology:PopulationCharacteristic',
          is_measured_at: 'http://trials.drugis.org/instances/uuid',
          label: 'Age (years)',
          has_result_property: [ONTOLOGY_URI + 'sample_size', ONTOLOGY_URI + 'mean', ONTOLOGY_URI + 'standard_deviation'],
          of_variable: [{
            '@type': 'ontology:Variable',
            measurementType: 'ontology:continuous',
            label: 'Age (years)'
          }]
        };
        var sex = {};
        var fastingPlasma = {
          '@id': 'http://trials.drugis.org/instances/uuid',
          '@type': 'ontology:Endpoint',
          is_measured_at: 'http://trials.drugis.org/instances/uuid',
          label: 'Fasting plasma glucose (mmol/L) change',
          has_result_property: [ONTOLOGY_URI + 'sample_size', ONTOLOGY_URI + 'mean', ONTOLOGY_URI + 'standard_deviation'],
          of_variable: [{
            '@type': 'ontology:Variable',
            measurementType: 'ontology:continuous',
            label: 'Fasting plasma glucose (mmol/L) change'
          }]
        };
        var hba1c = {
          '@id': 'http://trials.drugis.org/instances/uuid',
          '@type': 'ontology:Endpoint',
          is_measured_at: 'http://trials.drugis.org/instances/uuid',
          label: 'HbA1c (%) change',
          has_result_property: [ONTOLOGY_URI + 'sample_size', ONTOLOGY_URI + 'mean', ONTOLOGY_URI + 'standard_deviation'],
          of_variable: [Object({
            '@type': 'ontology:Variable',
            measurementType: 'ontology:continuous',
            label: 'HbA1c (%) change'
          })]
        };
        var bursitis = {
          '@id': 'http://trials.drugis.org/instances/uuid',
          '@type': 'ontology:AdverseEvent',
          is_measured_at: ['http://trials.drugis.org/instances/uuid', 'http://trials.drugis.org/instances/uuid'],
          label: 'Bursitis',
          has_result_property: [ONTOLOGY_URI + 'count', ONTOLOGY_URI + 'sample_size'],
          of_variable: [Object({
            '@type': 'ontology:Variable',
            measurementType: 'ontology:dichotomous',
            label: 'Bursitis'
          })]
        };
        var outcomes = [age, sex, fastingPlasma, hba1c, bursitis];

        var studyNode = {
          '@id': 'http://trials.drugis.org/studies/uuid',
          '@type': 'ontology:Study',
          label: 'Ahrén 2004',
          status: 'ontology:StatusCompleted',
          comment: 'Twelve- and 52-Week Efficacy of the Dipeptidyl Peptidase IV Inhibitor LAF237 in Metformin-Treated Patients With Type 2 Diabetes',
          has_activity: [],
          has_allocation: 'ontology:AllocationRandomized',
          has_arm: [{
            '@id': 'http://trials.drugis.org/instances/uuid',
            label: 'Placebo',
            comment: undefined
          }, {
            '@id': 'http://trials.drugis.org/instances/uuid',
            label: 'Vildagliptin',
            comment: undefined
          }],
          has_blinding: 'ontology:DoubleBlind',
          has_eligibility_criteria: [{
            '@id': 'http://trials.drugis.org/instances/uuid',
            comment: 'eligibility criteria'
          }],
          has_group: [],
          has_included_population: [{
            '@id': 'instance:uuid',
            '@type': 'ontology:StudyPopulation'
          }],
          has_indication: [{
            '@id': 'http://trials.drugis.org/instances/uuid',
            label: 'Type II diabetes mellitus'
          }],
          has_number_of_centers: 123,
          has_objective: [{
            '@id': 'http://trials.drugis.org/instances/uuid',
            comment: 'To assess the 12- and 52-week efficacy of the dipeptidyl peptidase IV inhibitor LAF237 (Vildagliptin) versus Placebo in patients with type 2 diabetes continuing Metformin treatment.'
          }],
          has_outcome: outcomes,
          has_publication: []



        };

        var expectedResult = {
          '@graph': [{
            '@id': 'http://trials.drugis.org/instances/uuid',
            '@type': 'ontology:MeasurementMoment',
            label: 'Baseline'
          }, {
            '@id': 'http://trials.drugis.org/instances/uuid',
            '@type': 'ontology:MeasurementMoment',
            label: 'Week 52'
          }, {
            '@id': 'http://trials.drugis.org/instances/uuid',
            '@type': 'ontology:MeasurementMoment',
            label: 'Week 12'
          }, studyNode],
          '@context': externalContext
        };

        expect(result).toEqual(expectedResult);
      });
    });
  });
});
'use strict';
define([
    'lodash',
    'xlsx-shim',
    'util/context',
    'util/constants',
    'angular',
    'angular-mocks'
  ],
  function(
    _,
    XLSX,
    externalContext,
    constants
  ) {
    var ONTOLOGY_URI = 'http://trials.drugis.org/ontology#';
    var INSTANCE_URI = 'http://trials.drugis.org/instances/uuid';
    var excelImportService;
    var rdfListService;
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

    describe('the excel import service', function() {
      beforeEach(module('addis.excelIO', function($provide) {
        $provide.value('UUIDService', uuidServiceMock);
        $provide.value('$stateParams', stateParams);
        $provide.value('GROUP_ALLOCATION_OPTIONS', constants.GROUP_ALLOCATION_OPTIONS);
        $provide.value('BLINDING_OPTIONS', constants.BLINDING_OPTIONS);
        $provide.value('STATUS_OPTIONS', constants.STATUS_OPTIONS);
      }));

      beforeEach(inject(function(ExcelImportService, RdfListService) {
        excelImportService = ExcelImportService;
        rdfListService = RdfListService;
      }));

      describe('for a valid simple upload', function() {
        var workbook;
        beforeEach(function(done) {
          loadExcel('teststudy.xlsx', function(loadedWorkbook) {
            workbook = loadedWorkbook;
            done();
          });
        });

        it('checkWorkbook should not return errors', function() {
          var result = excelImportService.checkWorkbook(workbook);
          expect(result.length).toBe(0);
        });

        it('createStudy should create a valid study', function() {
          var result = excelImportService.createStudy(workbook);
          var age = {
            '@id': INSTANCE_URI,
            '@type': 'ontology:PopulationCharacteristic',
            is_measured_at: INSTANCE_URI,
            label: 'Age (years)',
            has_result_property: [ONTOLOGY_URI + 'sample_size', ONTOLOGY_URI + 'mean', ONTOLOGY_URI + 'standard_deviation'],
            of_variable: [{
              '@type': 'ontology:Variable',
              measurementType: 'ontology:continuous',
              label: 'Age (years)'
            }]
          };
          var sex = {
            '@id': INSTANCE_URI,
            '@type': 'ontology:PopulationCharacteristic',
            is_measured_at: INSTANCE_URI,
            label: 'Sex',
            of_variable: [{
              '@type': 'ontology:Variable',
              measurementType: 'ontology:categorical',
              label: 'Sex',
              categoryList: {
                first: {
                  '@id': INSTANCE_URI,
                  '@type': 'ontology:Category',
                  label: 'male'
                },
                rest: {
                  first: {
                    '@id': INSTANCE_URI,
                    '@type': 'ontology:Category',
                    label: 'female'
                  },
                  rest: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'
                }
              }
            }]
          };
          var hba1c = {
            '@id': INSTANCE_URI,
            '@type': 'ontology:Endpoint',
            is_measured_at: INSTANCE_URI,
            label: 'HbA1c (%) change',
            has_result_property: [ONTOLOGY_URI + 'sample_size', ONTOLOGY_URI + 'mean', ONTOLOGY_URI + 'standard_deviation'],
            of_variable: [{
              '@type': 'ontology:Variable',
              measurementType: 'ontology:continuous',
              label: 'HbA1c (%) change'
            }]
          };
          var bursitis = {
            '@id': INSTANCE_URI,
            '@type': 'ontology:AdverseEvent',
            is_measured_at: [INSTANCE_URI, INSTANCE_URI],
            label: 'Bursitis',
            has_result_property: [ONTOLOGY_URI + 'count', ONTOLOGY_URI + 'sample_size'],
            of_variable: [{
              '@type': 'ontology:Variable',
              measurementType: 'ontology:dichotomous',
              label: 'Bursitis'
            }]
          };
          var outcomes = [age, sex, hba1c, bursitis];

          var studyNode = buildStudyNode();
          studyNode.has_outcome = outcomes;

          var measurements = buildMeasurements();

          var expectedResult = {
            '@graph': [].concat(
              buildBasicMeasurementMoment('Baseline'),
              buildBasicMeasurementMoment('Week 52'),
              buildBasicMeasurementMoment('Week 12'),
              measurements,
              studyNode
            ),
            '@context': externalContext
          };
          expect(result).toEqual(expectedResult);
        });
      });

      describe('for a valid structured upload', function() {
        var workbook;
        beforeEach(function(done) {
          loadExcel('teststudyStructured.xlsx', function(loadedWorkbook) {
            workbook = loadedWorkbook;
            done();
          });
        });

        it('checkWorkbook should not return errors', function() {
          var result = excelImportService.checkWorkbook(workbook);
          expect(result.length).toBe(0);
        });
        it('should create a valid study, including result properties', function() {
          var result = excelImportService.createStudy(workbook);
          var studyNode = buildStudyNode();
          var outcomes = [];
          studyNode.has_outcome = outcomes;
          studyNode.has_epochs = buildEpochs();
          studyNode.has_outcome = buildOutcome();
          studyNode.has_activity = buildActivities();
          studyNode.has_primary_epoch = 'treatmentPhaseEpochUri';

          var measurements = [{
            '@id': INSTANCE_URI,
            of_moment: 'week12MeasurementMomentUri',
            of_group: INSTANCE_URI,
            of_outcome: 'bursitisConceptUri',
            count: 1,
            sample_size: 51
          }, {
            '@id': INSTANCE_URI,
            of_moment: 'week12MeasurementMomentUri',
            of_group: INSTANCE_URI,
            of_outcome: 'bursitisConceptUri',
            count: 3,
            sample_size: 56
          }, {
            '@id': INSTANCE_URI,
            of_moment: 'week52MeasurementMomentUri',
            of_group: INSTANCE_URI,
            of_outcome: 'bursitisConceptUri',
            count: 2,
            sample_size: 29
          }, {
            '@id': INSTANCE_URI,
            of_moment: 'week52MeasurementMomentUri',
            of_group: INSTANCE_URI,
            of_outcome: 'bursitisConceptUri',
            count: 4,
            sample_size: 42
          }];

          var expectedResult = {
            '@graph': [].concat(
              buildMeasurementMoment('Baseline', 'baselineMeasurementMomentUri', 'randomisationEpochUri', 'End', 'PT0S'),
              buildMeasurementMoment('Week 52', 'week52MeasurementMomentUri', 'treatmentPhaseEpochUri', 'End', 'PT0S'),
              buildMeasurementMoment('Week 12', 'week12MeasurementMomentUri', 'treatmentPhaseEpochUri', 'Start', 'P84D'),
              measurements,
              studyNode
            ),
            '@context': externalContext
          };
          console.log(JSON.stringify(result['@graph'][3], null, 2));
          expect(result).toEqual(expectedResult);
        });
      });

      describe('for a minimal case with no measurement moments for a variable', function() {
        // minimalCaseNoMeasurementMoment.xslx
        var workbook;
        beforeEach(function(done) {
          loadExcel('minimalCaseNoMeasurementMoment.xlsx', function(loadedWorkbook) {
            workbook = loadedWorkbook;
            done();
          });
        });

        it('should create a valid study, including result properties', function() {
          var result = excelImportService.createStudy(workbook);

          var outcomes = [{
            '@id': INSTANCE_URI,
            '@type': 'ontology:Endpoint',
            is_measured_at: [],
            label: 'variable 1',
            has_result_property: [
              ONTOLOGY_URI + 'count',
              ONTOLOGY_URI + 'sample_size',
              ONTOLOGY_URI + 'event_count',
              ONTOLOGY_URI + 'percentage',
            ],
            of_variable: [{
              '@type': 'ontology:Variable',
              measurementType: 'ontology:dichotomous',
              label: 'variable 1'
            }]
          }];
          var studyNode = {
            '@id': 'http://trials.drugis.org/studies/uuid',
            '@type': 'ontology:Study',
            label: 'minimal',
            comment: 'long title',
            has_activity: [],
            has_arm: [],
            has_group: [],
            has_included_population: [{
              '@id': INSTANCE_URI,
              '@type': 'ontology:StudyPopulation'
            }],
            has_outcome: outcomes,
            has_publication: [],
            has_epochs: {
              first: {
                '@id': INSTANCE_URI,
                '@type': 'ontology:Epoch',
                label: 'Automatically generated primary epoch',
                duration: 'PT0S'
              },
              rest: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'
            },
            has_primary_epoch: INSTANCE_URI,
            status: undefined,
            has_allocation: undefined,
            has_blinding: undefined,
            has_eligibility_criteria: [],
            has_indication: [],
            has_number_of_centers: undefined,
            has_objective: []

          };

          var expectedResult = {
            '@graph': [studyNode],
            '@context': externalContext
          };

          expect(result).toEqual(expectedResult);
        });
      });
    });

    function loadExcel(fileName, callback) {
      var oReq = new XMLHttpRequest();

      function reqListener() {
        var response = oReq.response;
        var reader = new FileReader();
        reader.onloadend = function(file) {
          var data = file.target.result;
          try {
            var workbook = XLSX.read(data, {
              type: 'binary'
            });
            callback(workbook);
          } catch (error) {
            console.log(error);
          }
        };
        reader.readAsBinaryString(response);
      }
      oReq.open('GET', '/base/src/test/resources/excelImport/' + fileName);
      oReq.responseType = 'blob';
      oReq.addEventListener('load', reqListener);
      oReq.send();
    }

    function buildStudyNode() {
      return {
        '@id': 'http://trials.drugis.org/studies/uuid',
        '@type': 'ontology:Study',
        label: 'Ahrén 2004',
        status: 'ontology:StatusCompleted',
        comment: 'Twelve- and 52-Week Efficacy of the Dipeptidyl Peptidase IV Inhibitor LAF237 in Metformin-Treated Patients With Type 2 Diabetes',
        has_activity: [],
        has_allocation: 'ontology:AllocationRandomized',
        has_arm: [{
          '@id': INSTANCE_URI,
          label: 'Placebo',
          comment: undefined
        }, {
          '@id': INSTANCE_URI,
          label: 'Vildagliptin',
          comment: undefined
        }],
        has_blinding: 'ontology:DoubleBlind',
        has_eligibility_criteria: [{
          '@id': INSTANCE_URI,
          comment: 'eligibility criteria'
        }],
        has_group: [],
        has_included_population: [{
          '@id': INSTANCE_URI,
          '@type': 'ontology:StudyPopulation'
        }],
        has_indication: [{
          '@id': INSTANCE_URI,
          label: 'Type II diabetes mellitus'
        }],
        has_number_of_centers: 123,
        has_objective: [{
          '@id': INSTANCE_URI,
          comment: 'To assess the 12- and 52-week efficacy of the dipeptidyl peptidase IV inhibitor LAF237 (Vildagliptin) versus Placebo in patients with type 2 diabetes continuing Metformin treatment.'
        }],
        has_publication: [],
        has_epochs: {
          first: {
            '@id': INSTANCE_URI,
            '@type': 'ontology:Epoch',
            label: 'Automatically generated primary epoch',
            duration: 'PT0S'
          },
          rest: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'
        },
        has_primary_epoch: INSTANCE_URI
      };
    }

    function buildEpochs() {
      var epochs = [{
        '@id': 'randomisationEpochUri',
        '@type': 'ontology:Epoch',
        label: 'Randomisation',
        duration: 'PT0S'
      }, {
        '@id': 'treatmentPhaseEpochUri',
        '@type': 'ontology:Epoch',
        label: 'treatment phase',
        duration: 'P1W'
      }, {
        '@id': 'washoutEpochUri',
        '@type': 'ontology:Epoch',
        label: 'washout',
        duration: 'P1W'
      }];
      return rdfListService.unFlattenList(epochs);
    }

    function buildActivities() {
      return [{
        '@id': 'washoutActivityUri',
        '@type': 'ontology:WashOutActivity',
        label: 'washout'
      }, {
        '@id': 'vildaActivityUri',
        '@type': 'ontology:TreatmentActivity',
        label: 'Vildagliptin + placebo',
        has_drug_treatment: [{
          '@id': 'http://trials.drugis.org/instances/uuid',
          treatment_has_drug: 'vildaConceptUri',
          '@type': 'ontology:FixedDoseDrugTreatment',
          treatment_dose: [{
            '@id': 'uuid',
            value: 10,
            unit: 'milligramConceptUri',
            dosingPeriodicity: 'P1D'
          }]
        }, {
          '@id': 'http://trials.drugis.org/instances/uuid',
          treatment_has_drug: 'placeboConceptUri',
          '@type': 'ontology:FixedDoseDrugTreatment',
          treatment_dose: [{
            '@id': 'uuid',
            value: 20,
            unit: 'milligramConceptUri',
            dosingPeriodicity: 'P1D'
          }]
        }]
      }, {
        '@id': 'placeboActivityUri',
        '@type': 'ontology:TreatmentActivity',
        label: 'placebo',
        has_drug_treatment: [{
          '@id': 'http://trials.drugis.org/instances/uuid',
          treatment_has_drug: 'placeboConceptUri',
          '@type': 'ontology:TitratedDoseDrugTreatment',
          treatment_min_dose: [{
            '@id': 'uuid',
            value: 10,
            unit: 'milligramConceptUri',
            dosingPeriodicity: 'P1D'
          }],
          treatment_max_dose: [{
            '@id': 'uuid',
            value: 20,
            unit: 'milligramConceptUri',
            dosingPeriodicity: 'P1D'
          }]
        }]
      }, {
        '@id': 'randomisationActivityUri',
        '@type': 'ontology:RandomizationActivity',
        label: 'Randomisation'
      }];
    }

    function buildOutcome() {
      return [{
        '@id': 'bursitisConceptUri',
        '@type': 'ontology:AdverseEvent',
        label: 'Bursitis',
        has_result_property: [
          'http://trials.drugis.org/ontology#count',
          'http://trials.drugis.org/ontology#sample_size'
        ],
        of_variable: [{
          '@type': 'ontology:Variable',
          measurementType: 'ontology:dichotomous',
          label: 'Bursitis'
        }],
        is_measured_at: ['week12MeasurementMomentUri', 'week52MeasurementMomentUri']
      }];
    }

    function buildMeasurements() {
      var variables = [{
        label: 'Age (years)',
        results: {
          Placebo: {
            Baseline: {
              sample_size: 51,
              mean: 55.7,
              standard_deviation: 11
            }
          },
          Vildagliptin: {
            Baseline: {
              sample_size: 56,
              mean: 57.9,
              standard_deviation: 10
            }
          },
          'Overall population': {
            Baseline: {
              sample_size: 1,
              mean: 2,
              standard_deviation: 3
            }
          }
        }
      }, {
        label: 'Sex',
        results: {
          Placebo: {
            Baseline: {
              male: 34,
              female: 17
            }
          },
          Vildagliptin: {
            Baseline: {
              male: 39,
              female: 17
            }
          }
        }
      }, {
        label: 'HbA1c (%) change',
        results: {
          Placebo: {
            'Week 52': {
              sample_size: 47,
              mean: 0.1,
              standard_deviation: 0.1
            }
          },
          Vildagliptin: {
            'Week 52': {
              sample_size: 50,
              mean: -0.6,
              standard_deviation: 0.1
            }
          }
        }
      }, {
        label: 'Bursitis',
        results: {
          Placebo: {
            'Week 12': {
              count: 1,
              sample_size: 51
            },
            'Week 52': {
              count: 2,
              sample_size: 29
            }
          },
          Vildagliptin: {
            'Week 12': {
              count: 3,
              sample_size: 56
            },
            'Week 52': {
              count: 4,
              sample_size: 42
            }
          }
        }
      }];
      var measurements = [];
      _.forEach(variables, function(variable) {
        _.forEach(variable.results, function(armResults) {
          _.forEach(armResults, function(measurementMomentResults) {
            var measurement = {
              '@id': INSTANCE_URI,
              of_moment: INSTANCE_URI,
              of_group: INSTANCE_URI,
              of_outcome: INSTANCE_URI
            };
            if (variable.label === 'Sex') {
              measurement.category_count = [{
                '@id': INSTANCE_URI,
                category: INSTANCE_URI,
                count: measurementMomentResults.male
              }, {
                '@id': INSTANCE_URI,
                category: INSTANCE_URI,
                count: measurementMomentResults.female
              }];
              measurements.push(measurement);
            } else {
              measurements.push(_.merge(measurement, measurementMomentResults));
            }
          });
        });
      });
      return measurements;
    }

    function buildBasicMeasurementMoment(label) {
      return buildMeasurementMoment(label, INSTANCE_URI, INSTANCE_URI, 'End', 'PT0S');
    }

    function buildMeasurementMoment(label, uri, epochUri, anchor, offset) {
      return {
        '@id': uri,
        '@type': 'ontology:MeasurementMoment',
        label: label,
        relative_to_epoch: epochUri,
        relative_to_anchor: 'ontology:anchorEpoch' + anchor,
        time_offset: offset
      };
    }
  });
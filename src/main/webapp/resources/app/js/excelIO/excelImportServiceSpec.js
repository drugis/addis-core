'use strict';
define([
    'angular-mocks',
    'lodash',
    'xlsx',
    './../util/context',
    './../util/constants',
  ],
  function(
    angularMocks,
    _,
    XLSX,
    externalContext
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
      beforeEach(angular.mock.module('addis.excelIO', function($provide) {
        $provide.value('UUIDService', uuidServiceMock);
        $provide.value('$stateParams', stateParams);
      }));

      beforeEach(inject(function(ExcelImportService, RdfListService) {
        excelImportService = ExcelImportService;
        rdfListService = RdfListService;
      }));

      describe('for a valid simple upload', function() {
        var workbook;
        beforeEach(function() {
          workbook = require('test-resources/resources/excelIO/teststudy.xlsx');
        });

        it('checkSingleStudyWorkbook should not return errors', function() {
          var result = excelImportService.checkSingleStudyWorkbook(workbook);
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
        beforeEach(function() {
          workbook = require('test-resources/resources/excelIO/teststudyStructured.xlsx');
        });

        it('checkSingleStudyWorkbook should not return errors', function() {
          var result = excelImportService.checkSingleStudyWorkbook(workbook);
          expect(result.length).toBe(0);
        });
        it('should create a valid study, including result properties', function() {
          var result = excelImportService.createStudy(workbook);
          var expectedResult = buildExpectedStructuredStudyResult();
          expect(result).toEqual(expectedResult);
        });
      });

      describe('for a minimal unstructured case with no variables', function() {
        var workbook;
        beforeEach(function() {
          workbook = require('test-resources/resources/excelIO/minimalCaseAbsolutelyNothing.xlsx');
        });

        it('should create a valid study, including result properties', function() {
          var result = excelImportService.createStudy(workbook);

          var studyNode = {
            '@id': 'http://trials.drugis.org/studies/uuid',
            '@type': 'ontology:Study',
            label: 'nothing',
            comment: 'absolutely nothing',
            has_activity: [],
            has_arm: [],
            has_group: [],
            has_included_population: [{
              '@id': INSTANCE_URI,
              '@type': 'ontology:StudyPopulation'
            }],
            has_outcome: [],
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

      describe('for a minimal structured case with no variables', function() {
        var workbook;
        beforeEach(function() {
          workbook = require('test-resources/resources/excelIO/minimalStructuredCaseAbsolutelyNothing.xlsx');
        });

        it('should create a valid study, including result properties', function() {
          var result = excelImportService.createStudy(workbook);

          var studyNode = {
            '@id': 'http://trials.drugis.org/studies/uuid',
            '@type': 'ontology:Study',
            label: 'nothing',
            comment: 'absolutely nothing',
            has_activity: [],
            has_arm: [],
            has_group: [],
            has_included_population: [{
              '@id': INSTANCE_URI,
              '@type': 'ontology:StudyPopulation'
            }],
            has_outcome: [],
            has_publication: [],
            has_epochs: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil',
            has_primary_epoch: undefined,
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

      describe('for a minimal case with no measurement moments for a variable', function() {
        // minimalCaseNoMeasurementMoment.xslx
        var workbook;
        beforeEach(function() {
          workbook = require('test-resources/resources/excelIO/minimalCaseNoMeasurementMoment.xlsx');
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

      describe('for a whole dataset,', function() {
        var workbook;
        beforeEach(function() {
          workbook = require('test-resources/resources/excelIO/testdataset.xlsx');
        });

        describe('createDataset', function() {
          it('should return the dataset title and comment', function() {
            var result = excelImportService.createDataset(workbook);
            var expectedResult = {
              title: 'testdataset',
              description: 'testset description'
            };
            expect(result).toEqual(expectedResult);
          });
        });

        describe('createDatasetStudies', function() {
          it('should create the studies which are to be added to the dataset', function() {
            var result = excelImportService.createDatasetStudies(workbook);
            var expectedResult = [
              buildExpectedStructuredStudyResult(),
              buildExpectedSecondDatasetStudy()
            ];
            expect(result).toEqual(expectedResult);
          });
        });

        describe('createDatasetConcepts', function() {
          it('should return the dataset concepts', function() {
            var result = excelImportService.createDatasetConcepts(workbook);
            var expectedResult = [{
              label: 'Var 1',
              '@id': 'http://trials.drugis.org/concepts/variableConcept1',
              '@type': 'ontology:Variable'
            }, {
              label: 'Drug 1',
              '@id': 'http://trials.drugis.org/concepts/drugConcept1',
              '@type': 'ontology:Drug'
            }, {
              label: 'gram',
              '@id': 'http://trials.drugis.org/concepts/datasetGramConcept',
              '@type': 'ontology:Unit'
            }, {
              label: 'Var 2',
              '@id': 'http://trials.drugis.org/concepts/variableConcept2',
              '@type': 'ontology:Variable'
            }, {
              label: 'Var 3',
              '@id': 'http://trials.drugis.org/concepts/variableConcept3',
              '@type': 'ontology:Variable'
            }];
            expect(result).toEqual(expectedResult);
          });
        });
      });
    });

    function buildExpectedStructuredStudyResult() {
      var studyNode = buildStudyNode();
      var outcomes = [];
      studyNode.has_outcome = outcomes;
      studyNode.has_epochs = buildEpochs();
      studyNode.has_outcome = buildOutcome();
      studyNode.has_activity = buildActivities();
      studyNode.has_primary_epoch = 'http://trials.drugis.org/instances/treatmentPhaseEpochUri';

      var measurements = [{
        '@id': INSTANCE_URI,
        of_moment: 'http://trials.drugis.org/instances/week12MeasurementMomentUri',
        of_group: INSTANCE_URI,
        of_outcome: 'http://trials.drugis.org/instances/bursitisConceptUri',
        count: 1,
        sample_size: 51
      }, {
        '@id': INSTANCE_URI,
        of_moment: 'http://trials.drugis.org/instances/week52MeasurementMomentUri',
        of_group: INSTANCE_URI,
        of_outcome: 'http://trials.drugis.org/instances/bursitisConceptUri',
        count: 2,
        sample_size: 29
      }, {
        '@id': INSTANCE_URI,
        of_moment: 'http://trials.drugis.org/instances/week12MeasurementMomentUri',
        of_group: INSTANCE_URI,
        of_outcome: 'http://trials.drugis.org/instances/bursitisConceptUri',
        count: 3,
        sample_size: 56
      }, {
        '@id': INSTANCE_URI,
        of_moment: 'http://trials.drugis.org/instances/week52MeasurementMomentUri',
        of_group: INSTANCE_URI,
        of_outcome: 'http://trials.drugis.org/instances/bursitisConceptUri',
        count: 4,
        sample_size: 42
      }];

      return {
        '@graph': [].concat(
          buildMeasurementMoment('Baseline', 'http://trials.drugis.org/instances/baselineMeasurementMomentUri', 'http://trials.drugis.org/instances/randomisationEpochUri', 'End', 'PT0S'),
          buildMeasurementMoment('Week 52', 'http://trials.drugis.org/instances/week52MeasurementMomentUri', 'http://trials.drugis.org/instances/treatmentPhaseEpochUri', 'End', 'PT0S'),
          buildMeasurementMoment('Week 12', 'http://trials.drugis.org/instances/week12MeasurementMomentUri', 'http://trials.drugis.org/instances/treatmentPhaseEpochUri', 'Start', 'P84D'),
          measurements,
          studyNode,
          buildDrugs()
        ),
        '@context': externalContext
      };
    }

    function buildExpectedSecondDatasetStudy() {

      var measurements = [{
        '@id': INSTANCE_URI,
        of_moment: 'http://trials.drugis.org/instances/mm1',
        of_group: INSTANCE_URI,
        of_outcome: 'http://trials.drugis.org/instances/baseline',
        sample_size: 12,
        count: 4
      }, {
        '@id': INSTANCE_URI,
        of_moment: 'http://trials.drugis.org/instances/mm1',
        of_group: INSTANCE_URI,
        of_outcome: 'http://trials.drugis.org/instances/baseline',
        sample_size: 31,
        count: 4
      }, {
        '@id': INSTANCE_URI,
        of_moment: 'http://trials.drugis.org/instances/mm1',
        of_group: INSTANCE_URI,
        of_outcome: 'http://trials.drugis.org/instances/baseline',
        sample_size: 46,
        count: 9
      }];

      var studyNode = {
        '@id': 'http://trials.drugis.org/studies/uuid',
        '@type': 'ontology:Study',
        label: 'Teststudy 2',
        comment: 'long test study title 1',
        has_allocation: 'ontology:AllocationRandomized',
        has_blinding: 'ontology:DoubleBlind',
        status: 'ontology:StatusRecruiting',
        has_number_of_centers: 5,
        has_objective: [{
          '@id': 'http://trials.drugis.org/instances/uuid',
          comment: 'objectief'
        }],
        has_indication: [{
          '@id': 'http://trials.drugis.org/instances/uuid',
          label: 'indication'
        }],
        has_eligibility_criteria: [{
          '@id': 'http://trials.drugis.org/instances/uuid',
          comment: 'eg crit'
        }],
        has_activity: [{
          '@id': 'http://trials.drugis.org/instances/randomisation',
          '@type': 'ontology:RandomizationActivity',
          label: 'randomisation',
          has_activity_application: [{
            '@id': 'http://trials.drugis.org/instances/uuid',
            applied_in_epoch: 'http://trials.drugis.org/instances/epoch1',
            applied_to_arm: 'http://trials.drugis.org/instances/uuid'
          }, {
            '@id': 'http://trials.drugis.org/instances/uuid',
            applied_in_epoch: 'http://trials.drugis.org/instances/epoch1',
            applied_to_arm: 'http://trials.drugis.org/instances/uuid'
          }],
          comment: 'act 2 desc'
        }, {
          '@id': 'http://trials.drugis.org/instances/treatment',
          '@type': 'ontology:TreatmentActivity',
          label: 'treatment',
          has_activity_application: [{
            '@id': 'http://trials.drugis.org/instances/uuid',
            applied_in_epoch: 'http://trials.drugis.org/instances/epoch2',
            applied_to_arm: 'http://trials.drugis.org/instances/uuid'
          }, {
            '@id': 'http://trials.drugis.org/instances/uuid',
            applied_in_epoch: 'http://trials.drugis.org/instances/epoch2',
            applied_to_arm: 'http://trials.drugis.org/instances/uuid'
          }],
          comment: 'act 1 desc',
          has_drug_treatment: [{
            '@id': 'http://trials.drugis.org/instances/uuid',
            treatment_has_drug: 'http://trials.drugis.org/instances/drug',
            '@type': 'ontology:FixedDoseDrugTreatment',
            treatment_dose: [{
              '@id': 'http://trials.drugis.org/instances/uuid',
              value: 55,
              unit: 'http://trials.drugis.org/instances/milligramConceptUri',
              dosingPeriodicity: 'P1D'
            }]
          }]
        }],
        has_arm: [{
          '@id': 'http://trials.drugis.org/instances/uuid',
          label: 'arm 1',
          comment: 'arm 1 desc'
        }, {
          '@id': 'http://trials.drugis.org/instances/uuid',
          label: 'arm 2',
          comment: 'arm 2 desc'
        }],
        has_epochs: {
          first: {
            '@id': 'http://trials.drugis.org/instances/epoch1',
            '@type': 'ontology:Epoch',
            label: 'randomisation',
            duration: 'PT0S',
            comment: 'epoch 1 desc'
          },
          rest: {
            first: {
              '@id': 'http://trials.drugis.org/instances/epoch2',
              '@type': 'ontology:Epoch',
              label: 'treatment phase',
              duration: 'P1W',
              comment: 'epoch 2 desc'
            },
            rest: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'
          }
        },
        has_group: [],
        has_included_population: [{
          '@id': 'http://trials.drugis.org/instances/uuid',
          '@type': 'ontology:StudyPopulation'
        }],
        has_outcome: [{
          '@id': 'http://trials.drugis.org/instances/baseline',
          '@type': 'ontology:PopulationCharacteristic',
          label: 'Baseline char',
          has_result_property: ['http://trials.drugis.org/ontology#sample_size', 'http://trials.drugis.org/ontology#count'],
          of_variable: [{
            '@type': 'ontology:Variable',
            measurementType: 'ontology:dichotomous',
            label: 'Baseline char',
            sameAs: 'http://trials.drugis.org/concepts/bursitisDatasetUri'
          }],
          is_measured_at: ['http://trials.drugis.org/instances/mm1']
        }],
        has_publication: [],
        has_primary_epoch: 'http://trials.drugis.org/instances/epoch1'
      };

      return {
        '@graph': [].concat(
          buildMeasurementMoment('At end of epoch 1', 'http://trials.drugis.org/instances/mm1', 'http://trials.drugis.org/instances/epoch1', 'End', 'PT0S'),
          buildMeasurementMoment('7 day(s) from start of epoch 2', 'http://trials.drugis.org/instances/mm2', 'http://trials.drugis.org/instances/epoch2', 'Start', 'P1W'),
          measurements,
          studyNode, {
            '@id': 'http://trials.drugis.org/instances/drug',
            '@type': 'ontology:Drug',
            label: 'drug 1',
            sameAs: 'http://trials.drugis.org/concepts/placeboDatasetUri'
          }, {
            '@id': 'http://trials.drugis.org/instances/milligramConceptUri',
            '@type': 'ontology:Unit',
            conversionMultiplier: 0.001,
            label: 'milligram',
            sameAs: 'http://trials.drugis.org/concepts/datasetGramConcept'
          }
        ),
        '@context': externalContext
      };
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
        '@id': 'http://trials.drugis.org/instances/randomisationEpochUri',
        '@type': 'ontology:Epoch',
        label: 'Randomisation',
        duration: 'PT0S'
      }, {
        '@id': 'http://trials.drugis.org/instances/treatmentPhaseEpochUri',
        '@type': 'ontology:Epoch',
        label: 'treatment phase',
        duration: 'P1W'
      }, {
        '@id': 'http://trials.drugis.org/instances/washoutEpochUri',
        '@type': 'ontology:Epoch',
        label: 'washout',
        duration: 'P1W'
      }];
      return rdfListService.unFlattenList(epochs);
    }

    function buildActivities() {
      return [{
        '@id': 'http://trials.drugis.org/instances/washoutActivityUri',
        '@type': 'ontology:WashOutActivity',
        label: 'washout',
        has_activity_application: [{
          '@id': INSTANCE_URI,
          applied_in_epoch: 'http://trials.drugis.org/instances/washoutEpochUri',
          applied_to_arm: INSTANCE_URI
        }, {
          '@id': INSTANCE_URI,
          applied_in_epoch: 'http://trials.drugis.org/instances/washoutEpochUri',
          applied_to_arm: INSTANCE_URI
        }]
      }, {
        '@id': 'http://trials.drugis.org/instances/vildagliptinActivityUri',
        '@type': 'ontology:TreatmentActivity',
        label: 'Vildagliptin + placebo',
        has_drug_treatment: [{
          '@id': 'http://trials.drugis.org/instances/uuid',
          treatment_has_drug: 'http://trials.drugis.org/instances/vildaConceptUri',
          '@type': 'ontology:FixedDoseDrugTreatment',
          treatment_dose: [{
            '@id': INSTANCE_URI,
            value: 10,
            unit: 'http://trials.drugis.org/instances/milligramConceptUri',
            dosingPeriodicity: 'P1D'
          }]
        }, {
          '@id': 'http://trials.drugis.org/instances/uuid',
          treatment_has_drug: 'http://trials.drugis.org/instances/placeboConceptUri',
          '@type': 'ontology:FixedDoseDrugTreatment',
          treatment_dose: [{
            '@id': INSTANCE_URI,
            value: 20,
            unit: 'http://trials.drugis.org/instances/milligramConceptUri',
            dosingPeriodicity: 'P1D'
          }]
        }],
        has_activity_application: [{
          '@id': INSTANCE_URI,
          applied_in_epoch: 'http://trials.drugis.org/instances/treatmentPhaseEpochUri',
          applied_to_arm: INSTANCE_URI
        }]
      }, {
        '@id': 'http://trials.drugis.org/instances/placeboActivityUri',
        '@type': 'ontology:TreatmentActivity',
        label: 'placebo',
        has_drug_treatment: [{
          '@id': 'http://trials.drugis.org/instances/uuid',
          treatment_has_drug: 'http://trials.drugis.org/instances/placeboConceptUri',
          '@type': 'ontology:TitratedDoseDrugTreatment',
          treatment_min_dose: [{
            '@id': INSTANCE_URI,
            value: 10,
            unit: 'http://trials.drugis.org/instances/milligramConceptUri',
            dosingPeriodicity: 'P1D'
          }],
          treatment_max_dose: [{
            '@id': INSTANCE_URI,
            value: 20,
            unit: 'http://trials.drugis.org/instances/milligramConceptUri',
            dosingPeriodicity: 'P1D'
          }]
        }],
        has_activity_application: [{
          '@id': INSTANCE_URI,
          applied_in_epoch: 'http://trials.drugis.org/instances/treatmentPhaseEpochUri',
          applied_to_arm: INSTANCE_URI
        }]
      }, {
        '@id': 'http://trials.drugis.org/instances/randomisationActivityUri',
        '@type': 'ontology:RandomizationActivity',
        label: 'Randomisation',
        has_activity_application: [{
          '@id': INSTANCE_URI,
          applied_in_epoch: 'http://trials.drugis.org/instances/randomisationEpochUri',
          applied_to_arm: INSTANCE_URI
        }, {
          '@id': INSTANCE_URI,
          applied_in_epoch: 'http://trials.drugis.org/instances/randomisationEpochUri',
          applied_to_arm: INSTANCE_URI
        }]
      }];
    }

    function buildOutcome() {
      return [{
        '@id': 'http://trials.drugis.org/instances/bursitisConceptUri',
        '@type': 'ontology:AdverseEvent',
        label: 'Bursitis',
        has_result_property: [
          'http://trials.drugis.org/ontology#count',
          'http://trials.drugis.org/ontology#sample_size'
        ],
        of_variable: [{
          '@type': 'ontology:Variable',
          measurementType: 'ontology:dichotomous',
          label: 'Bursitis',
          sameAs: 'http://trials.drugis.org/concepts/bursitisDatasetUri'
        }],
        is_measured_at: ['http://trials.drugis.org/instances/week12MeasurementMomentUri', 'http://trials.drugis.org/instances/week52MeasurementMomentUri']
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

    function buildDrugs() {
      return [{
        '@id': 'http://trials.drugis.org/instances/placeboConceptUri',
        '@type': 'ontology:Drug',
        label: 'Placebo',
        sameAs: 'http://trials.drugis.org/concepts/placeboDatasetUri'
      }, {
        '@id': 'http://trials.drugis.org/instances/vildaConceptUri',
        '@type': 'ontology:Drug',
        label: 'Vildagliptin',
        sameAs: 'http://trials.drugis.org/concepts/vildaDatasetUri'
      }, {
        '@id': 'http://trials.drugis.org/instances/milligramConceptUri',
        '@type': 'ontology:Unit',
        conversionMultiplier: 0.001,
        label: 'milligram',
        sameAs: 'http://trials.drugis.org/concepts/datasetGramConcept'
      }];
    }
  });
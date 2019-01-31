'use strict';
define(['angular', 'angular-mocks', './analysis'], function() {
  describe('The single Study Benefit-Risk Analysis service', function() {
    var singleStudyBenefitRiskService;

    beforeEach(angular.mock.module('addis.analysis'));

    beforeEach(inject(function(SingleStudyBenefitRiskService) {
      singleStudyBenefitRiskService = SingleStudyBenefitRiskService;
    }));

    describe('addMissingOutcomesToStudies', function() {
      it('should find find 2 missing outcomes', function() {
        var studies = [{
          defaultMeasurementMoment: 'measurementMoment1',
          arms: [{
            measurements: {
              measurementMoment1: [{
                variableConceptUri: 'semanticOutcomeUri1'
              }]
            }
          }, {
            measurements: {
              measurementMoment1: [{
                variableConceptUri: 'variableConceptUri2'
              }]
            }
          }]
        }, {
          defaultMeasurementMoment: 'measurementMoment1',
          arms: [{
            measurements: {
              measurementMoment1: [{
                variableConceptUri: 'variableConceptUri3'
              }]
            }
          }, {
            measurements: {
              measurementMoment1: [{
                variableConceptUri: 'variableConceptUri4'
              }]
            }
          }]
        }, {
          defaultMeasurementMoment: 'measurementMoment1',
          arms: [{
            measurements: {
              measurementMoment1: [{
                variableConceptUri: 'semanticOutcomeUri1'
              }]
            }
          }, {
            measurements: {
              measurementMoment1: [{
                variableConceptUri: 'semanticOutcomeUri2'
              }]
            }
          }]
        }];
        var selectedOutcomes = [{
          outcome: {
            semanticOutcomeUri: 'semanticOutcomeUri1'
          }
        }, {
          outcome: {
            semanticOutcomeUri: 'semanticOutcomeUri2'
          }
        }];

        var result = singleStudyBenefitRiskService.addMissingOutcomesToStudies(studies, selectedOutcomes);
        expect(result[0].missingOutcomes).toEqual([selectedOutcomes[1]]);
        expect(result[1].missingOutcomes).toEqual(selectedOutcomes);
        expect(result[2].missingOutcomes).toEqual([]);
      });
    });

    describe('addHasMatchedMixedTreatmentArm', function() {
      it('should set hasMatchedMixedTreatmentArm to true for each study in which a selected intervention is matched to a mixed' +
        'treatment arm', function() {
          var studies = [{
            treatmentArms: [{
              interventionUids: [
                'uid 1',
                'uid 2'
              ]
            }, {
              interventionUids: [
                'uid 3'
              ]
            }]
          }, {
            treatmentArms: [{
              interventionUids: [
                'uid 1'
              ]
            }, {
              interventionUids: [
                'uid 2'
              ]
            }]
          }];
          var selectedInterventions = [{
            semanticInterventionUri: 'uid 1'
          }, {
            semanticInterventionUri: 'uid 2'
          }];
          var result = singleStudyBenefitRiskService.addHasMatchedMixedTreatmentArm(studies, selectedInterventions);
          expect(result[0].hasMatchedMixedTreatmentArm).toBeTruthy();
          expect(result[1].hasMatchedMixedTreatmentArm).toBeFalsy();
        });
    });

    describe('addOverlappingInterventionsToStudies', function() {
      it('should add a list of overlapping interventions to the studies', function() {
        var trialDataArm = {
          matchedProjectInterventionIds: [1, 2]
        };

        var studies = [{
          arms: [trialDataArm]
        }];

        var selectedInterventions = [{
          id: 1
        }, {
          id: 2
        }];

        var result = singleStudyBenefitRiskService.addOverlappingInterventionsToStudies(studies, selectedInterventions);

        expect(result).toEqual([{
          arms: [trialDataArm],
          overlappingInterventions: selectedInterventions
        }]);

      });
    });

    describe('recalculateGroup', function() {
      it('add group validity and sort the studies on this validity', function() {
        var studies = [{
          missingOutcomes: [],
          missingInterventions: [],
          hasMatchedMixedTreatmentArm: false
        }, {
          missingOutcomes: [],
          missingInterventions: [],
          hasMatchedMixedTreatmentArm: true
        }];
        var result = singleStudyBenefitRiskService.recalculateGroup(studies);
        var expectedResult = [{
          missingOutcomes: [],
          missingInterventions: [],
          hasMatchedMixedTreatmentArm: false,
          group: 0,
          groupLabel: 'Compatible studies'
        }, {
          missingOutcomes: [],
          missingInterventions: [],
          hasMatchedMixedTreatmentArm: true,
          group: 1,
          groupLabel: 'Incompatible studies'
        }];
        expect(result).toEqual(expectedResult);
      });
    });

    describe('findMissingOutcomes', function() {
      it('Find the missing outcomes for a given study', function() {
        var selectedOutcomes = [{
          outcome: {
            semanticOutcomeUri: 'notMissingUri'
          }
        }, {
          outcome: {
            semanticOutcomeUri: 'missingUri'
          }
        }];
        var study = {
          defaultMeasurementMoment: 'defmom',
          arms: [{
            measurements: {
              defmom: [{
                variableConceptUri: 'notMissingUri'
              }]
            }
          }]
        };
        var result = singleStudyBenefitRiskService.findMissingOutcomes(study, selectedOutcomes);
        var expectedResult = [{
          outcome: {
            semanticOutcomeUri: 'missingUri'
          }
        }];
        expect(result).toEqual(expectedResult);
      });
    });

    describe('getStudiesWithErrors', function() {
      it('should add any errors to studies if found', function() {
        var studies = [{
          defaultMeasurementMoment: 'defmom',
          arms: [{
            matchedProjectInterventionIds: []
          }]
        }];
        var alternatives = [{
          id: 1
        }];
        var result = singleStudyBenefitRiskService.getStudiesWithErrors(studies, alternatives);
        var expectedResult = [{
          defaultMeasurementMoment: 'defmom',
          arms: [{
            matchedProjectInterventionIds: []
          }],
          missingInterventions: [
            alternatives[0]
          ],
          overlappingInterventions: [],
          hasMatchedMixedTreatmentArm: false
        }];
        expect(result).toEqual(expectedResult);

      });
    });

    describe('isValidStudyOption', function() {
      it('should return true if a study is a valid option for the outcome', function() {
        var study = {
          missingOutcomes: [],
          missingInterventions: [],
          arms: [{
            measurements: {
              defmom: [{
                matchedProjectInterventionIds: [160],
                variableConceptUri: 'defmom',
                meanDifference: 5.0,
                stdErr: 2.0,
                referenceArm: 'refarm'
              }]
            }
          }],
          defaultMeasurementMoment: 'defmom'
        };
        var interventions = [];
        var outcome = {};
        var result = singleStudyBenefitRiskService.isValidStudyOption(study, interventions, outcome);
        expect(result).toBeTruthy();
      });

      it('should return false if the study has missing outcomes', function() {
        var study = {
          missingOutcomes: ['missingOutcome'],
          missingInterventions: [],
          arms: [{
            measurements: {
              defmom: []
            }
          }],
          defaultMeasurementMoment: 'defmom'
        };
        var interventions = [];
        var outcome = {};
        var result = singleStudyBenefitRiskService.isValidStudyOption(study, interventions, outcome);
        expect(result).toBeFalsy();
      });

      it('should return false if the study has missing interventions', function() {
        var study = {
          missingOutcomes: [],
          missingInterventions: ['missingIntervention'],
          arms: [{
            measurements: {
              defmom: []
            }
          }],
          defaultMeasurementMoment: 'defmom'
        };
        var interventions = [];
        var outcome = {};
        var result = singleStudyBenefitRiskService.isValidStudyOption(study, interventions, outcome);
        expect(result).toBeFalsy();
      });

      it('should return false if the study has mixed-treatment arms', function() {
        var study = {
          missingOutcomes: [],
          missingInterventions: [],
          arms: [{
            measurements: {
              defmom: []
            }
          }],
          defaultMeasurementMoment: 'defmom',
          hasMatchedMixedTreatmentArm: true
        };
        var interventions = [];
        var outcome = {};
        var result = singleStudyBenefitRiskService.isValidStudyOption(study, interventions, outcome);
        expect(result).toBeFalsy();
      });

      it('should return false if the study has missing value for the outcome+inventions combination', function() {
        var study = {
          missingOutcomes: [],
          missingInterventions: [],
          arms: [{
            matchedProjectInterventionIds: [160],
            measurements: {
              defmom: [{
                variableConceptUri: 'outcome1',
                mean: 5.0,
                stdErr: 2.0
              }, {
                variableConceptUri: 'outcome1'
              }]
            }
          }],
          defaultMeasurementMoment: 'defmom'
        };
        var interventions = [{
          id: 160
        }];
        var outcome = {
          outcome: {
            semanticOutcomeUri: 'outcome1'
          }
        };
        var result = singleStudyBenefitRiskService.isValidStudyOption(study, interventions, outcome);
        expect(result).toBeFalsy();
      });
    });
  });
});

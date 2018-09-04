define(['angular', 'angular-mocks', '../services'], function() {
  describe('The single Study Benefit-Risk Analysis service', function() {
    var singleStudyBenefitRiskService;
    
    beforeEach(angular.mock.module('addis.services'));
  

    beforeEach(inject(function(SingleStudyBenefitRiskService){
      singleStudyBenefitRiskService = SingleStudyBenefitRiskService;
    }));


    describe('addMissingOutcomesToStudies', function() {
      it('should find find 2 missing outcomes', function() {
        var studies = [{
          defaultMeasurementMoment: 'measurementMoment1',
          trialDataArms: [{
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
          trialDataArms: [{
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
          trialDataArms: [{
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
        var selectedOutcomes = [{outcome:{
          semanticOutcomeUri: 'semanticOutcomeUri1'
        }}, {outcome:{
          semanticOutcomeUri: 'semanticOutcomeUri2'
        }}];

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
          trialDataArms: [trialDataArm]
        }];

        var selectedInterventions = [{
          id: 1
        }, {
          id: 2
        }];

        var result = singleStudyBenefitRiskService.addOverlappingInterventionsToStudies(studies, selectedInterventions);

        expect(result).toEqual([{
          trialDataArms: [trialDataArm],
          overlappingInterventions: selectedInterventions
        }]);

      });
    });

  });
});

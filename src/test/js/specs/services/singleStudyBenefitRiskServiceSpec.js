define(['angular', 'angular-mocks', 'services'], function() {
  describe('The single Study Benefit-Risk Analysis service', function() {

    var mockProblemResource,
      mockScenarioResource;

    beforeEach(function() {
      var stateParams = jasmine.createSpyObj('stateParams', ['foo']);
      module('addis', function($provide) {
        $provide.value('$stateParams', stateParams);
      });
    });

    describe('getProblem', function() {

      beforeEach(module('addis.services'));
      beforeEach(module('addis.resources'));

      beforeEach(function() {

        mockProblemResource = jasmine.createSpyObj('ProblemResource', ['get']);
        mockScenarioResource = jasmine.createSpyObj('ScenarioResource', ['query']);

        module('addis', function($provide) {
          $provide.value('ProblemResource', mockProblemResource);
          $provide.value('ScenarioResource', mockScenarioResource);
        });
      });

      it('should return a problemResoure',
        inject(function(SingleStudyBenefitRiskService) {
          var mockProblem = {
            $promise: 'promise'
          };
          mockProblemResource.get.and.returnValue(mockProblem);
          expect(SingleStudyBenefitRiskService.getProblem()).toEqual(mockProblem.$promise);
        }));
    });

    describe('getDefaultScenario', function() {

      beforeEach(module('addis.services'));
      beforeEach(module('addis.resources'));

      beforeEach(function() {

        mockProblemResource = jasmine.createSpyObj('ProblemResource', ['get']);
        mockScenarioResource = jasmine.createSpyObj('ScenarioResource', ['query']);
        var mockHelpPopupService = jasmine.createSpyObj('HelpPopupService', ['loadLexicon']);
        var mockHttp = {
          get: function() {},
          defaults: {
            headers: {
              common: {}
            }
          }
        };


        module('addis', function($provide) {
          $provide.value('ProblemResource', mockProblemResource);
          $provide.value('ScenarioResource', mockScenarioResource);
          $provide.value('HelpPopupService', mockHelpPopupService);
          $provide.value('$http', mockHttp);
        });
      });

      it('should return the default scenario when getDefaultScenario is called',
        inject(function($rootScope, $q, SingleStudyBenefitRiskService) {
          var defaultScenario,
            scenariosDeferred = $q.defer(),
            scenarios = [{
              id: 13
            }];

          scenarios.$promise = scenariosDeferred.promise;
          mockScenarioResource.query.and.returnValue(scenarios);

          defaultScenario = SingleStudyBenefitRiskService.getDefaultScenario();
          expect(defaultScenario.then).not.toBeNull();
          defaultScenario.then(function(result) {
            defaultScenario = result;
          });
          scenariosDeferred.resolve(scenarios);
          $rootScope.$apply();

          expect(defaultScenario).toEqual(scenarios[0]);
        }));
    });

    describe('validateProblem', function() {
      it('should reject problems that do not have performance data for each combination of criterion and alternative',
        inject(function(SingleStudyBenefitRiskService) {
          var analysis = {
            selectedOutcomes: [{
              name: 'outcome1',
              semanticOutcomeUri: 'outcomeUri1'
            }, {
              name: 'outcome2',
              semanticOutcomeUri: 'outcomeUri2'
            }],
            selectedInterventions: [{
              name: 'intervention1',
              semanticInterventionUri: 'interventionUri1'
            }, {
              name: 'intervention2',
              semanticInterventionUri: 'interventionUri2'
            }]
          };
          var problem = {
            performanceTable: [{}]
          };
          expect(SingleStudyBenefitRiskService.validateProblem(analysis, problem)).toBeFalsy();
          problem = {
            performanceTable: [{
              alternativeUri: 'interventionUri1',
              criterionUri: 'outcomeUri1'
            }, {
              alternativeUri: 'interventionUri1',
              criterionUri: 'outcomeUri2'
            }, {
              alternativeUri: 'interventionUri2',
              criterionUri: 'outcomeUri1'
            }]
          };
          expect(SingleStudyBenefitRiskService.validateProblem(analysis, problem)).toBeFalsy();
          problem.performanceTable.push({
            alternativeUri: 'interventionUri2',
            criterionUri: 'outcomeUri2'
          });
          expect(SingleStudyBenefitRiskService.validateProblem(analysis, problem)).toBeTruthy();
        }));
    });

    describe('addMissingOutcomesToStudies', function() {
      beforeEach(module('addis.services'));

      it('should find find 2 missing outcomes', inject(function(SingleStudyBenefitRiskService) {
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

        var result = SingleStudyBenefitRiskService.addMissingOutcomesToStudies(studies, selectedOutcomes);
        expect(result[0].missingOutcomes).toEqual([selectedOutcomes[1]]);
        expect(result[1].missingOutcomes).toEqual(selectedOutcomes);
        expect(result[2].missingOutcomes).toEqual([]);
      }));
    });

    describe('concatWithNoDuplicates', function() {

      beforeEach(module('addis.services'));

      it('should add the source objects to the target if not already on target',
        inject(function(SingleStudyBenefitRiskService) {

          var source = [{
            name: 'bob'
          }, {
            name: 'dave'
          }];
          var target = [{
            name: 'mark'
          }, {
            name: 'dave'
          }, {
            name: 'lisa'
          }];

          var isNameEqual = function(option, seachItem) {
            return option.name === seachItem.name;
          };

          var expectResult = [{
            name: 'bob'
          }, {
            name: 'mark'
          }, {
            name: 'dave'
          }, {
            name: 'lisa'
          }];

          expect(SingleStudyBenefitRiskService.concatWithNoDuplicates(source, target, isNameEqual)).toEqual(expectResult);
        })
      );
    });

    describe('addHasMatchedMixedTreatmentArm', function() {
      beforeEach(module('addis.services'));
      it('should set hasMatchedMixedTreatmentArm to true for each study in which a selected intervention is matched to a mixed' +
        'treatment arm', inject(function(SingleStudyBenefitRiskService) {
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
          var result = SingleStudyBenefitRiskService.addHasMatchedMixedTreatmentArm(studies, selectedInterventions);
          expect(result[0].hasMatchedMixedTreatmentArm).toBeTruthy();
          expect(result[1].hasMatchedMixedTreatmentArm).toBeFalsy();
        }));
    });

    describe('addOverlappingInterventionsToStudies', function() {
      beforeEach(module('addis.services'));
      it('should add a list of overlapping interventions to the studies', inject(function(SingleStudyBenefitRiskService) {
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

        var result = SingleStudyBenefitRiskService.addOverlappingInterventionsToStudies(studies, selectedInterventions);

        expect(result).toEqual([{
          trialDataArms: [trialDataArm],
          overlappingInterventions: selectedInterventions
        }]);

      }));
    });

  });
});

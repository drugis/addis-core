define(['angular', 'angular-mocks', 'services'], function() {
  describe('The singleStudyBenefitRisk Analysis service', function() {

    var mockProblemResource,
      mockScenarioResource;

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
        inject(function(SingleStudyBenefitRiskAnalysisService) {
          var mockProblem = {
            $promise: 'promise'
          };
          mockProblemResource.get.and.returnValue(mockProblem);
          expect(SingleStudyBenefitRiskAnalysisService.getProblem()).toEqual(mockProblem.$promise);
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
        inject(function($rootScope, $q, SingleStudyBenefitRiskAnalysisService) {
          var defaultScenario,
            scenariosDeferred = $q.defer(),
            scenarios = [{
              id: 13
            }];

          scenarios.$promise = scenariosDeferred.promise;
          mockScenarioResource.query.and.returnValue(scenarios);

          defaultScenario = SingleStudyBenefitRiskAnalysisService.getDefaultScenario();
          expect(defaultScenario.then).not.toBeNull();
          defaultScenario.then(function(result) {
            defaultScenario = result;
          });
          scenariosDeferred.resolve(scenarios);
          $rootScope.$apply();

          expect(defaultScenario).toEqual(scenarios[0]);
        }));


    });

    describe('validateAnalysis', function() {
      beforeEach(function() {
        var stateParams = jasmine.createSpyObj('stateParams', ['foo']);
        module('addis', function($provide) {
          $provide.value('$stateParams', stateParams);
        });
      });
    });

    describe('validateProblem', function() {
      beforeEach(function() {
        var stateParams = jasmine.createSpyObj('stateParams', ['foo']);
        module('addis', function($provide) {
          $provide.value('$stateParams', stateParams);
        });
      });

      it('should reject problems that do not have performance data for each combination of criterion and alternative',
        inject(function(SingleStudyBenefitRiskAnalysisService) {
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
          expect(SingleStudyBenefitRiskAnalysisService.validateProblem(analysis, problem)).toBeFalsy();
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
          expect(SingleStudyBenefitRiskAnalysisService.validateProblem(analysis, problem)).toBeFalsy();
          problem.performanceTable.push({
            alternativeUri: 'interventionUri2',
            criterionUri: 'outcomeUri2'
          });
          expect(SingleStudyBenefitRiskAnalysisService.validateProblem(analysis, problem)).toBeTruthy();
        }));
    });

    describe('concatWithNoDuplicates', function() {

      beforeEach(function() {
        var stateParams = jasmine.createSpyObj('stateParams', ['foo']);
        module('addis', function($provide) {
          $provide.value('$stateParams', stateParams);
        });
      });
      beforeEach(module('addis.services'));

      it('should add the source objects to the target if not already on target',
        inject(function(SingleStudyBenefitRiskAnalysisService) {

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

          expect(SingleStudyBenefitRiskAnalysisService.concatWithNoDuplicates(source, target, isNameEqual)).toEqual(expectResult);
        })
      );

    });

    describe('addHasMatchedMixedTreatmentArm', function() {
      beforeEach(function() {
        var stateParams = jasmine.createSpyObj('stateParams', ['foo']);
        module('addis', function($provide) {
          $provide.value('$stateParams', stateParams);
        });
      });
      beforeEach(module('addis.services'));
      it('should set hasMatchedMixedTreatmentArm to true for each study in which a selected intervention is matched to a mixed' +
        'treatment arm', inject(function(SingleStudyBenefitRiskAnalysisService) {
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
          SingleStudyBenefitRiskAnalysisService.addHasMatchedMixedTreatmentArm(studies, selectedInterventions);
          expect(studies[0].hasMatchedMixedTreatmentArm).toBeTruthy();
          expect(studies[1].hasMatchedMixedTreatmentArm).toBeFalsy();
        }));
    });

    describe('addOverlappingInterventionsToStudies', function() {
      beforeEach(function() {
        var stateParams = jasmine.createSpyObj('stateParams', ['foo']);
        module('addis', function($provide) {
          $provide.value('$stateParams', stateParams);
        });
      });
      beforeEach(module('addis.services'));
      it('should add a list of overlapping interventions to the studies', inject(function(SingleStudyBenefitRiskAnalysisService) {
        var drugUri = 'http://trials.drugis.org/instances/333-333';
        var treatmentArm = {
          interventionUids: [drugUri]
        };

        var studies = [{
          treatmentArms: [treatmentArm]
        }];

        var selectedInterventions = [{
          semanticInterventionUri: '333-333'
        }, {
          semanticInterventionUri: '333-333'
        }];

        var result = SingleStudyBenefitRiskAnalysisService.addOverlappingInterventionsToStudies(studies, selectedInterventions);

        expect(result).toEqual([{
          treatmentArms: [treatmentArm],
          overlappingInterventions: selectedInterventions
        }]);

      }));
    });

  });
});

define(['angular', 'angular-mocks', 'services'], function() {
  describe('The singleStudyBenefitRisk Analysis service', function() {

    var mockProblemResource,
      mockScenarioResource,
      location;

    describe('getProblem', function() {

      beforeEach(module('addis.services'));
      beforeEach(module('addis.resources'));

      beforeEach(function() {

        mockProblemResource = jasmine.createSpyObj('ProblemResource', ['get']);
        mockScenarioResource = jasmine.createSpyObj('ScenarioResource', ['query']);
        mockLocation = jasmine.createSpyObj('$location', ['url']);

        module('addis', function($provide) {
          $provide.value('ProblemResource', mockProblemResource);
          $provide.value('ScenarioResource', mockScenarioResource);
          $provide.value('$location', mockLocation);
        });
      });

      it('should return a problemResoure',
        inject(function($rootScope, $q, $location, SingleStudyBenefitRiskAnalysisService) {
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
        mockLocation = jasmine.createSpyObj('$location', ['url']);

        module('addis', function($provide) {
          $provide.value('ProblemResource', mockProblemResource);
          $provide.value('ScenarioResource', mockScenarioResource);
          $provide.value('$location', mockLocation);
        });
      });

      it('should return the default scenario when getDefaultScenario is called',
        inject(function($rootScope, $q, $location, SingleStudyBenefitRiskAnalysisService) {
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

    describe("validateAnalysis", function() {
      beforeEach(function() {
        stateParams = jasmine.createSpyObj('stateParams', ['foo']);
        module('addis', function($provide) {
          $provide.value('$stateParams', stateParams);
        });
      });
    });

    describe("validateProblem", function() {
      beforeEach(function() {
        stateParams = jasmine.createSpyObj('stateParams', ['foo']);
        module('addis', function($provide) {
          $provide.value('$stateParams', stateParams);
        });
      });

      it("should reject problems that do not have performance data for each combination of criterion and alternative",
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

    describe("concatWithNoDuplicates", function() {

      beforeEach(function() {
        stateParams = jasmine.createSpyObj('stateParams', ['foo']);
        module('addis', function($provide) {
          $provide.value('$stateParams', stateParams);
        });
      });
      beforeEach(module('addis.services'));

      it('should add the source objects to the target if not already on target',
        inject(function($rootScope, $q, $location, SingleStudyBenefitRiskAnalysisService) {

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
        stateParams = jasmine.createSpyObj('stateParams', ['foo']);
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
          var modifiedStudies = SingleStudyBenefitRiskAnalysisService.addHasMatchedMixedTreatmentArm(studies, selectedInterventions);
          expect(modifiedStudies[0].hasMatchedMixedTreatmentArm).toBeTruthy();
          expect(modifiedStudies[1].hasMatchedMixedTreatmentArm).toBeFalsy();
        }));
    });

  });
});

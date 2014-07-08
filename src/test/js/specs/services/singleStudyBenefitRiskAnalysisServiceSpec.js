define(['angular', 'angular-mocks', 'services'], function() {
  describe("The singleStudyBenefitRisk Analysis service", function() {

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

      it("should reject analyses that contain fewer than two selectedInterventions and two selectedOutcomes",
        inject(function($rootScope, $q, $location, $stateParams, SingleStudyBenefitRiskAnalysisService) {
          var invalidAnalysis = {
            selectedInterventions: [],
            selectedOutcomes: []
          };
          expect(SingleStudyBenefitRiskAnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();

          invalidAnalysis = {
            selectedInterventions: [1],
            selectedOutcomes: []
          };
          expect(SingleStudyBenefitRiskAnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();

          invalidAnalysis = {
            selectedInterventions: [],
            selectedOutcomes: [1]
          };
          expect(SingleStudyBenefitRiskAnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();

          invalidAnalysis = {
            selectedInterventions: [1, 2, 3],
            selectedOutcomes: []
          };
          expect(SingleStudyBenefitRiskAnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();

          invalidAnalysis = {
            selectedInterventions: [],
            selectedOutcomes: [1, 2, 3]
          };
          expect(SingleStudyBenefitRiskAnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();

          var validAnalysis = {
            selectedInterventions: [1, 2],
            selectedOutcomes: [1, 2]
          };
          expect(SingleStudyBenefitRiskAnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();

          validAnalysis = {
            selectedInterventions: [1, 2, 3],
            selectedOutcomes: [1, 2, 3]
          };
          expect(SingleStudyBenefitRiskAnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();
        }));
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

    describe('keyify', function() {
      beforeEach(function() {
        stateParams = jasmine.createSpyObj('stateParams', ['foo']);
        module('addis', function($provide) {
          $provide.value('$stateParams', stateParams);
        });
      });

      it('should remove non-alphanumeric characters', inject(function(SingleStudyBenefitRiskAnalysisService) {
        expect(SingleStudyBenefitRiskAnalysisService.keyify("i'm/not$alpha&numeric")).toBe('imnotalphanumeric');
      }));

      it('should convert whitespace to dashes', inject(function(SingleStudyBenefitRiskAnalysisService) {
        expect(SingleStudyBenefitRiskAnalysisService.keyify('a horse a horse my')).toBe('a-horse-a-horse-my');
      }));

      it('should convert upper case to lowercase', inject(function(SingleStudyBenefitRiskAnalysisService) {
        expect(SingleStudyBenefitRiskAnalysisService.keyify('IM NOT SHOUTING')).toBe('im-not-shouting');
      }));
    });
  });
});
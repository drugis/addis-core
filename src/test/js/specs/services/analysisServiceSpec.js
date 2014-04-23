define(['angular', 'angular-mocks', 'services'], function() {
  describe("The analysis service", function() {

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
        inject(function($rootScope, $q, $location, AnalysisService) {
          var mockProblem = {
            $promise: 'promise'
          };
          mockProblemResource.get.and.returnValue(mockProblem);
          expect(AnalysisService.getProblem()).toEqual(mockProblem.$promise);
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
        inject(function($rootScope, $q, $location, AnalysisService) {
          var defaultScenario,
            scenariosDeferred = $q.defer(),
            scenarios = [{
              id: 13
            }];

          scenarios.$promise = scenariosDeferred.promise;
          mockScenarioResource.query.and.returnValue(scenarios);

          defaultScenario = AnalysisService.getDefaultScenario();
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
        inject(function($rootScope, $q, $location, $stateParams, AnalysisService) {
          var invalidAnalysis = {
            selectedInterventions: [],
            selectedOutcomes: []
          };
          expect(AnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();

          invalidAnalysis = {
            selectedInterventions: [1],
            selectedOutcomes: []
          };
          expect(AnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();

          invalidAnalysis = {
            selectedInterventions: [],
            selectedOutcomes: [1]
          };
          expect(AnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();

          invalidAnalysis = {
            selectedInterventions: [1, 2, 3],
            selectedOutcomes: []
          };
          expect(AnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();

          invalidAnalysis = {
            selectedInterventions: [],
            selectedOutcomes: [1, 2, 3]
          };
          expect(AnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();

          var validAnalysis = {
            selectedInterventions: [1, 2],
            selectedOutcomes: [1, 2]
          };
          expect(AnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();

          validAnalysis = {
            selectedInterventions: [1, 2, 3],
            selectedOutcomes: [1, 2, 3]
          };
          expect(AnalysisService.validateAnalysis(invalidAnalysis)).toBeFalsy();
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
        inject(function(AnalysisService) {
          var analysis = {
            selectedOutcomes: [{
              name: 'outcome1'
            }, {
              name: 'outcome2'
            }],
            selectedInterventions: [{
              name: 'intervention1'
            }, {
              name: 'intervention2'
            }]
          }
          var problem = {
            performanceTable: [{}]
          };
          expect(AnalysisService.validateProblem(analysis, problem)).toBeFalsy();
          problem = {
            performanceTable: [{
              'intervention1': 'value',
              'outcome1': 'value'
            }, {
              'intervention1': 'value',
              'outcome2': 'value'
            }, {
              'intervention2': 'value',
              'outcome1': 'value'
            }]
          };
          expect(AnalysisService.validateProblem(analysis, problem)).toBeFalsy();
          problem.performanceTable.push({
            'intervention2': 'value',
            'outcome2': 'value'
          });
          expect(AnalysisService.validateProblem(analysis, problem)).toBeTruthy();
        }));
    });

    describe('keyify', function() {
      beforeEach(function() {
        stateParams = jasmine.createSpyObj('stateParams', ['foo']);
        module('addis', function($provide) {
          $provide.value('$stateParams', stateParams);
        });
      });

      it('should remove non-alphanumeric characters', inject(function(AnalysisService) {
        expect(AnalysisService.keyify("i'm/not$alpha&numeric")).toBe('imnotalphanumeric');
      }));

      it('should convert whitespace to dashes', inject(function(AnalysisService) {
        expect(AnalysisService.keyify('a horse a horse my')).toBe('a-horse-a-horse-my');
      }));

      it('should convert upper case to lowercase', inject(function(AnalysisService) {
        expect(AnalysisService.keyify('IM NOT SHOUTING')).toBe('im-not-shouting');
      }));
    })
  });
});
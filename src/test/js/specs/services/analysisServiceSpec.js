define(['angular', 'angular-mocks', 'services'], function() {
  describe("The analysis service", function() {

    var mockProblemResource,
      mockScenarioResource,
      location;

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

    it('should create the problem, add it to the analysis and save the analysis,' +
      ' and then retrieve the default scenario when createProblem is called',
      inject(function($rootScope, $q, $location, AnalysisService) {
        var problemDeferred = $q.defer();
        var mockProblem = {
          foo: 'bar',
          $promise: problemDeferred.promise
        };
        mockProblemResource.get.and.returnValue(mockProblem);

        var analysisDeferred = $q.defer();
        var savedAnalysis = {
          id: 1,
          $promise: analysisDeferred.promise
        };
        var mockAnalysis = jasmine.createSpyObj('analysis', ['$save']);
        mockAnalysis.$save.and.returnValue(savedAnalysis);

        var scenariosDeferred = $q.defer();
        var scenarios = [{
          id: 0
        }];
        scenarios.$promise = scenariosDeferred.promise;
        mockScenarioResource.query.and.returnValue(scenarios);

        problemDeferred.resolve();
        analysisDeferred.resolve(mockAnalysis);
        scenariosDeferred.resolve(scenarios);

        var result = AnalysisService.createProblem(mockAnalysis);
        expect(result.then).not.toBeNull();
        result.then(function(value) {
          result = value;
        });
        scenariosDeferred.resolve(scenarios);
        $rootScope.$apply();
        expect(result).toEqual(scenarios[0]);
      }));

    it('should return the default scenario when getDefaultScenario is called',
      inject(function($rootScope, $q, $location, AnalysisService) {
        var defaultScenario,
            scenariosDeferred = $q.defer(),
            scenarios = [{
              id: 13
            }];

        scenarios.$promise = scenariosDeferred.promise;
        mockScenarioResource.query.and.returnValue(scenarios);

        defaultScenario = AnalysisService.getDefaultScenario()
        expect(defaultScenario.then).not.toBeNull();
        defaultScenario.then(function(result) {
          defaultScenario = result;
        });
        scenariosDeferred.resolve(scenarios);
        $rootScope.$apply();

        expect(defaultScenario).toEqual(scenarios[0]);
    }));


  });
});
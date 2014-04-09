define(['angular', 'angular-mocks', 'services'], function() {
  describe("The analysis service", function() {

    var mockProblemResource,
      mockScenarioResource;

    beforeEach(module('addis.services'));
    beforeEach(module('addis.resources'));

    beforeEach(function() {

      mockProblemResource = jasmine.createSpyObj('ProblemResource', ['get']);
      mockScenarioResource = jasmine.createSpyObj('ScenarioResource', ['query']);


      module('addis', function($provide) {
        // $provide.value('$location', {});
        // $provide.value('$stateParams', {});
        $provide.value('ProblemResource', mockProblemResource);
        $provide.value('ScenarioResource', mockScenarioResource);
      });
    });

    it('should create the problem, add it to the analysis and save the analysis,' +
      ' and then retrieve the default scenario and return the url for the default scenario',
      inject(function($rootScope, $q, AnalysisService) {

        var problemDeferred = $q.defer();
        var mockProblem = {
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
        var scenarios = [{id:0}];
        scenarios.$promise = scenariosDeferred.promise;
        mockScenarioResource.query.and.returnValue(scenarios);

        problemDeferred.resolve();
        // analysisDeferred.resolve();
        // scenariosDeferred.resolve();
        //$rootScope.$apply();

        expect(AnalysisService.createProblem(mockAnalysis)).toEqual('test');
      }));


  });
});
define(['angular', 'angular-mocks'], function () {
  describe('SingleProjectController', function () {
    beforeEach(module('addis.controllers'));
    var scope, projectsService, trialverseService, semanticOutcomeService, outcomeService, deferred, mockSemanticOutcomes,
      mockProject = {id: 1, name: 'projectName', description: 'testDescription', namespace: 'testNamespace', outcomes: [], trialverseId: 1, $save: function(){}},
      mockTrialverse = {id: 1, name: 'trialverseName', description: 'trialverseDescription'};

    beforeEach(inject(function ($controller, $q, $rootScope) {
      var mockStateParams = {id: mockProject.id};

      mockSemanticOutcomes = ["a", "b", 'c'] ;
      projectsService = jasmine.createSpyObj('projectsService', ['get', 'save']);
      projectsService.get.andReturn(mockProject);
      trialverseService = jasmine.createSpyObj('trialverseService', ['get']);
      trialverseService.get.andReturn(mockTrialverse);
      semanticOutcomeService = jasmine.createSpyObj('semanticOutcomeService', ['query']);
      semanticOutcomeService.query.andReturn(mockSemanticOutcomes);
      outcomeService = jasmine.createSpyObj('outcomeService', ['save']);

      spyOn(mockProject, '$save');

      scope = $rootScope;
      deferred = $q.defer();
      mockProject.$promise = deferred.promise;

      $controller('SingleProjectController', {
        $scope: scope,
        'ProjectsService': projectsService,
        'TrialverseService': trialverseService,
        'SemanticOutcomeService': semanticOutcomeService,
        'OutcomeService': outcomeService,
        $stateParams: mockStateParams
       });

    }));

    it('should place project information on the scope', function () {
      expect(projectsService.get).toHaveBeenCalledWith({id: mockProject.id});
      expect(scope.project).toEqual(mockProject);
    });

    it('should tell the scope whether the resource is loaded', function() {
      expect(scope.loading.loaded).toBeFalsy();
      deferred.resolve();
      scope.$apply();
      expect(scope.loading.loaded).toBeTruthy();
    });

    xit("should make an update call when an outcome is added", function() {
      scope.addOutcome({name: "name", motivation: "motivation", semanticOutcome: "semantics"});
      expect(scope.project.$save).toHaveBeenCalled();
    });

    it("should place the associated trialverse information on the scope on resolution", function() {
      deferred.resolve();
      scope.$apply();
      expect(trialverseService.get).toHaveBeenCalledWith({id: mockProject.trialverseId});
      expect(scope.trialverse).toEqual(mockTrialverse);
    });

    it("should place the possible semanticOutcomes on the scope on resolution", function() {
      deferred.resolve();
      scope.$apply();
      expect(semanticOutcomeService.query).toHaveBeenCalledWith({id: mockProject.trialverseId});
      expect(scope.semanticOutcomes).toEqual(mockSemanticOutcomes);
    });

  });
});
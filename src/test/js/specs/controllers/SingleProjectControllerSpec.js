define(['angular', 'angular-mocks'], function () {
  describe('SingleProjectController', function () {
    beforeEach(module('addis.controllers'));
    var scope, projectsService, trialverseService, semanticOutcomeService, semanticInterventionsService, outcomeService,
     interventionService, deferred, mockSemanticOutcomes, window,
      mockProject = {id: 1, owner: {id: 1}, name: 'projectName', description: 'testDescription', namespace: 'testNamespace', outcomes: [], trialverseId: 1, $save: function(){}},
      mockTrialverse = {id: 1, name: 'trialverseName', description: 'trialverseDescription'};

    beforeEach(inject(function ($controller, $q, $rootScope) {
      var mockStateParams = {id: mockProject.id};

      mockSemanticOutcomes = ["a", "b", 'c'] ;
      mockSemanticInterventions = ["e", "f", 'g'] ;
      projectsService = jasmine.createSpyObj('projectsService', ['get', 'save']);
      projectsService.get.andReturn(mockProject);
      trialverseService = jasmine.createSpyObj('trialverseService', ['get']);
      trialverseService.get.andReturn(mockTrialverse);
      semanticOutcomeService = jasmine.createSpyObj('semanticOutcomeService', ['query']);
      semanticOutcomeService.query.andReturn(mockSemanticOutcomes);
      outcomeService = jasmine.createSpyObj('outcomeService', ['save']);
      semanticInterventionService = jasmine.createSpyObj('semanticInterventionService', ['query']);
      semanticInterventionService.query.andReturn(mockSemanticInterventions);
      interventionService = jasmine.createSpyObj('interventionService', ['save']);

      spyOn(mockProject, '$save');

      scope = $rootScope;
      scope.createOutcomeModal = jasmine.createSpyObj('createOutcomeModal', ['close']);
      scope.createInterventionModal = jasmine.createSpyObj('createInterventionModal', ['close']);
      deferred = $q.defer();
      mockProject.$promise = deferred.promise;

      window = {config: {user: {id: 1}}};

      $controller('SingleProjectController', {
        $scope: scope,
        $window: window,
        $stateParams: mockStateParams,
        'ProjectsService': projectsService,
        'TrialverseService': trialverseService,
        'SemanticOutcomeService': semanticOutcomeService,
        'OutcomeService': outcomeService,
        'SemanticInterventionService': semanticInterventionService,
        'InterventionService': interventionService
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

    it("should make an update call when an outcome is added", function() {
      var newOutcome = {name: "name", motivation: "motivation", semanticOutcome: "semantics"};
      var newOutcomeWithProjectId = {name: "name", motivation: "motivation", semanticOutcome: "semantics", projectId: 1};
      scope.addOutcome(newOutcome);
      expect(scope.createOutcomeModal.close).toHaveBeenCalled();
      expect(outcomeService.save).toHaveBeenCalledWith(newOutcomeWithProjectId, jasmine.any(Function));
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

    it("isOwnProject should be true if the project is owned by the logged-in user", function() {
      deferred.resolve();
      scope.$apply();
      expect(scope.editMode.allowEditing).toBeTruthy();
    })

    it("isOwnProject should be false if the project is not owned by the logged-in user", function() {
      window.config.user.id = 2;
      deferred.resolve();
      scope.$apply();
      expect(scope.editMode.allowEditing).toBeFalsy();
    })

    it("should make an update call when an inervention is added", function() {
      var newIntervention = {name: "name", motivation: "motivation", semanticIntervention: "semantics"};
      var newInterventionWithProjectId = {name: "name", motivation: "motivation", semanticIntervention: "semantics", projectId: 1};
      scope.addIntervention(newIntervention);
      expect(scope.createInterventionModal.close).toHaveBeenCalled();
      expect(interventionService.save).toHaveBeenCalledWith(newInterventionWithProjectId, jasmine.any(Function));
    });

    it("should place the possible semanticInterventions on the scope on resolution", function() {
      deferred.resolve();
      scope.$apply();
      expect(semanticInterventionService.query).toHaveBeenCalledWith({id: mockProject.trialverseId});
      expect(scope.semanticInterventions).toEqual(mockSemanticInterventions);
    });



  });
});
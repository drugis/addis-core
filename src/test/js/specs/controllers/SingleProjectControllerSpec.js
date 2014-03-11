define(['angular', 'angular-mocks', 'underscore'], function () {
  describe('SingleProjectController', function () {
    beforeEach(module('addis.controllers'));
    var scope, deferred, window,
    projectsService, trialverseService, semanticOutcomeService, semanticInterventionsService,
    outcomeService, interventionService,
     mockSemanticOutcomes,
     mockProject = {id: 1, owner: {id: 1}, name: 'projectName',
       description: 'testDescription', namespace: 'testNamespace',trialverseId: 1, $save: function(){}},
     mockTrialverse = {id: 1, name: 'trialverseName', description: 'trialverseDescription'},
     mockOutcomes = [1, 2, 3],
     mockInterventions = [4, 5, 6];

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
      outcomeService = jasmine.createSpyObj('outcomeService', ['query', 'save']);
      outcomeService.query.andReturn(mockOutcomes);
      semanticInterventionService = jasmine.createSpyObj('semanticInterventionService', ['query']);
      semanticInterventionService.query.andReturn(mockSemanticInterventions);
      interventionService = jasmine.createSpyObj('interventionService', ['query', 'save']);
      interventionService.query.andReturn(mockInterventions);
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

    it('should place the outcome and intervention information on the scope once the project has been loaded', function() {
      deferred.resolve();
      scope.$apply();
      expect(scope.outcomes).toEqual(mockOutcomes);
      expect(scope.interventions).toEqual(mockInterventions);
      expect(scope.loading.loaded).toBeTruthy();
    });

    it('should tell the scope whether the resource is loaded', function() {
      expect(scope.loading.loaded).toBeFalsy();
      deferred.resolve();
      scope.$apply();
      expect(scope.loading.loaded).toBeTruthy();
    });

    it("should make an update call when an outcome is added", function() {
      var newOutcome = {name: "name", motivation: "motivation", semanticOutcome: "semantics"};
      var newOutcomeWithProjectId = _.extend({projectId: 1}, newOutcome);
      scope.model = newOutcome;
      scope.addOutcome(newOutcome);
      expect(scope.createOutcomeModal.close).toHaveBeenCalled();
      expect(outcomeService.save).toHaveBeenCalledWith(newOutcomeWithProjectId, jasmine.any(Function));
      expect(scope.model).toEqual({});
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
      var newInterventionWithProjectId = _.extend(newIntervention, {projectId: 1});
      scope.model = newIntervention
      scope.addIntervention(newIntervention);
      expect(scope.createInterventionModal.close).toHaveBeenCalled();
      expect(interventionService.save).toHaveBeenCalledWith(newInterventionWithProjectId, jasmine.any(Function));
      expect(scope.model).toEqual({});
    });

    it("should place the possible semanticInterventions on the scope on resolution", function() {
      deferred.resolve();
      scope.$apply();
      expect(semanticInterventionService.query).toHaveBeenCalledWith({id: mockProject.trialverseId});
      expect(scope.semanticInterventions).toEqual(mockSemanticInterventions);
    });



  });
});
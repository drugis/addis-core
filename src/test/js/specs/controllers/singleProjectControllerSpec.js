'use strict';
define(['angular-mocks', 'angular'], function(angularMocks, angular) {
  describe('the SingleProjectController', function() {
    var
      covariateOptionsResource = jasmine.createSpyObj('CovariateOptionsResource', ['query', 'getProjectCovariates']),
      covariateResource = jasmine.createSpyObj('CovariateResource', ['query']),
      interventionService = jasmine.createSpyObj('InterventionService', ['generateDescriptionLabel']),
      projectServiceMock = jasmine.createSpyObj('ProjectService', ['buildCovariateUsage']),
      analysisResourceMock = jasmine.createSpyObj('analysisResource', ['query', 'save']),
      userServiceMock = jasmine.createSpyObj('UserService', ['isLoginUserId']),
      covariateOptions = [{
        key: 'COV_OPTION_KEY',
        label: 'covariate option label'
      }],
      covariateOptionsWithPopchars = [{
        key: 'COV_OPTION_KEY',
        label: 'covariate option label'
      }, {
        key: 'popchar-key',
        label: 'covariate from popchar label'
      }],
      covariates = [{
        id: 1,
        definitionKey: 'COV_OPTION_KEY'
      }, {
        id: 2,
        definitionKey: 'popchar-key'
      }],
      covariateOptionsDeferred, covariateOptionsWithPopcharsDeferred, covariatesDeferred,
      mockAnalysesResult = {},
      mockAnalyses = [{}],
      analysisTypes = [{
        label: 'type1',
        stateName: 'stateName1'
      }, {
        label: 'type2',
        stateName: 'stateName2'
      }],
      userId = 568,
      stateParams = {
        userUid: userId,
        param: 1
      },
      scope, state,
      projectDeferred, analysisDeferred, studiesDeferred, interventionsDeferred,
      projectResource, trialverseResource, semanticOutcomeResource, semanticInterventionResource,
      outcomeResource, interventionResource, trialverseStudyResource,
      mockSemanticOutcomes, mockSemanticInterventions, reportResource, reportDeferred,
      mockProject = {
        id: 1,
        owner: {
          id: 1
        },
        name: 'projectName',
        description: 'testDescription',
        namespace: 'testNamespace',
        namespaceUid: 'aa2a-20a9g-205968',
        $save: function() {}
      },
      mockTrialverse = {
        id: 1,
        name: 'trialverseName',
        description: 'trialverseDescription'
      },
      mockOutcomes = [1, 2, 3],
      mockOutcome,
      outcomeDeferred,
      mockInterventions = [{
        name: 'a',
        val: 4
      }, {
        name: 'b',
        val: 5
      }, {
        name: 'c',
        val: 6
      }],
      mockIntervention,
      interventionDeferred,
      mockAnalysis = {
        projectId: 1,
        id: 2,
        analysisType: analysisTypes[0].label
      },
      mockStudy = {
        id: 5,
        name: 'testName'
      },
      mockStudies = [mockStudy],
      mockReport = {
        data: 'default report text.'
      };
    beforeEach(angularMocks.module('addis.controllers'));

    beforeEach(inject(function($controller, $q, $rootScope) {
      covariateOptionsDeferred = $q.defer();
      covariateOptions.$promise = covariateOptionsDeferred.promise;
      covariateOptionsDeferred.resolve(covariateOptions);
      covariateOptionsWithPopcharsDeferred = $q.defer();
      covariateOptionsWithPopchars.$promise = covariateOptionsWithPopcharsDeferred.promise;
      covariateOptionsWithPopcharsDeferred.resolve(covariateOptionsWithPopchars);
      covariateOptionsResource.query.and.returnValue(covariateOptions);
      covariateOptionsResource.getProjectCovariates.and.returnValue(covariateOptionsWithPopchars);
      covariatesDeferred = $q.defer();
      covariatesDeferred.resolve(covariates);
      covariates.$promise = covariatesDeferred.promise;
      covariateResource.query.and.returnValue(covariates);
      projectServiceMock.buildCovariateUsage.and.returnValue({});
      var analysesDefer = $q.defer();
      mockAnalysesResult.$promise = analysesDefer.promise;
      analysesDefer.resolve(mockAnalyses);
      analysisResourceMock.query.and.returnValue(mockAnalysesResult);
      mockSemanticOutcomes = ['a', 'b', 'c'];
      mockSemanticInterventions = ['e', 'f', 'g'];
      projectResource = jasmine.createSpyObj('projectResource', ['get', 'save', 'query']);
      projectResource.get.and.returnValue(mockProject);
      trialverseResource = jasmine.createSpyObj('trialverseResource', ['get']);
      trialverseResource.get.and.returnValue(mockTrialverse);
      semanticOutcomeResource = jasmine.createSpyObj('semanticOutcomeResource', ['query']);
      semanticOutcomeResource.query.and.returnValue(mockSemanticOutcomes);
      outcomeResource = jasmine.createSpyObj('outcomeResource', ['query', 'save']);
      outcomeResource.query.and.returnValue(mockOutcomes);
      semanticInterventionResource = jasmine.createSpyObj('semanticInterventionResource', ['query']);
      semanticInterventionResource.query.and.returnValue(mockSemanticInterventions);

      interventionsDeferred = $q.defer();
      mockInterventions.$promise = interventionsDeferred.promise;
      interventionResource = jasmine.createSpyObj('interventionResource', ['query', 'save']);
      interventionResource.query.and.returnValue(mockInterventions);

      interventionService.generateDescriptionLabel.and.returnValue('desc label');

      analysisResourceMock.save.and.returnValue(mockAnalysis);

      trialverseStudyResource = jasmine.createSpyObj('trialverseStudyResource', ['query']);
      trialverseStudyResource.query.and.returnValue(mockStudies);

      scope = $rootScope;
      scope.createOutcomeModal = jasmine.createSpyObj('createOutcomeModal', ['close']);
      scope.createInterventionModal = jasmine.createSpyObj('createInterventionModal', ['close']);
      scope.createAnalysisModal = jasmine.createSpyObj('createAnalysisModal', ['close']);

      projectDeferred = $q.defer();
      mockProject.$promise = projectDeferred.promise;
      analysisDeferred = $q.defer();
      mockAnalysis.$promise = analysisDeferred.promise;
      studiesDeferred = $q.defer();
      mockStudies.$promise = studiesDeferred.promise;
      outcomeDeferred = $q.defer();
      mockOutcome = {
        $promise: outcomeDeferred.promise
      };
      outcomeResource.save.and.returnValue(mockOutcome);
      interventionDeferred = $q.defer();
      mockIntervention = {
        $promise: interventionDeferred.promise
      };
      interventionResource.save.and.returnValue(mockIntervention);

      reportResource = jasmine.createSpyObj('reportResource', ['get']);
      reportResource.get.and.returnValue(mockReport);
      reportDeferred = $q.defer();
      mockReport.$promise = reportDeferred.promise;

      state = jasmine.createSpyObj('state', ['go']);

      $controller('SingleProjectController', {
        $scope: scope,
        $q: $q,
        $state: state,
        $stateParams: stateParams,
        $location: location,
        $modal: {
          open: function() {}
        },
        ProjectResource: projectResource,
        ProjectService: projectServiceMock,
        TrialverseResource: trialverseResource,
        TrialverseStudyResource: trialverseStudyResource,
        SemanticOutcomeResource: semanticOutcomeResource,
        OutcomeResource: outcomeResource,
        SemanticInterventionResource: semanticInterventionResource,
        InterventionResource: interventionResource,
        CovariateOptionsResource: covariateOptionsResource,
        CovariateResource: covariateResource,
        AnalysisResource: analysisResourceMock,
        ANALYSIS_TYPES: analysisTypes,
        InterventionService: interventionService,
        activeTab: 'details',
        UserService: userServiceMock,
        ReportResource: reportResource
      });
    }));
    describe('when first initialised', function() {
      it('should set loading to false', function() {
        expect(scope.loading.loaded).toBeFalsy();
      });

      it('should place project information on the scope', function() {
        expect(projectResource.get).toHaveBeenCalledWith(stateParams);
        expect(scope.project).toEqual(mockProject);
      });

      it('should not initially allow editing', function() {
        expect(scope.editMode.allowEditing).toBeFalsy();
      });
    });

    describe('after loading the project', function() {
      it('should place the outcome and intervention information on the scope', function() {
        projectDeferred.resolve();
        studiesDeferred.resolve();
        interventionsDeferred.resolve(mockInterventions);
        scope.$apply();
        expect(scope.outcomes).toEqual(mockOutcomes);
        var expextedInterventions = angular.copy(mockInterventions);
        delete expextedInterventions.$promise;
        expect(scope.interventions).toEqual(expextedInterventions);
        expect(scope.analyses).toEqual(mockAnalyses);
        expect(scope.loading.loaded).toBeTruthy();
      });

      it('should tell the scope whether the resource is loaded', function() {
        expect(scope.loading.loaded).toBeFalsy();
        projectDeferred.resolve();
        scope.$apply();
        expect(scope.loading.loaded).toBeTruthy();
      });

      it('should place the associated trialverse information on the scope', function() {
        projectDeferred.resolve();
        scope.$apply();
        expect(trialverseResource.get).toHaveBeenCalledWith({
          namespaceUid: mockProject.namespaceUid,
          version: mockProject.datasetVersion
        });
        expect(scope.trialverse).toEqual(mockTrialverse);
      });

      it('should place the possible semanticOutcomes on the scope on resolution', function() {
        projectDeferred.resolve();
        scope.$apply();
        expect(semanticOutcomeResource.query).toHaveBeenCalledWith({
          namespaceUid: mockProject.namespaceUid,
          version: mockProject.datasetVersion
        });
        expect(scope.semanticOutcomes).toEqual(mockSemanticOutcomes);
      });

      it('isOwnProject should be true if the project is owned by the logged-in user', function() {
        userServiceMock.isLoginUserId.and.returnValue(true);
        projectDeferred.resolve();
        scope.$apply();
        expect(scope.editMode.allowEditing).toBeTruthy();
      });

      it('isOwnProject should be false if the project is not owned by the logged-in user', function() {
        userServiceMock.isLoginUserId.and.returnValue(false);
        projectDeferred.resolve();
        scope.$apply();
        expect(scope.editMode.allowEditing).toBeFalsy();
      });

      it('should place the possible semanticInterventions on the scope on resolution', function() {
        projectDeferred.resolve();
        scope.$apply();
        expect(semanticInterventionResource.query).toHaveBeenCalledWith({
          namespaceUid: mockProject.namespaceUid,
          version: mockProject.datasetVersion
        });
        expect(scope.semanticInterventions).toEqual(mockSemanticInterventions);
      });

      it('should add the covariate options label to the covariates and place them on the scope', function() {
        projectDeferred.resolve();
        studiesDeferred.resolve();
        scope.$apply();
        expect(scope.covariates).toBeDefined();
        expect(scope.covariates[0]).toEqual({
          id: 1,
          definitionKey: 'COV_OPTION_KEY',
          definitionLabel: 'covariate option label'
        });
      });
      it('should place the report text on the scope', function() {
        projectDeferred.resolve();
        reportDeferred.resolve();
        scope.$apply();
        expect(scope.reportText.data).toEqual('default report text.');
      });
    });
  });
});

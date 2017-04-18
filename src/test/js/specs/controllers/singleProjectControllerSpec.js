'use strict';
define(['angular-mocks', 'angular', 'lodash'], function(angularMocks, angular, _) {
  describe('the SingleProjectController', function() {
    var
      covariateOptionsResource = jasmine.createSpyObj('CovariateOptionsResource', ['query', 'getProjectCovariates']),
      covariateResource = jasmine.createSpyObj('CovariateResource', ['query']),
      interventionService = jasmine.createSpyObj('InterventionService', ['generateDescriptionLabel']),
      projectServiceMock = jasmine.createSpyObj('ProjectService', ['buildCovariateUsage', 'buildOutcomeUsage', 'buildInterventionUsage', 'addMissingMultiplierInfo']),
      analysisResourceMock = jasmine.createSpyObj('analysisResource', ['query', 'save']),
      userServiceMock = jasmine.createSpyObj('UserService', ['isLoginUserId', 'hasLoggedInUser']),
      historyResourceMock = jasmine.createSpyObj('historyResource', ['get']),
      projectResource = jasmine.createSpyObj('projectResource', ['get', 'save', 'query']),
      semanticOutcomeResource = jasmine.createSpyObj('semanticOutcomeResource', ['query']),
      trialverseResource = jasmine.createSpyObj('trialverseResource', ['get']),
      outcomeResource = jasmine.createSpyObj('outcomeResource', ['query', 'save']),
      semanticInterventionResource = jasmine.createSpyObj('semanticInterventionResource', ['query']),
      interventionResource = jasmine.createSpyObj('interventionResource', ['query', 'save']),
      trialverseStudyResource = jasmine.createSpyObj('trialverseStudyResource', ['query']),
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
      mockSemanticOutcomes, mockSemanticInterventions, reportResource, reportDeferred,
      trialverseDeferred, analysesDeferred,
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
      outcomeResult,
      mockOutcomes = [1, 2, 3],
      mockOutcome,
      outcomeDeferred,
      interventionQueryResult,
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

      analysesDeferred = $q.defer();
      mockAnalysesResult.$promise = analysesDeferred.promise;
      analysesDeferred.resolve(mockAnalyses);
      analysisResourceMock.query.and.returnValue(mockAnalysesResult);

      mockSemanticOutcomes = ['a', 'b', 'c'];
      mockSemanticInterventions = ['e', 'f', 'g'];

      interventionsDeferred = $q.defer();
      var outcomeResultDeferred = $q.defer();
      outcomeResult = {
        $promise: outcomeResultDeferred.promise
      };
      outcomeResultDeferred.resolve(mockOutcomes);

      interventionQueryResult = _.extend({}, mockInterventions, {
        $promise: interventionsDeferred.promise
      });
      interventionResource.query.and.returnValue(interventionQueryResult);
      outcomeResource.query.and.returnValue(outcomeResult);
      semanticInterventionResource.query.and.returnValue(mockSemanticInterventions);
      interventionService.generateDescriptionLabel.and.returnValue('desc label');
      analysisResourceMock.save.and.returnValue(mockAnalysis);
      trialverseStudyResource.query.and.returnValue(mockStudies);
      semanticOutcomeResource.query.and.returnValue(mockSemanticOutcomes);
      projectServiceMock.buildCovariateUsage.and.returnValue({});
      projectServiceMock.addMissingMultiplierInfo.and.returnValue(mockInterventions);
      projectResource.get.and.returnValue(mockProject);
      trialverseResource.get.and.returnValue(mockTrialverse);

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
      trialverseDeferred = $q.defer();
      mockTrialverse.$promise = trialverseDeferred.promise;

      interventionResource.save.and.returnValue(mockIntervention);

      reportResource = jasmine.createSpyObj('reportResource', ['get']);
      reportResource.get.and.returnValue(mockReport);
      reportDeferred = $q.defer();
      mockReport.$promise = reportDeferred.promise;

      state = jasmine.createSpyObj('state', ['go']);
      userServiceMock.hasLoggedInUser.and.returnValue(true);


      var mockHistory = {
        creator: 'Jan',
        uri: 'http://computer:1234/versions/bla'
      };
      historyResourceMock.get.and.returnValue(mockHistory);

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
        ReportResource: reportResource,
        HistoryResource: historyResourceMock,
        project: mockProject
      });
    }));
    describe('after loading the project', function() {
      it('should place the outcome and intervention information on the scope', function() {
        projectDeferred.resolve();
        studiesDeferred.resolve();
        interventionsDeferred.resolve(mockInterventions);
        scope.$apply();
        expect(scope.outcomes).toEqual(mockOutcomes);
        expect(scope.interventions).toEqual(mockInterventions);
        expect(scope.analyses).toEqual(mockAnalyses);
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
        expect(scope.editMode.allowEditing).toEqual(userServiceMock.isLoginUserId());
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
      it('should add version information on the scope', function() {
        projectDeferred.resolve();
        trialverseDeferred.resolve({
          version: '/versions/dataserVerion'
        });
        scope.$apply();
        expect(scope.currentRevision).toEqual({
          creator: 'Jan',
          uri: 'http://computer:1234/versions/bla'
        });
      });
    });
  });
});

'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('the SingleProjectController', function() {
    var
      covariateOptionsResource = jasmine.createSpyObj('CovariateOptionsResource', ['query', 'getProjectCovariates']),
      interventionService = jasmine.createSpyObj('InterventionService', ['generateDescriptionLabel']),
      projectServiceMock = jasmine.createSpyObj('ProjectService', ['buildCovariateUsage', 'buildOutcomeUsage', 'buildInterventionUsage', 'addMissingMultiplierInfo']),
      analysisResourceMock = jasmine.createSpyObj('analysisResource', ['query', 'save']),
      userServiceMock = jasmine.createSpyObj('UserService', ['isLoginUserId', 'getLoginUser']),
      historyResourceMock = jasmine.createSpyObj('historyResource', ['get']),
      projectResource = jasmine.createSpyObj('projectResource', ['get', 'save', 'query']),
      semanticOutcomeResource = jasmine.createSpyObj('semanticOutcomeResource', ['query']),
      trialverseResource = jasmine.createSpyObj('trialverseResource', ['get']),
      semanticInterventionResource = jasmine.createSpyObj('semanticInterventionResource', ['query']),
      trialverseStudyResource = jasmine.createSpyObj('trialverseStudyResource', ['query']),
      dosageService = jasmine.createSpyObj('DosageService', ['get']),
      scaledUnitResource = jasmine.createSpyObj('ScaledUnitResource', ['query']),
      cacheServiceMock = jasmine.createSpyObj('CacheService', ['getOutcomes',
        'getInterventions', 'getCovariates', 'getAnalyses', 'getModelsByProject', 'evict']),
      pageTitleServiceMock = jasmine.createSpyObj('PageTitleService', ['setPageTitle']),
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
      trialverseDeferred, analysesDeferred, unitConceptsDeferred, scaledUnitsDeferred,
      mockProject = {
        id: 1,
        owner: {
          id: 1
        },
        name: 'projectName',
        description: 'testDescription',
        namespace: 'testNamespace',
        namespaceUid: 'aa2a-20a9g-205968',
        $save: function() { }
      },
      mockTrialverse = {
        id: 1,
        name: 'trialverseName',
        description: 'trialverseDescription'
      },
      mockOutcomes = [1, 2, 3],
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
      },
      mockScaledUnits = [{
        conceptUri: 'unitUri2',
        id: 1,
        multiplier: 1,
        name: 'liter',
        projectId: 1
      }],
      mockUnitConcepts = [{
        unitName: 'gram',
        unitUri: 'unitUri1'
      }, {
        unitName: 'liter',
        unitUri: 'unitUri2'
      }],
      userDefer;

    beforeEach(angular.mock.module('addis.controllers'));

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
      cacheServiceMock.getCovariates.and.returnValue(covariatesDeferred.promise);

      analysesDeferred = $q.defer();
      analysesDeferred.resolve(mockAnalyses);
      cacheServiceMock.getAnalyses.and.returnValue(analysesDeferred.promise);

      mockSemanticOutcomes = ['a', 'b', 'c'];
      mockSemanticInterventions = ['e', 'f', 'g'];

      interventionsDeferred = $q.defer();
      var outcomeResultDeferred = $q.defer();
      outcomeResultDeferred.resolve(mockOutcomes);

      cacheServiceMock.getInterventions.and.returnValue(interventionsDeferred.promise);
      cacheServiceMock.getOutcomes.and.returnValue(outcomeResultDeferred.promise);
      semanticInterventionResource.query.and.returnValue(mockSemanticInterventions);
      interventionService.generateDescriptionLabel.and.returnValue('desc label');
      analysisResourceMock.save.and.returnValue(mockAnalysis);
      trialverseStudyResource.query.and.returnValue(mockStudies);
      semanticOutcomeResource.query.and.returnValue(mockSemanticOutcomes);
      projectServiceMock.buildCovariateUsage.and.returnValue({});
      projectServiceMock.addMissingMultiplierInfo.and.returnValue(mockInterventions);
      projectResource.get.and.returnValue(mockProject);
      trialverseResource.get.and.returnValue(mockTrialverse);

      unitConceptsDeferred = $q.defer();
      mockUnitConcepts.$promise = unitConceptsDeferred.promise;
      dosageService.get.and.returnValue(mockUnitConcepts);

      scaledUnitsDeferred = $q.defer();
      mockScaledUnits.$promise = scaledUnitsDeferred.promise;
      scaledUnitResource.query.and.returnValue(mockScaledUnits);

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
      interventionDeferred = $q.defer();
      trialverseDeferred = $q.defer();
      mockTrialverse.$promise = trialverseDeferred.promise;

      reportResource = jasmine.createSpyObj('reportResource', ['get']);
      reportResource.get.and.returnValue(mockReport);
      reportDeferred = $q.defer();
      mockReport.$promise = reportDeferred.promise;

      state = jasmine.createSpyObj('state', ['go']);
      userDefer = $q.defer();
      userServiceMock.getLoginUser.and.returnValue(userDefer.promise);


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
          open: function() { }
        },
        ProjectResource: projectResource,
        ProjectService: projectServiceMock,
        TrialverseResource: trialverseResource,
        TrialverseStudyResource: trialverseStudyResource,
        SemanticOutcomeResource: semanticOutcomeResource,
        SemanticInterventionResource: semanticInterventionResource,
        CovariateOptionsResource: covariateOptionsResource,
        AnalysisResource: analysisResourceMock,
        ANALYSIS_TYPES: analysisTypes,
        InterventionService: interventionService,
        activeTab: 'details',
        UserService: userServiceMock,
        ReportResource: reportResource,
        HistoryResource: historyResourceMock,
        project: mockProject,
        DosageService: dosageService,
        ScaledUnitResource: scaledUnitResource,
        CacheService: cacheServiceMock,
        PageTitleService: pageTitleServiceMock
      });
    }));
    describe('after loading the project', function() {
      it('should place the outcome and intervention information, unitconcepts and units on the scope ', function() {
        projectDeferred.resolve();
        studiesDeferred.resolve();
        interventionsDeferred.resolve(mockInterventions);
        unitConceptsDeferred.resolve();
        scaledUnitsDeferred.resolve(mockScaledUnits);
        scope.$apply();
        expect(scope.outcomes).toEqual(mockOutcomes);
        expect(scope.interventions).toEqual(mockInterventions);
        expect(scope.analyses).toEqual(mockAnalyses);
        expect(scope.unitConcepts).toEqual(mockUnitConcepts);
        expect(scope.units).toEqual([{
          conceptName: 'liter',
          conceptUri: 'unitUri2',
          id: 1,
          multiplier: 1,
          name: 'liter',
          projectId: 1
        }]);
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

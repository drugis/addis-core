'use strict';
define(['angular-mocks'], function() {
  describe('the default report service', function() {
    var defaultReportService,
      analysisServiceMock = jasmine.createSpyObj('AnalysisService', ['getScaleName']),
      cacheServiceMock = jasmine.createSpyObj('CacheService', ['getProject',
        'getOutcomes',
        'getInterventions',
        'getCovariates',
        'getAnalyses',
        'getModelsByProject'
      ]);

    beforeEach(angular.mock.module('addis.project', function($provide) {
      $provide.value('CacheService', cacheServiceMock);
      $provide.value('AnalysisService', analysisServiceMock);
    }));

    beforeEach(inject(function(DefaultReportService) {
      defaultReportService = DefaultReportService;
    }));

    describe('generateDefaultReport', function() {

      describe('for the empty case', function() {
        var NO_OUTCOME_LIST = '###Outcomes\n*No outcomes defined*\n';
        var NO_INTERVENTIONS_LIST = '###Interventions\n*No interventions defined*\n';
        var NO_COVARIATES_LIST = '###Covariates\n*No covariates defined*\n';
        var NO_SYNTHESES_LIST = '###Evidence syntheses\n*No evidence syntheses defined*\n';
        var NO_BENEFIT_RISK_LIST = '###Benefit-risk analyses\n*No benefit-risk analyses defined*\n';
        var scope;

        beforeEach(inject(function($q, $rootScope) {
          scope = $rootScope;
          var projectDefer = $q.defer();
          projectDefer.resolve({});
          var outcomesDefer = $q.defer();
          outcomesDefer.resolve([]);
          var interventionsDefer = $q.defer();
          interventionsDefer.resolve([]);
          var covariatesDefer = $q.defer();
          covariatesDefer.resolve([]);
          var analysesDefer = $q.defer();
          analysesDefer.resolve([]);
          var modelsDefer = $q.defer();
          modelsDefer.resolve([]);

          cacheServiceMock.getProject.and.returnValue(projectDefer.promise);
          cacheServiceMock.getOutcomes.and.returnValue(outcomesDefer.promise);
          cacheServiceMock.getInterventions.and.returnValue(interventionsDefer.promise);
          cacheServiceMock.getCovariates.and.returnValue(covariatesDefer.promise);
          cacheServiceMock.getAnalyses.and.returnValue(analysesDefer.promise);
          cacheServiceMock.getModelsByProject.and.returnValue(modelsDefer.promise);
        }));

        it('should work for the empty case', function(done) {
          defaultReportService.generateDefaultReport().then(function(result) {
            var expectedResult = NO_OUTCOME_LIST + NO_INTERVENTIONS_LIST + NO_COVARIATES_LIST + NO_SYNTHESES_LIST + NO_BENEFIT_RISK_LIST;
            expect(result).toEqual(expectedResult);
            done();
          });
          scope.$apply();
        });
      });
      describe('for the filled-in case', function() {
        var scope;

        beforeEach(inject(function($q, $rootScope) {
          var outcomes = [{
            name: 'outcome 1'
          }, {
            name: 'outcome 2'
          }];
          var interventions = [{
            name: 'intervention1',
            id: 'interventionId1'
          }, {
            name: 'intervention2',
            id: 'interventionId2'
          }];
          var covariates = [{
            name: 'covariate1'
          }, {
            name: 'covariate2'
          }];
          var analyses = [{
            analysisType: 'Evidence synthesis',
            id: 3,
            outcome: {
              name: 'Headache'
            },
            title: 'nma',
            primaryModel: 102,
            interventionInclusions: [{
              interventionId: 'interventionId1'
            }]
          }];
          var project = {
            id: 2,
            owner: {
              id: 1
            }
          };
          var models = [{
            linearModel: {},
            id: 102,
            analysisId: 3,
            modelType: {
              type: 'network'
            },
            title: 'model1'
          }, {
            linearModel: {},
            id: 104,
            analysisId: 3,
            modelType: {
              type: 'network'
            },
            title: 'model2'
          }];
          scope = $rootScope;
          var projectDefer = $q.defer();
          projectDefer.resolve(project);
          var outcomesDefer = $q.defer();
          outcomesDefer.resolve(outcomes);
          var interventionsDefer = $q.defer();
          interventionsDefer.resolve(interventions);
          var covariatesDefer = $q.defer();
          covariatesDefer.resolve(covariates);
          var analysesDefer = $q.defer();
          analysesDefer.resolve(analyses);
          var modelsDefer = $q.defer();
          modelsDefer.resolve(models);

          cacheServiceMock.getProject.and.returnValue(projectDefer.promise);
          cacheServiceMock.getOutcomes.and.returnValue(outcomesDefer.promise);
          cacheServiceMock.getInterventions.and.returnValue(interventionsDefer.promise);
          cacheServiceMock.getCovariates.and.returnValue(covariatesDefer.promise);
          cacheServiceMock.getAnalyses.and.returnValue(analysesDefer.promise);
          cacheServiceMock.getModelsByProject.and.returnValue(modelsDefer.promise);
          analysisServiceMock.getScaleName.and.returnValue('odds ratio');
        }));
        it('should work for the complete case', function(done) {
          defaultReportService.generateDefaultReport().then(function(result) {
            var expectedResult = '###Outcomes\n - outcome 1\n - outcome 2\n\n' +
              '###Interventions\n - intervention1\n - intervention2\n\n' +
              '###Covariates\n - covariate1\n - covariate2\n\n' +
              '###Evidence syntheses\n\n####Analysis: nma\n' +
              '[Details](#/users/1/projects/2/nma/3)  \n' +
              '**Outcome**: Headache  \n' +
              '[[[network-plot analysis-id="3"]]]\n' +
              '**Primary model**: [model1](#/users/1/projects/2/nma/3/models/102)  \n' +
              '**model settings**: random effects evidence synthesis on the odds ratio scale.  \n' +
              '[[[relative-effects-plot analysis-id="3" model-id="102" baseline-treatment-id="interventionId1"]]]\n' +
              '**Secondary models**\n' +
              ' - [model2](#/users/1/projects/2/nma/3/models/104)\n\n' +
              '###Benefit-risk analyses\n' +
              '*No benefit-risk analyses defined*\n';
            expect(result).toEqual(expectedResult);
            done();
          });
          scope.$apply();
        });
      });
    });
  });
});

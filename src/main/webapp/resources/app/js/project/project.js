'use strict';

define([
  './report/reportResource',
  '../resources/analysisResource',
  './createProjectModalController',
  './editProjectController',
  './report/editReportController',
  './deleteDefinitionController',
  './updateProjectController',
  './copyProjectController',
  './report/insertDirectiveController',
  './repairInterventionController',
  './addScaledUnitController',
  './nmaReportViewDirective',
  './ssbrReportViewDirective',
  './report/markdownReportDirective',
  './report/comparisonResult/comparisonResultDirective',
  './report/relativeEffectsTable/relativeEffectsTableDirective',
  './report/relativeEffectsPlot/relativeEffectsPlotDirective',
  './report/rankProbabilitiesTable/rankProbabilitiesTableDirective',
  './report/rankProbabilitiesPlot/rankProbabilitiesPlotDirective',
  './report/forestPlot/forestPlotDirective',
  './report/treatmentEffects/treatmentEffectsDirective',
  './projectService',
  './report/reportDirectiveService',
  './report/defaultReportService',
  './report/cacheService',
  'angular',
  'angular-resource'
],
  function(
    ReportResource,
    AnalysisResource,
    CreateProjectModalController,
    EditProjectController,
    EditReportController,
    DeleteDefinitionController,
    UpdateProjectController,
    CopyProjectController,
    InsertDirectiveController,
    RepairInterventionController,
    AddScaledUnitController,
    nmaReportView,
    ssbrReportView,
    markdownReport,
    comparisonResult,
    relativeEffectsTable,
    relativeEffectsPlot,
    rankProbabilitiesTable,
    rankProbabilitiesPlot,
    forestPlot,
    treatmentEffects,
    ProjectService,
    ReportDirectiveService,
    DefaultReportService,
    CacheService,
    angular
  ) {
    return angular.module('addis.project', ['ngResource'])
      // resources
      .factory('ReportResource', ReportResource)
      .factory('AnalysisResource', AnalysisResource)

      // controllers
      .controller('CreateProjectModalController', CreateProjectModalController)
      .controller('EditProjectController', EditProjectController)
      .controller('EditReportController', EditReportController)
      .controller('DeleteDefinitionController', DeleteDefinitionController)
      .controller('UpdateProjectController', UpdateProjectController)
      .controller('CopyProjectController', CopyProjectController)
      .controller('InsertDirectiveController', InsertDirectiveController)
      .controller('RepairInterventionController', RepairInterventionController)
      .controller('AddScaledUnitController', AddScaledUnitController)

      //directives
      .directive('nmaReportView', nmaReportView)
      .directive('ssbrReportView', ssbrReportView)
      .directive('markdownReport', markdownReport)
      .directive('comparisonResult', comparisonResult)
      .directive('relativeEffectsTable', relativeEffectsTable)
      .directive('relativeEffectsPlot', relativeEffectsPlot)
      .directive('rankProbabilitiesTable', rankProbabilitiesTable)
      .directive('rankProbabilitiesPlot', rankProbabilitiesPlot)
      .directive('forestPlot', forestPlot)
      .directive('treatmentEffects', treatmentEffects)

      //services
      .service('ProjectService', ProjectService)
      .service('ReportDirectiveService', ReportDirectiveService)
      .service('DefaultReportService', DefaultReportService)
      .service('CacheService', CacheService);
  }
);

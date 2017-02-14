'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('addis.project', ['ngResource'])
    // resources
    .factory('ReportResource', require('project/report/reportResource'))
    .factory('AnalysisResource', require('resources/analysisResource'))
    .factory('ProblemResource', require('bower_components/gemtc-web/app/js/analyses/problemResource'))

    // controllers
    .controller('CreateProjectModalController', require('project/createProjectModalController'))
    .controller('EditProjectController', require('project/editProjectController'))
    .controller('EditReportController', require('project/report/editReportController'))
    .controller('DeleteDefinitionController',require('project/deleteDefinitionController'))
    .controller('UpdateProjectController', require('project/updateProjectController'))
    .controller('CopyProjectController', require('project/copyProjectController'))
    .controller('InsertNetworkGraphController', require('project/report/networkGraph/insertNetworkGraphController'))
    .controller('InsertComparisonResultController', require('project/report/comparisonResult/insertComparisonResultController'))
    .controller('InsertRelativeEffectsPlotController', require('project/report/relativeEffectsPlot/insertRelativeEffectsPlotController'))
    .controller('InsertRelativeEffectsTableController', require('project/report/relativeEffectsTable/insertRelativeEffectsTableController'))
    .controller('InsertRankProbabilitiesTableController', require('project/report/rankProbabilitiesTable/insertRankProbabilitiesTableController'))
.controller('InsertRankProbabilitiesPlotController', require('project/report/rankProbabilitiesPlot/insertRankProbabilitiesPlotController'))
    
    //directives
    .directive('nmaReportView', require('project/nmaReportViewDirective'))
    .directive('ssbrReportView', require('project/ssbrReportViewDirective'))
    .directive('markdownReport', require('project/report/markdownReportDirective'))
    .directive('comparisonResult', require('project/report/comparisonResult/comparisonResultDirective'))
    .directive('relativeEffectsTable', require('project/report/relativeEffectsTable/relativeEffectsTableDirective'))
    .directive('relativeEffectsPlot', require('project/report/relativeEffectsPlot/relativeEffectsPlotDirective'))
    .directive('rankProbabilitiesTable', require('project/report/rankProbabilitiesTable/rankProbabilitiesTableDirective'))
    .directive('rankProbabilitiesPlot', require('project/report/rankProbabilitiesPlot/rankProbabilitiesPlotDirective'))
    
    //services
    .service('ProjectService', require('project/projectService'))
    .service('ReportDirectiveService', require('project/report/reportDirectiveService'))
    .service('CacheService', require('project/report/cacheService'))
    ;
});

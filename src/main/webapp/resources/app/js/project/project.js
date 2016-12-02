'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('addis.project', ['ngResource'])
    // resources
    .factory('ReportResource', require('project/reportResource'))

    // controllers
    .controller('CreateProjectModalController', require('project/createProjectModalController'))
    .controller('EditProjectController', require('project/editProjectController'))
    .controller('EditReportController', require('project/editReportController'))
    .controller('InsertNetworkGraphController', require('project/insertNetworkGraphController'))
    .controller('InsertComparisonResultController', require('project/insertComparisonResultController'))

    //directives
    .directive('nmaReportView', require('project/nmaReportViewDirective'))
    .directive('ssbrReportView', require('project/ssbrReportViewDirective'))
    .directive('markdownReport', require('project/markdownReportDirective'))
    .directive('comparisonResult', require('project/comparisonResultDirective'))

    //services
    .service('ProjectService', require('project/projectService'))
    .service('ReportSubstitutionService', require('project/reportSubstitutionService'))
    ;
});

'use strict';
var requires = [
  'excelIO/excelImportService',
  'excelIO/excelExportService',
  'excelIO/excelExportUtilService'
];
define(requires.concat(['angular', 'angular-resource']), function(
  ExcelImportService,
  ExcelExportService,
  ExcelExportUtilService,
  angular
) {
  return angular.module('addis.excelIO', ['ngResource',
      'trialverse.util',
      'trialverse.study'
    ])
    //services
    .factory('ExcelImportService', ExcelImportService)
    .factory('ExcelExportService', ExcelExportService)
    .factory('ExcelExportUtilService', ExcelExportUtilService);
});
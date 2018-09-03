'use strict';
define([
  './excelImportService',
  './excelExportService',
  './excelExportUtilService',
  './excelIOUtilService',
  'angular',
  'angular-resource',
   '../study/study'],
  function(
    ExcelImportService,
    ExcelExportService,
    ExcelExportUtilService,
    ExcelIOUtilService,
    angular
  ) {
    return angular.module('addis.excelIO', [
      'ngResource',
      'trialverse.util',
      'trialverse.study'
    ])
      //services
      .factory('ExcelImportService', ExcelImportService)
      .factory('ExcelExportService', ExcelExportService)
      .factory('ExcelIOUtilService', ExcelIOUtilService)
      .factory('ExcelExportUtilService', ExcelExportUtilService);
  }
);

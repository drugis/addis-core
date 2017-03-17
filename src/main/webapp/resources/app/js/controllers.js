'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('addis.controllers', [])
    .controller('ProjectsController', require('controllers/projectsController'))
    .controller('SingleProjectController', require('controllers/singleProjectController'))
    .controller('SingleStudyBenefitRiskController', require('controllers/singleStudyBenefitRiskController'))
    .controller('NetworkMetaAnalysisContainerController', require('controllers/networkMetaAnalysisContainerController'))
    .controller('NetworkMetaAnalysisModelContainerController', require('controllers/networkMetaAnalysisModelContainerController'));
});

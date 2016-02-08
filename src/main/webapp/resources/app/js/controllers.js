'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('addis.controllers', [])
    .controller('ProjectsController', require('controllers/projectsController'))
    .controller('CreateProjectController', require('controllers/createProjectController'))
    .controller('SingleProjectController', require('controllers/singleProjectController'))
    .controller('SingleStudyBenefitRiskAnalysisController', require('controllers/singleStudyBenefitRiskAnalysisController'))
    .controller('NetworkMetaAnalysisContainerController', require('controllers/networkMetaAnalysisContainerController'))
    .controller('NetworkMetaAnalysisModelContainerController', require('controllers/networkMetaAnalysisModelContainerController'))
    .controller('StudyReadOnlyController', require('controllers/studyReadOnlyController'));
});

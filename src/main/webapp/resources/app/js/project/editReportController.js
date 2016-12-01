'use strict';
define(['angular', 'lodash', 'jQuery'],
  function(angular, _, $) {
    var dependencies = ['$scope', '$q', '$state', '$stateParams', '$location', '$modal',
      'ProjectResource',
      'ReportResource',
      '$timeout',
      'UserService',
      'OutcomeResource',
      'SemanticOutcomeResource',
      'SemanticInterventionResource',
      'InterventionResource',
      'CovariateOptionsResource',
      'CovariateResource',
      'AnalysisResource',
      'ANALYSIS_TYPES',
      'InterventionService',
      'TrialverseResource',
      'TrialverseStudyResource',
      '$filter'
    ];
    var EditReportcontroller = function($scope, $q, $state, $stateParams, location, modal,
      ProjectResource,
      ReportResource,
      $timeout,
      UserService,
      OutcomeResource,
      SemanticOutcomeResource,
      SemanticInterventionResource,
      InterventionResource,
      CovariateOptionsResource,
      CovariateResource,
      AnalysisResource,
      ANALYSIS_TYPES,
      InterventionService,
      TrialverseResource,
      TrialverseStudyResource,
      $filter) {

      $scope.project = ProjectResource.get($stateParams);
      $scope.reportText = '';
      $scope.showSaving = false;
      $scope.showSaved = false;
      $scope.userId = $stateParams.userUid;
      $scope.saveChanges = saveChanges;
      $scope.resetToDefault = resetToDefault;
      $scope.loadReport = loadReport;
      $scope.insertText = insertText;


      $q.all([$scope.project.$promise, ReportResource.get($stateParams).$promise]).then(function(values) {
        $scope.loading = {
          loaded: true
        };
        $scope.reportText = values[1].data;
        var isUserOwner = false;
        if (UserService.hasLoggedInUser()) {
          $scope.loginUserId = (UserService.getLoginUser()).id;
          isUserOwner = UserService.isLoginUserId($scope.project.owner.id);
        }
        $scope.editMode = {
          isUserOwner: isUserOwner,
          disableEditing: !isUserOwner
        };
        $scope.trialverse = TrialverseResource.get({
          namespaceUid: $scope.project.namespaceUid,
          version: $scope.project.datasetVersion
        });

        $scope.semanticOutcomes = SemanticOutcomeResource.query({
          namespaceUid: $scope.project.namespaceUid,
          version: $scope.project.datasetVersion
        });

        $scope.semanticInterventions = SemanticInterventionResource.query({
          namespaceUid: $scope.project.namespaceUid,
          version: $scope.project.datasetVersion
        });

        $scope.outcomes = OutcomeResource.query({
          projectId: $scope.project.id
        });

        InterventionResource.query({
          projectId: $scope.project.id
        }).$promise.then(generateInterventionDescriptions);
        loadCovariates();

        $scope.studies = TrialverseStudyResource.query({
          namespaceUid: $scope.project.namespaceUid,
          version: $scope.project.datasetVersion
        });
        $scope.studies.$promise.then(function() {
          $scope.analyses = AnalysisResource.query({
            projectId: $scope.project.id
          }, function(analyses) {
            $scope.analysesLoaded = true;
            $scope.analyses = _.sortBy(analyses, function(analysis) {
              if (analysis.analysisType === 'Evidence synthesis') {
                return 0;
              } else {
                return 1;
              }
            });
          });
        });
      });

      function insertTextAtCursor(reportText, text) {
        var input = $('#report-input');
        var cursorPos = input.prop('selectionStart');
        var textBefore = reportText.substring(0, cursorPos);
        var textAfter = reportText.substring(cursorPos);
        return textBefore + text + textAfter;
      }

      function insertText(reportText) {
        return insertTextAtCursor(reportText, '**its a sin**');
      }

      function saveChanges(reportText) {
        ReportResource.put($stateParams, reportText);
        $scope.showSaving = true;
        $timeout(function() {
          $scope.showSaving = false;
          $scope.showSaved = true;
        }, 1000);
        $timeout(function() {
          $scope.showSaved = false;
        }, 3000);
      }

      function resetToDefault() {
        ReportResource.delete($stateParams).$promise.then(function(value) {
          $scope.reportText = value.data;
        });
        return $scope.reportText;
      }

      function loadReport() {

        //Load outcome info 
        var resultString = '### Outcomes\n';
        if ($scope.outcomes.length === 0) {
          resultString = resultString + '**no outcomes defined**\n';
        } else {
          resultString = attachVar(resultString, $scope.outcomes);
        }

        //Load intervention info 
        resultString = resultString + '###Interventions\n';
        if ($scope.interventions.length === 0) {
          resultString = resultString + '**no interventions defined**\n';
        } else {
          resultString = attachVar(resultString, $scope.interventions);
        }

        //Load covariate info 
        resultString = resultString + '###Covariates\n';
        if ($scope.covariates.length === 0) {
          resultString = resultString + '**no covariates defined**\n';
        } else {
          resultString = attachVar(resultString, $scope.covariates);
        }

        //Load analysis info
        for (var i = 0; i < $scope.analyses.length; i++) {
          var currentAnalysis = $scope.analyses[i];
          resultString = resultString + '##Analysis: ' + currentAnalysis.title + '\n';
          if (!currentAnalysis.description) {
            resultString = resultString + '*no description*<br>\n';
          } else {
            resultString = resultString + currentAnalysis.description;
          }
          if (currentAnalysis.analysisType === 'Evidence synthesis') {
            resultString = attachNMAReport(resultString, currentAnalysis);
          } else if (currentAnalysis.analysisType === 'Benefit-risk analysis based on a single study' || currentAnalysis.analysisType === 'Benefit-risk analysis based on meta-analyses') {
            resultString = attachSSBRReport(resultString, currentAnalysis);
          }


        }
        return resultString;
      }

      function attachNMAReport(resultString, analysis) {
        var placeholderLink = 'https://www.google.com';

        resultString = resultString + '[View details]' + '(' + placeholderLink + ')<br>\n';
        resultString = resultString + '**Outcome:** ' + analysis.outcome.name + '<br>\n';
        resultString = resultString + 'plaatjecodemeuk\n'; //
        resultString = resultString + '###Primary model: ';
        var primaryModel;
        if (!primaryModel) {
          resultString = resultString + ' *not set*<br>\n';
        } else {
          resultString = resultString + primaryModel.title;
        }
        if (primaryModel) {
          resultString = resultString + '**model settings: ' + modelSettingsSummary(primaryModel) + '**<br>\n';
          resultString = resultString + '[View full model]' + '(' + placeholderLink + ')<br>\n';
        }
        resultString = resultString + 'plaatjecodemeuk x4\n'; //
        resultString = resultString + '###Secondary models\n';
        var otherModels = [];
        var showNoOtherUnarchived = true;
        if (otherModels.length < 1) {
          resultString = resultString + '*none*<br>\n';
        } else if (showNoOtherUnarchived) {
          resultString = resultString + '*all secondary models have been archived*<br>\n';
        }

        var models = []; //todo load the models 
        models = $filter('filter')(models, function(model) {
          return model.archived === false;
        });
        models = $filter('orderBy')(models, function(model) {
          return model.name;
        });
        for (var i = 0; i < models.length; i++) {
          resultString = resultString + '[' + models[i].title + '(' + placeholderLink + ')<br>\n';

        }
        return resultString;
      }

      function attachSSBRReport(resultString) {
        return resultString; //todo finish function
      }

      function modelSettingsSummary(model) {
        return '';
      }

      function attachVar(resultString, vars) {
        vars = $filter('orderBy')(vars, function(v) {
          return v.name;
        });
        for (var i = 0; i < vars.length; i++) {
          resultString = resultString + '- **' + vars[i].name + '**';
          if (vars.motivation) {
            resultString = resultString + '**' + vars.motivation + '**';
          }
          resultString = resultString + '\n';
        }
        return resultString;
      }

      function generateInterventionDescriptions(interventions) {
        $scope.interventions = interventions.map(function(intervention) {
          intervention.definitionLabel = InterventionService.generateDescriptionLabel(intervention, interventions);
          return intervention;
        });
        $scope.interventions = _.orderBy($scope.interventions, function(intervention) {
          return intervention.name.toLowerCase();
        });
      }

      function loadCovariates() {
        // we need to get the options in order to display the definition label, as only the definition key is stored on the covariate
        $q.all([CovariateOptionsResource.getProjectCovariates($stateParams).$promise,
          CovariateResource.query({
            projectId: $scope.project.id
          }).$promise
        ]).then(function(result) {
          var optionsMap = _.keyBy(result[0], 'key');
          $scope.covariates = result[1].map(function(covariate) {
            covariate.definitionLabel = optionsMap[covariate.definitionKey].label;
            return covariate;
          });
        });
      }


    };
    return dependencies.concat(EditReportcontroller);
  });

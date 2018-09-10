'use strict';
define(['lodash', 'angular'], function(_, angular) {
  var dependencies = [
    '$scope', '$q', '$state', '$stateParams', '$modal',
    'ProjectResource',
    'ProjectService',
    'TrialverseResource',
    'TrialverseStudyResource',
    'SemanticOutcomeResource',
    'SemanticInterventionResource',
    'CovariateOptionsResource',
    'AnalysisResource',
    'ANALYSIS_TYPES',
    'InterventionService',
    'activeTab',
    'UserService',
    'ReportResource',
    'HistoryResource',
    'DosageService',
    'ScaledUnitResource',
    'CacheService',
    'PageTitleService',
    'project'
  ];
  var SingleProjectController = function(
    $scope, $q, $state, $stateParams, $modal,
    ProjectResource,
    ProjectService,
    TrialverseResource,
    TrialverseStudyResource,
    SemanticOutcomeResource,
    SemanticInterventionResource,
    CovariateOptionsResource,
    AnalysisResource,
    ANALYSIS_TYPES,
    InterventionService,
    activeTab,
    UserService,
    ReportResource,
    HistoryResource,
    DosageService,
    ScaledUnitResource,
    CacheService,
    PageTitleService,
    project
  ) {
    // functions
    $scope.toggleShowArchived = toggleShowArchived;
    $scope.unarchiveAnalysis = unarchiveAnalysis;
    $scope.archiveAnalysis = archiveAnalysis;
    $scope.openRepairInterventionDialog = openRepairInterventionDialog;
    $scope.openCopyDialog = openCopyDialog;
    $scope.findStudyLabel = findStudyLabel;
    $scope.goToAnalysis = goToAnalysis;
    $scope.openEditProjectDialog = openEditProjectDialog;
    $scope.openAddAnalysisDialog = openAddAnalysisDialog;
    $scope.openCreateOutcomeDialog = openCreateOutcomeDialog;
    $scope.openCreateScaledUnitDialog = openCreateScaledUnitDialog;
    $scope.openCreateInterventionDialog = openCreateInterventionDialog;
    $scope.openEditOutcomeDialog = openEditOutcomeDialog;
    $scope.openEditInterventionDialog = openEditInterventionDialog;
    $scope.addCovariate = addCovariate;
    $scope.setActiveTab = setActiveTab;
    $scope.goToEditView = goToEditView;
    $scope.openDeleteDefinitionDialog = openDeleteDefinitionDialog;
    $scope.openUpdateDialog = openUpdateDialog;

    // init
    $scope.tabSelection = {
      activeTab: activeTab
    };

    $scope.showArchived = false;
    $scope.numberOfAnalysesArchived = 0;
    $scope.editMode = {
      allowEditing: false
    };
    $scope.duplicateOutcomeName = {
      isDuplicate: false
    };
    $scope.duplicateInterventionName = {
      isDuplicate: false
    };
    $scope.userId = $stateParams.userUid;

    $scope.project = project;
    setPageTitle(activeTab);
    $scope.projects = ProjectResource.query();

    $scope.editMode.allowEditing = !project.archived && UserService.isLoginUserId($scope.project.owner.id);
    UserService.getLoginUser().then(function(user) {
      $scope.editMode.allowCopying = !!user;
    });

    $scope.trialverse = TrialverseResource.get({
      namespaceUid: $scope.project.namespaceUid,
      version: $scope.project.datasetVersion
    });

    $scope.trialverse.$promise.then(function(dataset) {
      $scope.currentRevision = HistoryResource.get({
        userUid: $scope.userId,
        datasetUuid: $scope.project.namespaceUid,
        versionUuid: dataset.version.split('/versions/')[1]
      });
    });

    $scope.semanticOutcomes = SemanticOutcomeResource.query({
      namespaceUid: $scope.project.namespaceUid,
      version: $scope.project.datasetVersion
    });

    $scope.semanticInterventions = SemanticInterventionResource.query({
      namespaceUid: $scope.project.namespaceUid,
      version: $scope.project.datasetVersion
    });

    $scope.studies = TrialverseStudyResource.query({
      namespaceUid: $scope.project.namespaceUid,
      version: $scope.project.datasetVersion
    });

    loadAnalyses();

    $scope.reportText = ReportResource.get($stateParams);

    $scope.$on('scaledUnitsChanged', function() {
      loadUnits();
    });

    function reloadDefinitions() {
      $scope.loadingPromise = $q.all([loadCovariates(), loadUnits(), loadInterventions(), loadOutcomes()]);
    }

    function loadAnalyses() {
      $scope.analysesPromise = $scope.studies.$promise.then(function() {
        $scope.analyses = CacheService.getAnalyses({
          projectId: $scope.project.id
        }).then(function(analyses) {
          $scope.numberOfAnalysesArchived = _.reduce(analyses, function(accum, analysis) {
            return analysis.archived ? ++accum : accum;
          }, 0);
          if ($scope.numberOfAnalysesArchived === 0) {
            $scope.showArchived = false;
          }
          $scope.analyses = _(analyses).sortBy('title').sortBy(function(analysis) {
            if (analysis.analysisType === 'Evidence synthesis' && analysis.archived === false) {
              return 0;
            } else if (analysis.archived === false) {
              return 1;
            } else {
              return 2;
            }
          }).value();
          reloadDefinitions();
        });
      });
    }

    function loadUnits() {
      var unitsPromise = DosageService.get($stateParams.userUid, $scope.project.namespaceUid);
      var scaledUnitsPromise = ScaledUnitResource.query($stateParams).$promise;
      return $q.all([unitsPromise, scaledUnitsPromise]).then(function(results) {
        $scope.unitConcepts = results[0];
        var scaledUnits = results[1];

        var unitConcepts = _.keyBy($scope.unitConcepts, 'unitUri');
        $scope.units = _.map(scaledUnits, function(unit) {
          return _.extend({}, unit, {
            conceptName: unitConcepts[unit.conceptUri].unitName
          });
        });
      });
    }

    function loadInterventions() {
      return CacheService.getInterventions({
        projectId: $scope.project.id
      })
        .then(generateInterventionDescriptions)
        .then(checkUnitMultipliers)
        .then(function() {
          $scope.interventionUsage = ProjectService.buildInterventionUsage($scope.analyses, $scope.interventions);
        });
    }

    function loadOutcomes() {
      return CacheService.getOutcomes({
        projectId: $scope.project.id
      }).then(function(value) {
        $scope.outcomes = value;
        $scope.outcomeUsage = ProjectService.buildOutcomeUsage($scope.analyses, $scope.outcomes);
      });
    }

    function loadCovariates() {
      // we need to get the options in order to display the definition label, as only the definition key is stored on the covariate
      return $q.all([CovariateOptionsResource.getProjectCovariates($stateParams).$promise,
      CacheService.getCovariates({
        projectId: $scope.project.id
      })
      ]).then(function(result) {
        var optionsMap = _.keyBy(result[0], 'key');
        $scope.covariates = result[1].map(function(covariate) {
          covariate.definitionLabel = optionsMap[covariate.definitionKey].label;
          return covariate;
        });
      }).then(function() {
        $scope.covariateUsage = ProjectService.buildCovariateUsage($scope.analyses, $scope.covariates);
      });
    }

    function checkUnitMultipliers() {
      $scope.interventions = ProjectService.addMissingMultiplierInfo($scope.interventions);
      $scope.editMode.interventionRepairPossible = _.find($scope.interventions, 'hasMissingMultipliers');
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

    function findStudyLabel(analysis) {
      var study = _.find($scope.studies, function(study) {
        return 'http://trials.drugis.org/graphs/' + study.studyGraphUid === analysis.studyGraphUri;
      });
      if (study) {
        return study.name;
      }
    }

    function goToAnalysis(analysis) {
      var analysisType = angular.copy(_.find(ANALYSIS_TYPES, function(type) {
        return type.label === analysis.analysisType;
      }));

      if (analysisType.label === 'Benefit-risk analysis' && analysis.finalized) {
        analysisType.stateName = 'BenefitRisk';
      }

      $state.go(analysisType.stateName, {
        userUid: $scope.userId,
        projectId: $scope.project.id,
        analysisId: analysis.id
      });
    }

    function openEditProjectDialog() {
      $modal.open({
        templateUrl: '../project/editProject.html',
        controller: 'EditProjectController',
        resolve: {
          project: function() {
            return $scope.project;
          },
          otherProjectNames: function() {
            return $scope.projects.$promise.then(function(projects) {
              return _.reduce(projects, function(accum, project) {
                if ($scope.project.id !== project.id) {
                  accum.push(project.name);
                }
                return accum;
              }, []);
            });
          },
          callback: function() {
            return function(name, description) {
              $scope.project.name = name;
              $scope.project.description = description;
            };
          }
        }
      });
    }

    function openAddAnalysisDialog() {
      $modal.open({
        templateUrl: '../analysis/addAnalysis.html',
        scope: $scope,
        controller: 'AddAnalysisController'
      });
    }

    function openCreateOutcomeDialog() {
      $modal.open({
        templateUrl: '../outcome/addOutcome.html',
        scope: $scope,
        controller: 'AddOutcomeController',
        resolve: {
          callback: function() {
            return function() {
              CacheService.evict('outcomesPromises', $stateParams.projectId);
              loadOutcomes();
            };
          }
        }
      });
    }

    function openCreateScaledUnitDialog() {
      $modal.open({
        templateUrl: '../project/addScaledUnit.html',
        controller: 'AddScaledUnitController',
        resolve: {
          callback: function() {
            return loadUnits;
          },
          unitConcepts: function() {
            return $scope.unitConcepts;
          },
          scaledUnits: function() {
            return $scope.units;
          }
        }
      });
    }

    function openCreateInterventionDialog() {
      $modal.open({
        templateUrl: '../intervention/addIntervention.html',
        scope: $scope,
        controller: 'AddInterventionController',
        size: 'large',
        resolve: {
          callback: function() {
            return function() {
              CacheService.evict('interventionsPromises', $stateParams.projectId);
              loadInterventions();
            };
          }
        }
      });
    }

    function openEditOutcomeDialog(outcome) {
      $modal.open({
        templateUrl: '../outcome/editOutcome.html',
        controller: 'EditAddisOutcomeController',
        resolve: {
          outcome: function() {
            return outcome;
          },
          outcomes: function() {
            return $scope.outcomes;
          },
          usage: function() {
            return $scope.outcomeUsage[outcome.id];
          },
          successCallback: function() {
            return function() {
              CacheService.evict('outcomesPromises', $stateParams.projectId);
              loadOutcomes();
            };
          }
        }
      });
    }

    function openEditInterventionDialog(intervention) {
      $modal.open({
        templateUrl: '../intervention/editIntervention.html',
        controller: 'EditInterventionController',
        resolve: {
          intervention: function() {
            return intervention;
          },
          interventions: function() {
            return $scope.interventions;
          },
          successCallback: function() {
            return function() {
              CacheService.evict('interventionsPromises', $stateParams.projectId);
              loadInterventions();
            };
          }
        }
      });
    }

    function addCovariate() {
      $modal.open({
        templateUrl: '../covariates/addCovariate.html',
        scope: $scope,
        controller: 'AddCovariateController',
        resolve: {
          outcomes: function() {
            return $scope.outcomes;
          },
          callback: function() {
            return function() {
              CacheService.evict('covariatesPromises', $stateParams.projectId);
              loadCovariates();
            };
          }
        }
      });
    }

    function setActiveTab(tab) {
      switch (tab) {
        case $scope.tabSelection.activeTab:
          return;
        case 'report':
          $state.go('projectReport', $stateParams);
          break;
        case 'definitions':
          $state.go('project', $stateParams);
          break;
        case 'analyses':
          $state.go('projectAnalyses', $stateParams, {
            reload: true
          });
          break;
      }
      setPageTitle(tab);
    }

    function goToEditView() {
      $state.go('editReport', $stateParams);
    }

    function openDeleteDefinitionDialog(definition, type) {
      $modal.open({
        templateUrl: '../project/deleteDefinition.html',
        scope: $scope,
        controller: 'DeleteDefinitionController',
        resolve: {
          definition: function() {
            return _.assign({}, definition, {
              definitionType: type
            });
          },
          callback: function() {
            return function() {
              CacheService.evict('interventionsPromises', $stateParams.projectId);
              CacheService.evict('outcomesPromises', $stateParams.projectId);
              CacheService.evict('covariatesPromises', $stateParams.projectId);
              reloadDefinitions();
            };
          }
        }
      });
    }

    function openUpdateDialog() {
      $modal.open({
        templateUrl: '../project/updateProject.html',
        scope: $scope,
        controller: 'UpdateProjectController',
        resolve: {
          callback: function() {
            return function(newProjectId) {
              ProjectResource.setArchived({
                projectId: $scope.project.id
              }, {
                  isArchived: true
                });
              $state.go('project', {
                userUid: $stateParams.userUid,
                projectId: newProjectId
              });
            };
          }
        }
      });
    }

    function openCopyDialog() {
      $modal.open({
        templateUrl: '../project/copyProject.html',
        scope: $scope,
        controller: 'CopyProjectController',
        resolve: {
          callback: function() {
            return function(newProjectId) {
              $state.go('project', {
                userUid: UserService.getLoginUser().id,
                projectId: newProjectId
              });
            };
          }
        }
      });
    }

    function openRepairInterventionDialog(intervention) {
      $modal.open({
        templateUrl: '../project/repairIntervention.html',
        scope: $scope,
        controller: 'RepairInterventionController',
        resolve: {
          intervention: function() {
            return intervention;
          },
          callback: function() {
            return function() {
              CacheService.evict('interventionsPromises', $stateParams.projectId);
              loadInterventions();
            };
          }
        }
      });
    }

    function archiveAnalysis(analysis) {
      var params = {
        projectId: $scope.project.id,
        analysisId: analysis.id
      };
      AnalysisResource.setArchived(
        params, {
          isArchived: true
        }
      ).$promise.then(function() {
        CacheService.evict('analysisPromises', analysis.id);
        CacheService.evict('analysesPromises', $stateParams.projectId);
        loadAnalyses();
      });
    }


    function unarchiveAnalysis(analysis) {
      var params = {
        projectId: $scope.project.id,
        analysisId: analysis.id
      };
      AnalysisResource.setArchived(
        params, {
          isArchived: false
        }
      ).$promise.then(function() {
        CacheService.evict('analysisPromises', analysis.id);
        CacheService.evict('analysesPromises', $stateParams.projectId);
        loadAnalyses();
      });
    }

    function toggleShowArchived() {
      $scope.showArchived = !$scope.showArchived;
    }

    function setPageTitle(tab) {
      var title = $scope.project.name;
      switch (tab) {
        case 'definitions':
          title = title.concat('\'s definitions');
          break;
        case 'analyses':
          title = title.concat('\'s analyses');
          break;
        case 'report':
          title = title.concat('\'s report');
          break;
      }
      PageTitleService.setPageTitle('SingleProjectController', title);
    }

  };
  return dependencies.concat(SingleProjectController);
});

'use strict';
define([], function() {
  var dependencies = ['$scope', '$q', '$stateParams', 'AnalysisResource', 'InterventionResource', 'OutcomeResource'];
  var MetBenefitRiskStep1Controller = function($scope, $q, $stateParams, AnalysisResource, InterventionResource, OutcomeResource) {
    $scope.analysis = AnalysisResource.get($stateParams);
    $scope.alternatives = InterventionResource.query($stateParams);
    $scope.outcomes = OutcomeResource.query($stateParams);
    $scope.updateAlternatives = updateAlternatives;
    $scope.updateMbrOutcomeInclusions = updateMbrOutcomeInclusions;

    getOutcomesWithAnalyses();

    function getOutcomesWithAnalyses() {
      $q.all([$scope.analysis.$promise, $scope.outcomes.$promise]).then(function(result) {
        var analysis = result[0];
        var outcomes = result[1];
        var outcomeIds = outcomes.map(function(outcome) {
          return outcome.id;
        });
        AnalysisResource.query({
          projectId: $stateParams.projectId,
          outcomeIds: outcomeIds
        }).$promise.then(function(networkMetaAnalyses) {
          $scope.outcomesWithAnalyses = outcomes.map(function(outcome) {
            var outcomeAnalyses = networkMetaAnalyses.filter(function(nma) {
              return nma.outcome.id === outcome.id;
            });

            // set the radioBtn state based on the stored inclusions
            outcomeAnalyses.forEach(function(outcomeAnalysis) {
              var isInclusionSet = false;
              analysis.mbrOutcomeInclusions.forEach(function(outcomeInclusion) {
                if (outcomeInclusion.outcome.id === outcomeAnalysis.outcome.id) {
                  outcomeInclusion.isIncluded = true;
                  isInclusionSet =true;
                }
              });

              // if no analysis was isIncluded include the first as a defaults if there is one
              if(!isInclusionSet && outcomeAnalyses.length > 0) {
                outcomeAnalyses[0].isIncluded = true;
              }

            });
            return {
              outcome: outcome,
              networkMetaAnalyses: outcomeAnalyses
            };
          });
        });
      });
    }

    $q.all([$scope.analysis.$promise, $scope.alternatives.$promise]).then(function(result) {
      var analysis = result[0];
      var alternatives = result[1];

      $scope.alternatives = alternatives.map(function(alternative) {
        var isAlternativeInInclusions = analysis.includedAlternatives.find(function(includedAlternative) {
          return includedAlternative.id === alternative.id;
        });
        if (isAlternativeInInclusions) {
          alternative.isIncluded = true;
        }
        return alternative;
      });
    });

    $q.all([$scope.analysis.$promise, $scope.outcomes.$promise]).then(function(result) {
      var analysis = result[0];
      var outcomes = result[1];
      $scope.outcomes = outcomes.map(function(outcome) {
        var isOutcomeInInclusions = analysis.mbrOutcomeInclusions.find(function(mbrOutcomeInclusion) {
          return mbrOutcomeInclusion.outcome.id === outcome.id;
        });
        if (isOutcomeInInclusions) {
          outcome.isIncluded = true;
        }
        return outcome;
      });
    });

    function updateAlternatives() {
      $scope.analysis.includedAlternatives = $scope.alternatives.filter(function(alternative) {
        return alternative.isIncluded;
      });
      $scope.analysis.$save();
    }

    function updateMbrOutcomeInclusions() {
      $scope.analysis.mbrOutcomeInclusions = $scope.outcomesWithAnalyses.filter(function(outcomeWithAnalyses) {
        return outcomeWithAnalyses.outcome.isIncluded;
      }).map(function(outcomeWithAnalyses) {
        return {
          outcome: outcomeWithAnalyses.outcome,
          networkMetaAnalysis: outcomeWithAnalyses.networkMetaAnalyses.filter(function(networkAnalysis) {
            return networkAnalysis.isIncluded;
          })[0]
        };
      });
      $scope.analysis.$save();
    }


  };
  return dependencies.concat(MetBenefitRiskStep1Controller);
});

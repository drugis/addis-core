'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var BenefitRiskErrorService = function() {

    function isMissingDataType(outcomesWithAnalyses) {
      return _(outcomesWithAnalyses)
        .filter('outcome.isIncluded')
        .reject('dataType')
        .value()
        .length;
    }

    function isMissingAnalysis(outcomesWithAnalyses) {
      return _.find(outcomesWithAnalyses, function(outcomeWithAnalyses) {
        return outcomeWithAnalyses.dataType === 'network' && !outcomeWithAnalyses.selectedAnalysis;
      });
    }

    function numberOfSelectedOutcomes(outcomeInclusions) {
      var validOutcomes = _.filter(outcomeInclusions, function(inclusion) {
        if (!inclusion.outcome.isIncluded) {
          return false;
        } else {
          if (inclusion.selectedAnalysis) {
            return !inclusion.selectedAnalysis.archived && inclusion.selectedModel && !inclusion.selectedModel.archived;
          } else {
            return inclusion.selectedStudy;
          }
        }
      });
      return validOutcomes.length;
    }

    function isModelWithMissingAlternatives(outcomesWithAnalyses) {
      return _.find(outcomesWithAnalyses, function(outcomeWithAnalysis) {
        return outcomeWithAnalysis.outcome.isIncluded && outcomeWithAnalysis.selectedModel && outcomeWithAnalysis.selectedModel.missingAlternatives.length;
      });
    }

    function isModelWithoutResults(outcomesWithAnalyses) {
      return _.find(outcomesWithAnalyses, function(outcomeWithAnalysis) {
        return outcomeWithAnalysis.outcome.isIncluded && outcomeWithAnalysis.selectedModel && outcomeWithAnalysis.selectedModel.runStatus !== 'done';
      });
    }

    function isInvalidStudySelected(outcomeInclusions) {
      var invalidStudy = _.chain(outcomeInclusions)
        .filter(['dataType', 'single-study'])
        .find(function(inclusion) {
          return (
            (inclusion.selectedStudy && inclusion.selectedStudy.missingInterventions && inclusion.selectedStudy.missingInterventions.length > 0) ||
            (inclusion.selectedStudy && inclusion.selectedStudy.missingOutcomes && inclusion.selectedStudy.missingOutcomes.length > 0)
          );
        }).value();
      return invalidStudy;
    }

    function hasMissingStudy(outcomeInclusions) {
      return _.find(outcomeInclusions, function(inclusion) {
        return inclusion.dataType === 'single-study' && _.isEmpty(inclusion.selectedStudy);
      });
    }

    function findOverlappingOutcomes(outcomeInclusions) {
      return _(outcomeInclusions)
        .map('outcome')
        .filter('isIncluded')
        .groupBy('semanticOutcomeUri')
        .filter(function(outcomeByUri) {
          return outcomeByUri.length > 1;
        })
        .value();
    }

    return {
      hasMissingStudy: hasMissingStudy,
      isMissingDataType: isMissingDataType,
      isMissingAnalysis: isMissingAnalysis,
      isModelWithMissingAlternatives: isModelWithMissingAlternatives,
      isModelWithoutResults: isModelWithoutResults,
      isInvalidStudySelected: isInvalidStudySelected,
      numberOfSelectedOutcomes: numberOfSelectedOutcomes,
      findOverlappingOutcomes: findOverlappingOutcomes
    };
  };

  return dependencies.concat(BenefitRiskErrorService);
});

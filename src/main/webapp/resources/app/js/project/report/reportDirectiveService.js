'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var ReportStubstitutionService = function() {

    var DIRECTIVES = {
      'network-plot': {
        tag: 'network-plot',
        regex: /\[\[\[(network-plot\s+analysis-id=\&\#34;\d+\&\#34;\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<div style="max-width:500px"><' + replaceQuotes(p1) + '></network-plot></div>';
        },
        builder: function(analysisId) {
          return '[[[network-plot analysis-id="' + analysisId + '"]]]';
        }
      },
      'result-comparison': {
        tag: 'result-comparison',
        regex: /\[\[\[(comparison-result\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;\s+t1=\&\#34;\d+\&\#34;\s+t2=\&\#34;\d+\&\#34;\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></comparison-result>';
        },
        builder: function(analysisId, modelId, t1, t2) {
          return '[[[comparison-result' +
            ' analysis-id="' + analysisId + '"' +
            ' model-id="' + modelId + '"' +
            ' t1="' + t1 + '"' +
            ' t2="' + t2 + '"]]]';
        }
      },
      'relative-effects-table': {
        tag: 'relative-effects-table',
        regex: /\[\[\[(relative-effects-table\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;(\s+regression-level=\&\#34;\d+\&\#34;){0,1}\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></relative-effects-table>';
        },
        builder: function(analysisId, modelId, regressionLevel) {
          return regressionLevel !== undefined ? '[[[relative-effects-table' + ' analysis-id="' + analysisId + '"' + ' model-id="' +
            modelId + '" regression-level=' + '"' + regressionLevel + '"]]]' : '[[[relative-effects-table' + ' analysis-id="' +
            analysisId + '"' + ' model-id="' + modelId + '"]]]';
        }
      },
      'relative-effects-plot': {
        tag: 'relative-effects-plot',
        regex: /\[\[\[(relative-effects-plot\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;\s+baseline-treatment-id=\&\#34;\d+\&\#34;(\s+regression-level=\&\#34;\d+\&\#34;){0,1}\s*)\]\]\]/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '></relative-effects-plot>';
        },
        builder: function(analysisId, modelId, baselineTreatmentId, regressionLevel) {
          return regressionLevel !== undefined ? '[[[relative-effects-plot' + ' analysis-id="' + analysisId + '"' + ' model-id="' +
            modelId + '" baseline-treatment-id='+ '"' + baselineTreatmentId +'" regression-level=' + '"' + 
            regressionLevel + '"]]]' : '[[[relative-effects-plot' + ' analysis-id="' +
            analysisId + '"' + ' model-id="' + modelId + '" baseline-treatment-id='+ '"' + baselineTreatmentId + '"]]]';
        }
      }
    };

    function replaceQuotes(input) {
      return input.replace(/\&\#34;/g, '"');
    }

    function inlineDirectives(input) {
      _.forEach(DIRECTIVES, function(directive) {
        input = input.replace(directive.regex, directive.replacer);
      });
      return input;
    }

    function getDirectiveBuilder(directiveName) {
      return DIRECTIVES[directiveName].builder;
    }

    return {
      inlineDirectives: inlineDirectives,
      getDirectiveBuilder: getDirectiveBuilder
    };
  };
  return dependencies.concat(ReportStubstitutionService);
});

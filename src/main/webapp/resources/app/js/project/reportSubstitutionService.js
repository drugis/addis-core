'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var ReportStubstitutionService = function() {

    var whitelist = {
      'network-plot': {
        tag: 'network-plot',
        regex: /{{{(network-plot\s+analysis-id=\&\#34;\d+\&\#34;\s*)}}}/g,
        replacer: inlineNetworkPlot,
        builder: function(analysisId) {
          return '{{{network-plot analysis-id="' + analysisId + '"}}}';
        }
      },
      'result-comparison': {
        tag: 'result-comparison',
        regex: /{{{(comparison-result\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;\s+t1=\&\#34;\d+\&\#34;\s+t2=\&\#34;\d+\&\#34;\s*)}}}/g,
        replacer: inlineComparisonResult,
        builder: function(analysisId, modelId, t1, t2) {
          return '{{{comparison-result' +
            ' analysis-id="' + analysisId + '"' +
            ' model-id="' + modelId + '"' +
            ' t1="' + t1 + '"' +
            ' t2="' + t2 + '"}}}';
        }
      }
    };

    function inlineNetworkPlot(match, p1) {
      return '<' + p1.replace(/\&\#34;/g, '"') + '>';
    }

    function inlineComparisonResult(match, p1) {
      return '<' + p1.replace(/\&\#34;/g, '"') + '>';
    }

    function inlineDirectives(input) {
      _.forEach(whitelist, function(directive) {
        input = input.replace(directive.regex, directive.replacer);
      });
      return input;
    }


    function getDirectiveBuilder(directiveName) {
      return whitelist[directiveName].builder;
    }

    return {
      inlineDirectives: inlineDirectives,
      getDirectiveBuilder: getDirectiveBuilder
    };
  };
  return dependencies.concat(ReportStubstitutionService);
});

'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var ReportStubstitutionService = function() {

    var DIRECTIVES = {
      'network-plot': {
        tag: 'network-plot',
        regex: /{{{(network-plot\s+analysis-id=\&\#34;\d+\&\#34;\s+width=\&\#34;\d+\&\#34;\s+height=\&\#34;\d+\&\#34;\s*)}}}/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '>';
        },
        builder: function(analysisId, width, heigth) {
          return '{{{network-plot analysis-id="' + analysisId + '"' +
            'width="' + width + '"' +
            'heigth="' + heigth + '"}}}';
        }
      },
      'result-comparison': {
        tag: 'result-comparison',
        regex: /{{{(comparison-result\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;\s+t1=\&\#34;\d+\&\#34;\s+t2=\&\#34;\d+\&\#34;\s*)}}}/g,
        replacer: function(match, p1) {
          return '<' + replaceQuotes(p1) + '>';
        },
        builder: function(analysisId, modelId, t1, t2) {
          return '{{{comparison-result' +
            ' analysis-id="' + analysisId + '"' +
            ' model-id="' + modelId + '"' +
            ' t1="' + t1 + '"' +
            ' t2="' + t2 + '"}}}';
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

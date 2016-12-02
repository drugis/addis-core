'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var ReportStubstitutionService = function() {

    var whitelist = [{
      tag: 'network-plot',
      regex: /(\&amp;|\&){3}(network-plot\s+analysis-id=\&\#34;\d+\&\#34;\s*)(\&amp;|\&){3}/g,
      replacer: inlineNetworkPlot
    },
    {
      tag: 'result-comparison',
      regex: /(\&amp;|\&){3}(comparison-result\s+analysis-id=\&\#34;\d+\&\#34;\s+model-id=\&\#34;\d+\&\#34;\s+t1=\&\#34;\d+\&\#34;\s+t2=\&\#34;\d+\&\#34;\s*)(\&amp;|\&){3}/g,
      replacer: inlineComparisonResult
    }
    ];

    function inlineNetworkPlot(match, p1, p2) {
      return '<' + p2.replace(/\&\#34;/g, '"') + '>';
    }

    function inlineComparisonResult(match,p1,p2){
      return '<' + p2.replace(/\&\#34;/g, '"') + '>';
    }

    function inlineDirectives(input) {
      _.forEach(whitelist, function(directive) {
        input = input.replace(directive.regex, directive.replacer);
      });
      return input;
    }

    return {
      inlineDirectives: inlineDirectives
    };
  };
  return dependencies.concat(ReportStubstitutionService);
});

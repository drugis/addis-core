'use strict';
define(['angular'], function() {
  var dependencies = [];

  function SanitizeService() {

    var SPARQL_STRING_LITERAL = 'multiLineString';

    function sanitize(input, type) {
      var output = input;
      if (input && type && type === SPARQL_STRING_LITERAL) {
        output = input
          .replace(/[\\]/g, '\\\\')
          .replace(/[\"]/g, '\\\"')
          .replace(/[\/]/g, '\\/')
          .replace(/[\b]/g, '\\b')
          .replace(/[\f]/g, '\\f')
          .replace(/[\n]/g, '\\n')
          .replace(/[\r]/g, '\\r')
          .replace(/[\t]/g, '\\t');
      } else {
        // leave it alone for now
        output = input;
      }

      return output;
    }
    return {
      SPARQL_STRING_LITERAL: SPARQL_STRING_LITERAL,
      sanitize: sanitize
    };
  }
  return dependencies.concat(SanitizeService);
});

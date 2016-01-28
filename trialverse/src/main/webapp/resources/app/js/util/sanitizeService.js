'use strict';
define(['angular'], function() {
  var dependencies = [];

  function SanitizeService() {

    function sanitizeStringLiteral(input) {
      var output = input;
      if (input) {
        output = input
          .replace(/[\\]/g, '\\\\')
          .replace(/[\"]/g, '\\\"')
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
      sanitizeStringLiteral: sanitizeStringLiteral
    };
  }
  return dependencies.concat(SanitizeService);
});

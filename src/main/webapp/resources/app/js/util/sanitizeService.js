'use strict';
define(['angular'], function() {
  var dependencies = [];

  function SanitizeService() {

    var SINGLE_LINE_STRING = 'singleLineString';
    var MULTI_LINE_STRING = 'multiLineString';

    function sanatize(input, type) {

      if (input && type && type === MULTI_LINE_STRING) {
        var output = input
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
      SINGLE_LINE_STRING,
      MULTI_LINE_STRING,
      sanatize: sanatize
    };
  }
  return dependencies.concat(SanitizeService);
});
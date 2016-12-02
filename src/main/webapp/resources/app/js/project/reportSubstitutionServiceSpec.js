'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('the report substitution service', function() {
    var reportSubstitutionService;

    beforeEach(module('addis.project'));

    beforeEach(inject(function(ReportSubstitutionService) {
      reportSubstitutionService = ReportSubstitutionService;
    }));

    describe('inlineDirectives', function() {
      it('should substitute the network-plot', function() {
        var input =
          'report text\n' +
          '===========\n' +
          '{{{network-plot analysis-id=&#34;93&#34;}}}';
        var expectedResult =
          'report text\n' +
          '===========\n' +
          '<network-plot analysis-id="93">';
        var result = reportSubstitutionService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });
      it('should substitute the comparison directive', function() {

      });

      it('should ignore non-whitelisted stuff', function() {
        var input = '{{{leethaxxor-directive sql-inject="pwned"}}}';
        var expectedResult = '{{{leethaxxor-directive sql-inject="pwned"}}}';
        var result = reportSubstitutionService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });
    });
  });
});

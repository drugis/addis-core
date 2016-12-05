'use strict';
define(['angular-mocks'], function() {
  describe('the report directive service', function() {
    var reportDirectiveService;

    beforeEach(module('addis.project'));

    beforeEach(inject(function(ReportDirectiveService) {
      reportDirectiveService = ReportDirectiveService;
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
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });

      it('should substitute the comparison directive', function() {
        var input = '{{{comparison-result analysis-id=&#34;37&#34; model-id=&#34;42&#34; t1=&#34;123&#34; t2=&#34;321&#34;}}}';
        var expectedResult = '<comparison-result analysis-id="37" model-id="42" t1="123" t2="321">';
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });

      it('should ignore non-whitelisted stuff', function() {
        var input = '{{{leethaxxor-directive sql-inject="pwned"}}}';
        var expectedResult = '{{{leethaxxor-directive sql-inject="pwned"}}}';
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });
    });
  });
});

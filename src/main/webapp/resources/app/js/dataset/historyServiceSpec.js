'use strict';
define(['angular', 'angular-mocks'], function () {
  describe('history service', function () {
    beforeEach.module('trialverse.dataset');

    describe('addOrderIndex', function() {
      fit('should add order indices to history data', inject(function(HistoryService) {
        var data = ;
        var result = HistoryService.addOrderIndex(data);
      }));
    });
  });
});

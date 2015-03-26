'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('history service', function() {
    beforeEach(module('trialverse.dataset'));

    fdescribe('addOrderIndex', function() {

      it('should add a propert with tha value of zero in case of a single version', inject(function(HistoryService) {
        var data = [{
          '@id': 'http://localhost:8080/versions/12f736cc-10e5-4465-b138-e146e03f85a9'
        }];

        var result = HistoryService.addOrderIndex(data);

        data[0].idx = 0;
        expect(result).toEqual(data);
      }));

      it('should add propery to each of the version items indicating there order in the history', inject(function(HistoryService) {

        var data = [{
          '@id': 'http://localhost:8080/versions/1'
        }, {
          '@id': 'http://localhost:8080/versions/2',
          'previous': 'http://localhost:8080/versions/3'
        }, {
          '@id': 'http://localhost:8080/versions/3',
          'previous': 'http://localhost:8080/versions/1'
        }];

        var result = HistoryService.addOrderIndex(data);

        _.each(data, function(item) {
          if (item['@id'] === data[0]['@id']) {
            expect(item.idx).toBe(2);
          }
          if (item['@id'] === data[1]['@id']) {
            expect(item.idx).toBe(0);
          }
          if (item['@id'] === data[2]['@id']) {
            expect(item.idx).toBe(1);
          }
        });
      }));


    });
  });
});
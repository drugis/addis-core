'use strict';
define(['angular', 'angular-mocks'], function() {

  describe('the excel io util service', function() {
    var excelIOUtilService;
    beforeEach(angular.mock.module('addis.excelIO'));
    beforeEach(inject(function(ExcelIOUtilService) {
      excelIOUtilService = ExcelIOUtilService;
    }));

    describe('getTitleReference', function() {
      it('should return the coordinates of the cell one row to the right of the cell containing URI', function() {
        var sheet = {
          A1: {
            v: 'uri'
          },
          B1: {
            v: 'title'
          },
        };
        var result = excelIOUtilService.getTitleReference(sheet, 'uri');
        expect(result).toEqual('B1');
      });
    });

    describe('arrayToA1FromCoordinate', function() {
      it('should create an A1-indexed data object from a 2D array, starting from the anchor coordinate', function() {
        var data = [
          [1, 2, 3],
          [4, 5, 6]
        ];
        var expectedResult = {
          D5: 1,
          D6: 2,
          D7: 3,
          E5: 4,
          E6: 5,
          E7: 6
        };
        var result = excelIOUtilService.arrayToA1FromCoordinate(3, 4, data); // anchor D5
        expect(result).toEqual(expectedResult);
      });
    });

    describe('getReferenceValue', function() {
      it('should return the value of the cell a reference is refering to', function() {
        var sourceSheet = {
          A1: {
            f: '=otherSheet!A1'
          }
        };
        var column = 0;
        var row = 0;
        var workbook = {
          Sheets: {
            otherSheet: {
              A1: {
                v: 'value'
              }
            }
          }
        };
        var result = excelIOUtilService.getReferenceValue(sourceSheet, column, row, workbook);
        var expectedResult = 'value';
        expect(result).toEqual(expectedResult);
      });
    });

    describe('getReferenceValueColumnOffset', function() {
      it('should return the value of the cell next to the cell a reference is refering to, given the offset', function() {
        var sourceSheet = {
          A1: {
            f: 'otherSheet!B1'
          }
        };
        var column = 0;
        var row = 0;
        var columnOffset = -1;
        var workbook = {
          Sheets: {
            otherSheet: {
              A1: {
                v: 'value'
              }
            }
          }
        };
        var result = excelIOUtilService.getReferenceValueColumnOffset(sourceSheet, column, row, columnOffset, workbook);
        var expectedResult = 'value';
        expect(result).toEqual(expectedResult);
      });
      it('Should throw an exception when there is no value in the cell that is referenced', function() {
        var sourceSheet = {
          A1: {
            f: 'otherSheet!B1'
          }
        };
        var workbook = {
          Sheets: {
            otherSheet: {}
          }
        };
        var isThrown = false;
        try {
          excelIOUtilService.getReferenceValueColumnOffset(sourceSheet, 0, 0, -1, workbook);
        } catch (e) {
          isThrown = true;
          expect(e).toEqual('Broken reference: otherSheet!B1');
        }
        expect(isThrown).toBeTruthy();
      });
      it('Should throw an exception when the source formula is bad', function() {
        var sourceSheet = {
          A1: {
            f: 'undefined!BNaN'
          }
        };
        var workbook = {
          Sheets: {
            otherSheet: {
              A1: {
                v: 'value'
              }
            }
          }
        };
        var isThrown = false;
        try {
          excelIOUtilService.getReferenceValueColumnOffset(sourceSheet, 0, 0, -1, workbook);
        } catch (e) {
          isThrown = true;
          expect(e).toEqual('Broken reference: undefined!BNaN');
        }
        expect(isThrown).toBeTruthy();
      });
    });
  });
});
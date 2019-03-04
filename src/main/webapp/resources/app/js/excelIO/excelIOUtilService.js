'use strict';
define(['lodash', 'xlsx'], function(_, XLSX) {
  var dependencies = [];
  var ExcelIOUtilService = function() {
    var excelUtils = XLSX.utils;
    
    /***********
    ** export **
    ************/ 
    function cellValue(value) {
      return {
        v: value
      };
    }

    function cellNumber(value) {
      return {
        v: value,
        t: 'n'
      };
    }

    function cellFormula(formula) {
      return {
        f: formula
      };
    }

    function cellReference(reference) {
      return {
        '!ref': reference
      };
    }

    function cellLink(url) {
      return {
        v: url,
        l: {
          Target: url
        }
      };
    }

    function cellRange(startCol, startRow, endCol, endRow) {
      return {
        s: {
          c: startCol,
          r: startRow
        },
        e: {
          c: endCol,
          r: endRow
        }
      };
    }


    function getTitleReference(sheet, uri) {
      var uriReference = _.findKey(sheet, ['v', uri]);
      var titleReference = excelUtils.decode_cell(uriReference);
      titleReference.c += 1;
      return excelUtils.encode_cell(titleReference);
    }

    /*
     * Create a A1-indexed object from the two-dimensional data-array.
     * Columns will be created from the first index, rows from the second.
     */
    function arrayToA1FromCoordinate(anchorColumn, anchorRow, data) {
      return _.reduce(data, function(accum, column, colIndex) {
        return _.reduce(column, function(accum, cell, rowIndex) {
          accum[a1Coordinate(anchorColumn + colIndex, anchorRow + rowIndex)] = cell;
          return accum;
        }, accum);
      }, {});
    }

    /**********************
    ** export and import **
    ***********************/ 
    function a1Coordinate(column, row) {
      return excelUtils.encode_cell({
        c: column,
        r: row
      });
    }

    /***********
    ** import **
    ************/ 
    function getValueIfPresent(dataSheet, column, row) {
      var cell = dataSheet[a1Coordinate(column, row)];
      return cell ? cell.v : undefined;
    }

    function getValue(dataSheet, column, row) {
      var cell = dataSheet[a1Coordinate(column, row)];
      return cell.v;
    }

    function assignIfPresent(object, field, sheet, column, row) {
      var value = getValueIfPresent(sheet, column, row);
      if (value) {
        object[field] = value;
      }
    }

    function getReferenceValue(sourceSheet, column, row, workbook) {
      return getReferenceValueColumnOffset(sourceSheet, column, row, 0, workbook);
    }

    function getReferenceValueColumnOffset(sourceSheet, column, row, columnOffset, workbook) {
      var source = sourceSheet[a1Coordinate(column, row)];
      if (source) {
        var splitFormula = source.f.split('!');
        var targetSheet = splitFormula[0];
        if (targetSheet[0] === '=') {
          targetSheet = targetSheet.slice(1);
        }
        var targetCoordinates = excelUtils.decode_cell(splitFormula[1]);
        targetCoordinates.c += columnOffset;
        if(!isFinite(targetCoordinates.c) || !isFinite(targetCoordinates.r)) {
          throw 'Broken reference: ' + source.f;
        }
        targetCoordinates = excelUtils.encode_cell(targetCoordinates);
        targetSheet = targetSheet.replace(/\'/g, '');
        if (workbook.Sheets[targetSheet] && workbook.Sheets[targetSheet][targetCoordinates]) {
          return workbook.Sheets[targetSheet][targetCoordinates].v;
        } else {
          throw 'Broken reference: ' + source.f;
        }
      }
    }

    return {
      cellValue: cellValue,
      cellNumber: cellNumber,
      cellFormula: cellFormula,
      cellReference: cellReference,
      cellLink: cellLink,
      cellRange: cellRange,
      a1Coordinate: a1Coordinate,
      getTitleReference: getTitleReference,
      arrayToA1FromCoordinate: arrayToA1FromCoordinate,
      getValueIfPresent:getValueIfPresent,
      getValue: getValue,
      assignIfPresent: assignIfPresent,
      getReferenceValue: getReferenceValue,
      getReferenceValueColumnOffset: getReferenceValueColumnOffset
    };
  };
  return dependencies.concat(ExcelIOUtilService);
});

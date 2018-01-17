'use strict';
define(['lodash', 'xlsx-shim', 'file-saver'], function(_, XLSX, saveAs) {
  var dependencies = ['$q', 'StudyService', 'StudyInformationService', 'ArmService'];
  var ExcelExportService = function($q, StudyService, StudyInformationService, ArmService) {
    // functions

    // init

    function exportStudy() {
      var studyPromise = StudyService.getJsonGraph();
      var armsPromise = ArmService.queryItems();
      var studyInformationPromise= StudyInformationService.queryItems();


      return $q.all([studyPromise, studyInformationPromise, armsPromise]).then(function(results) {

        var study = StudyService.findStudyNode(results[0]);
        var studyInformation = results[1][0];
        var arms = results[2];

        var workBook = XLSX.utils.book_new();

        var studyDataSheet = {
          '!ref': 'A1:N6',
          A1: { //row 1
            v: 'Study Information'
          },
          I1: {
            v: 'Population Information'
          },
          K1: {
            v: 'Arm Information'
          },
          N1: {
            v: 'Measurement Information'
          },
          A3: { //row 3
            v: 'id'
          },
          B3: {
            v: 'addis url'
          },
          C3: {
            v: 'title'
          },
          D3: {
            v: 'group allocation'
          },
          E3: {
            v: 'blinding'
          },
          F3: {
            v: 'status'
          },
          G3: {
            v: 'number of centers'
          },
          H3: {
            v: 'objective'
          },
          I3: {
            v: 'indication'
          },
          J3: {
            v: 'eligibility criteria'
          },
          K3: {
            v: 'title'
          },
          L3: {
            v: 'description'
          },
          M3: {
            v: 'treatment'
          }
        };
        studyDataSheet['!merges'] = [{
          s: {
            c: 0,
            r: 0
          },
          e: {
            c: 7,
            r: 0
          }
        }, {
          s: {
            c: 8,
            r: 0
          },
          e: {
            c: 9,
            r: 0
          }
        }, {
          s: {
            c: 10,
            r: 0
          },
          e: {
            c: 12,
            r: 0
          }
        }];
        var studyData = {
          A4: {
            v: study.label
          },
          B4: {
            v: 'retrieve url u silly'
          },
          C4: {
            v: study.comment
          },
          D4: {
            v: studyInformation.allocation
          },
          E4: {
            v: studyInformation.blinding
          },
          F4: {
            v: studyInformation.status
          },
          G4: {
            v: studyInformation.numberOfCenters
          },
          H4: {
            v: studyInformation.objective.comment
          }
        };

        var armData = _.reduce(arms, function(acc, arm, idx) {
          var rowNum = (4 + idx);
          acc['K' + rowNum] = {
            v: arm.label
          };
          acc['L' + rowNum] = {
            v: arm.comment
          };
          return acc;
        }, {});
        _.merge(studyDataSheet, studyData, armData);

        XLSX.utils.book_append_sheet(workBook, studyDataSheet, 'Study data');
        var workBookout = XLSX.write(workBook, {
          bookType: 'xlsx',
          type: 'array'
        });
        saveAs(new Blob([workBookout], {
          type: 'application/octet-stream'
        }), 'sheetjs.xlsx');
      });
    }

    return {
      exportStudy: exportStudy
    };

    // MEMO
    // var data = [
    //   { name: 'Barack Obama", pres: 44 },
    //   { name: "Donald Trump", pres: 45 }
    // ];

    // /* generate a worksheet */
    // var ws = XLSX.utils.json_to_sheet(data);

    // /* add to workbook */
    // var workBook = XLSX.utils.book_new();
    // XLSX.utils.book_append_sheet(workBook, ws, "Presidents");

    // /* write workbook (use type 'array' for ArrayBuffer) */
    // var workBookout = XLSX.write(workBook, {bookType:'xlsx', type:'array'});

    // /* generate a download */
    // saveAs(new Blob([workBookout],{type:"application/octet-stream"}), "sheetjs.xlsx");
  };
  return dependencies.concat(ExcelExportService);
});
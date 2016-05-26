'use strict';
define(['lodash'], function(_) {
  var dependencies = ['ResultsTableService'];
  var NonConformantMeasurementTableService = function(ResultsTableService) {

    function createRow(variable, groupAndResults, numberOfGroups, label, measurementInstanceList) {

      var row = {
        variable: variable,
        group: groupAndResults.group,
        label: label,
        numberOfGroups: numberOfGroups,
        inputColumns: ResultsTableService.createInputColumns(variable, groupAndResults.results),
        measurementInstanceList: measurementInstanceList
      };

      return row;
    }

    function mapResultsByLabelAndGroup(arms, groups, resultValuesObjects) {
      var nonConformantLabelToMeasurementMap = _.reduce(resultValuesObjects, function(accum, resultValueObject) {
        accum[resultValueObject.comment] = {};
        return accum;
      }, {});

      var resultsByGroup = _.reduce(resultValuesObjects, function(accum, resVal) {
        var groupResult = accum[resVal.armUri] || [];
        accum[resVal.armUri] = groupResult.concat(resVal);
        return accum;
      }, {});

      _.forEach(Object.keys(nonConformantLabelToMeasurementMap), function(label) {
        _.forEach(arms, function(arm) {
          var results = _.filter(resultsByGroup[arm.armURI], function(resultValueObject) {
            return resultValueObject.comment === label;
          });
          nonConformantLabelToMeasurementMap[label][arm.armURI] = {
            group: arm,
            results: results
          };

        });

        _.forEach(groups, function(group) {

          var results = _.filter(resultsByGroup[group.groupUri], function(resultValueObject) {
            return resultValueObject.comment === label;
          });
          nonConformantLabelToMeasurementMap[label][group.groupUri] = {
            group: group,
            results: results
          };

        });
      });
      return nonConformantLabelToMeasurementMap;
    }

    function createInputRows(variable, nonConformantLabelToMeasurementMap) {
      return _.reduce(nonConformantLabelToMeasurementMap, function(accum, item, label) {
        var groupAndNonEmptyResults = _.filter(item, function(groupEntry) {
          return groupEntry.results.length > 0;
        });
        var spanCount = groupAndNonEmptyResults.length;
        var measurementInstanceList = _.reduce(groupAndNonEmptyResults, function(accum, groupEntry) {
          accum.push(groupEntry.results[0].instance);
          return accum;
        }, []);

        _.forEach(groupAndNonEmptyResults, function(groupAndResults) {
          accum = accum.concat(createRow(variable, groupAndResults, spanCount, label, measurementInstanceList));
        });

        return accum;
      }, []);

    }

    return {
      mapResultsByLabelAndGroup: mapResultsByLabelAndGroup,
      createInputRows: createInputRows,
    };
  };
  return dependencies.concat(NonConformantMeasurementTableService);
});

'use strict';
define(['lodash'], function(_) {
  var dependencies = ['ResultsTableService'];
  var NonConformantMeasurementTableService = function(ResultsTableService) {

    function createRow(variable, group, numberOfGroups, label, rowValueObjects, measurementInstanceList) {

      var row = {
        variable: variable,
        group: group,
        label: label,
        numberOfGroups: numberOfGroups,
        inputColumns: ResultsTableService.createInputColumns(variable, rowValueObjects),
        measurementInstanceList: measurementInstanceList
      };

      return row;
    }

    function createInputRows(variable, arms, groups, resultValuesObjects) {
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
          nonConformantLabelToMeasurementMap[label][arm.armURI] = arm;
          nonConformantLabelToMeasurementMap[label][arm.armURI].results = _.filter(resultsByGroup[arm.armURI], function(resultValueObject) {
            return (resultValueObject.comment === label);
          });

        });

        _.forEach(groups, function(group) {
          nonConformantLabelToMeasurementMap[label][group.groupUri] = group;
          nonConformantLabelToMeasurementMap[label][group.groupUri].results = _.filter(resultsByGroup[group.groupUri], function(resultValueObject) {
            return (resultValueObject.comment === label);
          });

        });
      });

      return _.reduce(nonConformantLabelToMeasurementMap, function(accum, item, label) {
        var itemsWithResult = _.filter(item, function(groupEntry) {
          return groupEntry.results.length > 0;
        });
        var spanCount = itemsWithResult.length;
        var measurementInstanceList = _.reduce(itemsWithResult, function(accum, groupEntry) {
          accum.push(groupEntry.results[0].instance);
          return accum;
        }, []);

        _.forEach(itemsWithResult, function(itemGroup) {
          accum = accum.concat(createRow(variable, itemGroup, spanCount, label, itemGroup.results, measurementInstanceList));
        });

        return accum;
      }, []);

    }

    return {
      createInputRows: createInputRows,
    };
  };
  return dependencies.concat(NonConformantMeasurementTableService);
});

'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = ['$q', 'StudyService', 'UUIDService', 'ACTIVITY_TYPE_OPTIONS'];
  var ActivityService = function($q, StudyService, UUIDService, ACTIVITY_TYPE_OPTIONS) {

    // private
    var INSTANCE_PREFIX = 'http://trials.drugis.org/instances/';
    var ONTOLOGY = 'ontology:';

    var FIXED_DOSE_TYPE = ONTOLOGY + 'FixedDoseDrugTreatment';
    var TITRATED_DOSE_TYPE = ONTOLOGY + 'TitratedDoseDrugTreatment';

    // public
    var ACTIVITY_TYPE_OPTIONS = _.keyBy(ACTIVITY_TYPE_OPTIONS, 'uri');

    function queryItems() {
      return StudyService.getJsonGraph().then(function(jsonGraph) {
        return StudyService.getStudy().then(function(study) {
          return _.map(study.has_activity, _.partial(createActivityObject, study, jsonGraph));
        });
      });
    }

    function createActivityObject(study, jsonGraph, activity) {
      var result = {
        activityUri: activity['@id'],
        activityType: ACTIVITY_TYPE_OPTIONS[activity['@type']],
        label: activity.label,
        activityDescription: activity.comment
      };
      if (activity.has_drug_treatment) {
        result.treatments = _.map(activity.has_drug_treatment, _.partial(createTreatmentObject, study, jsonGraph));
      }
      return result;
    }

    function findInstance(graph, uri) {
      return _.find(graph, function(node) {
        return node['@id'] === uri;
      });
    }

    function createTreatmentObject(study, graph, treatment) {
      var result = {
        drug: {
          uri: treatment.treatment_has_drug,
          label: findInstance(graph, treatment.treatment_has_drug).label
        },
        treatmentDoseType: treatment['@type']
      };

      if (result.treatmentDoseType === FIXED_DOSE_TYPE) {
        result.fixedValue = treatment.treatment_dose[0].value;
        result.dosingPeriodicity = treatment.treatment_dose[0].dosingPeriodicity;
        result.doseUnit = {
          uri: treatment.treatment_dose[0].unit,
          label: findInstance(graph, treatment.treatment_dose[0].unit).label
        };
      } else if (result.treatmentDoseType === TITRATED_DOSE_TYPE) {
        result.minValue = treatment.treatment_min_dose[0].value;
        result.maxValue = treatment.treatment_max_dose[0].value;
        result.dosingPeriodicity = treatment.treatment_min_dose[0].dosingPeriodicity;
        result.doseUnit = {
          uri: treatment.treatment_min_dose[0].unit,
          label: findInstance(graph, treatment.treatment_min_dose[0].unit).label
        };
      }
      return result;
    }

    function createTreatmentJson(treatment) {
      function createDoseValue(value) {
        return [{
          dosingPeriodicity: treatment.dosingPeriodicity,
          unit: treatment.doseUnit.uri,
          value: value
        }];
      }
      var newTreatment = {
        '@id': INSTANCE_PREFIX + UUIDService.generate(),
        '@type': treatment.treatmentDoseType,
        treatment_has_drug: treatment.drug.uri
      };
      if (treatment.treatmentDoseType === TITRATED_DOSE_TYPE) {
        newTreatment.treatment_min_dose = createDoseValue(treatment.minValue);
        newTreatment.treatment_max_dose = createDoseValue(treatment.maxValue);
      } else if (treatment.treatmentDoseType === FIXED_DOSE_TYPE) {
        newTreatment.treatment_dose = createDoseValue(treatment.fixedValue);
      }
      return newTreatment;
    }

    function addToGraphIfNotExists(graph, uri, type, label) {
      var foundNode = _.find(graph, function(node) {
        return node['@id'] === uri;
      });
      if (!foundNode) {
        graph.push({
          '@id': uri,
          '@type': type,
          label: label
        });
      }
    }

    function addDrugsAndUnitsToGraph(graph, treatments) {
      angular.forEach(treatments, function(treatment) {
        // add drug and unit to graph if they weren't there yet
        addToGraphIfNotExists(graph, treatment.drug.uri, 'ontology:Drug', treatment.drug.label);
        addToGraphIfNotExists(graph, treatment.doseUnit.uri, 'ontology:Unit', treatment.doseUnit.label);
      });
    }

    function addItem(item) {
      return StudyService.getJsonGraph().then(function(graph) {
        var newItem = {
          '@id': INSTANCE_PREFIX + UUIDService.generate(),
          '@type': item.activityType.uri,
          label: item.label,
          has_activity_application: []
        };
        if (item.activityDescription) {
          newItem.comment = item.activityDescription;
        }
        if (item.treatments && item.treatments.length > 0) {
          newItem.has_drug_treatment = _.map(item.treatments, createTreatmentJson);

          addDrugsAndUnitsToGraph(graph, item.treatments);
        }

        var study = _.find(graph, function(graphNode) {
          return graphNode['@type'] === 'ontology:Study';
        });
        study.has_activity.push(newItem);
        _.remove(graph, function(graphNode) {
          return graphNode['@type'] === 'ontology:Study';
        });
        graph.push(study);
        return StudyService.saveJsonGraph(graph);
      });
    }

    function editItem(item) {
      return StudyService.getJsonGraph().then(function(graph) {
        var study = _.find(graph, function(graphNode) {
          return graphNode['@type'] === 'ontology:Study';
        });
        var toEdit = _.find(study.has_activity, function(activity) {
          return item.activityUri === activity['@id'];
        });
        toEdit['@type'] = item.activityType.uri;
        toEdit.label = item.label;
        toEdit.comment = item.activityDescription;
        if (!toEdit.comment) {
          delete toEdit.comment;
        }
        if (item.treatments && item.treatments.length > 0) {
          toEdit.has_drug_treatment = _.map(item.treatments, createTreatmentJson);

          addDrugsAndUnitsToGraph(graph, item.treatments);
        } else {
          delete toEdit.has_drug_treatment;
        }
        return StudyService.saveJsonGraph(graph);
      });
    }

    function deleteItem(item) {
      return StudyService.getStudy().then(function(study) {
        _.remove(study.has_activity, function(activity) {
          return item.activityUri === activity['@id'];
        });
        return StudyService.save(study);
      });
    }

    return {
      queryItems: queryItems,
      addItem: addItem,
      editItem: editItem,
      deleteItem: deleteItem,
      ACTIVITY_TYPE_OPTIONS: ACTIVITY_TYPE_OPTIONS
    };
  };
  return dependencies.concat(ActivityService);
});

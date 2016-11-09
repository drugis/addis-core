'use strict';
define(['angular', 'angular-mocks', 'util/util'], function() {
  describe('the data model service', function() {
    var dataModelService,
      uuidServiceMock = jasmine.createSpyObj('UUIDService', ['generate']);

    beforeEach(function() {
      module('trialverse.util', function($provide) {
        $provide.value('UUIDService', uuidServiceMock);
      });
    });

    beforeEach(inject(function(DataModelService) {
      dataModelService = DataModelService;
    }));

    describe('updateCategories', function() {
      describe('if there is string-only categories in the graph', function() {
        it('should create new instance nodes and change all labels to refer to those instances', function() {
          var oldStyleGraph = {
            '@graph': [{
              '@id': 'http://catListBlankNode',
              'first': 'Male',
              'rest': 'listBlankNode2'
            }, {
              '@id': 'listBlankNode2',
              'first': 'Female',
              'rest': {
                '@list': ['Other']
              }
            }, {
              '@id': 'http://countBlankNode1',
              'category': 'Male',
              'http://trials.drugis.org/ontology#count': 100
            }, {
              '@id': 'http://countBlankNode2',
              'category': 'Female',
              'http://trials.drugis.org/ontology#count': 90
            }, {
              '@id': 'http://countBlankNode3',
              'category': 'Other',
              'http://trials.drugis.org/ontology#count': 80
            }, {
              '@id': 'http://variableBlankNode',
              '@type': 'http://trials.drugis.org/ontology#Variable',
              'categoryList': 'http://catListBlankNode',
              'measurementType': 'http://trials.drugis.org/ontology#categorical',
              'comment': '',
              'label': 'Sex'
            }, {
              '@id': 'http://measurementNode',
              'category_count': ['http://countBlankNode'],
              'of_group': 'groupUri',
              'of_moment': 'momentUri',
              'of_outcome': 'outcomeUri'
            }]
          };

          var expectedGraph = {
            '@graph': [{
              '@id': 'http://catListBlankNode',
              'first': 'http://trials.drugis.org/instances/newUuid1',
              'rest': 'listBlankNode2'
            }, {
              '@id': 'listBlankNode2',
              'first': 'http://trials.drugis.org/instances/newUuid2',
              'rest': {
                '@list': ['http://trials.drugis.org/instances/newUuid3']
              }
            }, {
              '@id': 'http://countBlankNode1',
              'category': 'http://trials.drugis.org/instances/newUuid1',
              'http://trials.drugis.org/ontology#count': 100
            }, {
              '@id': 'http://countBlankNode2',
              'category': 'http://trials.drugis.org/instances/newUuid2',
              'http://trials.drugis.org/ontology#count': 90
            }, {
              '@id': 'http://countBlankNode3',
              'category': 'http://trials.drugis.org/instances/newUuid3',
              'http://trials.drugis.org/ontology#count': 80
            }, {
              '@id': 'http://variableBlankNode',
              '@type': 'http://trials.drugis.org/ontology#Variable',
              'categoryList': 'http://catListBlankNode',
              'measurementType': 'http://trials.drugis.org/ontology#categorical',
              'comment': '',
              'label': 'Sex'
            }, {
              '@id': 'http://measurementNode',
              'category_count': ['http://countBlankNode'],
              'of_group': 'groupUri',
              'of_moment': 'momentUri',
              'of_outcome': 'outcomeUri'
            }, {
              '@id': 'http://trials.drugis.org/instances/newUuid1',
              '@type': 'http://trials.drugis.org/ontology#Category',
              'label': 'Male'
            }, {
              '@id': 'http://trials.drugis.org/instances/newUuid2',
              '@type': 'http://trials.drugis.org/ontology#Category',
              'label': 'Female'
            }, {
              '@id': 'http://trials.drugis.org/instances/newUuid3',
              '@type': 'http://trials.drugis.org/ontology#Category',
              'label': 'Other'
            }]
          };

          uuidServiceMock.generate.and.returnValues('newUuid1', 'newUuid2', 'newUuid3');

          expect(dataModelService.updateCategories(oldStyleGraph)).toEqual(expectedGraph);

        });
      });
    });

    describe('normalizeFirstAndRest', function() {
      it('should change all first and rest properties into RDF_FIRST and RDF_REST', function() {
        var oldStyleGraph = {
          '@graph': [{
            '@id': 'http://blankNode1',
            'http://www.w3.org/1999/02/22-rdf-syntax-ns#first': 'http://trials.drugis.org/instances/newUuid1',
            'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest': {
              '@list': ['http://trials.drugis.org/instances/newUuid2']
            }
          }, {
            '@id': 'http://blankNode2',
            'first': 'http://trials.drugis.org/instances/newUuid1',
            'rest': {
              '@list': ['http://trials.drugis.org/instances/newUuid2']
            }
          }]
        };
        var expectedGraph = {
          '@graph': [{
            '@id': 'http://blankNode1',
            'first': 'http://trials.drugis.org/instances/newUuid1',
            'rest': {
              '@list': ['http://trials.drugis.org/instances/newUuid2']
            }
          }, {
            '@id': 'http://blankNode2',
            'first': 'http://trials.drugis.org/instances/newUuid1',
            'rest': {
              '@list': ['http://trials.drugis.org/instances/newUuid2']
            }
          }]
        };
        expect(dataModelService.normalizeFirstAndRest(oldStyleGraph)).toEqual(expectedGraph);
      });
    });
  });
});

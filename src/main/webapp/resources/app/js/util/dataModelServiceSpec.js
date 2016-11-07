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
              'http://www.w3.org/1999/02/22-rdf-syntax-ns#first': 'Male',
              'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest': {
                '@list': ['Female']
              }
            }, {
              '@id': 'http://countBlankNode',
              'category': 'Male',
              'http://trials.drugis.org/ontology#count': 100
            }, {
              '@id': 'http://countBlankNode',
              'category': 'Female',
              'http://trials.drugis.org/ontology#count': 90
            }, {
              '@id': 'http://variableBlankNode',
              '@type': 'http://trials.drugis.org/ontology#Variable',
              'categoryList': 'http://catListBlankNode',
              'measurementType': 'http://trials.drugis.org/ontology#categorical',
              'comment': '',
              'label': 'Sex',
              'sameAs': 'http://trials.drugis.org/concepts/7af6e330-0a60-4d01-bfe8-63905965fafa'
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
              'http://www.w3.org/1999/02/22-rdf-syntax-ns#first': 'http://trials.drugis.org/instances/newUuid1',
              'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest': {
                '@list': ['http://trials.drugis.org/instances/newUuid2']
              }
            }, {
              '@id': 'http://countBlankNode',
              'category': 'http://trials.drugis.org/instances/newUuid1',
              'http://trials.drugis.org/ontology#count': 100
            }, {
              '@id': 'http://countBlankNode',
              'category': 'http://trials.drugis.org/instances/newUuid2',
              'http://trials.drugis.org/ontology#count': 90
            }, {
              '@id': 'http://variableBlankNode',
              '@type': 'http://trials.drugis.org/ontology#Variable',
              'categoryList': 'http://catListBlankNode',
              'measurementType': 'http://trials.drugis.org/ontology#categorical',
              'comment': '',
              'label': 'Sex',
              'sameAs': 'http://trials.drugis.org/concepts/7af6e330-0a60-4d01-bfe8-63905965fafa'
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
            }]
          };

          uuidServiceMock.generate.and.returnValues('newUuid1', 'newUuid2');

          expect(dataModelService.updateCategories(oldStyleGraph)).toEqual(expectedGraph);

        });
      });
    });
  });
});

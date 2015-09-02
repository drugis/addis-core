'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('history service', function() {
    beforeEach(module('trialverse.dataset'));

    describe('addOrderIndex', function() {

      it('should add a property with the value of zero in case of a single version', inject(function(HistoryService) {
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

        _.each(result, function(item) {
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
    describe('addMergeIndicators', function() {
      it('should annotate history items when they are a merge operation', inject(function(HistoryService) {
        var data = {
          '@graph': [{
            '@id': '_:b0',
            'revision': 'http://localhost:8080/revisions/9fbb399a-fdb3-4d72-99d4-f5f7965ed2f4'
          }, {
            '@id': '_:b1',
            'revision': 'http://localhost:8080/revisions/9fbb399a-fdb3-4d72-99d4-f5f7965ed2f4'
          }, {
            '@id': '_:b2',
            'graph': 'http://trials.drugis.org/graphs/83409439-cc62-4ff1-a30e-c8e9fd1804d7',
            'revision': 'http://localhost:8080/revisions/9c7db83f-1d3d-4b49-ae2b-3150d1848a34'
          }, {
            '@id': '_:b3',
            'revision': 'http://localhost:8080/revisions/9fbb399a-fdb3-4d72-99d4-f5f7965ed2f4'
          }, {
            '@id': '_:b4',
            'graph': 'http://trials.drugis.org/graphs/83409439-cc62-4ff1-a30e-c8e9fd1804d7',
            'revision': 'http://localhost:8080/revisions/9c7db83f-1d3d-4b49-ae2b-3150d1848a34'
          }, {
            '@id': '_:b5',
            'graph': 'http://trials.drugis.org/graphs/46c7c51f-2b10-49fd-a399-cd461510fd8e',
            'revision': 'http://localhost:8080/revisions/797973e4-d35a-4ba7-a624-edffe52333cd'
          }, {
            '@id': 'http://localhost:8080/datasets/b4420996-c8d5-4e1f-9521-8638ea91cc14',
            '@type': 'es:Dataset',
            'head': 'http://localhost:8080/versions/91b369d2-f341-4d91-a080-c918e0b7dcac',
            'creator': 'mailto:osmosisch@gmail.com',
            'date': '2015-09-01T10:47:54Z'
          }, {
            '@id': 'http://localhost:8080/revisions/1a03d818-e496-4fbb-9a6d-0555028f6a01',
            'dataset': 'http://localhost:8080/datasets/be177e27-7978-41de-b4f9-9267ddd1cc41'
          }, {
            '@id': 'http://localhost:8080/revisions/797973e4-d35a-4ba7-a624-edffe52333cd',
            '@type': 'es:Revision',
            'assertions': 'http://localhost:8080/assert/797973e4-d35a-4ba7-a624-edffe52333cd',
            'merge_type': 'es:MergeTypeCopyTheirs',
            'merged_revision': 'http://localhost:8080/revisions/1a03d818-e496-4fbb-9a6d-0555028f6a01'
          }, {
            '@id': 'http://localhost:8080/revisions/8137c22f-dfc7-4881-b413-7918575f2421',
            'dataset': 'http://localhost:8080/datasets/a06d83d2-5819-402f-b6f3-e2682766a723'
          }, {
            '@id': 'http://localhost:8080/revisions/9c7db83f-1d3d-4b49-ae2b-3150d1848a34',
            '@type': 'es:Revision',
            'assertions': 'http://localhost:8080/assert/9c7db83f-1d3d-4b49-ae2b-3150d1848a34',
            'merge_type': 'es:MergeTypeCopyTheirs',
            'merged_revision': 'http://localhost:8080/revisions/8137c22f-dfc7-4881-b413-7918575f2421'
          }, {
            '@id': 'http://localhost:8080/revisions/9fbb399a-fdb3-4d72-99d4-f5f7965ed2f4',
            '@type': 'es:Revision',
            'assertions': 'http://localhost:8080/assert/9fbb399a-fdb3-4d72-99d4-f5f7965ed2f4'
          }, {
            '@id': 'http://localhost:8080/versions/6dd4df72-48a5-4d53-8544-e76406c42422',
            '@type': 'es:DatasetVersion',
            'dataset': 'http://localhost:8080/datasets/b4420996-c8d5-4e1f-9521-8638ea91cc14',
            'default_graph_revision': '_:b1',
            'graph_revision': '_:b2',
            'previous': 'http://localhost:8080/versions/a7968201-d6d9-4bc6-97ef-795e31456a12',
            'creator': 'mailto:osmosisch@gmail.com',
            'date': '2015-09-01T10:48:02Z',
            'http://purl.org/dc/terms/title': 'Study copied from other dataset'
          }, {
            '@id': 'http://localhost:8080/versions/91b369d2-f341-4d91-a080-c918e0b7dcac',
            '@type': 'es:DatasetVersion',
            'dataset': 'http://localhost:8080/datasets/b4420996-c8d5-4e1f-9521-8638ea91cc14',
            'default_graph_revision': '_:b0',
            'graph_revision': ['_:b5', '_:b4'],
            'previous': 'http://localhost:8080/versions/6dd4df72-48a5-4d53-8544-e76406c42422',
            'creator': 'mailto:osmosisch@gmail.com',
            'date': '2015-09-01T10:49:27Z',
            'http://purl.org/dc/terms/title': 'Study copied from other dataset'
          }, {
            '@id': 'http://localhost:8080/versions/a7968201-d6d9-4bc6-97ef-795e31456a12',
            '@type': 'es:DatasetVersion',
            'dataset': 'http://localhost:8080/datasets/b4420996-c8d5-4e1f-9521-8638ea91cc14',
            'default_graph_revision': '_:b3',
            'creator': 'mailto:osmosisch@gmail.com',
            'date': '2015-09-01T10:47:54Z',
            'http://purl.org/dc/terms/title': 'Dataset created through Trialverse'
          }],
          '@context': {
            'revision': {
              '@id': 'http://drugis.org/eventSourcing/es#revision',
              '@type': '@id'
            },
            'dataset': {
              '@id': 'http://drugis.org/eventSourcing/es#dataset',
              '@type': '@id'
            },
            'title': {
              '@id': 'http://purl.org/dc/terms/title',
              '@type': 'http://www.w3.org/2001/XMLSchema#string'
            },
            'date': {
              '@id': 'http://purl.org/dc/terms/date',
              '@type': 'http://www.w3.org/2001/XMLSchema#dateTime'
            },
            'creator': {
              '@id': 'http://purl.org/dc/terms/creator',
              '@type': '@id'
            },
            'previous': {
              '@id': 'http://drugis.org/eventSourcing/es#previous',
              '@type': '@id'
            },
            'graph_revision': {
              '@id': 'http://drugis.org/eventSourcing/es#graph_revision',
              '@type': '@id'
            },
            'default_graph_revision': {
              '@id': 'http://drugis.org/eventSourcing/es#default_graph_revision',
              '@type': '@id'
            },
            'merged_revision': {
              '@id': 'http://drugis.org/eventSourcing/es#merged_revision',
              '@type': '@id'
            },
            'merge_type': {
              '@id': 'http://drugis.org/eventSourcing/es#merge_type',
              '@type': '@id'
            },
            'assertions': {
              '@id': 'http://drugis.org/eventSourcing/es#assertions',
              '@type': '@id'
            },
            'graph': {
              '@id': 'http://drugis.org/eventSourcing/es#graph',
              '@type': '@id'
            },
            'head': {
              '@id': 'http://drugis.org/eventSourcing/es#head',
              '@type': '@id'
            },
            'owl': 'http://www.w3.org/2002/07/owl#',
            'es': 'http://drugis.org/eventSourcing/es#'
          }
        };

        var result = HistoryService.addMergeIndicators(data['@graph']);

        expect(result[13].isMergeOperation).toBeTruthy();
      }));
    });
  });
});

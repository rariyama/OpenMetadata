package org.openmetadata.service.search.opensearch;

import static org.openmetadata.service.workflows.searchIndex.ReindexingUtil.ENTITY_TYPE_KEY;
import static org.openmetadata.service.workflows.searchIndex.ReindexingUtil.getUpdatedStats;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.openmetadata.common.utils.CommonUtil;
import org.openmetadata.schema.EntityInterface;
import org.openmetadata.schema.system.StepStats;
import org.openmetadata.service.exception.ProcessorException;
import org.openmetadata.service.search.IndexUtil;
import org.openmetadata.service.search.SearchIndexDefinition;
import org.openmetadata.service.search.SearchIndexFactory;
import org.openmetadata.service.util.JsonUtils;
import org.openmetadata.service.util.ResultList;
import org.openmetadata.service.workflows.interfaces.Processor;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.common.xcontent.XContentType;

@Slf4j
public class OpenSearchEntitiesProcessor implements Processor<BulkRequest, ResultList<? extends EntityInterface>> {
  private final StepStats stats = new StepStats();

  @Override
  public BulkRequest process(ResultList<? extends EntityInterface> input, Map<String, Object> contextData)
      throws ProcessorException {
    String entityType = (String) contextData.get(ENTITY_TYPE_KEY);
    if (CommonUtil.nullOrEmpty(entityType)) {
      throw new IllegalArgumentException("[EsEntitiesProcessor] entityType cannot be null or empty.");
    }

    LOG.debug(
        "[EsEntitiesProcessor] Processing a Batch of Size: {}, EntityType: {} ", input.getData().size(), entityType);
    BulkRequest requests;
    try {
      requests = buildBulkRequests(entityType, input.getData());
      LOG.debug(
          "[EsEntitiesProcessor] Batch Stats :- Submitted : {} Success: {} Failed: {}",
          input.getData().size(),
          input.getData().size(),
          0);
      updateStats(input.getData().size(), 0);
    } catch (Exception e) {
      LOG.debug(
          "[EsEntitiesProcessor] Batch Stats :- Submitted : {} Success: {} Failed: {}",
          input.getData().size(),
          0,
          input.getData().size());
      updateStats(0, input.getData().size());
      throw new ProcessorException("[EsEntitiesProcessor] Batch encountered Exception. Failing Completely.", e);
    }
    return requests;
  }

  private BulkRequest buildBulkRequests(String entityType, List<? extends EntityInterface> entities) {
    BulkRequest bulkRequests = new BulkRequest();
    for (EntityInterface entity : entities) {
      UpdateRequest request = getUpdateRequest(entityType, entity);
      bulkRequests.add(request);
    }
    return bulkRequests;
  }

  public static UpdateRequest getUpdateRequest(String entityType, EntityInterface entity) {
    SearchIndexDefinition.ElasticSearchIndexType indexType = IndexUtil.getIndexMappingByEntityType(entityType);
    UpdateRequest updateRequest = new UpdateRequest(indexType.indexName, entity.getId().toString());
    updateRequest.doc(
        JsonUtils.pojoToJson(Objects.requireNonNull(SearchIndexFactory.buildIndex(entityType, entity)).buildESDoc()),
        XContentType.JSON);
    updateRequest.docAsUpsert(true);
    return updateRequest;
  }

  @Override
  public void updateStats(int currentSuccess, int currentFailed) {
    getUpdatedStats(stats, currentSuccess, currentFailed);
  }

  @Override
  public StepStats getStats() {
    return stats;
  }
}

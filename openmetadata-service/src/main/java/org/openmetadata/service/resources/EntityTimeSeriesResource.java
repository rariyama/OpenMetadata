package org.openmetadata.service.resources;

import javax.ws.rs.core.Response;
import lombok.Getter;
import org.openmetadata.schema.EntityTimeSeriesInterface;
import org.openmetadata.schema.service.configuration.elasticsearch.ElasticSearchConfiguration;
import org.openmetadata.service.Entity;
import org.openmetadata.service.OpenMetadataApplicationConfig;
import org.openmetadata.service.jdbi3.EntityTimeSeriesRepository;
import org.openmetadata.service.search.IndexUtil;
import org.openmetadata.service.search.SearchRepository;
import org.openmetadata.service.security.Authorizer;

public abstract class EntityTimeSeriesResource<
    T extends EntityTimeSeriesInterface, K extends EntityTimeSeriesRepository<T>> {
  protected final Class<T> entityClass;
  protected final String entityType;
  @Getter protected final K repository;
  protected final Authorizer authorizer;
  public static SearchRepository searchRepository;
  public static ElasticSearchConfiguration esConfig;

  protected EntityTimeSeriesResource(String entityType, Authorizer authorizer) {
    this.entityType = entityType;
    this.entityClass = (Class<T>) Entity.getEntityClassFromType(entityType);
    this.repository = (K) Entity.getEntityTimeSeriesRepository(entityType);
    this.authorizer = authorizer;
    Entity.registerTimeSeriesResourcePermissions(entityType);
  }

  public void initialize(OpenMetadataApplicationConfig config) {
    esConfig = config.getElasticSearchConfiguration();
    searchRepository = IndexUtil.getSearchClient(esConfig, repository.getDaoCollection());
    // Nothing to do in the default implementation
  }

  protected Response create(T entity, String extension, String recordFQN) {
    entity = repository.createNewRecord(entity, extension, recordFQN);
    return Response.ok(entity).build();
  }
}

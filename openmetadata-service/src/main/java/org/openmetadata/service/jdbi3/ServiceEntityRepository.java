/*
 *  Copyright 2022 Collate
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openmetadata.service.jdbi3;

import static org.openmetadata.service.util.EntityUtil.objectMatch;

import java.util.UUID;
import lombok.Getter;
import org.openmetadata.schema.ServiceConnectionEntityInterface;
import org.openmetadata.schema.ServiceEntityInterface;
import org.openmetadata.schema.entity.services.ServiceType;
import org.openmetadata.schema.entity.services.connections.TestConnectionResult;
import org.openmetadata.service.secrets.SecretsManager;
import org.openmetadata.service.secrets.SecretsManagerFactory;
import org.openmetadata.service.util.EntityUtil;
import org.openmetadata.service.util.JsonUtils;

public abstract class ServiceEntityRepository<
        T extends ServiceEntityInterface, S extends ServiceConnectionEntityInterface>
    extends EntityRepository<T> {
  @Getter private final Class<S> serviceConnectionClass;
  @Getter private final ServiceType serviceType;

  protected ServiceEntityRepository(
      String collectionPath,
      String service,
      CollectionDAO dao,
      EntityDAO<T> entityDAO,
      Class<S> serviceConnectionClass,
      String updateFields,
      ServiceType serviceType) {
    super(collectionPath, service, entityDAO.getEntityClass(), entityDAO, dao, "", updateFields);
    this.serviceConnectionClass = serviceConnectionClass;
    this.serviceType = serviceType;
    quoteFqn = true;
  }

  @Override
  public T setFields(T entity, EntityUtil.Fields fields) {
    entity.setPipelines(fields.contains("pipelines") ? getIngestionPipelines(entity) : null);
    return entity;
  }

  @Override
  public T clearFields(T entity, EntityUtil.Fields fields) {
    if (!fields.contains("pipelines")) {
      entity.setPipelines(null);
    }
    return entity;
  }

  @Override
  public void prepare(T service, boolean update) {
    /* Nothing to do */
    service
        .getConnection()
        .setConfig(
            SecretsManagerFactory.getSecretsManager()
                .encryptServiceConnectionConfig(
                    service.getConnection().getConfig(),
                    service.getServiceType().value(),
                    service.getName(),
                    serviceType));
  }

  @Override
  public void storeEntity(T service, boolean update) {
    store(service, update);
  }

  @Override
  public void storeRelationships(T service) {
    // No relationships to store beyond what is stored in the super class
  }

  public T addTestConnectionResult(UUID serviceId, TestConnectionResult testConnectionResult) {
    T service = dao.findEntityById(serviceId);
    service.setTestConnectionResult(testConnectionResult);
    dao.update(serviceId, service.getFullyQualifiedName(), JsonUtils.pojoToJson(service));
    return service;
  }

  /** Remove the secrets from the secret manager */
  @Override
  protected void postDelete(T service) {
    SecretsManagerFactory.getSecretsManager()
        .deleteSecretsFromServiceConnectionConfig(
            service.getConnection().getConfig(), service.getServiceType().value(), service.getName(), serviceType);
  }

  @Override
  public ServiceUpdater getUpdater(T original, T updated, Operation operation) {
    return new ServiceUpdater(original, updated, operation);
  }

  public class ServiceUpdater extends EntityUpdater {

    public ServiceUpdater(T original, T updated, Operation operation) {
      super(original, updated, operation);
    }

    @Override
    public void entitySpecificUpdate() {
      updateConnection();
    }

    private void updateConnection() {
      ServiceConnectionEntityInterface origConn = original.getConnection();
      ServiceConnectionEntityInterface updatedConn = updated.getConnection();
      String origJson = JsonUtils.pojoToJson(origConn);
      String updatedJson = JsonUtils.pojoToJson(updatedConn);
      S decryptedOrigConn = JsonUtils.readValue(origJson, serviceConnectionClass);
      S decryptedUpdatedConn = JsonUtils.readValue(updatedJson, serviceConnectionClass);
      SecretsManager secretsManager = SecretsManagerFactory.getSecretsManager();
      decryptedOrigConn.setConfig(
          secretsManager.decryptServiceConnectionConfig(
              decryptedOrigConn.getConfig(), original.getServiceType().value(), serviceType));
      decryptedUpdatedConn.setConfig(
          secretsManager.decryptServiceConnectionConfig(
              decryptedUpdatedConn.getConfig(), updated.getServiceType().value(), serviceType));
      if (!objectMatch.test(decryptedOrigConn, decryptedUpdatedConn)) {
        // we don't want save connection config details in our database
        recordChange("connection", "old-encrypted-value", "new-encrypted-value", true);
      }
    }
  }
}

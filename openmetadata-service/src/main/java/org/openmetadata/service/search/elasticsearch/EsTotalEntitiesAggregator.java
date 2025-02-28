package org.openmetadata.service.search.elasticsearch;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.openmetadata.schema.dataInsight.DataInsightChartResult;
import org.openmetadata.schema.dataInsight.type.TotalEntitiesByType;
import org.openmetadata.service.dataInsight.DataInsightAggregatorInterface;

public class EsTotalEntitiesAggregator extends DataInsightAggregatorInterface {

  public EsTotalEntitiesAggregator(
      Aggregations aggregations, DataInsightChartResult.DataInsightChartType dataInsightChartType) {
    super(aggregations, dataInsightChartType);
  }

  @Override
  public DataInsightChartResult process() throws ParseException {
    List<Object> data = this.aggregate();
    return new DataInsightChartResult().withData(data).withChartType(this.dataInsightChartType);
  }

  @Override
  public List<Object> aggregate() throws ParseException {
    Histogram timestampBuckets = this.aggregationsEs.get(TIMESTAMP);
    List<Object> data = new ArrayList<>();
    List<Double> entityCount = new ArrayList<>();

    for (Histogram.Bucket timestampBucket : timestampBuckets.getBuckets()) {
      String dateTimeString = timestampBucket.getKeyAsString();
      Long timestamp = this.convertDatTimeStringToTimestamp(dateTimeString);
      MultiBucketsAggregation entityTypeBuckets = timestampBucket.getAggregations().get(ENTITY_TYPE);
      for (MultiBucketsAggregation.Bucket entityTypeBucket : entityTypeBuckets.getBuckets()) {
        String entityType = entityTypeBucket.getKeyAsString();
        Sum sumEntityCount = entityTypeBucket.getAggregations().get(ENTITY_COUNT);
        data.add(
            new TotalEntitiesByType()
                .withTimestamp(timestamp)
                .withEntityType(entityType)
                .withEntityCount(sumEntityCount.getValue()));
        entityCount.add(sumEntityCount.getValue());
      }
    }

    double totalEntities = entityCount.stream().mapToDouble(v -> v).sum();

    for (Object o : data) {
      TotalEntitiesByType el = (TotalEntitiesByType) o;
      el.withEntityCountFraction(el.getEntityCount() / totalEntities);
    }

    return data;
  }
}

package org.openmetadata.service.search.opensearch;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.openmetadata.schema.dataInsight.DataInsightChartResult;
import org.openmetadata.schema.dataInsight.type.PercentageOfServicesWithDescription;
import org.openmetadata.service.dataInsight.DataInsightAggregatorInterface;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.opensearch.search.aggregations.bucket.histogram.Histogram;
import org.opensearch.search.aggregations.metrics.Sum;

public class OsServicesDescriptionAggregator extends DataInsightAggregatorInterface {
  protected OsServicesDescriptionAggregator(
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
    Histogram timestampBuckets = this.aggregationsOs.get(TIMESTAMP);
    List<Object> data = new ArrayList<>();
    for (Histogram.Bucket timestampBucket : timestampBuckets.getBuckets()) {
      String dateTimeString = timestampBucket.getKeyAsString();
      Long timestamp = this.convertDatTimeStringToTimestamp(dateTimeString);
      MultiBucketsAggregation servicesBuckets = timestampBucket.getAggregations().get(SERVICE_NAME);
      for (MultiBucketsAggregation.Bucket serviceBucket : servicesBuckets.getBuckets()) {
        String serviceName = serviceBucket.getKeyAsString();
        Sum sumCompletedDescriptions = serviceBucket.getAggregations().get(COMPLETED_DESCRIPTION_FRACTION);
        Sum sumEntityCount = serviceBucket.getAggregations().get(ENTITY_COUNT);

        data.add(
            new PercentageOfServicesWithDescription()
                .withTimestamp(timestamp)
                .withServiceName(serviceName)
                .withEntityCount(sumEntityCount.getValue())
                .withCompletedDescription(sumCompletedDescriptions.getValue())
                .withCompletedDescriptionFraction(sumCompletedDescriptions.getValue() / sumEntityCount.getValue()));
      }
    }

    return data;
  }
}

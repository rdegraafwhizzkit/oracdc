package nl.whizzkit.oracdc.writer;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import nl.whizzkit.oracdc.CDCUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class KinesisWriter implements IWritable {

    private AmazonKinesis kinesisClient;
    private String streamName;
    private String partitionKeyPattern;

    public KinesisWriter() throws Exception {

        Properties properties = CDCUtils.readProperties("kinesis");
        streamName = properties.getProperty("streamName");
        partitionKeyPattern = properties.getProperty("partitionKeyPattern");

        AmazonKinesisClientBuilder clientBuilder = AmazonKinesisClientBuilder.standard();
        clientBuilder.setRegion(properties.getProperty("regionName"));
        clientBuilder.setCredentials(new ClasspathPropertiesFileCredentialsProvider());
        clientBuilder.setClientConfiguration(new ClientConfiguration());
        kinesisClient = clientBuilder.build();

    }

    @Override
    public void write(String output) {

        PutRecordsRequest putRecordsRequest = new PutRecordsRequest();
        putRecordsRequest.setStreamName(streamName);
        List<PutRecordsRequestEntry> putRecordsRequestEntryList = new ArrayList<>();
        PutRecordsRequestEntry putRecordsRequestEntry = new PutRecordsRequestEntry();
        putRecordsRequestEntry.setData(ByteBuffer.wrap(output.getBytes()));
        putRecordsRequestEntry.setPartitionKey(String.format(partitionKeyPattern, 0));
        putRecordsRequestEntryList.add(putRecordsRequestEntry);

        putRecordsRequest.setRecords(putRecordsRequestEntryList);
        PutRecordsResult putRecordsResult = kinesisClient.putRecords(putRecordsRequest);
        System.out.println("Put Result" + putRecordsResult);
    }
}
